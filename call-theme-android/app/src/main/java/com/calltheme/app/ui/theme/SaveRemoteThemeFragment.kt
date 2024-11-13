package com.calltheme.app.ui.theme

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.ads.control.admob.Admob
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.utils.AdsUtils
import com.calltheme.app.utils.BannerAdsHelpers
import com.screentheme.app.R
import com.screentheme.app.databinding.FragmentSaveRemoteThemeBinding
import com.screentheme.app.models.RemoteTheme
import com.screentheme.app.utils.extensions.FragmentDirections
import com.screentheme.app.utils.extensions.animateImageViewZoom
import com.screentheme.app.utils.helpers.dummyContacts
import org.koin.android.ext.android.inject


class SaveRemoteThemeFragment : BaseFragment() {

    private var _binding: FragmentSaveRemoteThemeBinding? = null
    private val saveRemoteThemeViewModel: SaveRemoteThemeViewModel by inject()
    private val binding get() = _binding!!
    private lateinit var remoteTheme: RemoteTheme
    private var isBannerShowed = false
    private var isDestinationChanged = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            remoteTheme = it.getParcelable("remoteThemeArg")!!
        }
    }

    override fun getViewBinding(): ViewBinding {
        _binding = FragmentSaveRemoteThemeBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {

        saveRemoteThemeViewModel.countOpen()

        val contact = dummyContacts.random()

        val contactName = contact["contact_name"]
        val phoneNumber = contact["phone_number"]

        binding.callerNameLabel.text = contactName
        binding.callerNumber.text = phoneNumber

        Glide.with(this)
            .load(remoteTheme.background)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
            .into(binding.callBackground)

        Glide.with(this)
            .load(remoteTheme.avatar)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
            .into(binding.callerAvatar)

        Glide.with(this)
            .load(remoteTheme.declineCallIcon)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
            .into(binding.callEnd)

        Glide.with(this)
            .load(remoteTheme.acceptCallIcon)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
            .into(binding.callAccept)

        binding.callAccept.animateImageViewZoom(800)
        binding.callEnd.animateImageViewZoom(800)

        binding.btGoBack.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        binding.btSaveTheme.setOnClickListener {
            if (!loadingDialog.isAdded)
                loadingDialog.show(requireActivity().supportFragmentManager, "loading_dialog")

            themeManager.downloadThemeImages(remoteTheme) {
                if (it) {
                    themeManager.saveThemeIdToPref(remoteTheme.id)

                    requireActivity().runOnUiThread {
                        val savedTheme = themeManager
                            .getThemeConfig(themeId = remoteTheme.id)

                        loadingDialog.dismiss()

                        val args = Bundle()
                        args.putParcelable("themeConfig", savedTheme)

                        val action = FragmentDirections.action(
                            args,
                            R.id.navigation_preview_theme_to_navigation_set_call_theme
                        )
                        try {
                            isDestinationChanged = true
                            findNavController().navigate(action)
                        } catch (e: IllegalArgumentException) {

                        }

                    }

                }

            }
        }
        binding.btGoPremium.visibility = View.GONE
        binding.btGoPremium.setOnClickListener {
            isDestinationChanged = true
            try {
                findNavController().navigate(R.id.action_navigation_preview_theme_to_navigation_subscription)
            }catch (exception:Exception){

            }

        }
        myActivity?.let { BannerAdsHelpers.requestLoadBannerAds(BannerAdsHelpers.BannerAdPlacement.THEME, it) }
        requestBannerListener()
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
                binding.frAds.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isDestinationChanged) {
            isDestinationChanged = false
        }
    }

    override fun onStop() {
        myActivity?.let { BannerAdsHelpers.requestLoadBannerAds(BannerAdsHelpers.BannerAdPlacement.THEME, it) }
        super.onStop()
    }

    override fun registerObservers() {
    }

}
