package com.wifi.wificharger.ui.splash

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ads.control.admob.AppOpenManager
import com.wifi.wificharger.utils.Logger
import com.wifi.wificharger.ui.splash.SplashViewModel.Companion.ROUTE_LANGUAGE
import com.wifi.wificharger.R
import com.wifi.wificharger.databinding.FragmentSplashBinding
import com.wifi.wificharger.ui.base.BaseFragment
import com.wifi.wificharger.utils.BannerAdsUtils
import com.wifi.wificharger.utils.ConsentHelper
import com.wifi.wificharger.utils.RemoteConfigManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 * Use the [SplashFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SplashFragment :
    BaseFragment<FragmentSplashBinding, SplashViewModel>(FragmentSplashBinding::inflate) {

    override val viewModel: SplashViewModel by viewModel()

    override fun initView() {
        AppOpenManager.getInstance().disableAppResume()
        viewBinding.tvReminder.isVisible = !viewModel.isPremium.value
    }

    override fun loadAds() {
        lifecycleScope.launch {
            (activity as? AppCompatActivity)?.let {
                ConsentHelper.obtain(
                    context = it,
                    loadAds = { viewModel.loadData(it) },
                    onDone = { it.finish() }
                )
            }

        }
    }

    override fun observeData() {
        viewModel.nextScreenRoute.observe(this) {
            if (it == ROUTE_LANGUAGE) {
                val args = Bundle()
                args.putBoolean("isFirstTime", true)
                navigate(
                    bundle = bundleOf("isFirstTime" to true),
                    actionId = R.id.action_navigation_splash_to_navigation_pick_language
                )
            } else {
                navigate(
                    bundle = null,
                    actionId = R.id.action_navigation_splash_to_permissionFragment
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!viewModel.isOnboardingCompleted) {
            (activity as? AppCompatActivity)?.let {
                viewModel.checkShowInterSplashWhenFail(it)
            }
        }
    }

    override fun onDestroy() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        super.onDestroy()
    }
}