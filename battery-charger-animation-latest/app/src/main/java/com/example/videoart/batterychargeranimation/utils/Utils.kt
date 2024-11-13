package com.example.videoart.batterychargeranimation.utils

import android.os.Handler

fun delay(milliseconds: Long = 300, action: () -> Unit) {
    Handler().postDelayed(action, milliseconds)
}