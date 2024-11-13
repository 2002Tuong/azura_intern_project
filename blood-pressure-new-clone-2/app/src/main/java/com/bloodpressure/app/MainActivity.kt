package com.bloodpressure.app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.bloodpressure.app.ads.AdsInitializer
import com.bloodpressure.app.ads.AdsManager
import com.bloodpressure.app.ads.BillingClientLifecycle
import com.bloodpressure.app.ads.OpenAdsManager
import com.bloodpressure.app.data.remote.RemoteConfig
import com.bloodpressure.app.fcm.NotificationController
import com.bloodpressure.app.screen.MainNavigationHost
import com.bloodpressure.app.screen.MainViewModel
import com.bloodpressure.app.screen.home.settings.ShareController
import com.bloodpressure.app.ui.theme.BloodPressureAndroidTheme
import com.bloodpressure.app.utils.DefaultReminderManager
import com.bloodpressure.app.utils.LanguageManager
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalBillingClientLifecycle
import com.bloodpressure.app.utils.LocalLanguageManager
import com.bloodpressure.app.utils.LocalRemoteConfig
import com.bloodpressure.app.utils.LocalShareController
import com.bloodpressure.app.utils.LocalTextFormatter
import com.bloodpressure.app.utils.TextFormatter
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.ironsource.mediationsdk.IronSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val remoteConfig: RemoteConfig by inject()
    private val adsInitializer: AdsInitializer by inject()
    private val viewModel: MainViewModel by viewModel()
    private val adsManager: AdsManager by lazy { AdsManager(this) }
    private val billingClientLifecycle: BillingClientLifecycle by inject()
    private val openAdsManager: OpenAdsManager by inject()
    private val languageManager: LanguageManager by inject()
    private val notificationController: NotificationController by inject()
    private val shareController: ShareController by lazy { ShareController(this) }
    private val textFormatter: TextFormatter by lazy { TextFormatter(this) }
    private val reminderManager: DefaultReminderManager by inject()
    private var onActivityResult: () -> Unit = {}
    private var getResult: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            withTimeoutOrNull(10_000) {
                remoteConfig.fetchRemoteConfigAsync()
            }

            if (remoteConfig.shouldHideNavigationBar) {
                hideNavigationBar()
            }

            if (!remoteConfig.offAllAds()) {
                adsInitializer.init(this@MainActivity)
            }

            val reminderMode = remoteConfig.getReminderMode()
            reminderManager.startDefaultReminder(reminderMode = reminderMode)
        }
        createNotificationChannel()
        viewModel.observePurchases()
        lifecycle.addObserver(billingClientLifecycle)
        lifecycle.addObserver(adsManager)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        getResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            onActivityResult.invoke()
        }
        setContent {
            CompositionLocalProvider(
                LocalLanguageManager provides languageManager,
                LocalShareController provides shareController,
                LocalTextFormatter provides textFormatter,
                LocalBillingClientLifecycle provides billingClientLifecycle,
                LocalAdsManager provides adsManager,
                LocalRemoteConfig provides remoteConfig,
            ) {
                val systemUiController = rememberSystemUiController()

                // Update the dark content of the system bars to match the theme
                DisposableEffect(systemUiController) {
                    systemUiController.setStatusBarColor(
                        color = Color.Transparent,
                        darkIcons = true
                    )
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = false
                    )
                    onDispose {}
                }
                BloodPressureAndroidTheme {
                    val navController = rememberNavController()
                    Surface(modifier = Modifier.fillMaxSize()) {
                        MainNavigationHost(
                            navController = navController,
                            onExitApp = { finish() },
                            hideNavigationBar = ::hideNavigationBar
                        )
                    }
                }
            }
        }
    }

    fun updateActivityResultAction(action: () -> Unit) {
        onActivityResult = action
    }

    fun getResultLauncher() = getResult

    override fun onDestroy() {
        super.onDestroy()
        openAdsManager.clear()
        remoteConfig.clear()
        adsInitializer.clear()
    }

    override fun onResume() {
        super.onResume()
        IronSource.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationController.createNotificationChannel()
            notificationController.createDefaultReminderNotificationChannel(this)
            notificationController.createFullscreenReminderChannel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isNotificationPermissionGranted(): Boolean = ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED

    private fun hideNavigationBar() {
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
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
        lifecycleScope.launch {
            remoteConfig.waitRemoteConfigLoaded()
            if (hasFocus && remoteConfig.shouldHideNavigationBar) {
                hideNavigationBar()
            }
        }
    }
}
