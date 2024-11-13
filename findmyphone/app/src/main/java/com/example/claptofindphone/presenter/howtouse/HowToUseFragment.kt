package com.example.claptofindphone.presenter.howtouse

import android.view.ViewGroup
import androidx.lifecycle.distinctUntilChanged
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.claptofindphone.R
import com.example.claptofindphone.ads.BannerAdsController
import com.example.claptofindphone.databinding.FragmentHowToUseBinding
import com.example.claptofindphone.presenter.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HowToUseFragment : BaseFragment(R.layout.fragment_how_to_use) {
    override val label: String
        get() = getString(R.string.how_to_use)

    override val isBackButtonEnable: Boolean
        get() = false

    override val isBottomBarEnable: Boolean
        get() = false
    override val isFabEnable: Boolean
        get() = false

    override val isRightButtonEnable: Boolean
        get() = true
    override val isApplyButtonEnable: Boolean
        get() = false
    override val bannerPlacement: BannerAdsController.BannerPlacement
        get() = BannerAdsController.BannerPlacement.HOW_TO_USE


    private val howToUseBinding by viewBinding(FragmentHowToUseBinding::bind)

    override fun initView() {
        super.initView()
        initBanner()
    }
    private fun initBanner() {
        adsHelper.bannerAdViewHowToUse.distinctUntilChanged().observe(viewLifecycleOwner) {
            it?.let {
                (it.parent as? ViewGroup)?.removeView(it)
                if(it.layoutParams != null) {
                    it.layoutParams.apply {
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                        width = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                }else {
                    it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                }

                howToUseBinding.root.addView(it)
            }
        }
    }
}