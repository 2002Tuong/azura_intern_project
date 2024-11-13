package com.artgen.app.ui.screen.imagepicker

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.artgen.app.R
import com.artgen.app.data.remote.repository.getUriFromFile
import java.io.File

class ArtGenFileProvider : FileProvider(R.xml.file_paths) {

    companion object {
        fun getImageUri(context: Context): Uri {
            val directory = File(context.cacheDir, "images").also { it.mkdirs() }
            val file = File.createTempFile(
                "selected_image_",
                null,
                directory
            )
            return context.getUriFromFile(file)
        }

        fun getImagesFolder(context: Context): String {
            val path = context.filesDir.absolutePath.plus("/images/")
            File(path).apply {
                if (!exists()) mkdir()
            }
            return path
        }
    }
}
