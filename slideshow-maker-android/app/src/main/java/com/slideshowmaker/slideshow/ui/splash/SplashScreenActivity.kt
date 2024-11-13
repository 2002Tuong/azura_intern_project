package com.slideshowmaker.slideshow.ui.splash

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.ironsource.mediationsdk.IronSource
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.data.RemoteConfigRepository
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.databinding.ActivitySplashScreenBinding
import com.slideshowmaker.slideshow.ui.HomeActivity
import com.slideshowmaker.slideshow.ui.dialog.LoadingAdsPopup
import com.slideshowmaker.slideshow.ui.language.LanguageActivity
import com.slideshowmaker.slideshow.utils.ConsentHelper
import com.slideshowmaker.slideshow.utils.LanguageHelper
import com.slideshowmaker.slideshow.utils.extentions.launchAndRepeatOnLifecycleStarted
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class SplashScreenActivity : AppCompatActivity(), KodeinAware {

    override val kodein by lazy { (application as VideoMakerApplication).kodein }
    private lateinit var layoutBinding: ActivitySplashScreenBinding

    private val viewModel: SplashScreenViewModel by instance()
    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.loadLocale(this)
        super.onCreate(savedInstanceState)
        layoutBinding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(layoutBinding.root)
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }

        lifecycleScope.launch {
            this@SplashScreenActivity.let {
                ConsentHelper.obtain(
                    context = it,
                    loadAds = { viewModel.loadData(it) },
                    onDone = { it.finish() }
                )
            }
        }


        launchAndRepeatOnLifecycleStarted {
            viewModel.navigateToMainScreen.collectLatest {
                if (it) {
                    navigateTo()
                }
            }
        }
        launchAndRepeatOnLifecycleStarted {
            viewModel.loadingState.collect {
                loading_progress.isVisible = it
            }
        }

        hideNavigationBar()
    }

    override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }

    private var loadingDialog: DialogFragment? = null

    fun showLoadingDialog() {
        LanguageHelper.loadLocale(this)
        val loadingPopup = LoadingAdsPopup.newInstance()
        loadingPopup.show(supportFragmentManager, null)
    }

    fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
    }

    private fun navigateTo() {
        if (!SharedPreferUtils.isFirstOpenComplete) {
            launchLanguage()
        } else {
            launchHome()
        }
    }

    private fun launchLanguage() {
        LanguageActivity.start(this, false)
//        viewModel.showOpenAdIfAvailableAsync
    }

    private fun launchHome() {
        Intent(this, HomeActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        IronSource.onResume(this)
        if (RemoteConfigRepository.appOpenAdsConfig?.enable == false) {
            viewModel.checkShowInterSplashWhenFail(this)
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
}
