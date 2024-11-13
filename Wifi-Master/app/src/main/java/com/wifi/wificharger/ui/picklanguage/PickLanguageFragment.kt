package com.wifi.wificharger.ui.picklanguage

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ads.control.ads.VioAdmob
import com.wifi.wificharger.R
import com.wifi.wificharger.databinding.FragmentPickLanguageBinding
import com.wifi.wificharger.ui.base.BaseFragment
import com.wifi.wificharger.utils.AdsUtils
import com.wifi.wificharger.utils.TrackingManager
import org.koin.androidx.viewmodel.ext.android.viewModel

class PickLanguageFragment : BaseFragment<FragmentPickLanguageBinding, PickLanguageViewModel>(
    FragmentPickLanguageBinding::inflate
) {

    companion object {
        const val REQUEST_PICKUP_LANGUAGE_KEY = "REQUEST_PICKUP_LANGUAGE_KEY"
    }

    override val viewModel: PickLanguageViewModel by viewModel()

    override fun initView() {
        adapter = LanguageAdapter(requireContext()) {
            viewBinding.confirmButton.isVisible = true
            try {
                findNavController().navigate(
                    resId = R.id.action_navigation_pick_language_to_navigation_pick_language_dup,
                    args = bundleOf("isFirstTime" to isFirstTime, "selectedPosition" to it, "listLanguage" to viewModel.listOfLanguages.value)
                )
            } catch (exception: IllegalArgumentException) {

            }
        }
        adapter.onItemClicked { language ->
            viewModel.languageLiveData.postValue(language)
        }

        viewBinding.apply {
            languagesRecyclerView.adapter = adapter
            languagesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

            if (isFirstTime) {
                goBackButton.visibility = View.GONE
            } else {
                goBackButton.visibility = View.VISIBLE
            }
            goBackButton.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    override fun loadAds() {
        activity?.let {
            AdsUtils.loadNativeOnboard(it)
        }
    }

    override fun observeData() {
        viewModel.listOfLanguages.observe(this) {
            adapter.updateItems(it)
            if (it.isNotEmpty()) {

                if (!isFirstTime) {
                    val currentAppLanguage =
                        viewModel.getCurrentAppLanguage()

                    if (currentAppLanguage != null) {
                        adapter.setSelectedLanguage(currentAppLanguage)
                    }
                }
            }
        }
    }

    private lateinit var adapter: LanguageAdapter

    private var isFirstTime: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            this.isFirstTime = it.getBoolean("isFirstTime")
        }
        if (this.isFirstTime)
            TrackingManager.logEvent("launch_language_selection")
        else
            TrackingManager.logEvent("launch_language_setting")
    }

    override fun onBindingAds() {
        AdsUtils.nativeLanguageFirstOpen.observe(viewLifecycleOwner) {
            if (it?.isLoading == true) {
                return@observe
            } else {
                if (it == null) {
                    viewBinding.frAds.visibility = View.GONE
                } else {
                    VioAdmob.getInstance().populateNativeAdView(
                        activity,
                        it,
                        viewBinding.frAds,
                        viewBinding.includeNative.shimmerContainerBanner
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }
}
