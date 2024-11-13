package com.calltheme.app.ui.theme

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.wrapper.ApNativeAd
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.calltheme.app.utils.AdsUtils
import com.screentheme.app.R
import com.screentheme.app.databinding.ItemLayoutNativeHomeBinding
import com.screentheme.app.models.RemoteTheme
import com.screentheme.app.utils.helpers.dummyContacts

interface ThemeAdapterCallback {
    fun onItemClicked(theme: RemoteTheme)
}

class ThemeAdapter(val themes: ArrayList<RemoteTheme> = ArrayList()) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var callback: ThemeAdapterCallback? = null
    private var originalThemes: ArrayList<RemoteTheme> = ArrayList()
    var isHiddenAd: Boolean = true //!AdsUtils.isAdEnabled

    companion object {
        const val ITEM = 1
        const val ADS = 2
    }

    val positionAdStart: Int get() = 2
    val distanceAd = 4
    var isWaitingToLoadAds = false

    override fun getItemViewType(position: Int): Int {
        return if (isHiddenAd) return ITEM
        else {
            var result = position - positionAdStart
            if (result < 0) ITEM
            else if (result > 0) {
                result %= (distanceAd + 1)
                if (result == 0) ADS else ITEM
            } else {
                ADS
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val theme = themes[position]
                holder.bind(theme)
            }

            is AdsViewHolder -> {
                val ads = AdsUtils.stackNative.removeFirstOrNull()
                Log.d("qvk", "ads: $ads")
                if (ads == null) {
                    isWaitingToLoadAds = true
                } else {
                    isWaitingToLoadAds = false
                    holder.bind(ads)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.theme_row, parent, false)
                view.layoutParams.apply {
                    //height = ((3 / 5f) * parent.height).toInt()

                }
                ViewHolder(view)
            }

            ADS -> {
                val view = ItemLayoutNativeHomeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AdsViewHolder(view)
            }

            else -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.theme_row, parent, false)

                ViewHolder(view)
            }
        }
    }

    fun updateItems(newThemes: ArrayList<RemoteTheme>) {
        themes.clear()
        themes.addAll(newThemes)
        notifyDataSetChanged()
    }

    fun setOriginalItems(newThemes: ArrayList<RemoteTheme>) {
        originalThemes = newThemes
    }

    fun getOriginalItems(): ArrayList<RemoteTheme> {
        return originalThemes
    }

    override fun getItemCount(): Int {
        return themes.size
    }

    fun setCallback(callback: ThemeAdapterCallback) {
        this.callback = callback
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val context: Context = itemView.context

        private val backgroundImageView: ImageView = itemView.findViewById(R.id.call_background)
        private val acceptImageView: ImageView = itemView.findViewById(R.id.call_accept)
        private val endImageView: ImageView = itemView.findViewById(R.id.call_end)
        private val avatarImageView: ImageView = itemView.findViewById(R.id.caller_avatar)
        private val callerName: TextView = itemView.findViewById(R.id.caller_name_label)
        private val callerNumber: TextView = itemView.findViewById(R.id.caller_number)
        private val premiumImageView: ImageView = itemView.findViewById(R.id.ivPremium)

        fun bind(remoteTheme: RemoteTheme) {

            itemView.setOnClickListener {
                callback?.onItemClicked(remoteTheme)
            }

            val contact = dummyContacts.random()

            val contactName = contact["contact_name"]
            val phoneNumber = contact["phone_number"]

            callerName.text = contactName
            callerNumber.text = phoneNumber


            Glide.with(context)
                .load(remoteTheme.background)
                .fitCenter()
                .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.background_call_placeholder)
                .into(backgroundImageView)

            Glide.with(context)
                .load(remoteTheme.avatar)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(avatarImageView)

            Glide.with(context)
                .load(remoteTheme.declineCallIcon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(endImageView)

            Glide.with(context)
                .load(remoteTheme.acceptCallIcon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(acceptImageView)

//            when {
//                remoteTheme.watchAds -> {
//                    premiumImageView.setImageResource(R.mipmap.ic_ads)
//                    premiumImageView.visibility = View.VISIBLE
//                }
//
//                remoteTheme.isPremium -> {
//                    premiumImageView.setImageResource(R.mipmap.ic_vip)
//                    premiumImageView.visibility = View.VISIBLE
//                }
//                else ->{
//                    premiumImageView.visibility = View.GONE
//                }
//            }
        }
    }
}

class AdsViewHolder(private val binding: ItemLayoutNativeHomeBinding) :
    RecyclerView.ViewHolder(binding.root) {

    val context: Context = itemView.context

    fun bind(nativeAd: ApNativeAd) {

        VioAdmob.getInstance().populateNativeAdView(
            context as Activity,
            nativeAd,
            binding.frAds,
            binding.includeNative.shimmerContainerBanner
        )

//        itemView.setOnClickListener {
//        }
//        val data = nativeAd.admobNativeAd
//
//        val adView = binding.nativeAdView
//        adView.mediaView = binding.adMedia
//        adView.headlineView = binding.adHeadline
//        adView.callToActionView = binding.adCallToAction
//        adView.iconView = binding.adAppIcon
//
//        (adView.headlineView as? TextView)?.text = data.headline
//
//        if (data.callToAction == null) {
//            binding.adCallToAction.isVisible = true
//        } else {
//            binding.adCallToAction.isVisible = false
//            binding.adCallToAction.text = data.callToAction
//        }
//        if (data.icon == null) {
//            binding.adAppIcon.isVisible = true
//        } else {
//            binding.adAppIcon.setImageDrawable(data.icon!!.drawable)
//            binding.adAppIcon.isVisible = false
//        }
//        adView.advertiserView = binding.adAdvertiser
//
//
//        adView.setNativeAd(data)
//
//        val vc = data.mediaContent
//
//        // Updates the UI to say whether or not this ad has a video asset.
//        if (vc?.hasVideoContent() == true) {
//            binding.adMedia.minimumHeight = 120
//            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
//            // VideoController will call methods on this object when events occur in the video
//            // lifecycle.
//        } else {
//            binding.adMedia.minimumHeight = 100
//        }
    }
}
