package com.example.videoart.batterychargeranimation.ui.batterycharger

import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import coil.request.ImageRequest
import com.example.videoart.batterychargeranimation.data.local.PreferenceUtils
import com.example.videoart.batterychargeranimation.databinding.ActivityAnimationBatteryChargerBinding
import com.example.videoart.batterychargeranimation.helper.ThemeManager
import com.example.videoart.batterychargeranimation.model.ClosingMethod
import com.example.videoart.batterychargeranimation.model.Duration
import com.example.videoart.batterychargeranimation.model.Theme
import com.example.videoart.batterychargeranimation.receiver.BatteryStateReceiver
import com.example.videoart.batterychargeranimation.ui.charging.ChargingActivity
import com.example.videoart.batterychargeranimation.utils.AdsUtils
import com.slideshowmaker.slideshow.modules.music_player.MusicPlayer
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File

private val defaulTheme = Theme(
    id = "DEFAULT_THEME",
    thumbnail = "file:///android_asset/default.png",
    animation = "",
    sound = "",
    fontId = "",
    category = "",
    fromRemote = false
)
class AnimationChargerActivity : AppCompatActivity(),
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener{
    private lateinit var binding: ActivityAnimationBatteryChargerBinding
    private var theme: Theme? = null
    private lateinit var batteryReceiver: BatteryStateReceiver
    private val musicPlayer: MusicPlayer by inject()

    private val isPlaySound = PreferenceUtils.soundActive
    private val duration = PreferenceUtils.duration
    private val closingMethod = PreferenceUtils.closingMethod

    private lateinit var detector: GestureDetectorCompat
    private val startChargingActivity = {
        Intent(this, ChargingActivity::class.java).apply {
            startActivity(this)
        }
    }
    private val countDown = object : CountDownTimer(PreferenceUtils.duration.value, 1000L) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            startChargingActivity.invoke()
            finishAndRemoveTask()
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimationBatteryChargerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        theme = ThemeManager.getInstance(this).getCurrentThemeConfig()

        val imageLoader = ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }.build()

        if(theme == null) theme = ThemeManager.getInstance(this).getTheme(ThemeManager.DEFAULT_THEME_ID)
        theme?.let {
            binding.preview.load(it.thumbnail, imageLoader)
            val request = ImageRequest.Builder(this)
                .data(it.animation)
                .target(
                    onSuccess = {result ->
                       binding.preview.load(result)
                    }
                ).build()
            imageLoader.enqueue(request)

            if(it.fontId.isNotEmpty()) {
                binding.batteryState.typeface = Typeface.createFromFile(File(it.fontId))
            }

            //apply sound
            if(it.sound.isNotEmpty() && isPlaySound) {
                musicPlayer.changeMusic(it.sound)
                musicPlayer.play()
            }


        }

        batteryReceiver = BatteryStateReceiver {
            binding.batteryState.text = "${it} %"
        }
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        lifecycleScope.launch {
            hideNavigationBar()
        }
        startCount()

        detector = GestureDetectorCompat(this, this)
        // Set the gesture detector as the double-tap
        // listener.
        detector.setOnDoubleTapListener(this)
        AdsUtils.requestNativeCharging(this)
    }

    private fun startCount() {
        if(duration != Duration.DURATION_ALWAYS) {
            countDown.start()
        }
    }


    override fun onResume() {
        super.onResume()
        theme?.let {
            if(it.sound.isNotEmpty() && isPlaySound) {
                musicPlayer.play()
            }

        }
    }

    override fun onPause() {
        super.onPause()

        musicPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        Log.d("ChargingActivity", "OnDestroy")
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if(detector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    fun hideNavigationBar() {
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        window.decorView.systemUiVisibility = flags
        window.decorView.setOnSystemUiVisibilityChangeListener {
            if ((it and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                window.decorView.systemUiVisibility = flags
            }
        }
    }

    //implement GestureDetector

    override fun onDown(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onDown: $event")
        if(closingMethod == ClosingMethod.SINGLE_TAP) {
            startChargingActivity.invoke()
            finishAndRemoveTask()
        }
        return true
    }

    override fun onFling(
        event1: MotionEvent?,
        event2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.d(DEBUG_TAG, "onFling: $event1 $event2")
        return true
    }

    override fun onLongPress(event: MotionEvent) {
        Log.d(DEBUG_TAG, "onLongPress: $event")
    }

    override fun onScroll(
        event1: MotionEvent?,
        event2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        Log.d(DEBUG_TAG, "onScroll: $event1 $event2")
        return true
    }

    override fun onShowPress(event: MotionEvent) {
        Log.d(DEBUG_TAG, "onShowPress: $event")
    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onSingleTapUp: $event")
        return true
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onDoubleTap: $event")
        if(closingMethod == ClosingMethod.DOUBLE_TAP) {
            startChargingActivity.invoke()
            finishAndRemoveTask()
        }
        return true
    }

    override fun onDoubleTapEvent(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onDoubleTapEvent: $event")
        return true
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onSingleTapConfirmed: $event")
        return true
    }
    companion object {
        private const val DEBUG_TAG = "DEBUG_TAG"
    }
}