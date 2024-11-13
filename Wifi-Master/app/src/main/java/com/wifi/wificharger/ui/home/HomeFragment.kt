package com.wifi.wificharger.ui.home

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.ads.control.admob.Admob
import com.ads.control.ads.VioAdmob
import com.thanosfisherman.wifiutils.WifiUtils
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener
import com.wifi.wificharger.R
import com.wifi.wificharger.data.model.Feature
import com.wifi.wificharger.data.model.FeatureTitle
import com.wifi.wificharger.data.model.Wifi
import com.wifi.wificharger.data.model.getWifiSignalStrength
import com.wifi.wificharger.data.model.mapToWifi
import com.wifi.wificharger.databinding.FragmentHomeBinding
import com.wifi.wificharger.ui.base.BaseFragment
import com.wifi.wificharger.ui.dialog.ExitAppDialog
import com.wifi.wificharger.ui.dialog.RatingAppDialog
import com.wifi.wificharger.ui.main.MainActivity
import com.wifi.wificharger.utils.AdsUtils
import com.wifi.wificharger.utils.BannerAdsUtils
import com.wifi.wificharger.utils.arePermissionsGranted
import com.wifi.wificharger.utils.getIpAddress
import com.wifi.wificharger.utils.handlePermissions
import com.wifi.wificharger.utils.requestLocationPermissionIfNeeded
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment: BaseFragment<FragmentHomeBinding, HomeViewModel>(
    FragmentHomeBinding::inflate
) {
    companion object {
        const val SETTINGS_PACKAGE = "com.android.settings"
        const val HOTSPOT_SETTINGS_CLASS = "com.android.settings.TetherSettings"
    }
    override val viewModel: HomeViewModel by viewModel()
    private var wifiInfo: WifiInfo? = null
    private var wifiQR: Wifi? = null
    private var isBannerShowed = false
    private var isWaitingToShowRated = false
    private val ratingAppDialog = RatingAppDialog()
    private val exitAppDialog = ExitAppDialog()
    private val scanQrCodeLauncher = registerForActivityResult(ScanQRCode()) { result ->
        // handle QRResult
        if (result is QRResult.QRSuccess) {
            val data = result.mapToWifi()
            data?.let(::connectToWifi)
        }
    }

    private val adapter = HomeAdapter(
        onItemClick = {
            isWaitingToShowRated = true
            viewModel.featureUseCount++
            if (it.name == FeatureTitle.WIFI_HOTSPOT) {
                launchHotspotSettings()
            } else {
                activity?.let { context ->
                    AdsUtils.forceShowInterDetail(context, onNavigate = {
                        navigateToFeature(it.name)
                    }) {
                        openFunctionalFeature(it.name)
                    }
                } ?: run {
                    when (it.name) {
                        FeatureTitle.SCAN_QR -> openFunctionalFeature(it.name)
                        else -> navigateToFeature(it.name)
                    }
                }
            }
        }
    )

    private fun openFunctionalFeature(feature: FeatureTitle) {
        when (feature) {
            FeatureTitle.WIFI_HOTSPOT -> launchHotspotSettings()
            FeatureTitle.SCAN_QR -> scanQrCodeLauncher.launch(null)
            else -> {}
        }
    }

    private fun navigateToFeature(featureName: FeatureTitle) {
        when (featureName) {
            FeatureTitle.CONNECT_WIFI -> {
                checkPermission {
                    navigate(bundle = bundleOf("route" to FeatureTitle.CONNECT_WIFI.name), R.id.action_homeFragment_to_listFragment)
                }
            }

            FeatureTitle.SHOW_PASSWORD_SAVED_WIFI -> {
                navigate(bundle = bundleOf("route" to FeatureTitle.SHOW_PASSWORD_SAVED_WIFI.name), R.id.action_homeFragment_to_listFragment)
            }

            FeatureTitle.WIFI_INFO -> {
                navigate(bundle = bundleOf("wifiInfo" to wifiInfo), R.id.action_homeFragment_to_infoFragment)
            }

            FeatureTitle.GENERATE_PASSWORD -> {
                navigate(actionId = R.id.action_homeFragment_to_generatePassword)
            }

            FeatureTitle.SIGNAL_STRENGTH -> {
                checkPermission {
                    navigate(bundle = bundleOf("route" to FeatureTitle.SIGNAL_STRENGTH.name), R.id.action_homeFragment_to_listFragment)
                }
            }

            FeatureTitle.SPEED_TEST -> {
                navigate(actionId = R.id.action_homeFragment_to_speedTestFragment)
            }

            FeatureTitle.CONNECTED_DEVICES -> {
                navigate(bundle = bundleOf("route" to FeatureTitle.CONNECTED_DEVICES.name, "ipAddress" to context?.getIpAddress(wifiInfo)), R.id.action_homeFragment_to_listFragment)
            }
            else -> {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermission {
            getCurrentWifi()
        }
    }

    private fun launchHotspotSettings() {
        val intent = Intent(Intent.ACTION_MAIN, null)
        val componentName = ComponentName(SETTINGS_PACKAGE, HOTSPOT_SETTINGS_CLASS)
        intent.setComponent(componentName)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun connectToWifi(wifi: Wifi) {
        wifiQR = wifi
        WifiUtils.withContext(requireActivity().applicationContext)
            .connectWith(wifi.name, wifi.password)
            .setTimeout(15000)
            .onConnectionResult(checkResult)
            .start()
    }

    private val checkResult = object : ConnectionSuccessListener {
        override fun success() {
            wifiQR?.let { viewModel.saveWifi(it) }
        }

        override fun failed(errorCode: ConnectionErrorCode) {
        }
    }

    override fun initView() {

        viewBinding.apply {
            activity?.let {
                requestBannerListener()
            }
            navBar.navTitle.text = "Wifi Manager"
            homeRecyclerView.layoutManager = GridLayoutManager(context, 2)

            homeRecyclerView.adapter = adapter

            adapter.submitList(
                listOf(
                    Feature(FeatureTitle.CONNECT_WIFI, icon = R.drawable.scan_wifi),
                    Feature(FeatureTitle.SHOW_PASSWORD_SAVED_WIFI, icon = R.drawable.ic_wifi_lock),
                    Feature(FeatureTitle.WIFI_INFO, icon = R.drawable.icon_wifi_info),
                    Feature(FeatureTitle.GENERATE_PASSWORD, icon = R.drawable.ic_gen_pass),
                    Feature(FeatureTitle.SCAN_QR, icon = R.drawable.ic_scan_qr),
                    Feature(FeatureTitle.SIGNAL_STRENGTH, icon = R.drawable.ic_signal_strength),
                    Feature(FeatureTitle.SPEED_TEST, icon = R.drawable.icon_speed_test),
                    Feature(FeatureTitle.CONNECTED_DEVICES, icon = R.drawable.ic_wifi_connected_devices),
                    Feature(FeatureTitle.WIFI_HOTSPOT, icon = R.drawable.icon_wifi_hotspot),
                )
            )
        }
    }
    private fun requestBannerListener() {
        BannerAdsUtils.homeBannerAds.value?.let {
            isBannerShowed = true
            Admob.getInstance().populateUnifiedBannerAdView(
                activity,
                it,
                viewBinding.bannerAds.bannerContainer
            )
        }

    }


    private fun getCurrentWifi() {
        if (context == null) return
        (activity as? MainActivity)?.requestLocationPermissionIfNeeded {
            if (it) {
                val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val request = NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .build()
                    val networkCallback = object : ConnectivityManager.NetworkCallback(
                        FLAG_INCLUDE_LOCATION_INFO
                    ) {
                        override fun onCapabilitiesChanged(
                            network: Network,
                            networkCapabilities: NetworkCapabilities
                        ) {
                            super.onCapabilitiesChanged(network, networkCapabilities)
                            wifiInfo = networkCapabilities.transportInfo as WifiInfo
                        }
                    }
                    connectivityManager.registerNetworkCallback(request, networkCallback)
                } else {
                    val mWifiManager =
                        requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    wifiInfo = mWifiManager.connectionInfo
                }
            }
        }

    }

    private fun checkPermission(onGrantPermission: () -> Unit) {
        val changeWifiState = Manifest.permission.CHANGE_WIFI_STATE
        if (!requireActivity().arePermissionsGranted(permissions = arrayListOf(changeWifiState))) {
            (requireActivity() as? MainActivity)?.handlePermissions(arrayListOf(changeWifiState)) {
                if (it) onGrantPermission.invoke()
            }
        } else {
            onGrantPermission.invoke()
        }
    }

    override fun onStop() {
        activity?.let {
            BannerAdsUtils.requestLoadBannerAds(BannerAdsUtils.BannerAdPlacement.HOME, it)
            AdsUtils.requestNativeHome(it, reload = true)
        }
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        viewBinding.gaugeView.setValue(0)
        wifiInfo?.let {
            viewBinding.gaugeGroup.isVisible = true
            val signals = getWifiSignalStrength(it.rssi)
            viewBinding.gaugeView.setValue(signals * 10)
            viewBinding.tvGauge.text= "$signals%"
        } ?: run {
            viewBinding.gaugeGroup.isVisible = false
        }
        if (isWaitingToShowRated) {
            onBackFromFeature()
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onExitApp()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun onBackFromFeature() {
        ratingAppDialog.onDismiss = {
        }
        ratingAppDialog.onFinishRate = {
        }
        if ((viewModel.featureUseCount == 2 || viewModel.featureUseCount == 4)) {
            if (!ratingAppDialog.isAdded && viewModel.canShowRate()) {
                isWaitingToShowRated = false
                ratingAppDialog.show(requireActivity())
            }
        }
    }

    private fun onExitApp() {
        ratingAppDialog.onDismiss = {
            if (!exitAppDialog.isAdded) exitAppDialog.show(requireActivity())
        }
        ratingAppDialog.onFinishRate = {
            if (!exitAppDialog.isAdded) exitAppDialog.show(requireActivity())
        }
        exitAppDialog.onExitApp {
            activity?.finish()
        }
        if (!ratingAppDialog.isAdded && viewModel.canShowRate()) {
            isWaitingToShowRated = false
            ratingAppDialog.show(requireActivity())
        } else {
            if (!exitAppDialog.isAdded) exitAppDialog.show(requireActivity())
        }
    }

    override fun observeData() {
        BannerAdsUtils.homeBannerAds.observe(this) { adView ->
            adView?.let {
                isBannerShowed = true
                //binding.bannerAds.flShimemr.visibility = View.GONE
                try {
                    (it.parent as? ViewGroup)?.removeView(it)
                    viewBinding.bannerAds.bannerContainer.removeAllViews()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
                Admob.getInstance().populateUnifiedBannerAdView(
                    activity,
                    adView,
                    viewBinding.bannerAds.bannerContainer
                )
            }
        }
        BannerAdsUtils.bannerHomeFailToLoad.observe(this) {
            if (it && !isBannerShowed) {
                viewBinding.frAds.visibility = View.GONE
            }
        }

        AdsUtils.nativeHome.observe(viewLifecycleOwner) {
            if (it?.isLoading == true) {
                return@observe
            } else {
                if (it == null) {
                    viewBinding.frAdsNative.visibility = View.GONE
                } else {
                    VioAdmob.getInstance().populateNativeAdView(
                        activity,
                        it,
                        viewBinding.frAdsNative,
                        viewBinding.includeNative.shimmerContainerBanner
                    )
                }
            }
        }
    }

    override fun loadAds() {
        activity?.let {
            AdsUtils.loadInterHome(it)
            AdsUtils.requestNativeDetail(it)
            AdsUtils.loadShowPasswordRewardedAd(it)
            AdsUtils.requestNativeExit(it)
        }
    }
}