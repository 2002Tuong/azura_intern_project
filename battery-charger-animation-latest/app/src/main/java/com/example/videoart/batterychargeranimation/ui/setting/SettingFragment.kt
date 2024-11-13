package com.example.videoart.batterychargeranimation.ui.setting

import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.videoart.batterychargeranimation.MainActivity
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.data.local.PreferenceUtils
import com.example.videoart.batterychargeranimation.data.remote.RemoteConfig
import com.example.videoart.batterychargeranimation.databinding.FragmentSettingBinding
import com.example.videoart.batterychargeranimation.extension.openPrivacy
import com.example.videoart.batterychargeranimation.extension.shareApp
import com.example.videoart.batterychargeranimation.helper.LanguageHelper
import com.example.videoart.batterychargeranimation.ui.base.BaseFragment
import com.example.videoart.batterychargeranimation.ui.language.LanguageSettingFragment

class SettingFragment : BaseFragment() {
    private lateinit var binding: FragmentSettingBinding
    private var rootFragment: Fragment? = null

    override fun getViewBinding(): ViewBinding {
        binding = FragmentSettingBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {
        rootFragment?.setFragmentResultListener(LanguageSettingFragment.REQUEST_PICK_LANGUAGE_KEY) { key, bundle ->
            val languageCode = bundle.getString(LanguageSettingFragment.LANGUAGE_CODE)
            val listLang = LanguageHelper.getSupportedLanguage()
            val chooseLang = listLang.find { it.code == languageCode }
            binding.curLanguage.text = getString(chooseLang?.name ?: R.string.lang_english)

            if(chooseLang != null) {
                val  mainActivity = activity as MainActivity
                LanguageHelper.changeLang(chooseLang.code, mainActivity)
                mainActivity.recreate()

            }
        }

//        binding.chooseLanguage.setOnClickListener {
//            findNavController().navigate(
//                R.id.action_mainFragment_to_languageSettingFragment,
//            )
//        }

        val languageCode = PreferenceUtils.langCode
        if(!languageCode.isNullOrEmpty()) {
            val listLang = LanguageHelper.getSupportedLanguage()
            val chooseLang = listLang.find { it.code == languageCode }
            binding.curLanguage.text = getString(chooseLang?.name ?: R.string.lang_english)
        }

//        binding.privacyPolicy.isVisible = RemoteConfig.cmpRequire
//
//        binding.privacyPolicy.setOnClickListener {
//            if(RemoteConfig.cmpRequire) {
//                myActivity?.let {
//                    ConsentHelper.updateConsent(it, {}, {it.finish()})
//                }
//            }
//        }

        binding.shareApp.setOnClickListener {
            requireContext().shareApp()
        }

        binding.rateApp.setOnClickListener {
            ratingAppDialog.show(requireActivity())
        }

    }

    override fun registerObservers() {
    }

    companion object {
        fun newInstance(rootFragment: Fragment): SettingFragment {
            val fragment = SettingFragment()
            fragment.rootFragment = rootFragment
            return fragment
        }
    }
}