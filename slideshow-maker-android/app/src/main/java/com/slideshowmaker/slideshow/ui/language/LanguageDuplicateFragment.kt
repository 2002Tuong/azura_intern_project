package com.slideshowmaker.slideshow.ui.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ads.control.ads.VioAdmob
import com.slideshowmaker.slideshow.BuildConfig
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.databinding.ActivityLanguageScreenBinding
import com.slideshowmaker.slideshow.utils.AdsHelper
import com.slideshowmaker.slideshow.utils.LanguageHelper
import timber.log.Timber
import java.lang.IllegalArgumentException

class LanguageDuplicateFragment : Fragment() {
    private lateinit var binding: ActivityLanguageScreenBinding
    private lateinit var languageAdapter: LanguageAdapter
    private var isFromSetting: Boolean = false
    private var selectedPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val a = object : Animation() {}
        a.duration = 0
        return a
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isFromSetting =it.getBoolean("isFromSetting")
            selectedPosition = it.getInt("selectPosition")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityLanguageScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(BuildConfig.is_debug) {
            Toast.makeText(requireContext(), "Call duplicate", Toast.LENGTH_SHORT).show()
        }
        binding.frAds.isVisible = AdsHelper.isAdEnabled
        AdsHelper.loadNativeOnboard(requireActivity())
        if (isFromSetting) {
            binding.frAds.visibility = View.GONE
            binding.imgBack.visibility = View.VISIBLE
        } else {
            binding.imgBack.visibility = View.GONE
            initAds()
        }

        setupAdapter()
        binding.imgChoose.isVisible = selectedPosition >= 0
        binding.imgChoose.setOnClickListener {
            LanguageHelper.changeLang(languageAdapter.itemSelected().code, requireContext())
            if (isFromSetting) {
                (requireActivity() as LanguageActivity).restartApp()
            } else {
                (requireActivity() as LanguageActivity).launchOnBoarding()
            }
        }

        Timber.d("qvk23 language: " + SharedPreferUtils.langCode)
        binding.imgBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun initAds() {
        AdsHelper.apNativeLanguageDupLoadFail.observe(viewLifecycleOwner) {
            if (it) {
                binding.frAds.visibility = View.GONE
                AdsHelper.apNativeLanguageLoadFail.value = false
            }
        }
        AdsHelper.apNativeLanguageDup.observe(viewLifecycleOwner) {
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
        languageAdapter = LanguageAdapter(requireContext(), selectedPosition)
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
        languageAdapter.updateView()
        languageAdapter.onLanguageSelectedCallback = {
            binding.imgChoose.visibility = View.VISIBLE
        }
        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvcLanguage.layoutManager = linearLayoutManager
        binding.rvcLanguage.adapter = languageAdapter
    }
}