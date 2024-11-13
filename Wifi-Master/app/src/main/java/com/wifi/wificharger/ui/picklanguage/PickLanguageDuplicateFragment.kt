package com.wifi.wificharger.ui.picklanguage

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ads.control.ads.VioAdmob
import com.wifi.wificharger.R
import com.wifi.wificharger.data.model.Language
import com.wifi.wificharger.databinding.FragmentPickLanguageBinding
import com.wifi.wificharger.ui.base.BaseFragment
import com.wifi.wificharger.utils.AdsUtils
import com.wifi.wificharger.utils.LanguageUtils
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PickLanguageDuplicateFragment : BaseFragment<FragmentPickLanguageBinding, PickLanguageViewModel>(
    FragmentPickLanguageBinding::inflate
) {

    private lateinit var adapter: LanguageAdapter

    override val viewModel: PickLanguageViewModel by viewModel()
    private val languageUtils: LanguageUtils by inject()
    private var isFirstTime: Boolean = false
    private var selectedPosition = RecyclerView.NO_POSITION
    private var listLanguage: ArrayList<Language>? = arrayListOf()


    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation {
        val a = object : Animation() {}
        a.duration = 0
        return a
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            this.isFirstTime = it.getBoolean("isFirstTime")
            this.selectedPosition = it.getInt("selectedPosition")
            this.listLanguage = if (Build.VERSION.SDK_INT >= 33) {
                it.getParcelableArrayList("listLanguage", Language::class.java)
            } else {
                it.getParcelableArrayList("listLanguage")
            }
        }
    }

    override fun initView() {
        viewBinding.confirmButton.isVisible = true
        adapter = LanguageAdapter(requireContext(), selectedItemPosition = selectedPosition) {
            selectedPosition = it
        }
        adapter.updateItems(listLanguage!!)

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

            confirmButton.setOnClickListener {
                val language = listLanguage!![selectedPosition]
                try {
                    findNavController().navigate(R.id.action_navigation_pick_language_dup_to_navigation_onboarding)
                } catch (exception: IllegalArgumentException) {

                }

                lifecycleScope.launch {
                    viewModel.setLanguageSelectedStatus()
                    languageUtils.setAppLanguage(requireContext(), language)
                }
            }
        }

    }

    override fun observeData() {

        AdsUtils.nativeLanguageDuplicateFirstOpen.observe(viewLifecycleOwner) {
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

    override fun loadAds() {
    }

}