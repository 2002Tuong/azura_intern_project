package com.parallax.hdvideo.wallpapers.ui.main.fragment

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.ads.nativead.NativeAd
import com.parallax.hdvideo.wallpapers.databinding.NativeAdUnifiedBinding
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.extension.isInvisible
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseAdapterList

class NativeAdViewHolder(bd: NativeAdUnifiedBinding) :
    BaseAdapterList.BaseViewHolder<NativeAd, NativeAdUnifiedBinding>(bd) {


    override fun onBind(data: NativeAd) {
        val nativeAdView = dataBinding.nativeAdView
        nativeAdView.mediaView = dataBinding.adMedia

        // Set other ad assets.
        nativeAdView.headlineView = dataBinding.adHeadline
        nativeAdView.callToActionView = dataBinding.adCallToAction
        nativeAdView.iconView = dataBinding.adAppIcon

        (nativeAdView.headlineView as? TextView)?.text = data.headline

        if (data.callToAction == null) {
            nativeAdView.callToActionView?.isHidden = true
        } else {
            nativeAdView.callToActionView?.isHidden = false
            (nativeAdView.callToActionView as Button).text = data.callToAction
        }
        if (data.icon == null) {
            nativeAdView.iconView?.isHidden = true
        } else {
            (nativeAdView.iconView as? ImageView)?.setImageDrawable(data.icon!!.drawable)
            nativeAdView.iconView?.isHidden = false
        }
        nativeAdView.starRatingView = dataBinding.adStars
        nativeAdView.advertiserView = dataBinding.adAdvertiser
        if (data.starRating == null || data.starRating!! <= 0) {
            nativeAdView.starRatingView?.visibility = View.GONE
            if (data.advertiser != null || data.advertiser?.isBlank() == false) {
                nativeAdView.advertiserView?.isInvisible = false
            }
        } else {
            (nativeAdView.starRatingView as RatingBar).rating = data.starRating!!.toFloat()
            nativeAdView.starRatingView?.visibility = View.VISIBLE
            nativeAdView.advertiserView?.isInvisible = true
        }

        nativeAdView.setNativeAd(data)


        val content = data.mediaContent

        // Updates the UI to say whether or not this ad has a video asset.
        if (content != null) {
            if (content.hasVideoContent()) {
                dataBinding.adMedia.minimumHeight = 120
                // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
                // VideoController will call methods on this object when events occur in the video
                // lifecycle.
            } else {
                dataBinding.adMedia.minimumHeight = 100
            }
        }
    }
}
