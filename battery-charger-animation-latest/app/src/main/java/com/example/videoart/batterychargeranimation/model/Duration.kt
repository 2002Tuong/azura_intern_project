package com.example.videoart.batterychargeranimation.model

import com.example.videoart.batterychargeranimation.R

enum class Duration(val value: Long, val stringId: Int) {
    DURATION_5(5000L, R.string.duration_5s),
    DURATION_10(10000L, R.string.duration_10s),
    DURATION_30(30000L, R.string.duration_30s),
    DURATION_ALWAYS(0, R.string.duration_always)
}