package com.example.videoart.batterychargeranimation.ui.language

import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.databinding.FragmentLanguageBinding
import com.example.videoart.batterychargeranimation.helper.LanguageHelper
import com.example.videoart.batterychargeranimation.ui.base.BaseFragment
import com.example.videoart.batterychargeranimation.utils.AdsUtils

class LanguageFragment : BaseFragment() {
    private lateinit var binding: FragmentLanguageBinding
    private lateinit var adapter: LanguageAdapter
    private var isFromSetting = false

    override fun getViewBinding(): ViewBinding {
        binding = FragmentLanguageBinding.inflate(layoutInflater)
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated() {
        AppOpenManager.getInstance().enableAppResume()
        AdsUtils.loadNativeOnboard(requireActivity())
        AdsUtils.requestNativeLanguageDup(requireActivity())
        setUpAdapter()
        binding.imgChoose.setOnClickListener {
            LanguageHelper.changeLang(adapter.itemSelected().code, requireContext())
            try {
                findNavController().navigate(
                    resId = R.id.action_languageFragment_to_languageDupFragment,
                    args = bundleOf( "selectedPosition" to adapter.itemIndex())
                )
            } catch (exception: IllegalArgumentException) {

            }
        }


        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

    }


    private fun restartApp() {
        findNavController().navigateUp()
    }

    override fun registerObservers() {
        AdsUtils.nativeLanguage.observe(viewLifecycleOwner) {
            if(it != null) {
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    it,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner
                )
            }
        }

        AdsUtils.nativeLanguageLoadFail.observe(viewLifecycleOwner) {
            if (it) {
                Log.d("AdsUtil", "call")
                binding.frAds.isVisible = false
                AdsUtils.nativeLanguageLoadFail.value = false
            } else {
                binding.frAds.isVisible = true
            }
        }
    }

    private fun setUpAdapter() {
        for(lang in LanguageHelper.getSupportedLanguage()) {
            Log.d("Lang", "${lang.code} | ${lang.isChoose}")
        }

        adapter = LanguageAdapter()
        adapter.setData(
            LanguageHelper.getSupportedLanguage().take(5)
        )

        adapter.languageSelectedCallback = {pos ->
            binding.imgChoose.isVisible = true
            try {
                findNavController().navigate(
                    resId = R.id.action_languageFragment_to_languageDupFragment,
                    args = bundleOf( "selectedPosition" to pos)
                )
            } catch (exception: IllegalArgumentException) {

            }

        }

        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvcLanguage.layoutManager = linearLayoutManager
        binding.rvcLanguage.adapter = adapter
    }
}