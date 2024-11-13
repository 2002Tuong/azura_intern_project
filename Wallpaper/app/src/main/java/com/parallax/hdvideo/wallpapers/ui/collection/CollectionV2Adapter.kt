package com.parallax.hdvideo.wallpapers.ui.collection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.ads.AdsManager
import com.parallax.hdvideo.wallpapers.data.model.NativeAdModel
import com.parallax.hdvideo.wallpapers.data.model.Category
import com.parallax.hdvideo.wallpapers.databinding.ItemListBinding
import com.parallax.hdvideo.wallpapers.databinding.NativeAdUnifiedBinding
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.extension.margin
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseAdapterList
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseGridAdapter
import com.parallax.hdvideo.wallpapers.ui.main.fragment.NativeAdViewHolder
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.other.GlideSupport
import io.reactivex.disposables.Disposable

class CollectionV2Adapter : BaseGridAdapter<Category>() {

    var onClickItemCallback: ((Int, Category, View) -> Unit)? = null
    private var nativeAdModel: NativeAdModel? = null
    private var loadingAd = false
    private var disposableAds: Disposable? = null

    override val amount: Int
        get() = listData.size

    override fun getItemViewType(position: Int): Int {
        return if (listData[position].id == Category.CATEGORY_ID_ADVERTISE) ADVERTISE_CODE else COLLECTION_CODE
    }


    override fun onCreateView(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ADVERTISE_CODE -> {
                val binding =
                    NativeAdUnifiedBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                binding.rootView.margin(20f, 20f, 20f, 20f)
                NativeAdViewHolder(binding)
            }
            else -> {
                CollectionViewHolder(
                    ItemListBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindView(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CollectionViewHolder) {
            val category = listData[position]
            holder.binData(category)
        } else {
            val viewHolder = (holder as NativeAdViewHolder)
            val adModel = nativeAdModel
            val params = viewHolder.dataBinding.rootView.layoutParams
            viewHolder.dataBinding.rootView.isHidden = (adModel == null)
            if (adModel != null) {
                adModel.amount = adModel.amount + 1
                viewHolder.dataBinding.rootView.margin(20f, 20f, 20f, 20f)
                if (params.height != ViewGroup.LayoutParams.WRAP_CONTENT)
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                viewHolder.onBind(adModel.nativeAd)
            } else {
                if (params.height != 0) params.height = 0
                viewHolder.dataBinding.rootView.margin(0f, 0f, 0f, 0f)
            }
        }
    }

    class CollectionViewHolder constructor(binding: ItemListBinding) :
        BaseAdapterList.BaseViewHolder<Category, ItemListBinding>(binding) {

        fun binData(category: Category) {
            dataBinding.tvCollection.text = category.name
            GlideSupport.load(
                dataBinding.ivCollection,
                WallpaperURLBuilder.shared.getFullStorage().plus(category.url)
            )
            if (category.isVideo) {
                GlideSupport.load(dataBinding.ivCollection, R.drawable.bg_cate_live_wallpaper_re)
            }

        }
    }

    private val canLoadAds: Boolean get() = (nativeAdModel == null || nativeAdModel!!.amount > RemoteConfig.commonData.numberOfAdImpressions)

    private fun loadAds() {
        if (loadingAd || !canLoadAds) return
        loadingAd = true
        disposableAds?.dispose()
        disposableAds = AdsManager
            .loadNativeAd(AdsManager.KEY_NATIVE_AD)?.let { rx ->
                rx.doFinally {
                    loadingAd = false
                    loadAds()
                }.subscribe({
                    val model = nativeAdModel
                    na`tiveAdModel = NativeAdModel(it, id = System.currentTimeMillis())
                    model?.nativeAd?.destroy()
                    Logger.d("loadAds succeed = $it")
                }, {
                    Logger.d("loadAds error")
                })
            }

    }

    override fun scrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            loadAds()
        }
    }

    override fun release() {
        super.release()
        nativeAdModel?.nativeAd?.destroy()
        nativeAdModel = null
        disposableAds?.dispose()
    }

}