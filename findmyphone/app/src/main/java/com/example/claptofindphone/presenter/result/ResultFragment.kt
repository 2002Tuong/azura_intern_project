package com.example.claptofindphone.presenter.result

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ads.control.ads.VioAdmob
import com.example.claptofindphone.NavGraphDirections
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.FragmentResultBinding
import com.example.claptofindphone.models.ActivatedState
import com.example.claptofindphone.presenter.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResultFragment : BaseFragment(R.layout.fragment_result) {

    override val isTopBarEnable = false
    override val isBottomBarEnable = false
    override val isFabEnable = false
    override val isBackButtonEnable = false
    override val isRightButtonEnable = false
    override val isApplyButtonEnable = false
    override val bannerVisibility: Int = View.GONE
    override val viewModel: ResultScreenViewModel by viewModels()
    private val resultArgs: ResultFragmentArgs by navArgs()
    private val layoutBinding: FragmentResultBinding by viewBinding()

    override fun initView() {
        super.initView()
        val isActive = resultArgs.active
        layoutBinding.txtActive.text = getString(isActive.textResId)
        layoutBinding.animaActive.setAnimation(isActive.animationResId)
        layoutBinding.animaActive.playAnimation()
        if(isActive == ActivatedState.Active) {
            viewModel.startCountDown(5000L)
            viewModel.setStopCount(false)
            layoutBinding.animaActive.scaleX = 0.5f
            layoutBinding.animaActive.scaleY = 0.5f
        }else {
            viewModel.setStopCount(true)
            layoutBinding.animaActive.setMinFrame(20)
            layoutBinding.animaActive.speed = 0.5f
            layoutBinding.animaActive.setMaxFrame(50)
        }
        initBanner()
        adsInit()
        setUpCountDown()
        layoutBinding.backBtn.setOnClickListener {
            it.findNavController().navigate(
                NavGraphDirections.globalToFindPhoneFragment()
            )
        }
    }

    private fun setUpCountDown() {
        viewModel.curTimeState.observe(viewLifecycleOwner) {
            layoutBinding.txtCountDown.text = it.toString()
        }

        viewModel.isStopCountState.observe(viewLifecycleOwner) {
            if(it) {
                layoutBinding.layoutCountDown.visibility = View.GONE
                layoutBinding.layoutActivate.visibility = View.VISIBLE
                if(resultArgs.active == ActivatedState.Active) {
                    viewModel.setActive(true)
                    startService()
                }else {
                    viewModel.setActive(false)
                    stopService()
                }

            }else {
                layoutBinding.layoutCountDown.visibility = View.VISIBLE
                layoutBinding.layoutActivate.visibility = View.GONE
            }
        }
    }

    private fun initBanner() {
        adsHelper.bannerAdViewSoundActive.distinctUntilChanged().observe(this) {
            it?.let {
                (it.parent as? ViewGroup)?.removeView(it)
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                layoutBinding.bottomLayout.addView(it)
            }
        }
    }

    private fun adsInit() {
        return
        adsHelper.resultApNativeAdLoadFail.observe(viewLifecycleOwner) {
            if(it) {
                layoutBinding.frAds.visibility = View.INVISIBLE
                adsHelper.resultApNativeAdLoadFail.value = false
            }
        }

        adsHelper.resultApNativeAd.observe(viewLifecycleOwner) {
            it?.let {
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    it,
                    layoutBinding.frAds,
                    layoutBinding.includeNative.shimmerContainerBanner
                )
            }
        }
    }
}