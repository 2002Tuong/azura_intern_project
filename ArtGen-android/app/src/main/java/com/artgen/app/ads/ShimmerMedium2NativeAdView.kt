package com.artgen.app.ads

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.artgen.app.databinding.ShimmerMedium2NativeAdViewBinding
import com.artgen.app.databinding.ShimmerMediumNativeAdViewBinding

class ShimmerMedium2NativeAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding =
        ShimmerMedium2NativeAdViewBinding.inflate(LayoutInflater.from(context), this, true)
}