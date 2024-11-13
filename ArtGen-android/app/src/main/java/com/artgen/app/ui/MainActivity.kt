package com.artgen.app.ui

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.artgen.app.ads.AdsInitializer
import com.artgen.app.ads.AdsManager
import com.artgen.app.ads.BillingClientLifecycle
import com.artgen.app.ads.OpenAdsManager
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.ui.screen.language.LanguageManager
import com.artgen.app.ui.screen.navigation.MainNavigationHost
import com.artgen.app.ui.screen.rating.RatingManager
import com.artgen.app.ui.screen.settings.ShareController
import com.artgen.app.ui.theme.ArtGenTheme
import com.artgen.app.utils.AdsUtils
import com.artgen.app.utils.LocalAdUtil
import com.artgen.app.utils.LocalAdsManager
import com.artgen.app.utils.LocalLanguageManager
import com.artgen.app.utils.LocalOpenAdsManager
import com.artgen.app.utils.LocalRatingManager
import com.artgen.app.utils.LocalRemoteConfig
import com.artgen.app.utils.LocalShareController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.ironsource.mediationsdk.IronSource
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val remoteConfig: RemoteConfig by inject()
    private val languageManager: LanguageManager by inject()
    private val adsManager: AdsManager by lazy { AdsManager(this) }
    private val adsInitializer: AdsInitializer by inject()
    private val shareController: ShareController by lazy { ShareController(this) }
    private val openAdsManager: OpenAdsManager by inject()
    private val adUtil: AdsUtils by inject()
    private val billingClientLifecycle: BillingClientLifecycle by inject()
    private val viewModel: MainViewModel by viewModel()
    private val dataStore: AppDataStore by inject()
    private val ratingManager by lazy { RatingManager(this, dataStore, remoteConfig) }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(adsManager)
        lifecycle.addObserver(billingClientLifecycle)
        viewModel.observePurchases()
        lifecycleScope.launch {
            remoteConfig.fetchRemoteConfigAsync()
            if (!remoteConfig.offAllAds()) {
                adsInitializer.init(this@MainActivity)
            }
            adUtil.isAdEnabled = !remoteConfig.offAllAds()

            if (remoteConfig.shouldHideNavigationBar()) {
                hideNavigationBar()
            }

            if (adUtil.isAdEnabled && intent.getStringExtra("SOURCE") == "REMINDER"){
                adsManager.loadImagePickerNativeAd()
            }
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            CompositionLocalProvider(
                LocalLanguageManager provides languageManager,
                LocalAdsManager provides adsManager,
                LocalRemoteConfig provides remoteConfig,
                LocalShareController provides shareController,
                LocalOpenAdsManager provides openAdsManager,
                LocalAdUtil provides adUtil,
                LocalRatingManager provides ratingManager
            ) {
                ArtGenTheme {
                    val navController = rememberAnimatedNavController()
                    Surface(modifier = Modifier.fillMaxSize()) {
                        MainNavigationHost(
                            navController = navController,
                            onExitApp = {
                                lifecycleScope.launch {
                                    dataStore.increaseExitAppCount()
                                }
                                finishAffinity()
                            }
                        )
                    }
                }
            }
        }
    }

    fun hideNavigationBar() {
        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && remoteConfig.shouldHideNavigationBar()) {
            hideNavigationBar()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adsInitializer.clear()
        remoteConfig.clear()
        openAdsManager.clear()
    }

    override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }

    override fun onResume() {
        super.onResume()
        IronSource.onResume(this)
    }

}

