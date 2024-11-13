package com.artgen.app.ui.screen.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.ads.control.admob.Admob
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.wrapper.ApNativeAd
import com.artgen.app.R
import com.artgen.app.ads.NativeAdsManager
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.databinding.FragmentWalkthroughBinding
import com.artgen.app.tracking.TrackingManager
import com.artgen.app.ui.MainActivity
import com.artgen.app.ui.screen.navigation.DeeplinkRoute.HOME
import com.artgen.app.utils.AdsUtils
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class WalkthroughFragment : Fragment() {

    lateinit var binding: FragmentWalkthroughBinding

    private lateinit var mViewPager: ViewPager2
    private var native1: ApNativeAd? = null
    private var native2: ApNativeAd? = null
    private var native3: ApNativeAd? = null
    private val adsManager: NativeAdsManager by inject()
    private val adUtil: AdsUtils by inject()
    private val remoteConfig: RemoteConfig by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adsManager.loadStylePickerNativeAd(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWalkthroughBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewPager = binding.viewPager
        TrackingManager.logEvent("onboarding_page_viewed")
        val adapter = OnboardingViewPagerAdapter(requireActivity(), requireContext())
        mViewPager.adapter = adapter
        TabLayoutMediator(binding.pageIndicator, mViewPager) { _, _ -> }.attach()

        mViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                TrackingManager.logEvent("onboarding_page_${position}_selected")

                when (position) {
                    0 -> {
                        native1?.let {
                            TrackingManager.logEvent("onboarding_native_page_${position}_viewed")
                            VioAdmob.getInstance().populateNativeAdView(
                                activity,
                                it,
                                binding.frAds,
                                binding.includeNative.shimmerContainerBanner
                            )
                        }
                    }

                    1 -> {
                        native2?.let {
                            TrackingManager.logEvent("onboarding_native_page_${position}_viewed")
                            VioAdmob.getInstance().populateNativeAdView(
                                activity,
                                it,
                                binding.frAds,
                                binding.includeNative.shimmerContainerBanner
                            )
                        }
                    }

                    2 -> {
                        native3?.let {
                            TrackingManager.logEvent("onboarding_native_page_${position}_viewed")
                            VioAdmob.getInstance().populateNativeAdView(
                                activity,
                                it,
                                binding.frAds,
                                binding.includeNative.shimmerContainerBanner
                            )
                        }
                    }
                }
            }
        })

        binding.btnNextStep.setOnClickListener {
            if (getItem() > mViewPager.childCount) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(HOME),
                    requireContext(),
                    MainActivity::class.java
                ).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                requireActivity().finish()
                startActivity(intent)
            } else {
                mViewPager.setCurrentItem(getItem() + 1, true)
            }
        }
        initNative()
    }

    private fun initNative() {
        adUtil.nativeOnBoarding1.observe(viewLifecycleOwner) {
            it?.let { nativeAd ->
                native1 = nativeAd
                if (remoteConfig.nativeOnboardCtaTop) {
                    Admob.getInstance().isCtaTop(R.id.ad_call_to_action_top)
                } else {
                    Admob.getInstance().isCtaTop(R.id.ad_call_to_action)

                }
                if (getItem() == 0) {
                    CoroutineScope(Dispatchers.Main).launch {
                        VioAdmob.getInstance().populateNativeAdView(
                            activity,
                            native1,
                            binding.frAds,
                            binding.includeNative.shimmerContainerBanner
                        )
                    }
                }
            }
        }

        adUtil.nativeOnBoarding2.observe(viewLifecycleOwner) {
            it?.let { nativeAd ->
                if (remoteConfig.nativeOnboardCtaTop) {
                    Admob.getInstance().isCtaTop(R.id.ad_call_to_action_top)
                } else {
                    Admob.getInstance().isCtaTop(R.id.ad_call_to_action)

                }
                native2 = nativeAd
                if (getItem() == 1) {
                    CoroutineScope(Dispatchers.Main).launch {
                        VioAdmob.getInstance().populateNativeAdView(
                            activity,
                            native2,
                            binding.frAds,
                            binding.includeNative.shimmerContainerBanner
                        )
                    }
                }
            }
        }

        adUtil.nativeOnBoarding3.observe(viewLifecycleOwner) {
            it?.let { nativeAd ->
                if (remoteConfig.nativeOnboardCtaTop) {
                    Admob.getInstance().isCtaTop(R.id.ad_call_to_action_top)
                } else {
                    Admob.getInstance().isCtaTop(R.id.ad_call_to_action)

                }
                native3 = nativeAd
                if (getItem() == 2) {
                    CoroutineScope(Dispatchers.Main).launch {
                        VioAdmob.getInstance().populateNativeAdView(
                            activity,
                            native3,
                            binding.frAds,
                            binding.includeNative.shimmerContainerBanner
                        )
                    }
                }
            }
        }
        adUtil.nativeOnBoardingFailLoad.observe(viewLifecycleOwner) { isFail ->
            if (isFail) {
                binding.frAds.visibility = View.INVISIBLE
            }
        }
    }

    private fun getItem(): Int {
        return mViewPager.currentItem
    }
}