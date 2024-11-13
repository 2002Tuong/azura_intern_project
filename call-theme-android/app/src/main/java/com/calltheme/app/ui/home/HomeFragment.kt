package com.calltheme.app.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.ads.control.admob.Admob
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.ads.control.funtion.AdCallback
import com.ads.control.util.AppConstant
import com.calltheme.app.ui.activity.MainActivity
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.ui.diytheme.DiyThemeFragment
import com.calltheme.app.ui.mydesign.MyDesignFragment
import com.calltheme.app.ui.settings.SettingsFragment
import com.calltheme.app.ui.theme.ThemeFragment
import com.calltheme.app.utils.AdsUtils
import com.calltheme.app.utils.BannerAdsHelpers
import com.screentheme.app.BuildConfig
import com.screentheme.app.R
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.databinding.FragmentHomeBinding
import com.screentheme.app.utils.Tracking
import com.screentheme.app.utils.delay
import com.screentheme.app.utils.helpers.BillingClientProvider
import com.screentheme.app.utils.helpers.CountTimeHelper
import org.koin.android.ext.android.inject

class HomeFragment : BaseFragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var adapter: HomeViewPagerAdapter

    private var listOfFragments = ArrayList<Fragment>()

    private lateinit var themeFragment: ThemeFragment
    private val diyThemeFragment = DiyThemeFragment()
    private val myDesignFragment = MyDesignFragment()
    private val settingsFragment = SettingsFragment.newInstance(this)
    private var isBannerShowed = false
    private var isDestinationChanged = false
    private var listSelectedCategory = mutableListOf<String>()

    private val countTimeHelper: CountTimeHelper by inject()

    override fun getViewBinding(): ViewBinding {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Tracking.logEvent("launch_home")

        arguments?.let {
            listSelectedCategory = it.getStringArrayList("selected_categories") ?: mutableListOf()
            Log.d("Category", "List: ${listSelectedCategory}")
        }
    }

    override fun onViewCreated() {
        themeFragment = ThemeFragment(listSelectedCategory)

        countTimeHelper.createReloadBannerTimer(CountTimeHelper.RELOAD_HOME_BANNER, CountTimeHelper.INTERVAL_TIME) {
            BannerAdsHelpers.requestLoadBannerAds(
                BannerAdsHelpers.BannerAdPlacement.HOME,
                requireContext()
            )
        }
        countTimeHelper.startReloadBannerTimer()

        AdsUtils.isCloseAdSplash.observe(viewLifecycleOwner) { isClose ->
            if (isClose) {
                myActivity?.let {
                    if (!BillingClientProvider.getInstance(it).isPurchased) {
                        AppOpenManager.getInstance().enableAppResume()
                    }
                }
            }
        }
        listOfFragments = ArrayList<Fragment>().apply {
            add(themeFragment)
            add(diyThemeFragment)
            add(myDesignFragment)
            add(settingsFragment)
        }

        adapter = HomeViewPagerAdapter(childFragmentManager, lifecycle, listOfFragments)
        binding.apply {
            myActivity?.let {
                if (BillingClientProvider.getInstance(it).isPurchased) {
                    frAds.visibility = View.GONE
                } else {
                    if (AppRemoteConfig.collapsibleBannerHome) {
                        VioAdmob.getInstance()
                            .loadCollapsibleBannerFragment(
                                myActivity,
                                BuildConfig.collapsible_banner_home,
                                frAds,
                                AppConstant.CollapsibleGravity.BOTTOM,
                                object : AdCallback() {

                                }
                            )
                    } else {
                        myActivity?.let {
                            BannerAdsHelpers.requestLoadBannerAds(BannerAdsHelpers.BannerAdPlacement.HOME, it)
                            requestBannerListener()
                        }
                    }
                }
            }
            homeViewPager.adapter = adapter
            homeViewPager.offscreenPageLimit = 4
            homeViewPager.isSaveEnabled = false
            homeViewPager.isUserInputEnabled = false
            homeViewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {

                    if (position == adapter.itemCount - 1) {
                    } else {
                    }
                }
            })

            bottomNavView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_theme -> {
                        AdsUtils.requestNativeHome(requireActivity(), true)
                        homeViewPager.setCurrentItem(0, true)
                    }

                    R.id.navigation_dyi_theme -> {
                        AdsUtils.requestNativeHome(requireActivity(), true)
                        homeViewPager.setCurrentItem(1, true)
                    }

                    R.id.navigation_my_design -> {
                        AdsUtils.requestNativeHome(requireActivity(), true)
                        homeViewPager.setCurrentItem(2, true)
                    }

                    R.id.navigation_settings -> {
                        AdsUtils.requestNativeHome(requireActivity(), true)
                        homeViewPager.setCurrentItem(3, true)
                    }
                }
                true
            }
        }

        exitAppPopup.onExitApp {
            exitAppPopup.dismiss()
            delay {
                try {
                    getNavController().navigate(R.id.action_navigation_home_to_navigation_thankyou)
                } catch (exception: Exception) {
                }

            }
        }

        exitAppPopup.onRateApp {
            ratingAppPopup.show(requireActivity())
        }

        binding.bottomNavView.apply {
            setOnApplyWindowInsetsListener(null)
        }

    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun requestBannerListener() {
        (myActivity as MainActivity).isDestinationChanged.observe(this) { destination ->
            if (destination.id == R.id.navigation_pick_language
                || destination.id == R.id.navigation_onboarding
                || destination.id == R.id.navigation_splash
            )
                return@observe
            isDestinationChanged = destination.id != R.id.navigation_home
        }
        BannerAdsHelpers.homeBannerAds.value?.let {
            isBannerShowed = true
            //binding.bannerAds.flShimemr.visibility = View.GONE
            Admob.getInstance().populateUnifiedBannerAdView(
                myActivity,
                it,
                binding.bannerAds.bannerContainer
            )
        }
        BannerAdsHelpers.homeBannerAds.observe(this) { adView ->
            adView?.let {
                isBannerShowed = true
                //binding.bannerAds.flShimemr.visibility = View.GONE
                try {
                    (it.parent as? ViewGroup)?.removeView(it)
                    binding.bannerAds.bannerContainer.removeAllViews()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
                Admob.getInstance().populateUnifiedBannerAdView(
                    myActivity,
                    adView,
                    binding.bannerAds.bannerContainer
                )
            }
        }
        BannerAdsHelpers.bannerHomeFailToLoad.observe(this) {
            if (it && !isBannerShowed) {
                binding.frAds.visibility = View.GONE
            }
        }
    }

    override fun onStop() {
        myActivity?.let { BannerAdsHelpers.requestLoadBannerAds(BannerAdsHelpers.BannerAdPlacement.HOME, it) }
        super.onStop()
    }
    override fun onResume() {
        super.onResume()
        if (!AppRemoteConfig.collapsibleBannerHome && isDestinationChanged) {
            isDestinationChanged = false
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!exitAppPopup.isAdded) exitAppPopup.show(requireActivity())
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun registerObservers() {
        AdsUtils.homeApNativeAd.observe(viewLifecycleOwner) {nativeAd ->
            nativeAd?.let {
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    it,
                    binding.frNativeAds,
                    binding.includeNative.shimmerContainerBanner
                )
            }
        }

        AdsUtils.homeApNativeAdLoadFail.observe(viewLifecycleOwner) {
            if(it) {
                binding.frNativeAds.isVisible = false
                AdsUtils.homeApNativeAdLoadFail.postValue(false)
            }
        }
    }
}