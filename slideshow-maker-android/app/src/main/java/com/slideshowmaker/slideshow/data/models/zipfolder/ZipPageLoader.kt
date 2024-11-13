package com.slideshowmaker.slideshow.data.models.zipfolder

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream

class ZipPageLoader(idTheme: String, private val file: File, private val context: Context) {
    private val tmpDir = File(context.externalCacheDir, idTheme).also {
        it.deleteRecursively()
        it.mkdirs()
    }

    fun unzipEffect(onUnzipSuccess: () -> Unit) {
        CoroutineScope(context = Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                ZipInputStream(FileInputStream(file)).use { zipInputStream ->
                    generateSequence { zipInputStream.nextEntry }
                        .filterNot { it.isDirectory }
                        .filterNot { it.name.contains("__MACOSX") }
                        .filter { it.name.endsWith(".png") }
                        .forEach { entry ->
                            File(tmpDir, entry.name.substringAfterLast("/"))
                                .also { it.createNewFile() }
                                .outputStream().use { pageOutputStream ->
                                    val buffer = ByteArray(2048)
                                    var len: Int
                                    while (
                                        zipInputStream.read(buffer, 0, buffer.size)
                                            .also { len = it } >= 0
                                    ) {
                                        pageOutputStream.write(buffer, 0, len)
                                    }
                                    pageOutputStream.flush()
                                }
                            zipInputStream.closeEntry()
                        }
                }
                file.delete()
            }

            onUnzipSuccess.invoke()
        }
    }
}