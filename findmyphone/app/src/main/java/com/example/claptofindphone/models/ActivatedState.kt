package com.example.claptofindphone.models

import androidx.annotation.Keep
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.example.claptofindphone.R

@Keep
enum class ActivatedState(@RawRes val animationResId: Int, @StringRes val textResId: Int) {
    Active(R.raw.active_animation, R.string.successful_activation),
    Deactivate(R.raw.deactive_animation, R.string.deactivated)
}