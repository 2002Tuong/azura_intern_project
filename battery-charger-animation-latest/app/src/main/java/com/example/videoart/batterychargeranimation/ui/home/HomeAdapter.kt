package com.example.videoart.batterychargeranimation.ui.home

import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import coil.transform.RoundedCornersTransformation
import com.ads.control.ads.VioAdmob
import com.ads.control.ads.wrapper.ApNativeAd
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.databinding.ItemListHomeAdsBinding
import com.example.videoart.batterychargeranimation.databinding.ItemListHomeBinding
import com.example.videoart.batterychargeranimation.model.RemoteTheme
import com.example.videoart.batterychargeranimation.model.Theme
import com.example.videoart.batterychargeranimation.utils.AdsUtils

interface ThemeAdapterCallback {
    fun onItemClicked(theme: RemoteTheme)
}

class HomeAdapter(
    val activity: Activity,
    val themes: MutableList<RemoteTheme> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var callback: ThemeAdapterCallback? = null
    private var originalThemes: List<RemoteTheme> = listOf()
    var isWaitingToLoadAds = false
    var apNativeAd: ApNativeAd? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var count = 0
        set(value) {
            field = value
            Log.d("HomeAdapter", "set value ${field}")
        }
    fun updateItems(newThemes: List<RemoteTheme>) {
        themes.clear()
        Log.d("Chip", "inviewModel ${originalThemes.size}")
        themes.addAll(newThemes)
        count = 0
        notifyDataSetChanged()
    }

    fun setOriginalItems(newThemes: List<RemoteTheme>) {
        originalThemes = newThemes
    }

    fun getOriginalItems(): List<RemoteTheme> {
        return originalThemes
    }


    fun setCallback(callback: ThemeAdapterCallback) {
        this.callback = callback
    }
    inner class HomeViewHolder(val binding: ItemListHomeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(theme1: RemoteTheme?, theme2: RemoteTheme?) {

            binding.thumbnail1.load(theme1?.thumbUrl) {
                placeholder(R.drawable.background_placeholder)
                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.READ_ONLY)
                transformations(listOf(RoundedCornersTransformation(20f)))
            }

            theme1?.let {theme ->
                binding.card1.setOnClickListener {
                    callback?.onItemClicked(theme)
                }
            }

            binding.thumbnail2.load(theme2?.thumbUrl) {
                placeholder(R.drawable.background_placeholder)
                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.READ_ONLY)
                transformations(listOf(RoundedCornersTransformation(20f)))
            }

            theme2?.let { theme ->
                binding.card2.setOnClickListener {
                    callback?.onItemClicked(theme)
                }
            }
        }
    }

    inner class AdsViewHolder(val binding: ItemListHomeAdsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(nativeAd: ApNativeAd?) {
            nativeAd?.let {
                VioAdmob.getInstance().populateNativeAdView(
                    activity,
                    nativeAd,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner
                )
            }
            if(nativeAd == null) {
                binding.root.isVisible = false
                binding.includeNative.shimmerContainerBanner.isVisible =false
            }else {
                binding.root.isVisible = true
                binding.includeNative.shimmerContainerBanner.isVisible =true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if(viewType == ITEM_CODE) {
            val binding = ItemListHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            binding.thumbnail1.layoutParams.apply {
                height = (26f/20 * width).toInt()
            }

            binding.thumbnail2.layoutParams.apply {
                height = (26f/20 * width).toInt()
            }
            return HomeViewHolder(binding)
        } else {
            val binding = ItemListHomeAdsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            binding.root.layoutParams.apply {
                height = FrameLayout.LayoutParams.WRAP_CONTENT
            }
            return AdsViewHolder(binding)
        }
    }

    //(themes.count() / 2)  number of item theme
    //(((themes.count() - 2) / 4 ) + 1) number of item ads
    override fun getItemCount(): Int {
        return (themes.count() / 2) + (((themes.count() - 2) / 4 ) + 1)
    }

    override fun getItemViewType(position: Int): Int {
        Log.d("HomeAdapter2", "position ${position}" )
        if(!AdsUtils.adsEnable) {
            return ITEM_CODE
        }
        return if(position < 2) {
            ITEM_CODE
        }else {
            if((position - 2) % 4 == 0) {
                ADS_CODE
            } else {
                ITEM_CODE
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is HomeViewHolder) {

            if(position * 2 >= themes.size -1) {
                return
            }
            val theme1 = themes[position * 2]
            val theme2 = themes[position * 2 + 1]
            holder.bind(theme1, theme2)
        } else {
            val ads = AdsUtils.stackNative.removeFirstOrNull()
            Log.d("AdapterHome", "ads: ${ads}")
            if(ads == null) {
                isWaitingToLoadAds = true
            } else {
                isWaitingToLoadAds = false
            }
            val itemHolder = holder as AdsViewHolder
            itemHolder.bind(ads)
        }

    }

    companion object {
        const val ADS_CODE = 4000
        const val ITEM_CODE = 1000
    }
}