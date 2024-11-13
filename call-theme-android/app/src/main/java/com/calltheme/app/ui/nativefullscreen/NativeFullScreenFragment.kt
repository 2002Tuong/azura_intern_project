package com.calltheme.app.ui.nativefullscreen

import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.utils.AdsUtils
import com.screentheme.app.R
import com.screentheme.app.databinding.FragmentNativeFullscreenBinding

class NativeFullScreenFragment: BaseFragment() {
    private lateinit var binding: FragmentNativeFullscreenBinding
    private var isNavigatingOut = false

    override fun getViewBinding(): ViewBinding {
        binding = FragmentNativeFullscreenBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {
        AppOpenManager.getInstance().disableAppResume()
        binding.btnClose.setOnClickListener {
            AdsUtils.isCloseAdSplash.postValue(true)
            isNavigatingOut = true
            findNavController().navigate(R.id.action_nativeFullScreenFragment_to_permissionFragment)
        }
    }

    override fun onStop() {
        if (!isNavigatingOut) {
            myActivity?.let {
                AdsUtils.requestNativeSplash(it, reload = true, {})
            }
        }
        super.onStop()
    }

    override fun registerObservers() {
        AdsUtils.nativeFullScreenApNativeAdLoadFail.observe(viewLifecycleOwner) {
            if (it) {
                binding.frAds.visibility = View.GONE
            }
        }
        AdsUtils.nativeFullScreenApNativeAd.observe(viewLifecycleOwner) {
            if (it != null) {
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    it,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner
                )
            }
        }
    }
}