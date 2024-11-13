package com.wifi.wificharger.ui.picklanguage

import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.wifi.wificharger.databinding.FragmentPickLanguageBinding
import com.wifi.wificharger.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class UpdateLanguageFragment : BaseFragment<FragmentPickLanguageBinding, PickLanguageViewModel>(
    FragmentPickLanguageBinding::inflate
) {


    private lateinit var adapter: LanguageAdapter

    override val viewModel: PickLanguageViewModel by viewModel()

    override fun initView() {
        adapter = LanguageAdapter(requireContext()) {}
        adapter.onItemClicked { language ->
            viewModel.languageLiveData.postValue(language)
        }

        viewBinding.apply {
            languagesRecyclerView.adapter = adapter
            languagesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            goBackButton.setOnClickListener {
                findNavController().popBackStack()
            }

            confirmButton.setOnClickListener {

                setFragmentResult(PickLanguageFragment.REQUEST_PICKUP_LANGUAGE_KEY, bundleOf("language" to viewModel.languageLiveData.value))

                findNavController().navigateUp()
            }
        }
    }

    override fun loadAds() {
//        adsManager.loadLanguageSettingNativeAd()
    }

    override fun observeData() {
        viewModel.listOfAllLanguages.observe(this) {
            adapter.updateItems(it)
            if (it.isNotEmpty()) {

                val currentAppLanguage = viewModel.getCurrentAppLanguage()

                if (currentAppLanguage != null) {
                    adapter.setSelectedLanguage(currentAppLanguage)
                }
            }
        }
    }

    override fun onBindingAds() {
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.RESUMED) {
//                adsManager.languageSettingNativeAd.collect {
//                    if (it?.isLoading == true) {
//                        return@collect
//                    } else {
//                        if (it == null) {
//                            viewBinding.frAds.visibility = View.GONE
//                        } else {
//                            VioAdmob.getInstance().populateNativeAdView(
//                                this@UpdateLanguageFragment.activity,
//                                it,
//                                viewBinding.frAds,
//                                viewBinding.includeNative.shimmerContainerBanner
//                            )
//                        }
//                    }
//                }
//            }
//        }
    }

}