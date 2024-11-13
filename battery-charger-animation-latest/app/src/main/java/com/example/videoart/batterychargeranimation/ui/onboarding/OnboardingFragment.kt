package com.example.videoart.batterychargeranimation.ui.onboarding

import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.wrapper.ApNativeAd
import com.example.videoart.batterychargeranimation.BuildConfig
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.databinding.FragmentOnboardingBinding
import com.example.videoart.batterychargeranimation.ui.base.BaseFragment
import com.example.videoart.batterychargeranimation.utils.AdsUtils
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OnboardingFragment : BaseFragment() {
    private lateinit var binding: FragmentOnboardingBinding
    private lateinit var adapter: OnBoardingViewPagerAdapter
    private var curPageIndex: Int = 0
    private var native1: ApNativeAd? = null
    private var native2: ApNativeAd? = null
    private var native3: ApNativeAd? = null
    private val listTitle = listOf(R.string.onboard_title1, R.string.onboard_title2, R.string.onboard_title3)
    override fun getViewBinding(): ViewBinding {
        binding = FragmentOnboardingBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {
        AdsUtils.requestNativePermission(requireActivity())
        setUpViewPager()
        binding.txtContinue.setOnClickListener {
            onNextPage()
        }

        binding.txtTitle.text = getString(listTitle[0])
    }

    override fun registerObservers() {
        AdsUtils.nativeOnboardLoadFail.observe(viewLifecycleOwner) {
            if(it) {
                binding.frAds.isVisible = false
                AdsUtils.nativeOnboardLoadFail.value =false
            }
        }

        AdsUtils.nativeOnboard1.observe(viewLifecycleOwner) {
            it?.let {nativeAd ->
                native1 = nativeAd
                if(getItem() == 0) {
                    displayAds(0)
                }
            }
        }

        AdsUtils.nativeOnboard2.observe(viewLifecycleOwner) {
            it?.let {nativeAd ->
                native2 = nativeAd
                if(getItem() == 1) {
                    displayAds(1)
                }
            }
        }

        AdsUtils.nativeOnboard3.observe(viewLifecycleOwner) {
            it?.let {nativeAd ->
                native3 = nativeAd
                if(getItem() == 2) {
                    displayAds(2)
                }
            }
        }

    }

    private fun setUpViewPager() {
        adapter = OnBoardingViewPagerAdapter(requireActivity())
        binding.vpTutorial.adapter = adapter
        binding.vpTutorial.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                curPageIndex = position
                if (curPageIndex >= 2) {
                    binding.txtContinue.text = getString(R.string.next)
                } else {
                    binding.txtContinue.text = getString(R.string.next)
                }

                binding.txtTitle.text = getString(listTitle[position])
                displayAds(position)

            }
        })

        TabLayoutMediator(binding.tlTutorial, binding.vpTutorial) {_, _ ->}.attach()
    }

    private fun onNextPage() {
        curPageIndex++
        if(curPageIndex < listTitle.size) {
            binding.txtTitle.text = getString(listTitle[curPageIndex])
        }
        if (curPageIndex >= 2) {
            binding.txtContinue.text = getString(R.string.next)
        } else {
            binding.txtContinue.text = getString(R.string.next)
        }
        if (curPageIndex < 3) {
            binding.vpTutorial.setCurrentItem(curPageIndex, true)
        } else if (curPageIndex == 3) {
            curPageIndex--
            launchHome()
        } else {
            launchHome()
        }
    }

    val adPageShowed = mutableMapOf(0 to false, 1 to false, 2 to false)
    fun displayAds(page: Int) {
        if (adPageShowed[1] == false && page == 0 && adPageShowed[0] == true ) {
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


    private fun getItem(): Int {
        return binding.vpTutorial.currentItem
    }
    private fun launchHome() {
        findNavController().navigate(R.id.action_onboardingFragment_to_permissionFragment)
    }
}