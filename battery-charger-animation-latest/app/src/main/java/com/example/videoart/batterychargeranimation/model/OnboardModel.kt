package com.example.videoart.batterychargeranimation.model

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes

@Keep
data class OnboardModel(
    @DrawableRes val backgroundId: Int,
    val title: String,
    val subTitle: String
)