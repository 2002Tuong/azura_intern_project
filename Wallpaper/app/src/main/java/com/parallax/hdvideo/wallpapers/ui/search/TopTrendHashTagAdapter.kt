package com.parallax.hdvideo.wallpapers.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.ads.AdsManager
import com.parallax.hdvideo.wallpapers.data.model.NativeAdModel
import com.parallax.hdvideo.wallpapers.data.model.HashTag
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.databinding.ItemListViewBinding
import com.parallax.hdvideo.wallpapers.databinding.ItemSearchFragmentRecyclerViewBinding
import com.parallax.hdvideo.wallpapers.databinding.ItemSearchThemeSuggestedBinding
import com.parallax.hdvideo.wallpapers.databinding.NativeAdUnifiedBinding
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseAdapterList
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseGridAdapter
import com.parallax.hdvideo.wallpapers.ui.collection.CollectionAdapter
import com.parallax.hdvideo.wallpapers.ui.main.fragment.NativeAdViewHolder
import io.reactivex.disposables.Disposable

class TopTrendHashTagAdapter(private val colorList : IntArray, var onClickColor: ((Int, HashTag)-> Unit)? = null) : BaseGridAdapter<HashTag>() {

    private var adapter: ColorAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var listColor: List<HashTag>? = null
    private val posAdStart
        get() = if (listColor.isNullOrEmpty()) {
            spanCount * 3
        } else {
            spanCount * 4
        }
    private var loadingAd = false
    private var disposableAds: Disposable? = null
    private var distanceAds: Int =  9 * spanCount
    private var adModel: NativeAdModel? = null
    private var hiddenAd: Boolean = AdsManager.isVipUser || !RemoteConfig.commonData.supportNative
    private val canLoadAd: Boolean
        get() = if (hiddenAd) false
        else adModel == null || adModel!!.amount > RemoteConfig.commonData.numberOfAdImpressions

    init {
        shouldHiddenProgressBar = true
    }

    override fun getItemViewType(position: Int): Int {
        return if (listColor != null) {
            if (position == 0 || position == 2) {
                TITLE_CODE
            } else if (position == 1) {
                SCROLL_VIEW_CODE
            } else {
                getSuggestionItem(position)
            }
        } else {
            if (position == 0) {
                TITLE_CODE
            } else {
                getSuggestionItem(position)
            }
        }
    }

    override fun getItemCount(): Int =
        if (listColor != null) amount + 2 else amount + 1

    override val amount: Int
        get() {
            val size = listData.size
            if (hiddenAd) return size
            val last = size - posAdStart
            return when {
                size == 0 -> 0
                last < 0 -> size
                else -> size + last / distanceAds + 1
            }
        }

    override fun onCreateView(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return  when (viewType) {
            ITEM_CODE -> {
                val binding = ItemSearchThemeSuggestedBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SearchSuggestionViewHolder(binding)
            }
            TITLE_CODE -> {
                val holder = CollectionAdapter.TitleViewHolder(
                    ItemListViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
                holder.dataBinding.parentView.setPadding(0,0,0,0)
                holder
            }
            ADVERTISE_CODE -> {
                val binding =
                    NativeAdUnifiedBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                NativeAdViewHolder(binding)
            }
            else -> {
                ColorListViewHolder(
                    ItemSearchFragmentRecyclerViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    private fun getSuggestionItem(position: Int): Int {
       return if (hiddenAd) ITEM_CODE
       else if (position == amount && !shouldHiddenProgressBar) {
            LOADING_CODE
        } else {
            var result = position - posAdStart
            if (result < 0) ITEM_CODE
            else if (result > 0) {
                result %= (distanceAds + 1)
                if (result == 0) ADVERTISE_CODE else ITEM_CODE
            } else {
                ADVERTISE_CODE
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        getLayoutManager().spanSizeLookup = spanSizeLookup
        val count = RemoteConfig.commonData.nativeAdCountTrend.toInt()
        if (count > spanCount) distanceAds = count
    }

    private val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(pos: Int): Int {
            return when (getItemViewType(pos)) {
                ITEM_CODE -> {
                    return 1
                }
                else -> spanCount
            }
        }
    }

    override fun onBindView(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchSuggestionViewHolder -> {
                val currentItem = getData(position)
                holder.dataBinding.apply {
                    imageView.setBackgroundColor(colorList[position % 10])
                    title.text = currentItem.name
                    when {
                        currentItem.couple -> {
                            requestManagerInstance.clear(imageView)
                            imageView.setImageDrawable(null)
                        }
                        else -> {
                            requestManagerInstance.load(currentItem.toUrl()).apply(requestOptions)
                                .into(imageView)
                        }
                    }
                    imageView.setOnClickListener {
                        onClickedItemCallback?.invoke(it, position, currentItem)
                    }
                }
            }
            is CollectionAdapter.TitleViewHolder -> {
                val binding = holder.dataBinding
                binding.tvSeeAll.isHidden = true
                binding.tvTitle.text = if (listColor == null) {
                    WallpaperApp.instance.getString(R.string.trending)
                } else {
                    if (position == 0) WallpaperApp.instance.getString(R.string.colors)
                    else WallpaperApp.instance.getString(R.string.trending)
                }
                binding.parentView.setOnClickListener(null)
            }
            is ColorListViewHolder -> {
                val binding = holder.dataBinding
                recyclerView = binding.recyclerView
                binding.recyclerView.layoutManager =
                    GridLayoutManager(WallpaperApp.instance, 1, GridLayoutManager.HORIZONTAL, false)
                adapter = ColorAdapter(layoutCallback = { R.layout.item_color_list_fragment },
                onBindViewCallback = { binding, hashTag, _ ->
                    binding.ivColor.setBackgroundColor(colorList[IntRange(0,9).random()])
                    requestManagerInstance.load(hashTag.toUrl(isMin = false, isThumb = false)).into(binding.ivColor)
                })
                listColor?.let { adapter?.setData(it) }
                binding.recyclerView.adapter = adapter
                adapter?.onClickedItemcallBack = onClickColor
            }
            is NativeAdViewHolder -> {
                val model = adModel
                val layoutParams = holder.dataBinding.rootView.layoutParams
                if (model != null) {
                    model.amount += 1
                    holder.dataBinding.rootView.isHidden = false
                    if (layoutParams.height != ViewGroup.LayoutParams.WRAP_CONTENT)
                        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    holder.onBind(model.nativeAd)
                } else {
                    holder.dataBinding.rootView.isHidden = true
                    if (layoutParams.height != 0) layoutParams.height = 0
                }
            }
        }
    }

    fun loadAds() {
        if (loadingAd || !canLoadAd) return
        loadingAd = true
        disposableAds?.dispose()
        disposableAds = AdsManager.loadNativeAd(AdsManager.KEY_NATIVE_AD)?.subscribe({
            loadingAd = false
            val model = adModel
            adModel = NativeAdModel(it, id = System.currentTimeMillis())
            notifyItemChanged(findPositionAdView())
            model?.nativeAd?.destroy()
        }, {
            loadingAd = false
        })
    }

    private fun findPositionAdView(): Int {
        val lastPos = getLayoutManager().findLastVisibleItemPosition()
        var res = lastPos - posAdStart
        return if (res <= 0) posAdStart
        else {
            res -= res % (distanceAds + 1)
            res + posAdStart
        }
    }

    private fun getAdVisible(positionInRecyclerView: Int): Int {
        if (hiddenAd) return 0
        val pos = positionInRecyclerView - posAdStart
        return if (pos < 0) 0 else pos / (distanceAds + 1) + 1
    }

    fun setDataColorAdapter(list: List<HashTag>) {
        if (list.isNotEmpty()) {
            listColor = list
        }
    }

    override fun scrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            loadAds()
        }
    }

    override fun getData(position: Int): HashTag {
        val offset = if (listColor.isNullOrEmpty()) 1 else 3
        return super.getData(position - getAdVisible(position) - offset)
    }

    override fun release() {
        removeAll()
        adModel?.nativeAd?.destroy()
        disposableAds?.dispose()
        adModel = null
        super.release()
    }

    fun refreshAdapterIfNeed() {
        if (getRecyclerView() != null) {
            val isHideAd = AdsManager.isVipUser || !RemoteConfig.commonData.supportNative
            val amount = RemoteConfig.commonData.nativeAdCount.toInt()
            val distanceAd = if (amount > spanCount) amount else 8 * spanCount
            if (isHideAd != this.hiddenAd || distanceAd != this.distanceAds) {
                this.hiddenAd = isHideAd
                this.distanceAds = distanceAd
                notifyDataSetChanged()
            }
        }
    }
    class SearchSuggestionViewHolder(binding: ItemSearchThemeSuggestedBinding) :
            BaseAdapterList.BaseViewHolder<WallpaperModel, ItemSearchThemeSuggestedBinding>(binding)

    class ColorListViewHolder(binding: ItemSearchFragmentRecyclerViewBinding) :
        BaseAdapterList.BaseViewHolder<WallpaperModel, ItemSearchFragmentRecyclerViewBinding>(binding)
}