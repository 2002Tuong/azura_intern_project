package com.example.videoart.batterychargeranimation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import com.example.videoart.batterychargeranimation.helper.BatteryHelper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BatteryStateReceiver(val callback: (Int) -> Unit = {}) : BroadcastReceiver(), KoinComponent {
    override fun onReceive(context: Context?, intent: Intent?) {

        if(intent?.action == Intent.ACTION_BATTERY_CHANGED) {
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val battery = level * 100 / scale
            Log.d("Battery", battery.toString())
            callback.invoke(battery)
        }
    }
}