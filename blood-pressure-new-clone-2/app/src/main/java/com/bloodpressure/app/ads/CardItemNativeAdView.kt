package com.bloodpressure.app.ads

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.bloodpressure.app.databinding.CardItemSmallNativeAdViewBinding
import com.google.android.gms.ads.nativead.NativeAd

class CardItemNativeAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding =
        CardItemSmallNativeAdViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        setupNativeAdView()
    }

    private fun setupNativeAdView() {
        binding.adView.apply {
            headlineView = binding.tvHeadline
            bodyView = binding.tvBody
            callToActionView = binding.btnAction
            iconView = binding.imvIcon
            binding.groupContent.isVisible = true
        }
    }

    fun loadAd(nativeAd: NativeAd) {
        if (nativeAd.responseInfo?.mediationAdapterClassName?.startsWith("com.google.ads.mediation.facebook") == true) {
            binding.tvHeadline.updateLayoutParams<ConstraintLayout.LayoutParams> {
                width = ConstraintLayout.LayoutParams.WRAP_CONTENT
            }
            binding.tvBody.updateLayoutParams<ConstraintLayout.LayoutParams> {
                width = ConstraintLayout.LayoutParams.WRAP_CONTENT
            }
        }
        val adView = binding.adView
        (adView.headlineView as TextView).text = nativeAd.headline
        (adView.bodyView as TextView).text = nativeAd.body
        (adView.callToActionView as Button).text = nativeAd.callToAction
        (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
        adView.iconView?.isVisible = nativeAd.icon?.drawable != null
        adView.setNativeAd(nativeAd)
    }
}