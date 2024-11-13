package com.calltheme.app.ui.setcalltheme

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.ui.dialog.PermissionsPopup
import com.calltheme.app.ui.dialog.SetThemeBottomSheetPopup
import com.calltheme.app.ui.edittheme.EditThemeFragment
import com.calltheme.app.ui.pickringtone.PickRingtonesFragment
import com.calltheme.app.utils.AdsUtils
import com.screentheme.app.R
import com.screentheme.app.databinding.FragmentSetCallThemeBinding
import com.screentheme.app.models.RingtoneModel
import com.screentheme.app.models.ThemeConfig
import com.screentheme.app.utils.extensions.FragmentDirections
import com.screentheme.app.utils.extensions.animateImageViewZoom
import com.screentheme.app.utils.extensions.arePermissionsGranted
import com.screentheme.app.utils.extensions.isShowOnLockScreenPermissionEnable
import com.screentheme.app.utils.helpers.RingtoneController
import com.screentheme.app.utils.helpers.ThemeManager
import com.screentheme.app.utils.helpers.isXiaomiDevice
import com.screentheme.app.utils.helpers.permissionList
import org.koin.android.ext.android.inject

class SetCallThemeFragment : BaseFragment() {

    lateinit var binding: FragmentSetCallThemeBinding
    private val setCallThemeViewModel: SetCallThemeViewModel by inject()
    private var incomingScreenId = ""
    private var isBannerShowed = false
    private var isNativeDisplayed = false
    private var isWaitingToNavigate = false
    private var isDestinationChanged = false
    private var nativeHeight: Int? = null

    private val ringtoneController: RingtoneController by inject()

    private val bottomSheetFragment = SetThemeBottomSheetPopup(
        onWatchAds = {
            AdsUtils.showRewardedVideo(
                activity = requireActivity(),
                onComplete = {
                    AdsUtils.loadSetCallRewardedAd(requireContext(), reload = true)
                },
                onRewardedAd = {
                    isWaitingToNavigate = it
                    Log.d("CustomTheme", "theme id: call")
                    if (it) {
                        val theme = setCallThemeViewModel.theme.value!!
                        handleDownloadTheme(theme)
                    }
                }
            )
        },
        onBuyPro = {
//            navigate(
//                actionId = R.id.action_resultFragment_to_subscriptionFragment
//            )
        }
    )

    private fun navigateToSaveSuccess(theme: ThemeConfig) {
        requireActivity().runOnUiThread {
            val args = Bundle()
            args.putParcelable(
                "themeConfig",
                themeManager
                    .getThemeConfig(themeId = theme.id)
            )

            val action = FragmentDirections.action(
                args,
                R.id.action_navigation_set_call_theme_to_navigation_save_theme_success
            )
            try {
                findNavController().navigate(action)
            } catch (exception: IllegalArgumentException) {

            }

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {

            val themeInBundle = it.getParcelable("themeConfig") as? ThemeConfig

            if (themeInBundle != null) {
                setCallThemeViewModel.theme.postValue(themeInBundle)
                incomingScreenId = it.getString("screen", "")
            }
        }
    }

    override fun getViewBinding(): ViewBinding {
        binding = FragmentSetCallThemeBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {
        setCallThemeViewModel.countOpen()

        if (incomingScreenId == "DiyThemeFragment" && setCallThemeViewModel.openCount.value!! > 0) {
        }

        setFragmentResultListener(EditThemeFragment.REQUEST_EDIT_THEME_KEY) { key, bundle ->
            val updateThemeConfig = bundle.getParcelable("theme") as? ThemeConfig

            if (updateThemeConfig != null) {
                setCallThemeViewModel.theme.postValue(updateThemeConfig)
            }

        }

        setFragmentResultListener(PickRingtonesFragment.REQUEST_PICKUP_RINGTONE_KEY) { key, bundle ->

            val selectedRingtoneItem = bundle.getParcelable("ringtone") as? RingtoneModel

            if (selectedRingtoneItem != null) {
                val updateTheme = setCallThemeViewModel.theme.value
                updateTheme?.ringtone = selectedRingtoneItem.uri.toString()

                setCallThemeViewModel.theme.postValue(updateTheme)
            }

        }

        binding.apply {
            callAccept.animateImageViewZoom(800)
            callEnd.animateImageViewZoom(800)

            doneButton.setOnClickListener {
                isDestinationChanged = true
                setCallThemeViewModel.theme.value ?: return@setOnClickListener
                handleSetCallTheme()
            }

            btGoBack.setOnClickListener {
                myActivity?.let { it1 -> AdsUtils.loadInterCustomize(it1) }
                findNavController().navigateUp()
            }

            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    myActivity?.let { it1 -> AdsUtils.loadInterCustomize(it1) }
                    findNavController().navigateUp()
                }
            }

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

//            editThemeButton.setOnClickListener {
//                isDestinationChanged = true
//                val theme = setCallThemeViewModel.theme.value ?: return@setOnClickListener
//
//                val args = Bundle()
//                args.putParcelable("themeConfig", theme)
//
//                val action = FragmentDirections.action(
//                    args,
//                    R.id.action_navigation_set_call_theme_to_navigation_edit_theme
//                )
//                try {
//                    findNavController().navigate(action)
//                } catch (exception: IllegalArgumentException) {
//
//                }
//            }
//
//            pickRingtoneButton.setOnClickListener {
//                isDestinationChanged = true
//                try {
//                    findNavController().navigate(R.id.action_navigation_set_call_theme_to_navigation_pick_ringtone)
//                } catch (exception: IllegalArgumentException) {
//
//                }
//            }

            previewButton.setOnClickListener {
                onPreview()
            }

            btGoPremium.visibility = View.GONE
            btGoPremium.setOnClickListener {
                isDestinationChanged = true
                try {
                    getNavController().navigate(R.id.action_navigation_set_call_theme_to_navigation_subscription)
                } catch (exception: IllegalArgumentException) {

                }
            }
        }
        AdsUtils.loadSetCallRewardedAd(requireContext())
    }

    private fun handleDownloadTheme(theme: ThemeConfig) {
        if (!loadingDialog.isAdded)
            loadingDialog.show(requireActivity().supportFragmentManager, "loading_dialog")
        Log.d("CustomTheme", "theme id: ${theme}")
        themeManager.downloadThemeImages(theme) { success ->
            Log.d("qvk", "success: $success")
            if (success) {
                themeManager
                    .saveThemeIdToPref(themeId = theme.id)
                themeManager.currentThemeId = theme.id

                val ringtoneUri = Uri.parse(theme.ringtone)
                if (ringtoneUri != null) {
                    ringtoneController.setRingtone(ringtoneUri)
                }
                isWaitingToNavigate = false
                navigateToSaveSuccess(theme)

            } else {

            }
            loadingDialog.dismiss()
        }
    }

    private fun handleSetCallTheme() {
        if (
            !requireActivity().arePermissionsGranted(permissions = permissionList()) ||
            (isXiaomiDevice() && !requireActivity().isShowOnLockScreenPermissionEnable())
        ) {

            if (!permissionsPopup.isAdded) {
                val permissionsDialog = PermissionsPopup(isShowXiaomiPermission = true)
                permissionsDialog.onDismiss = {
                    if (
                        requireActivity().arePermissionsGranted(permissions = permissionList()) &&
                        (isXiaomiDevice() && requireActivity().isShowOnLockScreenPermissionEnable())
                    ) {
                        if (!bottomSheetFragment.isAdded) bottomSheetFragment.show(parentFragmentManager, SetThemeBottomSheetPopup.TAG)
                    }
                }
                permissionsDialog.onShowingRequestDialog = {
                    AdsUtils.requestNativeSetCallTheme(requireActivity(), reload = true)
                }
                permissionsDialog.show(
                    requireActivity().supportFragmentManager,
                    "PermissionsDialog"
                )
            }
        } else {
            if (!bottomSheetFragment.isAdded) bottomSheetFragment.show(parentFragmentManager, SetThemeBottomSheetPopup.TAG)
        }
    }

    private fun onPreview() {
        val theme = setCallThemeViewModel.theme.value ?: return
        val args = Bundle()
        args.putParcelable("themeConfig", theme)
        val layoutParams = binding.frAds
        nativeHeight = layoutParams.height
        nativeHeight?.let { nativeHeight -> args.putInt("nativeHeight", nativeHeight) }

        val action = FragmentDirections.action(
            args,
            R.id.action_navigation_set_call_theme_to_navigation_preview_call
        )
        isDestinationChanged = true
        try {
            findNavController().navigate(action)
        } catch (exception: IllegalArgumentException) {

        }
    }

    private fun initAds() {
        AdsUtils.setCallThemeApNativeAdLoadFail.observe(viewLifecycleOwner) {
            if (it) {
                binding.frAds.visibility = View.GONE
            }
        }
        AdsUtils.setCallThemeApNativeAd.observe(viewLifecycleOwner) {
            if (it != null) {
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    it,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner
                )
            }
        }
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun requestBannerListener() {
//        BannerAdsUtils.themeBannerAd.value?.let {
//            isBannerShowed = true
//            binding.bannerAds.flShimemr.visibility = View.GONE
//            Admob.getInstance().populateUnifiedBannerAdView(
//                myActivity,
//                it,
//                binding.bannerAds.bannerContainer
//            )
//        }
//        BannerAdsUtils.themeBannerAd.observe(this) { adView ->
//            adView?.let {
//                isBannerShowed = true
//                //binding.bannerAds.flShimemr.visibility = View.GONE
//                try {
//                    (it.parent as? ViewGroup)?.removeView(it)
//                    binding.bannerAds.bannerContainer.removeAllViews()
//                } catch (ex: Exception) {
//                    ex.printStackTrace()
//                }
//                Admob.getInstance().populateUnifiedBannerAdView(
//                    myActivity,
//                    adView,
//                    binding.bannerAds.bannerContainer
//                )
//            }
//        }
//        BannerAdsUtils.bannerThemeFailToLoad.observe(this) {
//            if (it && !isBannerShowed) {
//                binding.frAds.visibility = View.GONE
//            }
//        }
    }

    override fun onResume() {
        super.onResume()
        AppOpenManager.getInstance().enableAppResume()
        if (isDestinationChanged) {
            isDestinationChanged = false
        }
        if (isWaitingToNavigate) {
            isWaitingToNavigate = false
            val theme = setCallThemeViewModel.theme.value ?: return
            handleDownloadTheme(theme)
        //navigateToSaveSuccess(theme)
        }
        initAds()
    }

    override fun onStop() {
        AdsUtils.requestNativeSetCallTheme(requireActivity(), reload = true)
        super.onStop()
    }

    override fun registerObservers() {

        setCallThemeViewModel.theme.observe(this) { theme ->
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
    }

}