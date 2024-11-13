package com.example.videoart.batterychargeranimation.ui.charging

import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.core.view.isVisible
import com.ads.control.ads.VioAdmob
import com.example.videoart.batterychargeranimation.MainActivity
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.databinding.ActivityChargingBinding
import com.example.videoart.batterychargeranimation.receiver.BatteryStateReceiver
import com.example.videoart.batterychargeranimation.utils.AdsUtils

class ChargingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChargingBinding
    private lateinit var receiver: BatteryStateReceiver

    private val countDownLoadAds = object : CountDownTimer(30000L, 1000L) {
        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {
            AdsUtils.requestNativeCharging(this@ChargingActivity, true)
            this.start()
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChargingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receiver = BatteryStateReceiver {
            binding.currentCharge.text = getString(R.string.current_charging, it)
        }

        binding.icClose.setOnClickListener {
            Intent(this, MainActivity::class.java).apply {
                startActivity(this)
                finish()
            }
        }

        registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        AdsUtils.nativeCharging.observe(this) {nativeAd ->
            nativeAd?.let {
                VioAdmob.getInstance().populateNativeAdView(
                    this,
                    it,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner
                )
            }
            countDownLoadAds.start()
        }

        AdsUtils.nativeChargingLoadFail.observe(this) {
            if(it) {
                binding.frAds.isVisible = false
                AdsUtils.nativeChargingLoadFail.value =false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AdsCharging", "call onDestroy")
        countDownLoadAds.cancel()
        unregisterReceiver(receiver)
    }
}