package com.example.videoart.batterychargeranimation.ui.result

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import coil.load
import com.ads.control.ads.VioAdmob
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.databinding.FragmentResultBinding
import com.example.videoart.batterychargeranimation.model.Theme
import com.example.videoart.batterychargeranimation.service.ChargerService
import com.example.videoart.batterychargeranimation.ui.base.BaseFragment
import com.example.videoart.batterychargeranimation.ui.home.HomeFragment
import com.example.videoart.batterychargeranimation.utils.AdsUtils

class ResultFragment : BaseFragment() {
    private lateinit var binding: FragmentResultBinding
    private lateinit var theme: Theme
    override fun getViewBinding(): ViewBinding {
        binding = FragmentResultBinding.inflate(layoutInflater)
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            theme = it.getParcelable(HomeFragment.CLICK_THEME)!!
        }
    }

    override fun onViewCreated() {
        binding.preview.load(theme.thumbnail)

        binding.doneText

        binding.home.setOnClickListener {
            val action = ResultFragmentDirections.actionResultFragmentToMainFragment()
            findNavController().navigate(action)
        }

        binding.imgAppIcon.setOnClickListener {
            val action = ResultFragmentDirections.actionResultFragmentToMainFragment()
            findNavController().navigate(action)
        }
        val intent = Intent(requireContext(), ChargerService::class.java)
        if(!ChargerService.isServiceRunning) {
            requireActivity().startService(intent)
        }
    }

    override fun registerObservers() {
        AdsUtils.nativeResult.observe(viewLifecycleOwner) {
            if(it != null) {
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    it,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner
                )
            }
        }

        AdsUtils.nativeResultLoadFail.observe(viewLifecycleOwner) {
            if(it) {
                binding.frAds.isVisible = false
                AdsUtils.nativeResultLoadFail.value = false
            }
        }
    }
}