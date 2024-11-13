package com.wifi.wificharger.ui.main

import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.wifi.wificharger.R
import com.wifi.wificharger.data.local.AppDataStore
import com.wifi.wificharger.data.remote.RemoteConfig
import com.wifi.wificharger.databinding.ActivityMainBinding
import com.wifi.wificharger.utils.LanguageUtils
import com.wifi.wificharger.utils.RemoteConfigManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {
    lateinit var navController: NavController
    var onRequestPermissionsResultCallback: ((requestCode: Int, permissions: Array<out String>, grantResults: IntArray) -> Unit)? = null
    private lateinit var binding: ActivityMainBinding
    private val remoteConfig: RemoteConfig = RemoteConfig()
    private val viewModel: MainViewModel by viewModel()
    private val appDataStore: AppDataStore by inject()
    private val languageUtils: LanguageUtils by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.onBackPressedDispatcher.addCallback {
            if (::navController.isInitialized.not()) return@addCallback
            if (navController.currentDestination?.id == R.id.navigation_pick_language ||
                navController.currentDestination?.id == R.id.navigation_onboarding ||
                navController.currentDestination?.id == R.id.navigation_pick_language_dup
            ) {
                finish()
            }
        }
        lifecycleScope.launch {
            RemoteConfigManager.getInstance().fetchRemoteConfigAsync()
            RemoteConfigManager.getInstance().waitRemoteConfigLoaded()

            if (remoteConfig.shouldHideNavigationBar()) {
                hideSystemUI()
            }
            withContext(Dispatchers.Main) {
                setupView()
            }
            val currentAppLanguage = appDataStore.selectedLanguage

            if (currentAppLanguage.isEmpty()) {
                languageUtils.setAppLanguageByCode(
                    this@MainActivity,
                    Resources.getSystem().configuration.locale.language
                )
            } else {
                languageUtils.setAppLanguageByCode(this@MainActivity, currentAppLanguage)
            }
        }
    }

    private fun hideSystemUI() {
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window?.statusBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else @Suppress("DEPRECATION") {
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && remoteConfig.shouldHideNavigationBar()) {
            hideSystemUI()
        }
    }

    private fun setupView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResultCallback?.invoke(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        RemoteConfigManager.getInstance().clear()
    }

    override fun onPause() {
        super.onPause()
//        IronSource.onPause(this)
    }

    override fun onResume() {
        super.onResume()
//        IronSource.onResume(this)
    }
}