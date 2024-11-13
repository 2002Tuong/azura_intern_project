package com.calltheme.app.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.telecom.TelecomManager
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.calltheme.app.utils.BannerAdsHelpers
import com.screentheme.app.R
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.databinding.ActivityMainBinding
import com.screentheme.app.utils.extensions.getLanguage
import com.screentheme.app.utils.helpers.CountTimeHelper
import com.screentheme.app.utils.helpers.LanguageSupporter
import com.screentheme.app.utils.helpers.REQUEST_CODE_SET_DEFAULT_DIALER
import com.screentheme.app.utils.helpers.REQUEST_CODE_WRITE_SETTINGS
import com.screentheme.app.utils.helpers.SharePreferenceHelper
import com.screentheme.app.utils.helpers.isQPlus
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController

    val isDestinationChanged : MutableLiveData<NavDestination> = MutableLiveData()
    private val countTimeHelper: CountTimeHelper by inject()
    var interSelectCategoryEnable = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        countTimeHelper.createCategoryInterTime(
            CountTimeHelper.NEXT_TIME_INTER_CATEGORY,
            CountTimeHelper.INTERVAL_TIME
        ) {
            interSelectCategoryEnable = true
        }

        var currentAppLanguage = SharePreferenceHelper.getLanguage(this)

        if (currentAppLanguage == null) {
            LanguageSupporter.setAppLanguageByCode(
                this,
                Resources.getSystem().configuration.locale.language
            )
        } else {
            LanguageSupporter.setAppLanguageByCode(this, currentAppLanguage.languageCode)
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_theme,
                R.id.navigation_dyi_theme,
                R.id.navigation_my_design,
                R.id.navigation_settings
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        supportActionBar?.hide()
        navController
            .addOnDestinationChangedListener { _, destination, _ ->
                isDestinationChanged.postValue(destination)
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResultCallback?.invoke(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            Log.d("qvk", "resultCode = $resultCode")
            if (resultCode == Activity.RESULT_OK) {
                onRequestDialerCallBack?.invoke(true)
            } else {
                onRequestDialerCallBack?.invoke(false)
            }
        } else if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            if (Settings.System.canWrite(this)) {
                onRequestWriteSettingsPermissionCallback?.invoke(true)
            } else {
                onRequestWriteSettingsPermissionCallback?.invoke(false)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && AppRemoteConfig.hideNavigationBar) {
            hideNavigationBar()
        }
    }

    @SuppressLint("InlinedApi")
    fun launchSetDefaultDialerIntent(callback: (granted: Boolean) -> Unit) {

        val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
        val isAlreadyDefaultDialer = packageName == telecomManager.defaultDialerPackage

        if (isAlreadyDefaultDialer) {
            return
        }
        if (isQPlus()) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager!!.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(
                    RoleManager.ROLE_DIALER
                )
            ) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
            }
        } else {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(
                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                packageName
            ).apply {
                try {
                    startActivityForResult(this, REQUEST_CODE_SET_DEFAULT_DIALER)

                } catch (e: ActivityNotFoundException) {
                } catch (e: Exception) {
                }
            }
        }

        onRequestDialerCallBack = {
            callback.invoke(it)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (navController.currentDestination?.id == R.id.navigation_pick_language ||
            navController.currentDestination?.id == R.id.navigation_onboarding ||
            navController.currentDestination?.id == R.id.navigation_pick_language_duplicate
        ) {
            finish()
        }
    }

    override fun onDestroy() {
        BannerAdsHelpers.onDestroy()
        countTimeHelper.cancelAll()
        super.onDestroy()
        Log.d("MainActivity", "onDestroy")
    }

    fun startCategoryTimer() {
        countTimeHelper.startCategoryInterTime()
    }

    fun stopCategoryTimer() {
        countTimeHelper.stopCategoryInterTime()
    }

    fun restartCategoryTimer(){
        countTimeHelper.stopCategoryInterTime()
        countTimeHelper.startCategoryInterTime()
    }
}