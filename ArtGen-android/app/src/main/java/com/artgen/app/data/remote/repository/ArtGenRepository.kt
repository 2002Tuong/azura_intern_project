package com.artgen.app.data.remote.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.FileProvider
import com.artgen.app.BuildConfig
import com.artgen.app.R
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.data.remote.ApiService
import com.artgen.app.ui.screen.genart.ArtGenResult
import com.artgen.app.ui.screen.imagepicker.ArtGenFileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class ArtGenRepository(
    private val apiService: ApiService,
    private val context: Context,
    private val dataStore: AppDataStore
) {

    suspend fun saveImageToGallery(
        inputUri: Uri,
        folderName: String = context.getString(R.string.app_name),
        compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        shouldCompress: Boolean = true,
    ): Uri? {
        return withContext(Dispatchers.IO) {
            val bitmap = loadBitmapFromUri(inputUri) ?: return@withContext null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = contentValues(compressFormat)
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
                values.put(MediaStore.Images.Media.IS_PENDING, true)
                // RELATIVE_PATH and IS_PENDING are introduced in API 29.
                val uri: Uri? = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values,
                )
                if (uri != null) {
                    saveImageToStream(
                        bitmap,
                        context.contentResolver.openOutputStream(uri),
                        compressFormat,
                        shouldCompress,
                    )
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    context.contentResolver.update(uri, values, null, null)

                    return@withContext uri
                }
                return@withContext null
            }
            val directory =
                File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + folderName,
                )

            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = System.currentTimeMillis().toString() +
                    getImageExtensionByCompressFormat(compressFormat)
            try {
                val file = File(directory, fileName)
                saveImageToStream(
                    bitmap,
                    FileOutputStream(file),
                    compressFormat,
                    shouldCompress,
                )

                val values = contentValues(compressFormat)
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values,
                )
                return@withContext context.getUriFromFile(file)
            } catch (exception: IOException) {
                return@withContext null
            }
        }
    }

    suspend fun clearTempData() {
        withContext(Dispatchers.IO) {
            runCatching {
                dataStore.setPhotoUri("")
                dataStore.setSelectedArtStyle("")
                File(ArtGenFileProvider.getImagesFolder(context)).listFiles()?.forEach { file ->
                    file.delete()
                }
            }
        }
    }

    private fun getImageExtensionByCompressFormat(compressFormat: Bitmap.CompressFormat?): String {
        return if (compressFormat == Bitmap.CompressFormat.PNG) ".png" else ".jpg"
    }

    private fun saveImageToStream(
        bitmap: Bitmap,
        outputStream: OutputStream?,
        compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        shouldCompress: Boolean = true,
    ) {
        if (outputStream != null) {
            try {
                val quality = if (shouldCompress) 90 else 100
                bitmap.compress(compressFormat, quality, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun contentValues(compressFormat: Bitmap.CompressFormat): ContentValues {
        val mimeType =
            if (compressFormat == Bitmap.CompressFormat.PNG) "image/png" else "image/jpeg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        return values
    }

    suspend fun generateArt(
        inputImage: Uri?,
        imageId: String,
        styleId: String,
        prompt: String
    ): Result<ArtGenResult> {
        return withContext(Dispatchers.IO) {
            try {
                val imageIdPart = imageId.takeIf { it.isNotEmpty() }?.let {
                    MultipartBody.Part.createFormData(name = "image_id", value = it)
                }
                val inputImagePart = if (imageId.isEmpty() && inputImage != null) {
                    uriToFile(inputImage)?.toFormData("input_image")
                } else {
                    null
                }
                val style = MultipartBody.Part.createFormData(
                    name = "style",
                    value = styleId,
                )
                 val text = if (prompt.isNotEmpty()) {
                     MultipartBody.Part.createFormData(
                         name = "text",
                         value = prompt
                     )
                 } else {
                     null
                 }
                val response = apiService.generateArt(
                    imageId = imageIdPart,
                    inputImage = inputImagePart,
                    style = style,
                    text = text
                )
                val artGenResponse = response.body()
                if (response.isSuccessful && artGenResponse != null) {
                    val bitmap = artGenResponse.outputImage.base64ToBitmap()
                    val uri = saveBitmapToTempFile(bitmap)
                    Result.success(ArtGenResult(artGenResponse.imageId, uri!!))
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun File.toFormData(partName: String) = MultipartBody.Part.createFormData(
        partName,
        this.name,
        this.asRequestBody(MEDIA_TYPE_IMAGE.toMediaType())
    )

    private fun uriToFile(uri: Uri): File? {
        val bitmap = loadBitmapFromUri(uri) ?: return null
        val filePath = ArtGenFileProvider.getImagesFolder(context) + "input_image"
        try {
            FileOutputStream(filePath).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return File(filePath)
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        return BitmapFactory.decodeStream(inputStream)
    }

    private fun saveBitmapToTempFile(bitmap: Bitmap): Uri? {
        return try {
            val outPutFile = File.createTempFile(
                "result_temp_",
                ".jpg",
                File(ArtGenFileProvider.getImagesFolder(context))
            )
            FileOutputStream(outPutFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            context.getUriFromFile(outPutFile)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun String.base64ToBitmap(): Bitmap {
        val decodedString: ByteArray = Base64.decode(this, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    companion object {
        internal const val MEDIA_TYPE_IMAGE = "image/*"
    }
}

fun Context.getUriFromFile(file: File): Uri {
    return FileProvider.getUriForFile(
        this,
        "${BuildConfig.APPLICATION_ID}.fileprovider",
        file,
    )
}
