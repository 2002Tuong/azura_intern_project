package com.calltheme.app.ui.picklanguage

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.ads.control.ads.VioAdmob
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.utils.AdsUtils
import com.screentheme.app.R
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.databinding.FragmentPickLanguageBinding
import com.screentheme.app.utils.SCREEN_TYPE_0
import com.screentheme.app.utils.SCREEN_TYPE_2
import com.screentheme.app.utils.Tracking
import com.screentheme.app.utils.addCircleRipple
import com.screentheme.app.utils.extensions.getLanguage
import com.screentheme.app.utils.helpers.BillingClientProvider
import com.screentheme.app.utils.helpers.LanguageSupporter
import com.screentheme.app.utils.helpers.SharePreferenceHelper
import com.screentheme.app.utils.screenType
import org.koin.android.ext.android.inject


class PickLanguageFragment : BaseFragment() {

    companion object {
        const val REQUEST_PICKUP_LANGUAGE_KEY = "REQUEST_PICKUP_LANGUAGE_KEY"
    }

    private lateinit var binding: FragmentPickLanguageBinding

    private lateinit var adapter: LanguageAdapter

    private var isFirstTime: Boolean = false

    override fun getViewBinding(): ViewBinding {
        binding = FragmentPickLanguageBinding.inflate(layoutInflater)
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            this.isFirstTime = it.getBoolean("isFirstTime")
        }
        if (this.isFirstTime)
            Tracking.logEvent("launch_language_selection")
        else
            Tracking.logEvent("launch_language_setting")
    }

    override fun onViewCreated() {
        adapter = LanguageAdapter(requireContext()) {
            binding.confirmButton.isVisible = true
            if (AppRemoteConfig.nativeLanguageDuplicate) {
                try {
                    findNavController().navigate(
                        resId = R.id.action_navigation_pick_language_to_navigation_pick_language_duplicate,
                        args = bundleOf("isFirstTime" to isFirstTime, "selectedPosition" to it)
                    )
                } catch (exception: IllegalArgumentException) {

                }
            }
        }
        adapter.onItemClicked { language ->
            pickLanguageViewModel.languageLiveData.postValue(language)
        }

        if (!AppRemoteConfig.nativeLanguageDuplicate) {
            myActivity?.let {
                AdsUtils.loadNativeOnboard(it)
            }
        }

        binding.apply {
            when (screenType) {
                SCREEN_TYPE_2 -> {
                    navTitle.setTextColor(requireContext().getColor(R.color.white))
                    navBar.setBackgroundColor(requireContext().getColor(R.color.ripple_color))
                    confirmButton.setImageResource(R.drawable.icon_check)
                }
                SCREEN_TYPE_0 -> {
                    confirmButton.addCircleRipple()
                }
            }
            languagesRecyclerView.adapter = adapter
            languagesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

            if (isFirstTime) {
                goBackButton.visibility = View.GONE
                initNative()
            } else {
                goBackButton.visibility = View.VISIBLE
            }
            goBackButton.setOnClickListener {
                findNavController().popBackStack()
            }

            if (!AppRemoteConfig.nativeLanguageDuplicate) {
                confirmButton.setOnClickListener {

                    if (!isFirstTime) {
                        setFragmentResult(
                            REQUEST_PICKUP_LANGUAGE_KEY,
                            bundleOf("language" to pickLanguageViewModel.languageLiveData.value)
                        )

                        findNavController().navigateUp()
                    } else {

                        val language = pickLanguageViewModel.languageLiveData.value
                        if (language != null) {
                            try {
                                findNavController().navigate(R.id.action_navigation_pick_language_to_navigation_onboarding)
                            } catch (exception: IllegalArgumentException) {

                            }

                            SharePreferenceHelper.saveBoolean(
                                requireContext(),
                                SharePreferenceHelper.KEY_ALREADY_SET_LANGUAGE,
                                true
                            )
                            LanguageSupporter.setAppLanguage(requireContext(), language)
                        }
                    }
                }
            }

        }

    }

    private fun initNative() {
        if (BillingClientProvider.getInstance(myActivity!!).isPurchased) {
            binding.frAds.visibility = View.GONE
        } else {
            AdsUtils.nativeLanguageFirstOpen.observe(viewLifecycleOwner) {
                if (it?.isLoading == true) {
                    return@observe
                } else {
                    if (it == null) {
                        binding.frAds.visibility = View.GONE
                    } else {
                        VioAdmob.getInstance().populateNativeAdView(
                            myActivity,
                            it,
                            binding.frAds,
                            binding.includeNative.shimmerContainerBanner
                        )
                    }
                }
            }
        }
    }

    override fun registerObservers() {
        pickLanguageViewModel.listOfLanguages.observe(this) {
            adapter.updateItems(it)
            if (it.isNotEmpty()) {

                if (!isFirstTime) {
                    val currentAppLanguage =
                        SharePreferenceHelper.getLanguage(requireContext())

                    if (currentAppLanguage != null) {
                        adapter.setSelectedLanguage(currentAppLanguage)
                    }
                }
            }
        }
    }

}