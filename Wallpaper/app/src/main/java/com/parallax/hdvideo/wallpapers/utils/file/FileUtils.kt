package com.parallax.hdvideo.wallpapers.utils.file

import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.extension.memoryInGB
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper
import io.reactivex.Single
import okhttp3.ResponseBody
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


object FileUtils {

    const val APP_FOLDER_CACHE = "wall_paper_app_cache_dir"
    const val APP_FOLDER = "Wallpaper"
    const val FOLDER_PLAYLIST = "Playlist"
    const val fileFirstName = "ahaa"

    val cacheDirect by lazy { WallpaperApp.instance.cacheDir }
    private val BUFFER_SIZE_VALUE: Int

    init {
        BUFFER_SIZE_VALUE = if (WallpaperApp.instance.memoryInGB > 2)
            DEFAULT_BUFFER_SIZE
        else 1024 * 4
    }

    private val internalDirect by lazy {
        val file = File(WallpaperApp.instance.filesDir, APP_FOLDER)
        if (!file.exists()) {
            file.mkdir()
        }
        file
    }

    private val appCacheDirect by lazy {
        val file = File(cacheDirect, APP_FOLDER_CACHE)
        if (!file.exists()) {
            file.mkdir()
        }
        file
    }

    private val folderPlaylist by lazy {
        val file = File(internalDirect, FOLDER_PLAYLIST)
        if(!file.exists()) {
            file.mkdir()
        }
        file
    }

    private val folderVideo by lazy {
        val file = File(internalDirect, "Video")
        if(!file.exists()) {
            file.mkdir()
        }
        file
    }

     val folderParallax by lazy {
        val file = File(internalDirect,"parallax")
        if(!file.exists()){
            file.mkdir()
        }
        file
    }

    val exoplayerCacheDirect by lazy {
        createTempFile("exoplayer_video_cached")
    }

    fun createTempFile(fileName: String) : File {
        if (!appCacheDirect.exists()) appCacheDirect.mkdir()
        return File(appCacheDirect, fileName)
    }

    fun deleteAppCache() {
        appCacheDirect.deleteRecursively()
    }

    fun deleteAll() {
        cacheDirect.deleteRecursively()
    }

    fun getFileName(url: String): String {
        val s = url.lastIndexOf("/") + 1
        val l = if (url.contains("?")) url.lastIndexOf("?", s) else url.length
        return url.substring(s, l)
    }

    fun getFileFromURL(url: String): File {
        val fileName = fileFirstName.plus("_") + url.substring(url.lastIndexOf("/") + 1)
        return File(internalDirect, fileName)
    }

    fun copyFile(fromFile: File, toFile: File) : File {
        return fromFile.copyTo(toFile, true, BUFFER_SIZE_VALUE)
    }

    fun copyToInternalDir(fromFile: File) : File {
        val file = File(internalDirect, fromFile.lastName())
        return fromFile.copyTo(file, bufferSize = BUFFER_SIZE_VALUE)
    }


    fun getFileNameFromURL(url: String): String {
        return fileFirstName.plus("_") + url.substring(url.lastIndexOf("/") + 1)
    }

    val isExternalStorageWritable: Boolean
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    val isExternalStorageReadable: Boolean
        get() =  Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)

    @Suppress("DEPRECATION")
    fun addImageToGallery(fileExport: File, url: String? = null) : Uri? {
        val fileName = url?.let { getFileNameFromURL(it) } ?: fileFirstName.plus("_").plus(fileExport.lastName())
        val isVideo = fileName.contains(".mp4")
        val contentValues = ContentValues().apply {
            val current = System.currentTimeMillis()
            put(MediaStore.Files.FileColumns.DATE_ADDED, current/ 1000)
            put(MediaStore.Files.FileColumns.DATE_TAKEN, current)
            put(MediaStore.Files.FileColumns.DATE_MODIFIED, current / 1000)
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, if (isVideo) "video/mp4" else "image/jpeg")
        }
        val contentResolver = WallpaperApp.instance.contentResolver
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            var file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            file.setReadable(true)
            file.setWritable(true)
            file = fileExport.copyTo(file, true, BUFFER_SIZE_VALUE)
            contentValues.put(MediaStore.Files.FileColumns.DATA, file.path)
            contentResolver.insert(
                    if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    else MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
            )
        } else {
            deleteFileUsingFileName(fileName)
            contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 1)
            val uri = contentResolver.insert(
                    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    contentValues
            )?.also { copyFileData(it, fileExport) }
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            uri?.also { contentResolver.update(it, contentValues, null, null) }
        }
    }

    fun copyFileToPlaylistFolder(fileExport: File, url : String) : File {
        val file = getPlayListFile(url)
        file.setReadable(true)
        file.setWritable(true)
        fileExport.copyTo(file,true, BUFFER_SIZE_VALUE)
        return file
    }

    fun getPlayListFile(url: String) : File {
        return File(folderPlaylist, getFileNameFromURL(url))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun copyFileData(destination: Uri, fileExport: File) {
        try {
            WallpaperApp.instance.contentResolver.openFileDescriptor(destination, "w")?.use { p ->
                ParcelFileDescriptor.AutoCloseOutputStream(p).use {parcel ->
                    val bytes = ByteArray(BUFFER_SIZE_VALUE)
                    fileExport.inputStream().use {inputStream ->
                        var read: Int
                        while (inputStream.read(bytes).also { read = it } != -1) {
                            parcel.write(bytes, 0, read)
                        }
                    }
                }
            }
            WallpaperApp.instance.contentResolver.openFileDescriptor(destination, "rw")?.use { p ->
                ExifInterface(p.fileDescriptor)
                    .apply {
                        val curDateTime = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).format(
                            Date())
                        setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, curDateTime)
                        setAttribute(ExifInterface.TAG_DATETIME, curDateTime)
                        setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, curDateTime)
                        saveAttributes()
                    }
            }
        } catch (e: Exception) { }
    }

    fun buildPath(base: File?, vararg folders: String?): File? {
        var result = base
        for (segment in folders) {
            if (result == null) {
                return null
            } else {
                if (segment.isNullOrEmpty()) {
                    return result
                }
                result = File(result, segment)
                if (!result.exists()) {
                    result.mkdirs()
                }
            }
        }
        return result
    }

    fun write(body: ResponseBody, fileName: String): File {
        val file = createTempFile(fileName)
        try {
            var inStream: InputStream? = null
            var outStream: OutputStream? = null
            try {
                if (file.exists()) {
                    file.delete()
                }
                file.createNewFile()
                file.setWritable(true)
                file.setReadable(true)
                val fileReader = ByteArray(BUFFER_SIZE_VALUE)
                var fileSizeDownloaded: Long = 0
                inStream = body.byteStream()
                outStream = FileOutputStream(file)
                var read: Int
                while (true) {
                    read = inStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                }
                outStream.flush()
            } catch (e: IOException) {
                file.delete()
                null
            } finally {
                inStream?.close()
                outStream?.close()
            }
        } catch (e: IOException) {
            Logger.d("write file", e.message)
        }
        return file
    }

    fun downloadZipFile(urlStr: String?, destinationFilePath: String): File {
        var inStream: InputStream? = null
        var outStream: OutputStream? = null
        var httpConnection: HttpURLConnection? = null

        try {
            val url = URL(urlStr)

            httpConnection = url.openConnection() as HttpURLConnection
            httpConnection.connect()

            if (httpConnection.responseCode != HttpURLConnection.HTTP_OK) {
                Logger.d(
                    "downloadZipFile",
                    "Server ResponseCode=" + httpConnection.responseCode + " ResponseMessage=" + httpConnection.responseMessage
                )
            }

            // download the file
            inStream = httpConnection.inputStream

            Logger.d("downloadZipFile", "destinationFilePath=$destinationFilePath")
            File(destinationFilePath).createNewFile()
            outStream = FileOutputStream(destinationFilePath)

            val bytes = ByteArray(4096)
            var count: Int
            while (inStream.read(bytes).also { count = it } != -1) {
                outStream.write(bytes, 0, count)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            try {
                outStream?.close()
                inStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            httpConnection?.disconnect()
        }
        val f = File(destinationFilePath)
        Logger.d("downloadZipFile", "f.getParentFile().getPath()=" + f.parentFile.path)
        Logger.d("downloadZipFile", "f.getName()=" + f.name.replace(".zip", ""))
        return f
    }

    fun unzip(inputStream: InputStream, folderName: String) : File? {
        var zipStream: ZipInputStream? = null
        val folder = File(internalDirect, folderName)
        try {
            if (folder.exists()) {
                return folder
            }
            folder.mkdir()
            zipStream = ZipInputStream(BufferedInputStream(inputStream))
            var zipEntry: ZipEntry? = null
            val bufferSize = 2048
            val bytes = ByteArray(bufferSize)
            val newFolderName = folder.canonicalPath
            while (true) {
                zipEntry = zipStream.nextEntry
                if (zipEntry == null) break
                val fileName = zipEntry.name
                val f = File(folder, fileName)
                if (f.canonicalPath.startsWith(newFolderName)) {
                    val fOut = FileOutputStream(f)
                    try {
                        var currentByte: Int
                        while (zipStream.read(bytes).also { currentByte = it } != -1) {
                            fOut.write(bytes, 0, currentByte)
                        }
                    } catch (ee: Exception) {
                    } finally {
                        fOut.flush()
                        fOut.close()
                        zipStream.closeEntry()
                    }
                }
            }
            return folder
        }catch (e: Exception) {
            Logger.d("error unzip")
        } finally {
            try {
                zipStream?.close()
            } catch (e2: Exception) {

            }
        }
        return null
    }

    fun unzip(inputStream: InputStream, location: String, isAll: Boolean = false): String {
        var resPath = ""
        return try {
            val file = File(internalDirect, location)
            if (!file.isDirectory) {
                file.mkdirs()
            }
            val zipStream = ZipInputStream(BufferedInputStream(inputStream))
            zipStream.use { zin ->
                var zipEntry: ZipEntry?
                while (zin.nextEntry.also { zipEntry = it } != null) {
                    if (zipEntry == null) break
                    val path: String = zipEntry!!.name
                    Logger.d("path", path)
                    if (zipEntry!!.isDirectory) {
                        val unzipFile = File(file, path)
                        resPath = unzipFile.path
                        if (!unzipFile.isDirectory) {
                            unzipFile.mkdirs()
                        }
                    } else {
                        val newPath: String = file.path + "/" + path
                        val fileOut = FileOutputStream(newPath, false)
                        val bufferOut = BufferedOutputStream(fileOut)
                        val buffer = ByteArray(2048)
                        var c = zin.read(buffer, 0, 2048)
                        while (c != -1) {
                            bufferOut.write(buffer, 0, c)
                            c = zin.read()
                        }
                        zin.closeEntry()
                        bufferOut.close()
                        fileOut.close()
                    }
                }
                zin.close()
                resPath
            }
        } catch (e: Exception) {
            Logger.e("Unzip exception", e)
            resPath
        }
    }


    fun getAllImages(folderName: String): MutableList<String> {
        val folder = File(folderName)
        val fileList: MutableList<String> = mutableListOf()
        return try {
            val listFile = folder.listFiles()
            if (listFile != null && listFile.isNotEmpty()) {
                listFile.forEach { file ->
                    if (file.name.endsWith(".png")
                        || file.name.endsWith(".jpg")
                        || file.name.endsWith(".jpeg")
                        || file.name.endsWith(".gif")
                        || file.name.endsWith(".bmp")
                        || file.name.endsWith(".webp")
                    ) {
                        fileList.add(file.path)
                    }
                }
            }
            fileList
        } catch (e: java.lang.Exception) {
            fileList
        }
    }

    fun copyWallpaperVideo(path: String?) : File? {
        if (path == null) return null
        val exportFile = File(path)
        val file = File(folderVideo, System.currentTimeMillis().toString() + "_" + exportFile.lastName())
        try {
            if (file.exists()) file.delete()
            exportFile.copyTo(file,true, BUFFER_SIZE_VALUE)
        }catch (e: Exception) {
        }
        return file
    }

    fun delete(path: String?) {
        path?.also { delete(File(it)) }
    }

    fun delete(file: File?) {
        try {
            file?.apply { delete() }
        } catch (e: Exception){ }
    }

    fun copyFileFromUri(uriString: String) : File? {
        try {
            val uri = Uri.parse(uriString)
            val fileName = getFileNameFromUri(uri) ?:
            System.currentTimeMillis().toString() + "."  + MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            val file = File(folderVideo, System.currentTimeMillis().toString() + "_" + fileName)
            if (file.exists()) file.delete()
            val outputStream = FileOutputStream(file)
            WallpaperApp.instance.contentResolver.openInputStream(uri)?.use { input ->
                outputStream.use {output ->
                    input.copyTo(output, BUFFER_SIZE_VALUE)
                }
            }
            return file
        }catch (e: Exception) {

        }
        return null
    }

    fun getFileNameFromUri(uri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            if (uri == null) null
            else WallpaperApp.instance.run { contentResolver.query(uri, null, null, null, null) }?.run {
                cursor = this
                val nameIndex = getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                moveToFirst()
                getString(nameIndex)
            }
        } catch (e: Exception) {
            null
        } finally {
            cursor?.close()
        }
    }

    fun localStorageQuery() : Single<List<WallpaperModel>> {
        return Single.fromCallable {
            val resultList = mutableListOf<WallpaperModel>()
            val projection = arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.MEDIA_TYPE
            )
            val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = " +
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}" +
                    if (WallpaperHelper.isSupportLiveWallpaper) " OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = " + "${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}"
                    else ""

            val order = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
            val destination = MediaStore.Files.getContentUri("external")
            WallpaperApp.instance.contentResolver.query(
                    destination,
                    projection,
                    selection,
                    null,
                    order
            )?.use {
                while (it.moveToNext()) {
                    resultList.add(WallpaperModel().apply {
                        val id = it.getLong(
                                it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                        )
                        val isVideo = it.getInt(
                                it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
                        ) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                        this.id = id.toString()
                        name = it.getString(
                                it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                        )
                        url = ContentUris.withAppendedId(
                                if(isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                                else MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                id
                        ).toString()
                    })
                }
            }
            resultList
        }
    }

    fun deleteFileUsingFileName(fileName: String): Boolean {
        return getUriFromFileName(fileName)?.let {
            WallpaperApp.instance.contentResolver.delete(it, MediaStore.Files.FileColumns.DISPLAY_NAME + "=?", arrayOf(fileName)) > 0
        } ?: false
    }

    fun getUriFromFileName(fileName: String): Uri? {
        val projection = arrayOf(MediaStore.Files.FileColumns._ID)
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        return WallpaperApp.instance.contentResolver.query(
                uri,
                projection,
                MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE ?",
                arrayOf(fileName),
                null
        )?.use {
            it.moveToFirst()
            if (it.count > 0) {
                val fileId = it.getLong(it.getColumnIndex(projection[0]))
                Uri.parse("$uri/$fileId")
            } else null
        }
    }
}