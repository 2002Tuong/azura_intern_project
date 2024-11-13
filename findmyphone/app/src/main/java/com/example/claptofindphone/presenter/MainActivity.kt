@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package com.example.claptofindphone.presenter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ads.control.admob.AppOpenManager
import com.example.claptofindphone.NavGraphDirections
import com.example.claptofindphone.R
import com.example.claptofindphone.data.local.PreferenceSupplier
import com.example.claptofindphone.databinding.ActivityMainBinding
import com.example.claptofindphone.models.ActivatedState
import com.example.claptofindphone.presenter.common.MediaPlayerHelper
import com.example.claptofindphone.presenter.common.audio.ObserveClapService
import com.example.claptofindphone.presenter.common.throttleFirst
import com.example.claptofindphone.presenter.dialog.ExitDialog
import com.example.claptofindphone.presenter.dialog.InappRatingDialog
import com.example.claptofindphone.presenter.result.ActiveManager
import com.example.claptofindphone.presenter.select.SelectFragment
import com.example.claptofindphone.utils.AdsHelper
import com.example.claptofindphone.utils.LanguageSupporter
import com.ironsource.mediationsdk.IronSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks
import javax.inject.Inject


@AndroidEntryPoint
open class MainActivity : AppCompatActivity() {

    private val layoutBinding by viewBinding(ActivityMainBinding::bind)
    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment?)!!.navController
    }

    private lateinit var serviceIntent: Intent
    private val exitDialog = ExitDialog()
    private val inappRatingDialog = InappRatingDialog()

    @Inject
    lateinit var mediaPlayerHelper: MediaPlayerHelper

    @Inject
    lateinit var preferenceSupplier: PreferenceSupplier

    @Inject
    lateinit var adsHelper: AdsHelper

    @Inject
    lateinit var activeManager: ActiveManager

    @Inject
    lateinit var languageSupporter: LanguageSupporter
    fun startService() {
        if (!ObserveClapService.serviceRunning) {
            startService(serviceIntent)
        }
    }

    fun stopService() {
        stopService(serviceIntent)
    }


    public override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        languageSupporter.loadLocale(this)
        if (ClapToFindApplication.get().getVibrationHelper().getIsVibrating()) {
            ClapToFindApplication.get().stopVibrate()
        }
        val isStoppedSound = intent.extras?.getBoolean("SHOULD_STOP_AUDIO") as? Boolean
        if (isStoppedSound == true) {
            //stop sound
            stopSound()
            activeManager.setActive(false)

        }

        ClapToFindApplication.get().turnOfFlash()
        setContentView(R.layout.activity_main)
        serviceIntent = Intent(this.applicationContext, ObserveClapService::class.java)


        with(layoutBinding) {

            imageBack.clicksWithLifecycleScope {
                navController.apply {
                    navigate(NavGraphDirections.globalToFindPhoneFragment())
                }
            }

            imageRight.clicksWithLifecycleScope {
                navController.popBackStack()
            }

            txtApply.clicksWithLifecycleScope {
                val fragmentInstance =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.childFragmentManager?.fragments?.get(
                        0
                    )
                (fragmentInstance as SelectFragment).applySelect()
                navController.apply {
                    navigate(NavGraphDirections.globalToResultFragment(ActivatedState.Active))
                }
            }
            imageHowToUse.clicksWithLifecycleScope {

                navController.apply {
                    navigate(NavGraphDirections.globalToHowToUse())
                }
            }
            txtFindPhone.isSelected = true
            txtFindPhone.clicksWithLifecycleScope {
                it.isSelected = true
                txtSetting.isSelected = false
                navController.apply {
                    navigateUp()
                    navigate(NavGraphDirections.globalToFindPhoneFragment())
                }
            }
            txtSetting.isSelected = false
            txtSetting.clicksWithLifecycleScope {
                it.isSelected = true
                txtFindPhone.isSelected = false
                navController.apply {
                    if (currentDestination?.id != R.id.findPhoneFragment) navigateUp()
                    navigate(NavGraphDirections.globalToSettingFragment())
                }
            }
        }

        with(exitDialog) {
            onExitApp {
                this.dismiss()
                this@MainActivity.finish()
            }
            onRateApp {
                inappRatingDialog.show(this@MainActivity)
            }
        }
    }

    fun setTitle(title: String) {
        layoutBinding.textViewToolbarTitle.text = title
    }

    fun showTopBar(isShow: Boolean) {
        layoutBinding.topAppBar.isVisible = isShow
    }

    fun showBackButton(isShow: Boolean) {
        layoutBinding.imageBack.isVisible = isShow
    }

    fun showRightButton(isShow: Boolean) {
        layoutBinding.imageRight.isVisible = isShow
    }

    fun showApplyButton(isShow: Boolean) {
        layoutBinding.txtApply.isVisible = isShow
    }

    fun showHowToUseButton(isShow: Boolean) {
        layoutBinding.imageHowToUse.isVisible = isShow
    }

    fun showProfileMenu(isShow: Boolean) {
        layoutBinding.toolbar.menu.findItem(R.id.item_profile).isVisible = isShow
    }

    fun showBottomBar(isShow: Boolean) {
        layoutBinding.bottomAppBar.isVisible = isShow
    }

    fun hideKeyBoard(view: View) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager?
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    protected fun View.clicksWithLifecycleScope(action: (View) -> Unit) {
        this.clicks().throttleFirst(500).catch {
            Log.d("MainActivity", "clicks Exception: $it")
        }.onEach {
            action(this)
        }.launchIn(lifecycleScope)
    }

    protected fun <T> Flow<T>.onEachWithLifecycleScope(action: suspend (T) -> Unit) {
        this.flowWithLifecycle(lifecycle).onEach { action(it) }.launchIn(lifecycleScope)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (ClapToFindApplication.get().getVibrationHelper().getIsVibrating()) {
            ClapToFindApplication.get().stopVibrate()
        }
        ClapToFindApplication.get().turnOfFlash()
        val isStoppedSound = intent?.extras?.getBoolean("SHOULD_STOP_AUDIO") as? Boolean
        if (isStoppedSound == true) {
            //stop sound
            stopSound()
        }
    }

    override fun onResume() {
        super.onResume()
        IronSource.onResume(this)
        AppOpenManager.getInstance().enableAppResume()
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.currentDestination?.id == R.id.findPhoneFragment)
                    exitDialog.show(this@MainActivity)
                else if (navController.currentDestination?.id == R.id.permissionFragment)
                    finish()
                else
                    navController.navigateUp()
            }
        }
        this.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun stopSound() {
//        myMediaPlayerHelper.stopSound(null)
        ClapToFindApplication.get().setActiveFindStatus(false)
    }

    fun showBannerAds(isBannerVisible: Int) {

    }

    fun reloadBanner() {

    }
}

