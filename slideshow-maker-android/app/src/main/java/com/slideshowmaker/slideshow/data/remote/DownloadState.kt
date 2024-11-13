package com.slideshowmaker.slideshow.data.remote

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.slideshowmaker.slideshow.utils.FileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import java.io.File

sealed class DownloadState {
    data class Downloading(val progress: Int) : DownloadState()
    object Finished : DownloadState()
    data class Failed(val error: Throwable? = null) : DownloadState()
}

fun ResponseBody.saveFile(filePath: String): Flow<DownloadState> {
    return flow {
        emit(DownloadState.Downloading(0))
        val destinationFile = File(filePath)

        try {
            byteStream().use { inputStream ->
                destinationFile.outputStream().use { outputStream ->
                    val sumBytes = contentLength()
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var progressBytes = 0L
                    var readBytes = inputStream.read(buffer)
                    while (readBytes >= 0) {
                        outputStream.write(buffer, 0, readBytes)
                        progressBytes += readBytes
                        readBytes = inputStream.read(buffer)
                        emit(DownloadState.Downloading(((progressBytes * 100) / sumBytes).toInt()))
                    }
                }
            }
            emit(DownloadState.Finished)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            FileHelper.deleteFile(arrayListOf(filePath))
            emit(DownloadState.Failed(e))
        }
    }.flowOn(Dispatchers.IO).distinctUntilChanged()
}
