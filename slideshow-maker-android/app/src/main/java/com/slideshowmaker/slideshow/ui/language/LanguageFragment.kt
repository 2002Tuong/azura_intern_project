package com.slideshowmaker.slideshow.ui.language

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ads.control.ads.VioAdmob
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.databinding.ActivityLanguageScreenBinding
import com.slideshowmaker.slideshow.utils.AdsHelper
import com.slideshowmaker.slideshow.utils.LanguageHelper
import timber.log.Timber
import java.lang.IllegalArgumentException

class LanguageFragment : Fragment() {
    private lateinit var binding: ActivityLanguageScreenBinding
    private lateinit var languageAdapter: LanguageAdapter
    private var isFromSetting = false



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityLanguageScreenBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.frAds.isVisible = AdsHelper.isAdEnabled
        AdsHelper.loadNativeOnboard(requireActivity())
        isFromSetting = (requireActivity() as LanguageActivity).isFromSetting
        Log.d("isFromsetting", isFromSetting.toString())
        if (isFromSetting) {
            binding.frAds.visibility = View.GONE
            binding.imgBack.visibility = View.VISIBLE
        } else {
            binding.imgBack.visibility = View.GONE
            initAds()
        }

        setupAdapter()
        binding.imgChoose.setOnClickListener {
            LanguageHelper.changeLang(languageAdapter.itemSelected().code, requireContext())
            if (isFromSetting) {
                (requireActivity() as LanguageActivity).restartApp()
            } else {
                (requireActivity() as LanguageActivity).launchOnBoarding()
            }
        }

        Timber.d("qvk23 language: " + SharedPreferUtils.langCode)
        if (SharedPreferUtils.langCode.isEmpty()) {
            binding.imgChoose.visibility = View.INVISIBLE
        }
        binding.imgBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun initAds() {
        AdsHelper.apNativeLanguageLoadFail.observe(viewLifecycleOwner) {
            if (it) {
                binding.frAds.visibility = View.GONE
                AdsHelper.apNativeLanguageLoadFail.value = false
            }
        }
        AdsHelper.apNativeLanguage.observe(viewLifecycleOwner) {
            if (it != null) {
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    it,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner
                )
                AdsHelper.apNativeLanguage.value = null

            }
        }
    }

    private fun setupAdapter() {
        languageAdapter = LanguageAdapter(requireContext()) {
            try {
                findNavController().navigate(
                    resId = R.id.action_languageFragment_to_languageDuplicateFragment,
                    args = bundleOf("isFromSetting" to isFromSetting, "selectPosition" to it)
                )
            } catch (e: IllegalArgumentException) {

            }
        }
        languageAdapter.setData(
            if (isFromSetting) {
                if (SharedPreferUtils.langCode.isEmpty()) {
                    SharedPreferUtils.setLanguageCode("en")
                }
                LanguageHelper.getSupportedLanguage()
            }
            else
                LanguageHelper.getSupportedLanguage().take(5)
        )
        languageAdapter.onLanguageSelectedCallback = {
            binding.imgChoose.visibility = View.VISIBLE
        }
        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvcLanguage.layoutManager = linearLayoutManager
        binding.rvcLanguage.adapter = languageAdapter
    }
}