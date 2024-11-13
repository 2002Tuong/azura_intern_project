package com.example.videoart.batterychargeranimation

import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import coil.request.ImageRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.videoart.batterychargeranimation.helper.FileHelper
import com.example.videoart.batterychargeranimation.helper.ThemeManager.Companion.THEMES_FOLDER
import com.example.videoart.batterychargeranimation.ui.dialog.LoadingDialogFragment
import com.google.android.material.imageview.ShapeableImageView
import com.slideshowmaker.slideshow.data.remote.AnimationServerInterface
import com.slideshowmaker.slideshow.data.remote.DownloadState
import com.slideshowmaker.slideshow.data.remote.saveFile
import com.slideshowmaker.slideshow.modules.music_player.MusicPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File

class TestActivity : AppCompatActivity() {
    val dialog = LoadingDialogFragment()
    private val service: AnimationServerInterface by inject()
    private val musicPlayer: MusicPlayer by inject()
    private val finishDownLoad: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        val imageLoader = ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
        dialog.show(supportFragmentManager, "")
        val view = findViewById<ShapeableImageView>(R.id.preview)
        view.load(
            "https://charging-battery.ap-south-1.linodeobjects.com/Cutie/Cute 7/v2/Thumbnail_Sipmle 7.png",
            imageLoader
        )
        val imageRequest = ImageRequest.Builder(this)
            .data("https://charging-battery.ap-south-1.linodeobjects.com/Cutie/Cute 7/v2/Cute 7_APNG.webp")
            .target(
                onStart = {

                },
                onError = {

                },
                onSuccess = {result ->
                    dialog.dismiss()
                    view.load(result)
                }
            ).build()
        imageLoader.enqueue(imageRequest)

        val filePath = FileHelper.createTempAudioFile("123")
        lifecycleScope.launch(Dispatchers.IO) {
            service.downloadLargeFile("https://charging-battery.ap-south-1.linodeobjects.com/Cutie/Cute 7/Mp3_Cute7.mp3")
                .saveFile(filePath)
                .collectLatest {
                    if(it == DownloadState.Finished) {
                        finishDownLoad.update { true }
                    }
                }
        }

        val filePath2 = FileHelper.createTempAudioFile("font_temp")
        lifecycleScope.launch(Dispatchers.IO) {
            service.downloadLargeFile("https://charging-battery.ap-south-1.linodeobjects.com/Fonts/Robot Monster.ttf")
                .saveFile(filePath2).collectLatest {
                    if(it == DownloadState.Finished) {
                        Log.d("FIle", "${filesDir.path}")
                        val themesFolder = File(filesDir, THEMES_FOLDER)
                        val themeFolder = File(themesFolder, "0001")
                        // Create the necessary directories if they don't exist
                        themesFolder.mkdirs()
                        themeFolder.mkdirs()

                        val temp = "${themeFolder}/font"
                        FileHelper.copyFileTo(filePath2, temp)
                        runOnUiThread {
                            val typeface = Typeface.createFromFile(File(temp))
                            findViewById<TextView>(R.id.battery_state).typeface = typeface
                        }
                    }
                }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                finishDownLoad.collectLatest {
                    if(it) {
                        musicPlayer.changeMusic(filePath)
                        musicPlayer.play()
                    }
                }
            }
        }


    }
}