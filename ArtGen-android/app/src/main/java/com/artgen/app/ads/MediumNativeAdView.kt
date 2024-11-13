package com.artgen.app.ads

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.databinding.MediumNativeAdViewBinding
import com.google.android.gms.ads.nativead.NativeAd
import org.koin.android.ext.android.inject

class MediumNativeAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val isCtaTop: Boolean = false
) : FrameLayout(context, attrs) {

    private val binding =
        MediumNativeAdViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        setupNativeAdView()
    }

    private fun setupNativeAdView() {
        binding.adMedia.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
        val ctaBtn = if (isCtaTop) {
            binding.btnActionTop.visibility = View.VISIBLE
            binding.btnAction.visibility = View.GONE
            binding.btnActionTop
        } else {
            binding.btnAction.visibility = View.VISIBLE
            binding.btnActionTop.visibility = View.GONE
            binding.btnAction
        }
        binding.adView.apply {
            headlineView = binding.tvHeadline
            bodyView = binding.tvBody
            callToActionView = ctaBtn
            iconView = binding.imvIcon
            mediaView = binding.adMedia
        }
    }

    fun loadAd(nativeAd: NativeAd?) {
        nativeAd ?: return
        if (nativeAd.responseInfo?.mediationAdapterClassName?.startsWith("com.google.ads.mediation.facebook") == true) {
            binding.tvHeadline.updateLayoutParams<ConstraintLayout.LayoutParams> {
                width = ConstraintLayout.LayoutParams.WRAP_CONTENT
            }
            binding.tvBody.updateLayoutParams<ConstraintLayout.LayoutParams> {
                width = ConstraintLayout.LayoutParams.WRAP_CONTENT
            }
            binding.adMedia.updateLayoutParams<ConstraintLayout.LayoutParams> {
                width = ConstraintLayout.LayoutParams.WRAP_CONTENT
            }
        }
        val adView = binding.adView
        (adView.headlineView as TextView).text = nativeAd.headline
        (adView.bodyView as TextView).text = nativeAd.body
        (adView.callToActionView as Button).text = nativeAd.callToAction
        (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
        nativeAd.mediaContent?.let {
            adView.mediaView?.setMediaContent(it)
        }
        adView.iconView?.isVisible = nativeAd.icon?.drawable != null
        adView.setNativeAd(nativeAd)
    }
}
