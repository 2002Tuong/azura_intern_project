package com.parallax.hdvideo.wallpapers.services.wallpaper

import android.content.Context
import android.content.SharedPreferences
import android.service.wallpaper.WallpaperService
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import com.parallax.hdvideo.wallpapers.ads.AppOpenAdManager
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import com.parallax.hdvideo.wallpapers.utils.file.FileUtils
import com.techpro.parallax.wallpaper.gl.ParallaxSurfaceView
import com.techpro.parallax.wallpaper.model.ParallaxModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ParallaxService : WallpaperService(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var localStorage: LocalStorage

    private val listEngines: ArrayList<GLEngine> = ArrayList()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ParallaxService onCreate")
        localStorage.get().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        Log.d(TAG, "ParallaxService onDestroy")
        localStorage.get().unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onCreateEngine(): Engine {
        return GLEngine()
    }

    override fun onSharedPreferenceChanged(pref: SharedPreferences?, key: String?) {
        if (key == AppConstants.PreferencesKey.PARALLAX_DATA_CHANGE) {
            for (engine in listEngines) {
                engine.reload()
            }
        }
    }

    private fun getNameFolder(item: WallpaperModel): String {
        val list = item.url!!.split("/")
        return if (item.url == null) "" else list[list.size - 2]
    }

    private fun getImagesParallax(): ArrayList<ParallaxModel> {
        val item = localStorage.parallaxInfo!!
        val listImages = ArrayList<ParallaxModel>()
        val configParam = item.configParam!!.split(",")

        val folder = File(FileUtils.folderParallax.path + "/" + getNameFolder(item))
        folder.listFiles()?.let { listFiles ->
            val bg: File? = listFiles.firstOrNull { it.name == AppConstants.IMAGE_BACKGROUND_NAME }
            if (bg != null) {

                listImages.clear()
                listImages.add(ParallaxModel(bg.path))
                val size = listFiles.size
                (1..size).forEach { index ->
                    listFiles.firstOrNull { it.name == "${AppConstants.IMAGE_LAYER}$index.png" }?.let {
                        val model = ParallaxModel(it.path)
                        model.setValue(configParam[index - 1].toFloat())
                        listImages.add(model)
                    }
                }
            }
        }
        return listImages
    }

    //----------------------------------------------------------------------------------------------

    inner class GLEngine : Engine() {
        private var surfaceView: GLSurfaceView? = null

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            listEngines.add(this)
            AppOpenAdManager.switchOnOff(false)
            surfaceView = GLSurfaceView(this@ParallaxService)
            Log.d(Companion.TAG, "onCreate $isPreview")
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            reload()
        }

        fun reload() {
            if (surfaceView != null) surfaceView!!.setImage(getImagesParallax(), isPreview)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                Log.d(Companion.TAG, "onResume $isPreview")
                surfaceView!!.onResume()
            } else {
                Log.d(Companion.TAG, "onPause $isPreview")
                surfaceView!!.onPause()
            }
        }

        override fun onDestroy() {
            listEngines.remove(this)
            super.onDestroy()
            AppOpenAdManager.switchOnOff(true)
            surfaceView!!.destroy()
            Log.d(Companion.TAG, "onDestroy $isPreview")
        }

        private inner class GLSurfaceView : ParallaxSurfaceView {
            constructor(context: Context?) : super(context) {}
            constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

            override fun getHolder(): SurfaceHolder {
                return surfaceHolder
            }
        }
    }

    companion object {
        const val TAG = "ParallaxService"
    }

}