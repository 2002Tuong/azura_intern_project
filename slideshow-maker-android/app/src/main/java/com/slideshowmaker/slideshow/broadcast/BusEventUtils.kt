package com.slideshowmaker.slideshow.broadcast

import org.greenrobot.eventbus.EventBus

object BusEventUtils {
    fun notifyNetworkChange() {
        EventBus.getDefault().post(ChangeNetworkEvent())
    }
}