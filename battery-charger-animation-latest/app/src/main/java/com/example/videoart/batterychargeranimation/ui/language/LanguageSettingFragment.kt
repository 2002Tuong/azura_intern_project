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
import com.example.videoart.batterychargeranimation.databinding.FragmentLanguageSettingBinding
import com.example.videoart.batterychargeranimation.helper.LanguageHelper
import com.example.videoart.batterychargeranimation.ui.base.BaseFragment
import com.example.videoart.batterychargeranimation.utils.AdsUtils

class LanguageSettingFragment: BaseFragment() {

    private lateinit var binding: FragmentLanguageSettingBinding
    private lateinit var adapter: LanguageAdapter

    companion object {
        const val REQUEST_PICK_LANGUAGE_KEY = "REQUEST_PICK_LANGUAGE_KEY"
        const val LANGUAGE_CODE = "LANGUAGE_CODE"
    }
    override fun getViewBinding(): ViewBinding {
        binding = FragmentLanguageSettingBinding.inflate(layoutInflater)
        return binding
    }


    override fun onViewCreated() {
        setUpAdapter()
        binding.imgChoose.setOnClickListener {
            LanguageHelper.changeLang(adapter.itemSelected().code, requireContext())

            Log.d("Setting", "call to this")
            setFragmentResult(REQUEST_PICK_LANGUAGE_KEY, bundleOf(LANGUAGE_CODE to adapter.itemSelected().code))
            restartApp()

        }

        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

    }


    private fun restartApp() {
        findNavController().navigateUp()
    }

    override fun registerObservers() {
    }

    private fun setUpAdapter() {
        for(lang in LanguageHelper.getSupportedLanguage()) {
            Log.d("Lang", "${lang.code} | ${lang.isChoose}")
        }

        adapter = LanguageAdapter()
        adapter.setData(
            LanguageHelper.getSupportedLanguage()

        )

        adapter.languageSelectedCallback = {pos ->
            binding.imgChoose.isVisible = true
        }

        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvcLanguage.layoutManager = linearLayoutManager
        binding.rvcLanguage.adapter = adapter
    }
}