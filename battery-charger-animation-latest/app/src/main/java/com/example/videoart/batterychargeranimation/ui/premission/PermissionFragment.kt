package com.example.videoart.batterychargeranimation.ui.premission

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.wrapper.ApNativeAd
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.data.local.PreferenceUtils
import com.example.videoart.batterychargeranimation.databinding.FragmentPermissionBinding
import com.example.videoart.batterychargeranimation.ui.base.BaseFragment
import com.example.videoart.batterychargeranimation.utils.AdsUtils

class PermissionFragment : BaseFragment() {
    private lateinit var binding: FragmentPermissionBinding
    private val viewModel: PermissionViewModel by viewModels()
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {isGranted ->
        if(isGranted) {
            viewModel.setReadStorage(true)
            PreferenceUtils.setStoragePermission(true)
        }else {
            viewModel.setReadStorage(false)
        }
    }
    private var currentAds = 1;
    private var nativeAd1: ApNativeAd? = null
    private var nativeAd2: ApNativeAd? = null
    private var nativeAd3: ApNativeAd? = null

    override fun getViewBinding(): ViewBinding {
        binding = FragmentPermissionBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {
        //AdsUtils.requestNativePermission(requireActivity())
        AdsUtils.requestLoadBanner(requireActivity())
        PreferenceUtils.setDisplayOverPermission(Settings.canDrawOverlays(requireContext()))
        PreferenceUtils.setStoragePermission(checkPermission(requiredPermission()))

        binding.btnContinue.isEnabled = false

        viewModel.setReadStorage(PreferenceUtils.storagePermissionGranted)
        viewModel.setDisplayOver(PreferenceUtils.displayOverAppGranted)

        binding.swStoragePermission.setOnClickListener {
            if(!checkPermission(requiredPermission())) {
                Log.d("Permissison", "call")
                requestPermissionLauncher.launch(requiredPermission())
            }
            Log.d(this::class.simpleName, "${nativeAd2}")
            currentAds = 2
            if(nativeAd2 != null) {
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    nativeAd2,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner
                )
            }
        }

        binding.swOverlayStoragePermission.setOnClickListener {
            AppOpenManager.getInstance().disableAppResume()
            Log.d(this::class.simpleName, "${nativeAd3}")
            currentAds = 3
            if(nativeAd3 != null) {
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    nativeAd3,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner
                )
            }
            if(!Settings.canDrawOverlays(requireContext())) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + requireContext().packageName))
                startActivityForResult(intent,DISPLAY_OVER_APP_PERMISSION)
            }
        }

        binding.btnContinue.setOnClickListener {
            val nav = PermissionFragmentDirections.actionPermissionFragmentToMainFragment()
            getNavController().navigate(nav)
            PreferenceUtils.isFirstOpenComplete = true
            AppOpenManager.getInstance().enableAppResume()
        }
    }

    override fun registerObservers() {
        initAds()
        viewModel.permissionReadStorage.observe(viewLifecycleOwner) {
            if(it) {
                binding.swStoragePermission.setImageResource(R.drawable.icon_switch_on)
                binding.swStoragePermission.isEnabled = false

                if(viewModel.permissionDisplayOverOtherApps.value != null &&
                    viewModel.permissionDisplayOverOtherApps.value == true) {
                    onFullPermission()
                } else {
                    binding.btnContinue.isEnabled = false
                }
            }else {
                binding.swStoragePermission.setImageResource(R.drawable.icon_switch_off)
            }
        }

        viewModel.permissionDisplayOverOtherApps.observe(viewLifecycleOwner) {
            if(it) {
                binding.swOverlayStoragePermission.setImageResource(R.drawable.icon_switch_on)
                binding.swOverlayStoragePermission.isEnabled = false

                if(viewModel.permissionReadStorage.value != null &&
                    viewModel.permissionReadStorage.value == true) {
                    onFullPermission()
                } else {
                    binding.btnContinue.isEnabled = false
                }
            }else {
                binding.swOverlayStoragePermission.setImageResource(R.drawable.icon_switch_off)
            }
        }
    }

    private fun initAds() {
        AdsUtils.nativePermission1.observe(viewLifecycleOwner) {native ->
            native?.let {
                nativeAd1 = it
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    nativeAd1,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner)
            }
        }

        AdsUtils.nativePermission2.observe(viewLifecycleOwner) {native ->
            native?.let {
                nativeAd2 = it
                if(currentAds == 2) {
                    VioAdmob.getInstance().populateNativeAdView(
                        requireActivity(),
                        nativeAd2,
                        binding.frAds,
                        binding.includeNative.shimmerContainerBanner)
                }
            }
        }

        AdsUtils.nativePermission3.observe(viewLifecycleOwner) {native ->
            native?.let {
                nativeAd3 = it
                if(currentAds == 3) {
                    VioAdmob.getInstance().populateNativeAdView(
                        requireActivity(),
                        nativeAd3,
                        binding.frAds,
                        binding.includeNative.shimmerContainerBanner)
                }
            }
        }

        AdsUtils.nativePermissionLoadFail.observe(viewLifecycleOwner) {
            if(it) {
                binding.frAds.isVisible = false
                AdsUtils.nativePermissionLoadFail.value =false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == DISPLAY_OVER_APP_PERMISSION || resultCode == RESULT_OK) {
            //AppOpenManager.getInstance().enableAppResume()
            if(Settings.canDrawOverlays(requireContext())) {
                viewModel.setDisplayOver(true)
                PreferenceUtils.setDisplayOverPermission(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(Settings.canDrawOverlays(requireContext()) && checkPermission(requiredPermission())) {
            onFullPermission()
        }

        //AppOpenManager.getInstance().enableAppResume()
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requiredPermission(): String {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    private fun onFullPermission() {
        binding.btnContinue.isEnabled = true
        binding.txtPermissionState.text = getString(R.string.permission_allowed)
        binding.txtPermissionState.setTextColor(resources.getColor(R.color.green_400, null))
        binding.continueTxt.setTextColor(resources.getColor(R.color.green_500))
        binding.continueIcon.setColorFilter(resources.getColor(R.color.green_500))

    }

    companion object {
        const val DISPLAY_OVER_APP_PERMISSION = 1000
    }
}