package com.parallax.hdvideo.wallpapers.ui.main.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.parallax.hdvideo.wallpapers.data.model.HashTag
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.databinding.Item4ImagesHomeFragmentBinding
import com.parallax.hdvideo.wallpapers.databinding.ItemListHomeFragmentBinding
import com.parallax.hdvideo.wallpapers.databinding.ItemRecyclerViewMainBinding
import com.parallax.hdvideo.wallpapers.databinding.NativeAdUnifiedBinding
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseAdapterList
import com.parallax.hdvideo.wallpapers.ui.list.AppScreen

class FeaturesAdapter(
    override var screenType: AppScreen = AppScreen.HOME,
    override var scrollListener: OnScrollListener? = null
) : MainFragmentAdapter(screenType, scrollListener) {

    //(View, Int, HashTags, Int) -> view holder, position, data, type = (3 items) || (4 pictures in 1)
    var onClickItemHashTagCallback: ((View, Int, HashTag, Int) -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        return if (position == amount && !shouldHiddenProgressBar) {
            LOADING_CODE
        } else {
            var result = position - posAdStart
            if (shouldHiddenAd || result < 0) {
                getModelViewType(getData(position))
            }
            else if (result > 0) {
                result %= (distanceAd + 1)
                if (result == 0) ADVERTISE_CODE else {
                    getModelViewType(getData(position))
                }
            } else {
                ADVERTISE_CODE
            }
        }
    }

    private fun getModelViewType(model: WallpaperModel) =
        model.listHashTags?.let {
            if (it.size == 3) COLLECTION_CODE else FOUR_IMAGES_CODE
        } ?: ITEM_CODE

    override fun onCreateView(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        return when (viewType) {
            ADVERTISE_CODE -> {
                val binding =
                    NativeAdUnifiedBinding.inflate(LayoutInflater.from(context), parent, false)
                NativeAdViewHolder(binding)
            }
            ITEM_CODE -> {
                val binding =
                    ItemRecyclerViewMainBinding.inflate(LayoutInflater.from(context), parent, false)
                setBackgroundColor(binding.cardView)
                MainFragmentViewHolder(binding)
            }
            COLLECTION_CODE -> {
                val binding = ItemListHomeFragmentBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
                setBackgroundColor(binding.cardView1)
                setBackgroundColor(binding.cardView2)
                setBackgroundColor(binding.cardView3)
                ItemCollectionHomeFragmentVH(binding)
            }
            FOUR_IMAGES_CODE -> {
                val binding = Item4ImagesHomeFragmentBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
                ItemFourImagesVH(binding)
            }
            else -> super.onCreateView(parent, viewType)
        }
    }

    override fun onBindView(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindView(holder, position)
        val model = getData(position)
        if (holder is ItemCollectionHomeFragmentVH) {
            val listHashTag = model.listHashTags ?: return
            if (listHashTag.size < 3) return
            val binding = holder.dataBinding
            binding.tv1.text = listHashTag[0].name
            binding.tv2.text = listHashTag[1].name
            binding.tv3.text = listHashTag[2].name
            val request = if (widthOfItem > 0) requestOptions
            else requestOptions.clone().override(widthOfItem, heightOfItem / 3)

            requestManagerInstance.load(listHashTag[0].toUrl(0)).apply(request)
                .into(binding.iv1)
            requestManagerInstance.load(listHashTag[1].toUrl(0)).apply(request)
                .into(binding.iv2)
            requestManagerInstance.load(listHashTag[2].toUrl(0)).apply(request)
                .into(binding.iv3)
            binding.cardView1.setOnClickListener {
                onClickItemHashTagCallback?.invoke(it, 0, listHashTag[0], COLLECTION_CODE)
            }
            binding.cardView2.setOnClickListener {
                onClickItemHashTagCallback?.invoke(it, 1, listHashTag[1], COLLECTION_CODE)
            }
            binding.cardView3.setOnClickListener {
                onClickItemHashTagCallback?.invoke(it, 2, listHashTag[2], COLLECTION_CODE)
            }
        }
        if (holder is ItemFourImagesVH) {
            val listHashTag = model.listHashTags ?: return
            val binding = holder.dataBinding
            val item = listHashTag.first()
            binding.tvTitle.text = item.name
            val request = if (widthOfItem > 0)
                requestOptions
             else
                requestOptions.clone().override(widthOfItem / 4, heightOfItem / 4)
            requestManagerInstance.load(item.toUrl(0)).apply(request)
                .into(binding.iv1)
            requestManagerInstance.load(item.toUrl(1)).apply(request)
                .into(binding.iv2)
            requestManagerInstance.load(item.toUrl(2)).apply(request)
                .into(binding.iv3)
            requestManagerInstance.load(item.toUrl(3)).apply(request)
                .into(binding.iv4)
            binding.root.setOnClickListener {
                onClickItemHashTagCallback?.invoke(it, 0, item, FOUR_IMAGES_CODE)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        getLayoutManager().spanSizeLookup = spanSizeLookup
    }

    private val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(pos: Int): Int {
            return when (getItemViewType(pos)) {
                ITEM_CODE, COLLECTION_CODE, FOUR_IMAGES_CODE -> {
                    return  1
                }
                LOADING_CODE -> {
                    spanCount
                }
                else -> spanCount
            }
        }
    }

    class ItemCollectionHomeFragmentVH(bd: ItemListHomeFragmentBinding) :
        BaseAdapterList.BaseViewHolder<WallpaperModel, ItemListHomeFragmentBinding>(bd) {
    }

    class ItemFourImagesVH(bd: Item4ImagesHomeFragmentBinding) :
        BaseAdapterList.BaseViewHolder<WallpaperModel, Item4ImagesHomeFragmentBinding>(bd) {
    }
}