package com.slideshowmaker.slideshow.ui.picker.view

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.utils.KotlinEpoxyHolder

@EpoxyModelClass(layout = R.layout.view_epoxy_native_ads_layout)
abstract class NativeAdsEpoxyModel : EpoxyModelWithHolder<NativeAdsEpoxyModel.Holder>() {

    @EpoxyAttribute
    lateinit var nativeAds: NativeAd

    @EpoxyAttribute
    var lastNativeAd: Boolean? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        val adView = holder.adView
        (adView.headlineView as TextView).text = nativeAds.headline
        (adView.bodyView as TextView).text = nativeAds.body
        (adView.callToActionView as Button).text = nativeAds.callToAction
        (adView.iconView as ImageView).setImageDrawable(nativeAds.icon?.drawable)
        adView.setNativeAd(nativeAds)
    }

    class Holder : KotlinEpoxyHolder() {
        val adView: NativeAdView by bind(R.id.adView)
        override fun bindView(itemView: View) {
            super.bindView(itemView)
            adView.callToActionView = itemView.findViewById(R.id.btnAction)
            adView.headlineView = itemView.findViewById(R.id.tvHeadline)
            adView.bodyView = itemView.findViewById(R.id.tvBody)
            adView.iconView = itemView.findViewById(R.id.imvIcon)
        }
    }

    companion object {
        internal const val ID = "NativeAdsEpoxyModel"
    }
}
