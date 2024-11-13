package com.wifi.wificharger.ui.onboarding

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.wrapper.ApNativeAd
import com.google.android.material.tabs.TabLayoutMediator
import com.wifi.wificharger.BuildConfig
import com.wifi.wificharger.R
import com.wifi.wificharger.databinding.FragmentWalkthroughBinding
import com.wifi.wificharger.ui.base.BaseFragment
import com.wifi.wificharger.utils.AdsUtils
import com.wifi.wificharger.utils.Session
import com.wifi.wificharger.utils.TrackingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class WalkthroughFragment : BaseFragment<FragmentWalkthroughBinding, WalkThroughViewModel>(
    FragmentWalkthroughBinding::inflate
) {

    override val viewModel: WalkThroughViewModel by viewModel()
    private var native1: ApNativeAd? = null
    private var native2: ApNativeAd? = null
    private var native3: ApNativeAd? = null
    private var slideChangeCount = 0
    private var isFirstOpen = true
    private var timer: CountDownTimer? = null

    private lateinit var mViewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TrackingManager.logEvent("launch_onboarding")
    }

    override fun initView() {
        activity?.let { AdsUtils.requestNativePermission(it) }
        mViewPager = viewBinding.viewPager

        val adapter = OnboardingViewPagerAdapter(requireActivity(), requireContext())
        mViewPager.adapter = adapter
        TabLayoutMediator(viewBinding.pageIndicator, mViewPager) { _, _ -> }.attach()

        mViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                slideChangeCount++
                if (position == adapter.itemCount - 1) {
                    viewBinding.btnNextStep.isEnabled = false
                    viewBinding.btnNextStep.setTextColor(resources.getColor(R.color.grey_700, null))
                    timer?.cancel()
                    timer = object : CountDownTimer(1500L, 100L) {
                        override fun onTick(p0: Long) = Unit

                        override fun onFinish() {
                            timer?.cancel()
                            viewBinding.btnNextStep.isEnabled = true
                            viewBinding.btnNextStep.setTextColor(resources.getColor(R.color.blue_800, null))
                        }
                    }.start()
                } else {
                    timer?.cancel()
                }

                isFirstOpen = false

                when (position) {
                    0 -> {
                        viewBinding.textView.text = getString(R.string.show_wifi_password)
                        viewBinding.textViewSubTitle.text = getString(R.string.wifi_shown)
                    }

                    1 -> {
                        viewBinding.textView.text = getString(R.string.all_connect_devices)
                        viewBinding.textViewSubTitle.text = getString(R.string.detect_devices)
                    }

                    2 -> {
                        viewBinding.textView.text = getString(R.string.available_wifi_list)
                        viewBinding.textViewSubTitle.text = getString(R.string.scan_wifi)
                    }
                }
                displayAds(position)
            }
        })

        viewBinding.btnNextStep.setOnClickListener {
            if (getItem() > mViewPager.childCount) {
                try {
                    findNavController().navigate(R.id.action_navigation_onboarding_to_permissionFragment)
                } catch (exception: IllegalArgumentException) {

                }


                viewModel.setOnBoardingShow()
            } else {
                mViewPager.setCurrentItem(getItem() + 1, true)
            }
        }
    }

    override fun loadAds() {
    }

    override fun observeData() {

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
            TrackingManager.logEvent("onboarding_native_page_${page}_viewed")
            CoroutineScope(Dispatchers.Main).launch {
                VioAdmob.getInstance().populateNativeAdView(
                    activity,
                    ad,
                    viewBinding.frAds,
                    viewBinding.includeNative.shimmerContainerBanner
                )
            }
            adPageShowed[page] = true
        } else {

            if (BuildConfig.DEBUG)
                Toast.makeText(requireContext(), "ad is null", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onBindingAds() {
        initNative()
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
                viewBinding.frAds.visibility = View.INVISIBLE
            }
        }
    }


    private fun getItem(): Int {
        return mViewPager.currentItem
    }
}