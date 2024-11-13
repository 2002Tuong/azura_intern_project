package com.example.videoart.batterychargeranimation.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.videoart.batterychargeranimation.ui.batterycharger.AnimationChargerActivity


class ChargerStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == Intent.ACTION_POWER_CONNECTED) {
            Toast.makeText(context, "Power in", Toast.LENGTH_SHORT).show()
            Intent(context, AnimationChargerActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

                context?.startActivity(this)
            }

        }else {
            Toast.makeText(context, "Power off", Toast.LENGTH_SHORT).show()
        }
    }
}