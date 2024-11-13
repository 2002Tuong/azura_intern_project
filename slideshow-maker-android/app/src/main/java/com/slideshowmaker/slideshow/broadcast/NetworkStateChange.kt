package com.slideshowmaker.slideshow.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NetworkStateChange : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        BusEventUtils.notifyNetworkChange()
    }
}