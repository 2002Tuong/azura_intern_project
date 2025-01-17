package com.example.videoart.batterychargeranimation.helper

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.view.View
import com.example.videoart.batterychargeranimation.App
import com.example.videoart.batterychargeranimation.BuildConfig
import com.example.videoart.batterychargeranimation.R
import java.io.BufferedInputStream
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object FileHelper {
    val internalPath = App.instance.applicationContext.getExternalFilesDir(null as String?)

    //private val dataFolderPath = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/tempdata"
    private val txtTempFolderPath =
        "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/tempText"
    private val videoTempFolder =
        "$internalPath/tempvideo"
    private val tempRecordAudioFolder =
        "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/tempRecordAudio"

    val themeFolderPath = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/theme"
    val frameFolderPath = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/frame"
    val filterFolderPath = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/filter"

    val themeFolderUnzip = "${App.instance.applicationContext.externalCacheDir}"

    val audioDefaultFolderPath = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/audio"
    val defaultAudio = "$audioDefaultFolderPath/upbeat.mp3"
    val airboundAudio = "$audioDefaultFolderPath/airbound.mp3"
    val allNightDanceAudio = "$audioDefaultFolderPath/all_night_dance.mp3"
    val waitForUsAudio = "$audioDefaultFolderPath/wait_for_us.mp3"
    val topOfTheMorning = "$audioDefaultFolderPath/top_of_the_morning.mp3"
    private val musicTempDataFolderPath =
        "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/musicTempData"
    private val stickerTempFolderPath =
        "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/stickerTemp"
    val outputFolderPath =
        "$internalPath/DCIM/${App.instance.applicationContext.getString(R.string.folder_name)}"
    val myStuioFolderPath get() = outputFolderPath


    val tempImageFolderPath = "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/tempImage"

    fun saveBitmapToTempData(bitmap: Bitmap?): String {
        val tempDataFolderPath = tempImageFolderPath
        val tempDataFolder = File(tempDataFolderPath)
        if (!tempDataFolder.exists()) {
            tempDataFolder.mkdirs()
        }
        val outFile =
            File("$tempDataFolderPath/${System.currentTimeMillis()}${View.generateViewId()}")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(outFile)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return outFile.absolutePath
    }

    fun saveBitmapToTempData(bitmap: Bitmap?, outFileName: String): String {
        val tempDataFolderPath = tempImageFolderPath
        val tempDataFolder = File(tempDataFolderPath)
        tempDataFolder.mkdirs()
        val outFile = File("$tempDataFolderPath/$outFileName")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(outFile)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return outFile.absolutePath
    }

    fun deleteTempFolder() {
        Thread {
            val internalPath = Environment.getExternalStorageDirectory()
            val tempDataFolderPath =
                "$internalPath/Android/data/${BuildConfig.APPLICATION_ID}/tempdata"
            val tempDataFolder = File(tempDataFolderPath)
            if (tempDataFolder.exists() && tempDataFolder.isDirectory) {
                for (file in tempDataFolder.listFiles()) {
                    file.delete()
                }
            }
        }.start()
    }

    fun getTempMp3OutPutFile(): String {
        File(musicTempDataFolderPath).mkdirs()
        return "$musicTempDataFolderPath/audio_${System.currentTimeMillis()}.mp4"
    }

    fun getTempAudioOutPutFile(fileType: String): String {
        return "${App.instance.applicationContext.cacheDir.path}/audio_${System.currentTimeMillis()}.$fileType"
    }

    fun saveStickerToTemp(bitmap: Bitmap): String {
        File(stickerTempFolderPath).mkdirs()

        val outFile = File("$stickerTempFolderPath/sticker_${System.currentTimeMillis()}")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(outFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return outFile.absolutePath
    }

    fun getTempVideoPath(): String {
        File(videoTempFolder).mkdirs()
        return "$videoTempFolder/video-temp-${System.currentTimeMillis()}.mp4"
    }

    fun getTempM4aAudioPath(): String {
        File(videoTempFolder).mkdirs()
        return "$videoTempFolder/video-temp-${System.currentTimeMillis()}.mp3"
    }
    /*    fun getTempVideoPath(inPath: String):String {
            File(videoTempFolder).mkdirs()
            val file = File(inPath)
            val name = "${file.parentFile.name}${file.name}"
            return "$videoTempFolder/$name"
        }*/

    fun getOutputVideoPath(): String {
        File(outputFolderPath).mkdirs()
        return "$outputFolderPath/video-${System.currentTimeMillis()}.mp4"
    }

    fun getOutputVideoPath(size: Int): String {
        File(outputFolderPath).mkdirs()
        return "$outputFolderPath/video-${System.currentTimeMillis()}-$size.mp4"
    }


    fun clearTemp() {
        val tempMusicFolder = File(musicTempDataFolderPath)
        try {
            if (tempMusicFolder.exists() && tempMusicFolder.isDirectory && tempMusicFolder.listFiles() != null) {
                for (file in tempMusicFolder.listFiles()) {
                    file.delete()
                }
            }

        } catch (e: java.lang.Exception) {

        }

        val tempVideoFolder = File(videoTempFolder)
        try {
            if (tempVideoFolder.exists() && tempVideoFolder.isDirectory && tempVideoFolder.listFiles() != null) {
                for (file in tempVideoFolder.listFiles()) {
                    file.delete()
                }
            }

        } catch (e: java.lang.Exception) {

        }

        val tempStickerFolder = File(stickerTempFolderPath)
        try {
            if (tempStickerFolder.exists() && tempStickerFolder.isDirectory && tempStickerFolder.listFiles() != null) {
                for (file in tempStickerFolder.listFiles()) {
                    file.delete()
                }
            }

        } catch (e: java.lang.Exception) {

        }

        val tempImageFolder = File(tempImageFolderPath)
        try {
            if (tempImageFolder.exists() && tempImageFolder.isDirectory && tempImageFolder.listFiles() != null) {
                for (file in tempImageFolder.listFiles()) {
                    file.delete()
                }
            }

        } catch (e: java.lang.Exception) {

        }

    }

    fun getTextTempOutFile(): String {
        File(txtTempFolderPath).mkdirs()
        return "$txtTempFolderPath/${System.currentTimeMillis()}.txt"
    }


    fun writeTextListFile(filePathList: ArrayList<String>): String {
        val txtOutFilePath = getTextTempOutFile()
        val outFile = File(txtOutFilePath)
        try {
            val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(outFile)))
            for (path in filePathList) {
                writer.write("file '${path}'\n")
            }
            writer.close()
        } catch (e: java.lang.Exception) {

        }

        return outFile.absolutePath

    }

    fun getAudioRecordTempFilePath(): String {
        File(tempRecordAudioFolder).mkdirs()
        return "${tempRecordAudioFolder}/record_${System.currentTimeMillis()}.3gp"
    }

    fun copyFileTo(inPath: String, outPath: String) {
        val inputStream = FileInputStream(File(inPath))
        val outputStream = FileOutputStream(File(outPath))
        copyFile(inputStream, outputStream)
    }

    private fun copyFile(inputStream: InputStream, outputStream: FileOutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
    }

    fun deleteFile(pathList: ArrayList<String>) {
        for (path in pathList) {
            val file = File(path)
            if (file.exists()) file.delete()
        }
    }

    fun createTempAudioFile(id: String): String {
        val fileDir = File(App.instance.applicationContext.cacheDir, "/audio/")
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        return File(App.instance.applicationContext.cacheDir, "/audio/${id}").path
    }

    fun listDownloadedAudio(): List<Triple<String, String, Int>> {
        val fileDir = File(App.instance.applicationContext.cacheDir, "/audio/")
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        return fileDir.listFiles()
            ?.filter { it.exists() }
            ?.filter { extractAudioDuration(it.path) != null }
            ?.map { Triple(it.nameWithoutExtension, it.path, extractAudioDuration(it.path) ?: 0) }
            .orEmpty()
    }

    private fun extractAudioDuration(filePath: String): Int? {
        return try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(filePath)
            val durationStr =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?: "0"
            (durationStr.toLong() / 1000L).toInt()
        } catch (exception: java.lang.RuntimeException) {
            null
        }
    }

    fun unpackZip(path: String, zipName: String): Boolean {
        val `is`: InputStream
        val zis: ZipInputStream
        try {
            var filename: String
            `is` = FileInputStream(path + zipName)
            zis = ZipInputStream(BufferedInputStream(`is`))
            var ze: ZipEntry
            val buffer = ByteArray(1024)
            var count: Int
            while (true) {
                ze = zis.nextEntry ?: break
                filename = ze.name
                if (filename.contains(File.separator)) continue

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory) {
                    val fmd = File(path + filename)
                    fmd.mkdirs()
                    continue
                }
                val fout = FileOutputStream(path + filename)
                while (zis.read(buffer).also { count = it } != -1) {
                    fout.write(buffer, 0, count)
                }
                fout.close()
                zis.closeEntry()
            }
            zis.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

}