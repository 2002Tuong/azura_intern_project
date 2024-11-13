package com.calltheme.app.ui.onboarding

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.ads.control.admob.Admob
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.wrapper.ApNativeAd
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.utils.AdsUtils
import com.google.android.material.tabs.TabLayoutMediator
import com.screentheme.app.BuildConfig
import com.screentheme.app.R
import com.screentheme.app.databinding.FragmentWalkthroughBinding
import com.screentheme.app.utils.Session
import com.screentheme.app.utils.Tracking
import com.screentheme.app.utils.helpers.SharePreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalkthroughFragment : BaseFragment() {

    lateinit var binding: FragmentWalkthroughBinding

    private lateinit var mViewPager: ViewPager2
    private var native1: ApNativeAd? = null
    private var native2: ApNativeAd? = null
    private var native3: ApNativeAd? = null
    private var slideChangeCount = 0
    private var isFirstOpen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Tracking.logEvent("launch_onboarding")
    }

    override fun getViewBinding(): ViewBinding {
        binding = FragmentWalkthroughBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {
        Tracking.logEvent("onboarding_page_viewed")
        myActivity?.let { AdsUtils.requestNativePermission(it) }
        mViewPager = binding.viewPager

        val adapter = OnboardingViewPagerAdapter(requireActivity(), requireContext())
        mViewPager.adapter = adapter
        TabLayoutMediator(binding.pageIndicator, mViewPager) { _, _ -> }.attach()

        mViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

                slideChangeCount++
                if (position == adapter.itemCount - 1) {
                    binding.btnNextStep.isEnabled = false
                    binding.btnNextStep.setTextColor(resources.getColor(R.color.grey_700, null))
                    timer?.cancel()
                    timer = object : CountDownTimer(1500L, 100L) {
                        override fun onTick(p0: Long) = Unit

                        override fun onFinish() {
                            timer?.cancel()
                            binding.btnNextStep.isEnabled = true
                            binding.btnNextStep.setTextColor(resources.getColor(R.color.colorPrimary, null))
                        }
                    }.start()
                } else {
                    timer?.cancel()
                }

                isFirstOpen = false

                when (position) {
                    0 -> {
                        binding.textView.text = getString(R.string.personalize_your_call_theme)
                    }

                    1 -> {
                        binding.textView.text = getString(R.string.easy_to_create_your_own_call)
                    }

                    2 -> {
                        binding.textView.text = getString(R.string.flash_effect_on_incoming_call)
                    }
                }
                displayAds(position)
            }
        })

        binding.btnNextStep.setOnClickListener {
            if (getItem() > mViewPager.childCount) {
                try {
                    findNavController().navigate(R.id.action_navigation_onboarding_to_permissionFragment)
                } catch (exception: IllegalArgumentException) {

                }
                SharePreferenceHelper.saveBoolean(
                    requireContext(),
                    SharePreferenceHelper.KEY_ALREADY_WENT_THROUGH_INFO,
                    true
                )
            } else {
                mViewPager.setCurrentItem(getItem() + 1, true)
            }
        }
        initNative()
    }

    val adPageShowed = mutableMapOf(0 to false, 1 to false, 2 to false)
    override fun onResume() {
        super.onResume()
        if (Session.isNativeAdClick){
            Session.isNativeAdClick = false
            displayAds(getItem())
        }
    }
    fun displayAds(page: Int) {
        if (adPageShowed[1] == false && page == 0 && adPageShowed[0] == true && !Session.isNativeAdClick) {
            return
        }
        val pageViewed = adPageShowed[page]
        var ad = when (page) {
            0 -> native1
            1 -> native2
            2 -> native3
            else -> native1
        } ?: native1

        if (pageViewed == true) {
            ad = AdsUtils.backupOnboardingAds.lastOrNull() ?: native1
            try {
                AdsUtils.backupOnboardingAds.removeLast()
            } catch (exception: Exception) {

            }
            if (BuildConfig.DEBUG)
                Toast.makeText(requireContext(), "showing backup ad", Toast.LENGTH_SHORT).show()
            AdsUtils.loadBackupOnboardingAd(requireContext(), 1)
        } else {
            if (BuildConfig.DEBUG)
                Toast.makeText(requireContext(), "showing normal ad", Toast.LENGTH_SHORT).show()
        }

        if (ad != null) {
            Tracking.logEvent("onboarding_native_page_${page}_viewed")
            CoroutineScope(Dispatchers.Main).launch {
                VioAdmob.getInstance().populateNativeAdView(
                    activity,
                    ad,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner
                )
            }
            adPageShowed[page] = true
        } else {

            if (BuildConfig.DEBUG)
                Toast.makeText(requireContext(), "ad is null", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onStop() {
        super.onStop()
    }

    private fun initNative() {

        AdsUtils.nativeOnBoarding1.observe(viewLifecycleOwner) {
            it?.let { nativeAd ->
                native1 = nativeAd
                if (getItem() == 0) {
                    displayAds(0)
                }
            }
        }

        AdsUtils.nativeOnBoarding2.observe(viewLifecycleOwner) {
            it?.let { nativeAd ->
                native2 = nativeAd
                if (getItem() == 1) {
                    displayAds(1)
                }
            }
        }

        AdsUtils.nativeOnBoarding3.observe(viewLifecycleOwner) {
            it?.let { nativeAd ->
                native3 = nativeAd
                if (getItem() == 2) {
                    displayAds(2)
                }
            }
        }
        AdsUtils.nativeOnBoardingFailLoad.observe(viewLifecycleOwner) { isFail ->
            if (isFail) {
                binding.frAds.visibility = View.INVISIBLE
            }
        }
    }

    override fun registerObservers() {
    }

    private fun getItem(): Int {
        return mViewPager.currentItem
    }
}