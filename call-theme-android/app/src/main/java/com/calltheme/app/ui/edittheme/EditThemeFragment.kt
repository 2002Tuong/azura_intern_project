package com.calltheme.app.ui.edittheme

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.ads.control.admob.Admob
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.ui.pickavatar.PickAvatarFragment
import com.calltheme.app.ui.pickbackground.PickBackgroundFragment
import com.calltheme.app.ui.pickringtone.PickRingtonesFragment
import com.calltheme.app.utils.BannerAdsHelpers
import com.screentheme.app.R
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.databinding.FragmentEditThemeBinding
import com.screentheme.app.models.AvatarModel
import com.screentheme.app.models.BackgroundModel
import com.screentheme.app.models.CallIconModel
import com.screentheme.app.models.RingtoneModel
import com.screentheme.app.models.ThemeConfig
import com.screentheme.app.utils.extensions.animateImageViewZoom

class EditThemeFragment : BaseFragment() {

    companion object {
        const val REQUEST_EDIT_THEME_KEY = "REQUEST_EDIT_THEME_KEY"
    }

    lateinit var binding: FragmentEditThemeBinding
    lateinit var editThemeViewModel: EditThemeViewModel

    private var callIconAdapter = CallIconInEditAdapter()
    private var callIcons = ArrayList<CallIconModel>()
    private var isBannerShowed = false
    private var isDestinationChanged = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val themeInBundle = it.getParcelable("themeConfig") as? ThemeConfig

            if (themeInBundle != null) {
                editThemeViewModel.theme.postValue(themeInBundle)
            }
        }
    }

    override fun getViewBinding(): ViewBinding {
        binding = FragmentEditThemeBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {



        setFragmentResultListener(PickBackgroundFragment.REQUEST_PICKUP_BACKGROUND_KEY) { key, bundle ->

            val updateBackground = bundle.getParcelable("background") as? BackgroundModel

            if (updateBackground != null) {
                val updateTheme = editThemeViewModel.theme.value
                updateTheme?.background = updateBackground.background

                editThemeViewModel.theme.postValue(updateTheme)
            }

        }

        setFragmentResultListener(PickAvatarFragment.REQUEST_PICKUP_AVATAR_KEY) { key, bundle ->

            val updateAvatar = bundle.getParcelable("avatar") as? AvatarModel

            if (updateAvatar != null) {
                val updateTheme = editThemeViewModel.theme.value
                updateTheme?.avatar = updateAvatar.avatar

                editThemeViewModel.theme.postValue(updateTheme)
            }

        }

        setFragmentResultListener(PickRingtonesFragment.REQUEST_PICKUP_RINGTONE_KEY) { key, bundle ->

            val selectedRingtoneItem = bundle.getParcelable("ringtone") as? RingtoneModel

            if (selectedRingtoneItem != null) {
                val updateTheme = editThemeViewModel.theme.value
                updateTheme?.ringtone = selectedRingtoneItem.uri.toString()

                editThemeViewModel.theme.postValue(updateTheme)
            }

        }

        binding.apply {
            btGoBack.setOnClickListener {
                fragmentManager?.popBackStack()
            }

            callAccept.animateImageViewZoom(800)
            callEnd.animateImageViewZoom(800)

            binding.callIconRecyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            callIconAdapter.setCallBack {
                val updateTheme = editThemeViewModel.theme.value

                if (updateTheme != null) {
                    updateTheme.acceptCallIcon = it.accept_call_icon
                    updateTheme.declineCallIcon = it.decline_call_icon

                    editThemeViewModel.theme.postValue(updateTheme)
                }

            }
            binding.callIconRecyclerView.adapter = callIconAdapter

            pickBackgroundButton.setOnClickListener {
                isDestinationChanged = true
                try {
                    findNavController().navigate(R.id.navigation_edit_theme_to_navigation_pick_background)
                } catch (exception: Exception) {

                }

            }

            applyChangeButton.setOnClickListener {
                isDestinationChanged = true
                setFragmentResult(
                    REQUEST_EDIT_THEME_KEY,
                    bundleOf("theme" to editThemeViewModel.theme.value)
                )

                findNavController().navigateUp()
            }

            takeAvatarButton.setOnClickListener {
                isDestinationChanged = true
                try {
                    findNavController().navigate(R.id.navigation_edit_theme_to_navigation_pick_avatar)
                } catch (exception: Exception) {

                }

            }

            pickRingtoneButton.setOnClickListener {
                isDestinationChanged = true
                try {
                    findNavController().navigate(R.id.navigation_edit_theme_to_navigation_pick_ringtone)
                } catch (exception: Exception) {
                }
            }
        }
        myActivity?.let { BannerAdsHelpers.requestLoadBannerAds(BannerAdsHelpers.BannerAdPlacement.CUSTOMIZE, it) }
        requestBannerListener()
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun requestBannerListener() {
        BannerAdsHelpers.customizeBannerAd.value?.let {
            isBannerShowed = true
            binding.bannerAds.flShimemr.visibility = View.GONE
            Admob.getInstance().populateUnifiedBannerAdView(
                myActivity,
                it,
                binding.bannerAds.bannerContainer
            )
        }
        BannerAdsHelpers.customizeBannerAd.observe(this) { adView ->
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
        BannerAdsHelpers.bannerCustomizeFailToLoad.observe(this) {
            if (it && !isBannerShowed) {
                binding.frAds.visibility = View.GONE
            }
        }
    }

    override fun onStop() {
        myActivity?.let { BannerAdsHelpers.requestLoadBannerAds(BannerAdsHelpers.BannerAdPlacement.CUSTOMIZE, it) }
        super.onStop()
    }

    override fun registerObservers() {
        editThemeViewModel.theme.observe(this) { theme ->
            binding.apply {

                Glide.with(requireActivity())
                    .load(theme.background)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(callBackground)

                Glide.with(requireActivity())
                    .load(theme.avatar)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(callerAvatar)

                Glide.with(requireActivity())
                    .load(theme.declineCallIcon)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(callEnd)

                Glide.with(requireActivity())
                    .load(theme.acceptCallIcon)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(callAccept)
            }
        }
        callIcons = AppRemoteConfig.callThemeConfigs().diy_call_icons
        callIconAdapter.updateItems(callIcons)
    }

    override fun onResume() {
        super.onResume()
        if (isDestinationChanged) {
            isDestinationChanged = false
        }
    }

    override fun initializeViewModels() {
        super.initializeViewModels()
        editThemeViewModel = requireActivity().run {
            ViewModelProvider(this).get(EditThemeViewModel::class.java)
        }
    }

}