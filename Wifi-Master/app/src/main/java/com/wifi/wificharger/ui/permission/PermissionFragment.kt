package com.wifi.wificharger.ui.permission

import android.Manifest
import android.os.Build
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.ads.control.ads.VioAdmob
import com.wifi.wificharger.R
import com.wifi.wificharger.databinding.FragmentPermissionBinding
import com.wifi.wificharger.ui.base.BaseFragment
import com.wifi.wificharger.ui.base.BaseViewModel
import com.wifi.wificharger.ui.main.MainActivity
import com.wifi.wificharger.utils.AdsUtils
import com.wifi.wificharger.utils.BannerAdsUtils
import com.wifi.wificharger.utils.arePermissionsGranted
import com.wifi.wificharger.utils.requestLocationPermissionIfNeeded
import org.koin.androidx.viewmodel.ext.android.viewModel

class PermissionFragment : BaseFragment<FragmentPermissionBinding, BaseViewModel>(
    FragmentPermissionBinding::inflate
) {

    override val viewModel: BaseViewModel by viewModel()
    private var isNavigateOut = false

    override fun initView() {
        activity?.let {
            BannerAdsUtils.requestLoadBannerAds(BannerAdsUtils.BannerAdPlacement.HOME, it)
            AdsUtils.requestNativeHome(it)
        }
        if (checkLocationPermission()) {
            navigateToHomeScreen()
        }
        viewBinding.navBar.btnContinue.isVisible = true
        viewBinding.navBar.navTitle.text = "Permission"
        viewBinding.callingScreenSwitch.setOnClickListener {
            val mainActivity = activity as? MainActivity ?: return@setOnClickListener

            mainActivity.requestLocationPermissionIfNeeded(
                onGrantPermission = ::onGrantPermission,
                preAction = {
                    AdsUtils.requestNativePermission(mainActivity, reload = true)

                }
            )
        }
        viewBinding.navBar.btnContinue.setOnClickListener {
            navigateToHomeScreen()
        }

    }

    private fun checkLocationPermission(): Boolean {
        val accessFineLocation = Manifest.permission.ACCESS_FINE_LOCATION
        return activity?.arePermissionsGranted(permissions = arrayListOf(accessFineLocation)) ?: false
    }

    override fun observeData() {
        AdsUtils.permissionApNativeAdLoadFail.observe(viewLifecycleOwner) {
            if (it) {
                viewBinding.frAds.visibility = View.GONE
            }
        }
        AdsUtils.permissionApNativeAd.observe(viewLifecycleOwner) {
            if (it != null) {
                VioAdmob.getInstance().populateNativeAdView(
                    activity,
                    it,
                    viewBinding.frAds,
                    viewBinding.includeNative.shimmerContainerBanner
                )
            }
        }
    }

    override fun loadAds() {
    }

    override fun onStop() {
        activity?.let {
            if (!isNavigateOut) AdsUtils.requestNativePermission(it, reload = true)
        }
        super.onStop()
    }

    private fun navigateToHomeScreen() {
        isNavigateOut = true
        findNavController().navigate(R.id.action_permissionFragment_to_navigation_home)
    }


    private fun onGrantPermission(isGranted: Boolean) {
        viewBinding.callingScreenSwitch.isChecked = isGranted
        viewBinding.navBar.btnContinue.setTextColor(
            resources.getColor(
                if (isGranted) R.color.white else R.color.grey_600,
                null
            )
        )
        viewBinding.navBar.btnContinue.isEnabled = isGranted
    }
}