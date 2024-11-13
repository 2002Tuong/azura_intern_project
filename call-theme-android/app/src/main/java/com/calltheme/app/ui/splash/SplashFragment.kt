package com.calltheme.app.ui.splash

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.ads.control.admob.AppOpenManager
import com.calltheme.app.ui.activity.MainActivity
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.ui.picklanguage.PickLanguageViewModel
import com.calltheme.app.ui.splash.SplashViewModel.Companion.ROUTE_LANGUAGE
import com.calltheme.app.utils.AdsUtils
import com.google.android.gms.ads.AdActivity
import com.screentheme.app.R
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.databinding.FragmentSplashBinding
import com.screentheme.app.utils.ConsentHelper
import com.screentheme.app.utils.extensions.FragmentDirections
import com.screentheme.app.utils.extensions.isAlreadyDefaultDialer
import com.screentheme.app.utils.helpers.BillingClientProvider
import com.screentheme.app.utils.helpers.LanguageSupporter
import com.screentheme.app.utils.helpers.ThemeManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.android.ext.android.inject

class SplashFragment : BaseFragment() {

    private val viewModel: SplashViewModel by inject()

    private lateinit var binding: FragmentSplashBinding
    private val FETCH_REMOTE_CONFIG_TIMEOUT = 10000L // 10 seconds

    override fun getViewBinding(): ViewBinding {
        binding = FragmentSplashBinding.inflate(layoutInflater)
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated() {
        AppOpenManager.getInstance().disableAppResume()

        lifecycleScope.launch {
            myActivity?.let {
                ConsentHelper.obtain(
                    context = it,
                    loadAds = { viewModel.startSplashFlow(it) },
                    onDone = { it.finish() }
                )
            }
        }

        val defaultTheme =
            themeManager.getThemeConfig(ThemeManager.DEFAULT_THEME_ID)

        if (defaultTheme == null) {
            themeManager.saveDefaultTheme()
            themeManager
                .saveThemeIdToPref(ThemeManager.DEFAULT_THEME_ID)
            themeManager.currentThemeId =
                ThemeManager.DEFAULT_THEME_ID
        }

        binding.apply {
            containAdsTextView.visibility =
                if (BillingClientProvider.getInstance(requireActivity()).isPurchased || !AdsUtils.isAdEnabled) View.INVISIBLE else View.VISIBLE

            btnContinue.setOnClickListener {
                if (AdsUtils.nativeFullScreenApNativeAdLoadFail.value == true) {
                    findNavController().navigate(R.id.action_navigation_splash_to_permissionFragment)
                } else {
                    findNavController().navigate(R.id.action_navigation_splash_to_nativeFullScreenFragment)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!viewModel.isOnboardingCompleted || !AppRemoteConfig.nativeSplash) {
            myActivity?.let {
                if (AppRemoteConfig.interSplash){
                    viewModel.checkShowInterSplashPriority3WhenFail(it)
                } else {
                    viewModel.checkShowInterSplashWhenFail(it)
                }
            }
        }
    }

    override fun registerObservers() {
        pickLanguageViewModel.languageLiveData.observe(this) { language ->
            if (language != null) {
                LanguageSupporter.setAppLanguage(activity as MainActivity, language)
            }
        }

        viewModel.isShownLoadingAds.observe(this) { isShownLoadingAds ->
            if (isShownLoadingAds) {
                binding.loadingAdsLayout.visibility = View.VISIBLE
            } else {
                binding.loadingAdsLayout.visibility = View.GONE
            }
        }

        viewModel.nextScreenRoute.observe(this) {
            if (it == ROUTE_LANGUAGE) {
                val args = Bundle()
                args.putBoolean("isFirstTime", true)

                val action = FragmentDirections.action(
                    args,
                    R.id.action_navigation_splash_to_navigation_pick_language
                )
                findNavController().navigate(action)
            } else {
                if(requireContext().isAlreadyDefaultDialer()) {
                    if(getNavController().currentDestination?.id == R.id.navigation_splash) {
                        findNavController().navigate(R.id.action_navigation_splash_to_navigation_home)
                    }
                } else {
                    findNavController().navigate(R.id.action_navigation_splash_to_permissionFragment)
                }
//                binding.loadingGroup.isVisible = false
//                binding.btnContinue.isVisible = true
            }
        }

        BillingClientProvider.getInstance(requireActivity()).initialize()

    }
}