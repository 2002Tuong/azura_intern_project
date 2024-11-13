package com.parallax.hdvideo.wallpapers.ui.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.viewpager2.widget.ViewPager2
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.wrapper.ApNativeAd
import com.google.android.material.tabs.TabLayoutMediator
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.ads.AdsManager
import com.parallax.hdvideo.wallpapers.databinding.FragmentOnboardingBinding
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.popFragment2
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragmentBinding
import com.parallax.hdvideo.wallpapers.ui.base.viewmodel.BaseViewModel
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.utils.LanguageUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingFragment : BaseFragmentBinding<FragmentOnboardingBinding, BaseViewModel>() {
    override val resLayoutId: Int
        get() = R.layout.fragment_onboarding
    private var currentPageIndex = 0

    @Inject
    lateinit var languageUtils: LanguageUtils
    @Inject
    lateinit var localStorage: LocalStorage

    private var nativeAd1: ApNativeAd? = null
    private var nativeAd2: ApNativeAd? = null
    private var nativeAd3: ApNativeAd? = null

    override fun init(view: View) {
        super.init(view)
        languageUtils.loadLocale(requireContext())
        AdsManager.loadNativeOnBroad(requireContext())
        setupViewPager()
        dataBinding.txtContinue.setOnClickListener { onNextPage() }
        initNative()
    }

    private fun setupViewPager() {
        dataBinding.vpTutorial.adapter = OnBoardingViewPagerAdapter(requireContext())
        dataBinding.vpTutorial.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPageIndex = position
                if (currentPageIndex >= 2) {
                    dataBinding.txtContinue.text = getString(R.string.next)
                } else {
                    dataBinding.txtContinue.text = getString(R.string.next)
                }

                when(position) {
                    0 -> {
                        nativeAd1?.let {
                            VioAdmob.getInstance().populateNativeAdView(
                                requireActivity(),
                                it,
                                dataBinding.frAds,
                                dataBinding.includeNative.shimmerContainerBanner
                            )
                        }
                    }
                    1 -> {
                        nativeAd2?.let {
                            VioAdmob.getInstance().populateNativeAdView(
                                requireActivity(),
                                it,
                                dataBinding.frAds,
                                dataBinding.includeNative.shimmerContainerBanner
                            )
                        }
                    }
                    2 -> {
                        nativeAd3?.let {
                            VioAdmob.getInstance().populateNativeAdView(
                                requireActivity(),
                                it,
                                dataBinding.frAds,
                                dataBinding.includeNative.shimmerContainerBanner
                            )
                        }
                    }
                }
            }
        })
        TabLayoutMediator(dataBinding.tlTutorial, dataBinding.vpTutorial) { _, _ -> }.attach()
    }

    private fun getItem(): Int {
        return dataBinding.vpTutorial.currentItem
    }

    private fun onNextPage() {
        currentPageIndex++
        if (currentPageIndex >= 2) {
            dataBinding.txtContinue.text = getString(R.string.next)
        } else {
            dataBinding.txtContinue.text = getString(R.string.next)
        }
        if (currentPageIndex < 3) {
            dataBinding.vpTutorial.setCurrentItem(currentPageIndex, true)
        } else if (currentPageIndex == 3) {
            currentPageIndex--
            launchHome()
        } else {
            launchHome()
        }
    }

    private fun launchHome() {
        localStorage.firstOpenComplete = true
        popFragment2(this)
        checkIntent()
    }

    private fun checkIntent() {
        (activity as? MainActivity)?.let { it.checkIntent(it.intent) }
    }

    private fun initNative() {
        AdsManager.onBoarding1ApNativeAd.observe(this) {
            it?.let { nativeAd ->
                nativeAd1 = nativeAd
                if (getItem() == 0) {
                    CoroutineScope(Dispatchers.Main).launch {
                        VioAdmob.getInstance().populateNativeAdView(
                            requireActivity(),
                            nativeAd1,
                            dataBinding.frAds,
                            dataBinding.includeNative.shimmerContainerBanner
                        )
                    }
                }
            }
        }

        AdsManager.onBoarding2ApNativeAd.observe(this) {
            it?.let { nativeAd ->
                nativeAd2 = nativeAd
                if (getItem() == 1) {
                    CoroutineScope(Dispatchers.Main).launch {
                        VioAdmob.getInstance().populateNativeAdView(
                            requireActivity(),
                            nativeAd2,
                            dataBinding.frAds,
                            dataBinding.includeNative.shimmerContainerBanner
                        )
                    }
                }
            }
        }

        AdsManager.onBoarding3ApNativeAd.observe(this) {
            it?.let { nativeAd ->
                nativeAd3 = nativeAd
                if (getItem() == 2) {
                    CoroutineScope(Dispatchers.Main).launch {
                        VioAdmob.getInstance().populateNativeAdView(
                            requireActivity(),
                            nativeAd3,
                            dataBinding.frAds,
                            dataBinding.includeNative.shimmerContainerBanner
                        )
                    }
                }
            }
        }
        AdsManager.onBoardingApNativeAdLoadFail.observe(this) { isFail ->
            if (isFail) {
                dataBinding.frAds.visibility = View.GONE
                AdsManager.onBoardingApNativeAdLoadFail.value = false
            }
        }
    }

}