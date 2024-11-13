package com.wifi.wificharger.ui.speedtest

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.wifi.wificharger.data.model.FeatureTitle
import com.wifi.wificharger.databinding.FragmentSpeedTestBinding
import com.wifi.wificharger.ui.base.BaseFragment
import com.wifi.wificharger.ui.base.BaseViewModel
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class SpeedTestFragment: BaseFragment<FragmentSpeedTestBinding, BaseViewModel>(
    FragmentSpeedTestBinding::inflate
) {

    private val speedTestSocket by lazy {
        SpeedTestSocket()
    }
    override val viewModel: BaseViewModel by viewModel()
    private var isDownload = true

    override fun initView() {
        with(viewBinding) {
            navBar.navTitle.text = FeatureTitle.SPEED_TEST.title
            navBar.goBackButton.isVisible = true
            navBar.goBackButton.setOnClickListener {
                navigateUp()
            }
            gaugeView.setValue(0)
            val dlSpeedTestListener = object : ISpeedTestListener {
                override fun onCompletion(report: SpeedTestReport?) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        if (isDownload) {
                            isDownload = false
                            viewBinding.tvDlValue.text = "${format(report?.transferRateBit?.toDouble()?.div(1000000))} mbps"
                        } else {
                            isDownload = true
                            viewBinding.tvUlValue.text = "${format(report?.transferRateBit?.toDouble()?.div(1000000))} mbps"
                        }
                    }
                    if (isDownload) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            gaugeView.setValue(0)
                        }
                        speedTestSocket.startUpload("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", 10000000)
                    }

                }

                override fun onProgress(percent: Float, report: SpeedTestReport?) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        gaugeView.setValue(if (percent == 0f || percent == 100f) 0 else mbpsToGauge(
                            report?.transferRateBit?.toDouble()?.div(1000000)
                        ))
                        tvGauge.text = format(report?.transferRateBit?.toDouble()?.div(1000000))
                    }
                }

                override fun onError(speedTestError: SpeedTestError?, errorMessage: String?) {

                }
            }

            speedTestSocket.downloadSetupTime = 10000
            btnTest.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    speedTestSocket.addSpeedTestListener(dlSpeedTestListener)
                    speedTestSocket.startDownload("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                }
            }
        }


    }

    private fun mbpsToGauge(s: Double?): Int {
        if (s == null) return 0
        return (1000 * (1 - 1 / 1.3.pow(sqrt(s)))).toInt()
    }

    private fun format(d: Double?): String {
        if (d == null) return ""
        val l: Locale? = resources.configuration.locale
        if (d < 10) return String.format(l, "%.2f", d)
        return if (d < 100) String.format(l, "%.1f", d) else "" + d.roundToInt()
    }

    override fun observeData() {
        // TODO("Not yet implemented")
    }

    override fun loadAds() {
        // TODO("Not yet implemented")
    }
}