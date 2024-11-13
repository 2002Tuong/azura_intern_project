package com.calltheme.app.ui.setcalltheme

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.ads.control.admob.Admob
import com.ads.control.ads.VioAdmob
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.utils.AdsUtils
import com.calltheme.app.utils.BannerAdsHelpers
import com.screentheme.app.R
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.databinding.FragmentSaveThemeSuccessfullyBinding
import com.screentheme.app.models.ThemeConfig
import com.screentheme.app.utils.helpers.dummyContacts
import org.koin.android.ext.android.inject

class SaveThemeSuccessfullyFragment : BaseFragment() {

    private lateinit var binding: FragmentSaveThemeSuccessfullyBinding

    private val saveThemeSuccessfullyViewModel: SaveThemeSuccessfullyViewModel by inject()
    private var navigateToPremium = false
    private var isNativeDisplayed = false
    private var isBannerShowed = false
    override fun getViewBinding(): ViewBinding {
        binding = FragmentSaveThemeSuccessfullyBinding.inflate(layoutInflater)
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val themeInBundle = it.getParcelable("themeConfig") as? ThemeConfig

            if (themeInBundle != null) {
                saveThemeSuccessfullyViewModel.themeConfigLiveData.postValue(themeInBundle)
            }
        }
    }

    override fun onViewCreated() {
        binding.apply {
            goHomeButton.setOnClickListener {
                try {
                    findNavController().navigate(R.id.action_navigation_save_theme_success_to_navigation_home)
                } catch (exception: IllegalArgumentException) {

                }
            }

            btGoBack.setOnClickListener {
                findNavController().popBackStack()
            }

            btGoPremium.visibility = View.GONE
            btGoPremium.setOnClickListener {
                navigateToPremium = true
                try {
                    findNavController().navigate(R.id.action_navigation_save_theme_success_to_navigation_subscription)
                } catch (exception: IllegalArgumentException) {

                }

            }
            if (AppRemoteConfig.nativeSaveTheme) {
                requestNativeAds()
                binding.frBanner.visibility = View.GONE
            } else {
                myActivity?.let { BannerAdsHelpers.requestLoadBannerAds(BannerAdsHelpers.BannerAdPlacement.THEME, it) }
                requestBannerListener()
                binding.frAds.isVisible = false
            }
        }
    }


    @SuppressLint("FragmentLiveDataObserve")
    private fun requestBannerListener() {
        BannerAdsHelpers.themeBannerAd.value?.let {
            isBannerShowed = true
            //binding.bannerAds.flShimemr.visibility = View.GONE
            Admob.getInstance().populateUnifiedBannerAdView(
                myActivity,
                it,
                binding.bannerAds.bannerContainer
            )
        }
        BannerAdsHelpers.themeBannerAd.observe(this) { adView ->
            adView?.let {
                isBannerShowed = true
                //binding.bannerAds.flShimemr.visibility = View.GONE
                try {
                    (it.parent as? ViewGroup)?.removeView(it)
                    binding.bannerAds.bannerContainer.removeAllViews()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
                Admob.getInstance().populateUnifiedBannerAdView(
                    myActivity,
                    adView,
                    binding.bannerAds.bannerContainer
                )
            }
        }
        BannerAdsHelpers.bannerThemeFailToLoad.observe(this) {
            if (it && !isBannerShowed) {
                binding.frBanner.visibility = View.GONE
            }
        }
    }


    private fun requestNativeAds() {
        binding.apply {
            myActivity?.let {
                AdsUtils.requestNativeSaveThemeSuccessfully(it, onLoaded = {
                    isNativeDisplayed = true
                    VioAdmob.getInstance().populateNativeAdView(
                        myActivity,
                        it,
                        frAds,
                        includeNative.shimmerContainerBanner
                    )
                }, onLoadFail = {
                    if (isNativeDisplayed) return@requestNativeSaveThemeSuccessfully
                    binding.frAds.isVisible = false
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (navigateToPremium) {
            navigateToPremium = false
            requestNativeAds()
        }
    }

    override fun onStop() {
        myActivity?.let { BannerAdsHelpers.requestLoadBannerAds(BannerAdsHelpers.BannerAdPlacement.THEME, it) }
        requestNativeAds()
        super.onStop()
    }

    override fun registerObservers() {
        saveThemeSuccessfullyViewModel.themeConfigLiveData.observe(this) {
            binding.apply {

                val contact = dummyContacts.random()

                val contactName = contact["contact_name"]
                val phoneNumber = contact["phone_number"]

                callerNameLabel.text = contactName
                callerNumber.text = phoneNumber

                Log.d("CustomTheme", "background ${it.background}")
                Log.d("Custom theme", "avatar ${it.avatar}")
                Glide.with(requireContext())
                    .load(it.background)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(binding.callBackground)

                Glide.with(requireContext())
                    .load(it.avatar)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(binding.callerAvatar)

                Glide.with(requireContext())
                    .load(it.declineCallIcon)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(binding.callEnd)

                Glide.with(requireContext())
                    .load(it.acceptCallIcon)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(binding.callAccept)
            }
        }
    }

}