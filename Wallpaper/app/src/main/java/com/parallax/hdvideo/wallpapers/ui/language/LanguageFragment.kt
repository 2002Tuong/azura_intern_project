package com.parallax.hdvideo.wallpapers.ui.language


import android.os.Build
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.ads.control.ads.VioAdmob
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.ads.AdsManager
import com.parallax.hdvideo.wallpapers.databinding.FragmentLanguageBinding
import com.parallax.hdvideo.wallpapers.extension.popFragment2
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragmentBinding
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.utils.LanguageUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LanguageFragment : BaseFragmentBinding<FragmentLanguageBinding, LanguageScreenViewModel>() {
    override val resLayoutId: Int
        get() = R.layout.fragment_language
    private lateinit var languageAdapter: LanguageAdapter
    @Inject
    lateinit var languageUtils: LanguageUtils

    override fun init(view: View) {
        super.init(view)
        languageUtils.loadLocale(requireContext())
        AdsManager.requestNativeLanguage(requireActivity(), false)
        setupAdapter()
        dataBinding.imgChoose.setOnClickListener {
            languageUtils.changeLang(languageAdapter.itemSelected().languageCode, requireContext())
            launchOnBoard()
        }

        viewModel.selectedLanguageState.observe(viewLifecycleOwner) {
            dataBinding.imgChoose.isEnabled = !it.isNullOrEmpty()
        }

        initAds()
    }

    private fun launchOnBoard() {
        popFragment2(this)
        checkIntent()
    }

    private fun checkIntent() {
        (activity as? MainActivity)?.let { it.checkIntent(it.intent) }
    }

    private fun setupAdapter() {
        languageAdapter = LanguageAdapter(requireContext())
        languageAdapter.setOnLanguageChange {
            viewModel.selectLanguage(it)
        }
        languageAdapter.setData(
            languageUtils.getSupportedLanguage().take(5)
        )
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        dataBinding.rvcLanguage.addItemDecoration(SpacingDecorator(8))
        dataBinding.rvcLanguage.layoutManager = layoutManager
        dataBinding.rvcLanguage.adapter = languageAdapter
    }

    fun hideNavigationBar() {
        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        activity?.window?.decorView?.systemUiVisibility = flags
        activity?.window?.decorView?.setOnSystemUiVisibilityChangeListener {
            if ((it and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                activity?.window?.decorView?.systemUiVisibility = flags
            }
        }
    }

    private fun initAds() {
        AdsManager.languageApNativeAdLoadFail.observe(this) {
            if (it) {
                dataBinding.frAds.visibility = View.GONE
                AdsManager.languageApNativeAdLoadFail.value = false
            }
        }
        AdsManager.languageApNativeAd.observe(this) {
            if (it != null) {
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    it,
                    dataBinding.frAds,
                    dataBinding.includeNative.shimmerContainerBanner
                )
            }
        }
    }
}