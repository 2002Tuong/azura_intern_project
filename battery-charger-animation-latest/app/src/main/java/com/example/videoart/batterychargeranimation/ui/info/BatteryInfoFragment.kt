package com.example.videoart.batterychargeranimation.ui.info

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.ads.control.ads.VioAdmob
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.databinding.FragmentBatteryInfoBinding
import com.example.videoart.batterychargeranimation.receiver.BatteryStateReceiver
import com.example.videoart.batterychargeranimation.ui.base.BaseFragment
import com.example.videoart.batterychargeranimation.utils.AdsUtils
import org.koin.android.ext.android.inject


class BatteryInfoFragment : BaseFragment() {
    private lateinit var binding: FragmentBatteryInfoBinding
    private lateinit var receiver: BatteryStateReceiver
    private val viewModel: BatteryInfoViewModel by inject()

    private val infoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val batteryLevel = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL,0)
            val batteryIsCharging = intent?.getIntExtra(BatteryManager.EXTRA_STATUS,-1) ?: -1
            val batteryTemperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0)?.div(10)
            val batteryVoltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0)?.div(1000)
            val batteryTechnology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
            val batteryHealth = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD) ?: BatteryManager.BATTERY_HEALTH_UNKNOWN

            val isCharging =batteryIsCharging == BatteryManager.BATTERY_STATUS_CHARGING
                    || batteryIsCharging == BatteryManager.BATTERY_STATUS_FULL
            binding.batteryType.info = batteryTechnology
            binding.batteryTemperature.info = batteryTemperature?.toFloat().toString()
            binding.batteryVoltage.info = batteryVoltage?.toFloat().toString()
            binding.batteryChargingType.info = if(isCharging) {
                getString(R.string.plugged)
            } else {
                getString(R.string.unplugged)
            }

            binding.batteryHealth.info = when(batteryHealth) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "OverHeat"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "OverVoltage"
                else -> "Unknown"
            }

        }

    }
    override fun getViewBinding(): ViewBinding {
        binding = FragmentBatteryInfoBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {
        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        receiver = BatteryStateReceiver {
            viewModel.batteryCapacity.value = it
        }
        requireActivity().registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        requireActivity().registerReceiver(infoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        binding.batteryCapacity.info = getBatteryCapacity().toString() + "mAh"
    }

    override fun registerObservers() {
        viewModel.batteryCapacity.observe(viewLifecycleOwner) {
            binding.batteryStateText.text = "${it} %"
            when(it) {
                in 0..10 -> {
                    binding.batteryStateImg.setImageResource(R.drawable.battery_10_percent)
                    binding.batteryStateText.setTextColor(Color.parseColor("#FF692E"))
                }
                in 11..40 -> {
                    binding.batteryStateImg.setImageResource(R.drawable.battery_20_percent)
                    binding.batteryStateText.setTextColor(Color.parseColor("#FF692E"))
                }
                in 41..70 -> {
                    binding.batteryStateImg.setImageResource(R.drawable.battery_60_percent)
                    binding.batteryStateText.setTextColor(Color.parseColor("#38E053"))
                }
                in 71..90 -> {
                    binding.batteryStateImg.setImageResource(R.drawable.battery_80_percent)
                    binding.batteryStateText.setTextColor(Color.parseColor("#38E053"))
                }
                else -> {
                    binding.batteryStateImg.setImageResource(R.drawable.battery_100_percent)
                    binding.batteryStateText.setTextColor(Color.parseColor("#38E053"))
                }
            }
        }

        AdsUtils.nativeBatteryInfo.observe(viewLifecycleOwner) {
            it?.let {nativeAd ->
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    nativeAd,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner
                )
            }
        }

        AdsUtils.nativeBatteryInfoLoadFail.observe(viewLifecycleOwner) {
            if(it) {
                binding.frAds.isVisible = false
                AdsUtils.nativeBatteryInfoLoadFail.value = false
            }
        }
    }

    fun getBatteryCapacity() : Double {
        var mPowerProfile: Any? = null
        var batteryCapacity = 0.0
        val POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile"
        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                .getConstructor(Context::class.java)
                .newInstance(requireContext())
            batteryCapacity = Class.forName(POWER_PROFILE_CLASS)
                .getMethod("getBatteryCapacity")
                .invoke(mPowerProfile) as Double
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return  batteryCapacity
    }
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(receiver)
        requireActivity().unregisterReceiver(infoReceiver)
    }
}