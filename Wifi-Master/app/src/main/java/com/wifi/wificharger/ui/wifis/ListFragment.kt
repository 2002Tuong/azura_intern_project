package com.wifi.wificharger.ui.wifis

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.ads.control.ads.VioAdmob
import com.thanosfisherman.wifiutils.WifiUtils
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener
import com.unity3d.services.core.properties.ClientProperties.getApplicationContext
import com.wifi.wificharger.R
import com.wifi.wificharger.data.model.ConnectedWifi
import com.wifi.wificharger.data.model.FeatureTitle
import com.wifi.wificharger.data.model.Wifi
import com.wifi.wificharger.databinding.FragmentListBinding
import com.wifi.wificharger.ui.base.BaseFragment
import com.wifi.wificharger.ui.dialog.LoginDialog
import com.wifi.wificharger.ui.main.MainActivity
import com.wifi.wificharger.utils.AdsUtils
import com.wifi.wificharger.utils.Logger
import com.wifi.wificharger.utils.scanWifi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import kotlin.coroutines.resume


class ListFragment : BaseFragment<FragmentListBinding, ListViewModel>(
    FragmentListBinding::inflate
) {
    override val viewModel: ListViewModel by viewModel()
    private var wifiInfo: Wifi? = null
    private var password: String = ""
    private var screen: String = ""
    private var ipAddress: String = ""
    private val adapter by lazy {
        WifiListAdapter(
            onItemClick = { wifi ->
                wifiInfo = wifi
                activity?.let { AdsUtils.requestNativeDetail(it, reload = true) }
                LoginDialog.newInstance(wifi.name).show(this.childFragmentManager) { password ->
                    connectToWifi(wifi, password)
                }
            }
        )
    }

    private val signalAdapter by lazy {
        SignalStrengthAdapter(
            onItemClick = { wifi ->

            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = arguments?.getString("route") ?: ""
        ipAddress = arguments?.getString("ipAddress") ?: ""
    }

    private val savedAdapter by lazy {
        SavedWifiListAdapter(
            onItemClick = { wifi ->
                activity?.let { activity ->
                    AdsUtils.showRewardedVideo(activity,
                        onRewardedAd = {
                            if (it) {
                                viewModel.updateWifi(wifi)
                                viewModel.getSavedWifi()
                            }
                        }
                    )
                }
            }
        )
    }

    private val connectedDevicesAdapter by lazy {
        ConnectedDevicesAdapter(
            onItemClick = {}
        )
    }

    override fun initView() {
        viewBinding.navBar.goBackButton.setOnClickListener {
            navigateUp()
        }
        viewBinding.navBar.goBackButton.isVisible = true
        viewBinding.navBar.navTitle.text = screen
        when (screen) {
            FeatureTitle.CONNECT_WIFI.name -> {
                viewBinding.navBar.navTitle.text = FeatureTitle.CONNECT_WIFI.title
                setupScanWifiUI()
            }

            FeatureTitle.SHOW_PASSWORD_SAVED_WIFI.name -> {
                viewBinding.navBar.navTitle.text = FeatureTitle.SHOW_PASSWORD_SAVED_WIFI.title
                setupSavedWifiUI()
            }

            FeatureTitle.SIGNAL_STRENGTH.name -> {
                viewBinding.navBar.navTitle.text = FeatureTitle.SIGNAL_STRENGTH.title
                setupSignalStrengthUI()
            }

            FeatureTitle.CONNECTED_DEVICES.name -> {
                viewBinding.navBar.navTitle.text = FeatureTitle.CONNECTED_DEVICES.title
                setupConnectedDevices()
            }
        }
    }

    private fun setupConnectedDevices() {
        viewBinding.progressLoading.isVisible = false
        viewBinding.groupScanWifi.isVisible = false
        viewBinding.rcvList.adapter = connectedDevicesAdapter
        lifecycleScope.launch(Dispatchers.IO) {
            if (ipAddress.isNotEmpty()) getConnectedDevices(ipAddress)
        }
    }

    private suspend fun getConnectedDevices(yourPhoneIPAddress: String): ArrayList<InetAddress> {
        val ret = ArrayList<InetAddress>()
        return suspendCancellableCoroutine { continuation ->
            val myIPArray = yourPhoneIPAddress.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            var currentPingAddress: InetAddress
            for ((loopCurrentIP, i) in (0..255).withIndex()) {
                try {

                    // build the next IP address
                    currentPingAddress = InetAddress.getByName(
                        myIPArray[0] + "." +
                                myIPArray[1] + "." +
                                myIPArray[2] + "." +
                                loopCurrentIP.toString()
                    )

                    // 50ms Timeout for the "ping"
                    if (currentPingAddress.isReachable(50)) {
                        ret.add(currentPingAddress)
                        lifecycleScope.launch(Dispatchers.Main) {
                            connectedDevicesAdapter.submitList(ret.map { ConnectedWifi(it.hostAddress) })
                        }
                    }
                } catch (ex: UnknownHostException) {
                } catch (ex: IOException) {
                } catch (ex: ArrayIndexOutOfBoundsException) {

                }
            }
            continuation.resume(ret)
        }
    }

    private fun setupSignalStrengthUI() {
        with(viewBinding) {
            rcvList.adapter = signalAdapter
            groupScanWifi.isVisible = false
        }
    }

    private fun setupScanWifiUI() {
        with(viewBinding) {
            ResourcesCompat.getDrawable(resources, R.drawable.divider_line, null)?.let {
                val itemDecoration = LineDivider(it)
                rcvList.addItemDecoration(itemDecoration)
            }

            rcvList.adapter = adapter
            swWifi.isChecked = true
            swWifi.setOnClickListener {
                if (!swWifi.isChecked) {
                    WifiUtils.withContext(getApplicationContext()).disableWifi()
                    adapter.submitList(emptyList())
                } else {
                    WifiUtils.withContext(getApplicationContext()).enableWifi()
                    scanWifi()
                }
            }
        }
    }

    private fun scanWifi() {
        viewBinding.rcvList.isVisible = false
        viewBinding.progressLoading.isVisible = true
        (activity as? MainActivity)?.scanWifi(::scanSuccess, ::scanFailure)
    }

    private fun setupSavedWifiUI() {
        with(viewBinding) {
            rcvList.adapter = savedAdapter
            groupScanWifi.isVisible = false
        }
        viewModel.getSavedWifi()
    }

    private fun connectToWifi(wifi: Wifi, password: String) {
        this.password = password
        try {
            WifiUtils.withContext(getApplicationContext())
                .connectWith(wifi.name, password)
                .setTimeout(15000)
                .onConnectionResult(checkResult)
                .start()
        } catch (e: Exception) {
            Toast.makeText(context, "Connect failed. Please try again", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onResume() {
        super.onResume()
        if (screen == FeatureTitle.CONNECT_WIFI.name || screen == FeatureTitle.SIGNAL_STRENGTH.name) {
            scanWifi()
        }
    }

    override fun onStop() {
        activity?.let { AdsUtils.requestNativeDetail(it, reload = true) }
        super.onStop()
    }


    private val checkResult = object : ConnectionSuccessListener {
        override fun success() {
            Toast.makeText(context, "SUCCESS!", Toast.LENGTH_SHORT)
                .show()
            wifiInfo?.let {
                viewModel.saveWifi(it.copy(password = password))
            }
        }

        override fun failed(errorCode: ConnectionErrorCode) {
            Toast.makeText(
                context,
                "EPIC FAIL! $errorCode",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun scanSuccess(results: List<Wifi>) {
        Logger.d("wifi results: $results")
        viewModel.setListWifi(results)
    }

    private fun scanFailure(results: List<Wifi>) {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        viewModel.setListWifi(results)
    }

    override fun observeData() {
        viewModel.listWifi.observe(viewLifecycleOwner) {
            viewBinding.rcvList.isVisible = true
            viewBinding.progressLoading.isVisible = false
            when (screen) {
                FeatureTitle.CONNECT_WIFI.name -> adapter.submitList(it)
                FeatureTitle.SHOW_PASSWORD_SAVED_WIFI.name -> savedAdapter.submitList(it.map {
                    it.copy(
                        icon = R.drawable.wifi_local
                    )
                })

                FeatureTitle.SIGNAL_STRENGTH.name -> signalAdapter.submitList(it.map { it.copy(icon = R.drawable.ic_signal_wifi) })
            }
        }
    }

    override fun loadAds() {
        if (screen == FeatureTitle.SHOW_PASSWORD_SAVED_WIFI.name) {
            activity?.let { AdsUtils.loadShowPasswordRewardedAd(it) }
        }
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
}
