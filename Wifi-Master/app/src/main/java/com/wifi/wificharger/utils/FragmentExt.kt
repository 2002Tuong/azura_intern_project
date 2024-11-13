package com.wifi.wificharger.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

fun Fragment.getActivity(): FragmentActivity? {
    return try {
        requireActivity()
    } catch (e: IllegalStateException) {
        null
    }
}