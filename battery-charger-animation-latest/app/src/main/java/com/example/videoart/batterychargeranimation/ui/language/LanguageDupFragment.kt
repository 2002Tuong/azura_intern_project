package com.example.videoart.batterychargeranimation.ui.language

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.ads.control.ads.VioAdmob
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.databinding.FragmentLanguageBinding
import com.example.videoart.batterychargeranimation.helper.LanguageHelper
import com.example.videoart.batterychargeranimation.ui.base.BaseFragment
import com.example.videoart.batterychargeranimation.utils.AdsUtils


class LanguageDupFragment : BaseFragment() {
    private lateinit var binding: FragmentLanguageBinding
    private lateinit var adapter: LanguageAdapter
    private var isFirstTime: Boolean = false
    private var selectedPosition = RecyclerView.NO_POSITION
    override fun getViewBinding(): ViewBinding {
        binding = FragmentLanguageBinding.inflate(layoutInflater)
        return binding
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val a = object : Animation() {}
        a.duration = 0
        return a
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //this.isFirstTime = it.getBoolean("isFirstTime")
            this.selectedPosition = it.getInt("selectedPosition")
        }
    }

    override fun onViewCreated() {

        setUpAdapter()
        adapter.updateView(selectedPosition)
        binding.imgBack.visibility = View.GONE
        binding.imgChoose.visibility = View.VISIBLE
        binding.imgChoose.setOnClickListener {
            LanguageHelper.changeLang(adapter.itemSelected().code, requireContext())
            findNavController().navigate(R.id.action_languageDupFragment_to_onboardingFragment)
        }
    }

    override fun registerObservers() {
        AdsUtils.nativeLanguageDup.observe(viewLifecycleOwner) {
            if(it != null) {
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    it,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner
                )
            }
        }

        AdsUtils.nativeLanguageDupLoadFail.observe(viewLifecycleOwner) {
            if (it) {
                Log.d("AdsUtil", "call")
                binding.frAds.isVisible = false
                AdsUtils.nativeLanguageDupLoadFail.value = false
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
        }

        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvcLanguage.layoutManager = linearLayoutManager
        binding.rvcLanguage.adapter = adapter
    }

}