package com.slideshowmaker.slideshow.utils.extentions

import android.media.audiofx.Equalizer

fun Equalizer.setEffect(dBList:IntArray) {
    dBList.forEachIndexed { index, dB ->
        setBandLevel(index.toShort(), dB.toShort())
    }
}