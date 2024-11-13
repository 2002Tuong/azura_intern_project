package com.wifi.wificharger.ui.info

import android.net.wifi.WifiInfo
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.ads.control.ads.VioAdmob
import com.wifi.wificharger.data.model.FeatureTitle
import com.wifi.wificharger.databinding.FragmentInfoBinding
import com.wifi.wificharger.ui.base.BaseFragment
import com.wifi.wificharger.ui.base.BaseViewModel
import com.wifi.wificharger.utils.AdsUtils
import com.wifi.wificharger.utils.Logger
import com.wifi.wificharger.utils.getIpAddress
import org.koin.androidx.viewmodel.ext.android.viewModel


class InfoFragment: BaseFragment<FragmentInfoBinding, BaseViewModel>(
    FragmentInfoBinding::inflate
) {

    override val viewModel: BaseViewModel by viewModel()
    private var wifiInfo: WifiInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wifiInfo = arguments?.getParcelable("wifiInfo") as? WifiInfo
        Logger.d("wifiInfo: $wifiInfo")
    }
    override fun initView() {
        with(viewBinding) {
            navBar.goBackButton.setOnClickListener {
                navigateUp()
            }
            navBar.navTitle.text = FeatureTitle.WIFI_INFO.title
            navBar.goBackButton.isVisible = true

            wifiName.tvTitle.text = "Wifi Name"
            status.tvTitle.text = "Status"
            ipAddress.tvTitle.text = "Ip Address"
            serverAddress.tvTitle.text = "Server Address"
            dns1.tvTitle.text = "D.N.S 1"
            dns2.tvTitle.text = "D.N.S 2"
            bssid.tvTitle.text = "BSSID"
            linkSpeed.tvTitle.text = "Link Speed"
            levels.tvTitle.text = "Levels"
            channelWidth.tvTitle.text = "Channel Width"
            frequency.tvTitle.text = "Frequency"
            wifiName.tvDetail.text = wifiInfo?.ssid
            status.tvDetail.text = "Connected"
            ipAddress.tvDetail.text = context?.getIpAddress(wifiInfo)
            serverAddress.tvDetail.text = wifiInfo?.macAddress
            dns1.tvDetail.text = wifiInfo?.ssid
            dns2.tvDetail.text = wifiInfo?.ssid
            bssid.tvDetail.text = wifiInfo?.bssid
            linkSpeed.tvDetail.text = wifiInfo?.linkSpeed.toString()
            levels.tvDetail.text = wifiInfo?.rssi.toString()
            channelWidth.tvDetail.text = wifiInfo?.ssid
            frequency.tvDetail.text = wifiInfo?.frequency.toString()
        }
    }



    override fun observeData() {
        AdsUtils.nativeDetail.observe(viewLifecycleOwner) {
            if (it?.isLoading == true) {
                return@observe
            } else {
                if (it == null) {
                    viewBinding.frAds.visibility = View.GONE
                } else {
                    VioAdmob.getInstance().populateNativeAdView(
                        activity,
                        it,
                        viewBinding.frAds,
                        viewBinding.includeNative.shimmerContainerBanner
                    )
                }
            }
        }
    }

    override fun loadAds() {
        // TODO("Not yet implemented")
    }

    override fun onStop() {
        activity?.let { AdsUtils.requestNativeDetail(it, reload = true) }
        super.onStop()
    }
}