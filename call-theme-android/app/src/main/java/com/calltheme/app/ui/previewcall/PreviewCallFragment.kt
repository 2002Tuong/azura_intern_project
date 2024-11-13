package com.calltheme.app.ui.previewcall

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.animation.Animation
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.utils.BannerAdsHelpers
import com.screentheme.app.databinding.FragmentPreviewCallBinding
import com.screentheme.app.models.ThemeConfig
import com.screentheme.app.utils.extensions.animateImageViewZoom
import com.screentheme.app.utils.helpers.FlashController
import com.screentheme.app.utils.helpers.RingtoneController
import com.screentheme.app.utils.helpers.VibrateController
import org.koin.android.ext.android.inject

class PreviewCallFragment : BaseFragment() {

    private lateinit var binding: FragmentPreviewCallBinding
    private val previewCallViewModel: PreviewCallViewModel by inject()
    private val ringtoneHelper: RingtoneController by inject()
    private val vibrateController: VibrateController by inject()
    private val flashController: FlashController by inject()

    private var isBannerShowed = false
    private var isDestinationChanged = false
    private var ringtoneUri: Uri? = null
    private var themeConfig: ThemeConfig? = null
    private var nativeHeight: Int? = null
    override fun getViewBinding(): ViewBinding {
        binding = FragmentPreviewCallBinding.inflate(layoutInflater)

        nativeHeight?.let {
            val layout = binding.frAds
            val params = layout.layoutParams
            params.height = it
            layout.layoutParams = params
        }
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val themeInBundle = it.getParcelable("themeConfig") as? ThemeConfig
            themeConfig = it.getParcelable("themeConfig") as? ThemeConfig
            nativeHeight = it.getInt("nativeHeight")

            if (themeInBundle != null) {
                previewCallViewModel.themeConfigLiveData.postValue(themeInBundle)
            }
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val a = object : Animation() {}
        a.duration = 0
        return a
    }

    override fun onViewCreated() {
        binding.apply {
            callAccept.animateImageViewZoom(800)
            callEnd.animateImageViewZoom(800)
            binding.root.setOnClickListener {
                findNavController().popBackStack()
            }
//            btGoBack.setOnClickListener {
//            }
            themeConfig?.let { themeConfig ->
                ringtoneUri = if (TextUtils.isEmpty(themeConfig.ringtone)) {
                    try {
                        ringtoneHelper.getCurrentRingtoneUri()
                    } catch (e: SecurityException) {
                        null
                    }
                } else {
                    Uri.parse(themeConfig.ringtone)
                }

                binding.apply {
                    Glide.with(requireActivity())
                        .load(themeConfig.background)
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                        .into(callBackground)

                    Glide.with(requireActivity())
                        .load(themeConfig.avatar)
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                        .into(callerAvatar)

                    Glide.with(requireActivity())
                        .load(themeConfig.declineCallIcon)
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                        .into(callEnd)

                    Glide.with(requireActivity())
                        .load(themeConfig.acceptCallIcon)
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                        .into(callAccept)
                }
            }

//            setCallThemeButton.setOnClickListener {
//                isDestinationChanged = true
//                val theme = previewCallViewModel.themeConfigLiveData.value ?: return@setOnClickListener
//
//                if (
//                    !requireActivity().arePermissionsGranted(permissions = permissionList()) ||
//                    (isXiaomiDevice() && !requireActivity().isShowOnLockScreenPermissionEnable())
//                ) {
//                    if (!permissionsDialog.isAdded) {
//                        val permissionsDialog = PermissionsDialog(isShowXiaomiPermission = true)
//                        permissionsDialog.show(
//                            requireActivity().supportFragmentManager,
//                            "PermissionsDialog"
//                        )
//                    }
//                    return@setOnClickListener
//                }
//
//                if (!loadingDialog.isAdded)
//                    loadingDialog.show(requireActivity().supportFragmentManager, "loading_dialog")
//
//                ThemeManager.getInstance(requireContext()).downloadThemeImages(theme) { success ->
//                    if (success) {
//                        ThemeManager.getInstance(requireContext())
//                            .saveThemeIdToPref(themeId = theme.id)
//                        ThemeManager.getInstance(requireContext()).currentThemeId = theme.id
//
//                        val ringtoneUri = Uri.parse(theme.ringtone)
//                        if (ringtoneUri != null) {
//                            RingtoneHelper.getInstance(requireContext()).setRingtone(ringtoneUri)
//                        }
//
//                        requireActivity().runOnUiThread {
//                            val args = Bundle()
//                            args.putParcelable(
//                                "themeConfig",
//                                ThemeManager.getInstance(requireContext())
//                                    .getThemeConfig(themeId = theme.id)
//                            )
//
//                            val action = FragmentDirections.action(
//                                args,
//                                R.id.action_navigation_preview_call_to_navigation_save_theme_success
//                            )
//                            try {
//                                findNavController().navigate(action)
//                            } catch (exception: IllegalArgumentException) {
//
//                            }
//
//                        }
//                    } else {
//
//                    }
//
//                    loadingDialog.dismiss()
//                }
//                return@setOnClickListener
//            }
        }
//        myActivity?.let { BannerAdsUtils.requestLoadBannerAds(BannerAdsUtils.BannerAdPlacement.THEME, it) }
        requestBannerListener()
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun requestBannerListener() {
//        BannerAdsUtils.themeBannerAd.value?.let {
//            isBannerShowed = true
//            //binding.bannerAds.flShimemr.visibility = View.GONE
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
        if (PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getBoolean("vibrate_preference", false)
        ) {
            vibrateController.vibrateDevice()
        }

        if (PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getBoolean("caller_flash_preference", false)
        ) {
            flashController.startFlashLight()
        }
        ringtoneUri?.let { ringtoneHelper.playRingtone(it) }
        if (isDestinationChanged) {
            isDestinationChanged = false
        }
    }

    override fun onStop() {
        myActivity?.let { BannerAdsHelpers.requestLoadBannerAds(BannerAdsHelpers.BannerAdPlacement.THEME, it) }
        super.onStop()
    }

    override fun onPause() {
        vibrateController.stopVibration()
        flashController.stopFlashLight()
        ringtoneHelper.stopRingtone()
        super.onPause()
    }
    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun onDestroy() {
        vibrateController.stopVibration()
        flashController.stopFlashLight()
        ringtoneHelper.stopRingtone()
        super.onDestroy()
    }

    override fun registerObservers() {

//        previewCallViewModel.themeConfigLiveData.observe(this) { themeConfig ->
//
//            ringtoneUri = if (TextUtils.isEmpty(themeConfig.ringtone)) {
//                try {
//                    ringtoneHelper.getCurrentRingtoneUri()
//                } catch (e: SecurityException) {
//                    null
//                }
//            } else {
//                Uri.parse(themeConfig.ringtone)
//            }
//
//            binding.apply {
//                Glide.with(requireActivity())
//                    .load(themeConfig.background)
//                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
//                    .into(callBackground)
//
//                Glide.with(requireActivity())
//                    .load(themeConfig.avatar)
//                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
//                    .into(callerAvatar)
//
//                Glide.with(requireActivity())
//                    .load(themeConfig.decline_call_icon)
//                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
//                    .into(callEnd)
//
//                Glide.with(requireActivity())
//                    .load(themeConfig.accept_call_icon)
//                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
//                    .into(callAccept)
//            }
//        }

    }


}