package com.parallax.hdvideo.wallpapers.ui.main.fragment

import android.animation.Animator
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.ads.AdsManager
import com.parallax.hdvideo.wallpapers.data.model.NativeAdModel
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.databinding.ItemRecyclerViewMainBinding
import com.parallax.hdvideo.wallpapers.databinding.NativeAdUnifiedBinding
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.extension.toGlideUrl
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseAdapterList
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseGridAdapter
import com.parallax.hdvideo.wallpapers.ui.custom.ExoPlayVideo
import com.parallax.hdvideo.wallpapers.ui.list.AppScreen
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.dp2Px
import com.parallax.hdvideo.wallpapers.utils.dpToPx
import com.parallax.hdvideo.wallpapers.utils.image.CropBitmapTransformation
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper
import io.reactivex.disposables.Disposable
import java.io.File
import java.util.Random
import kotlin.math.max
import kotlin.math.min


open class MainFragmentAdapter(
    open var screenType: AppScreen = AppScreen.HOME,
    open var scrollListener: OnScrollListener? = null): BaseGridAdapter<WallpaperModel>() {

    private val handler = Handler(Looper.getMainLooper())
    private var nativeAdModel: NativeAdModel? = null
    val posAdStart: Int get() = spanCount * 2
    protected var distanceAd: Int =  8 * spanCount
    override var enabled: Boolean = false
    var shouldHiddenAd: Boolean = AdsManager.isVipUser || !RemoteConfig.commonData.supportNative
    private val random = Random()
    var onClickTopButtonCallback: ((WallpaperModel) -> Unit)? = null
    var onClickDeleteButtonCallback: ((WallpaperModel) -> Unit)? = null
    private var highestPos = 0

    private val maximumVideoOnScreen = 2
    private lateinit var exoPlayer: Array<ExoPlayVideo>
    private var latestScrollOffset = 0
    private var scrolledToBottom = false
    private val thresholdDistance = -dp2Px(50f)
    var isPlayFirstVideo = false
    var isAutoPlayVideo = false
    private var allowPlayVideo = true
    private var loadingAd = false
    private var disposableAds: Disposable? = null
    private var loadedNativeAdCount = 0
    private var loadedInterstitialAd = false
    override val amount: Int
        get() {
            val size = listData.size
            if (shouldHiddenAd) return size
            val last = size - posAdStart
            return when {
                size == 0 -> 0
                last < 0 -> size
                else -> size + last / distanceAd + 1
            }
        }

    override fun getItemViewType(position: Int): Int {
        if (shouldHiddenAd) return super.getItemViewType(position)
        return if (position == amount && !shouldHiddenProgressBar) {
            LOADING_CODE
        } else {
            var result = position - posAdStart
            if (result < 0) ITEM_CODE
            else if (result > 0) {
                result %= (distanceAd + 1)
                if (result == 0) ADVERTISE_CODE else ITEM_CODE
            } else {
                ADVERTISE_CODE
            }
        }
    }

    override fun onCreateView(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        return if (viewType == ADVERTISE_CODE) {
            val binding = NativeAdUnifiedBinding.inflate(LayoutInflater.from(context), parent, false)
            NativeAdViewHolder(binding)
        } else {
            val binding = ItemRecyclerViewMainBinding.inflate(LayoutInflater.from(context), parent, false)
            if (screenType == AppScreen.DOWNLOAD) {
                binding.favoriteStatus.setImageResource(R.drawable.ic_delete_re)
                binding.favoriteStatus.isHidden = false
                binding.ivDelete.isHidden = false
            } else if (screenType == AppScreen.FAVORITE) {
                binding.favoriteStatus.isHidden = false
                binding.ivDelete.isHidden = true
            }
            setBackgroundColor(binding.cardView)
            MainFragmentViewHolder(binding)
        }
    }

    override fun onBindView(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MainFragmentViewHolder) {
            val model = getData(position)
            val totalWall = getPositionInListData(position) + 1
            if (highestPos < totalWall) highestPos = totalWall
            val binding = holder.dataBinding
            when {
                model.isVideo -> {
                    binding.latTypePhotoView.layoutParams = FrameLayout.LayoutParams(dpToPx(28f),dpToPx(28f))
                    binding.statusLivePhotoView.isHidden = true
                    binding.latTypePhotoView.isHidden = false
                    binding.statusLivePhotoView.isHidden = true
                    binding.latTypePhotoView.setAnimation(R.raw.live_animation)
                }
                model.is4DImage -> {
                    binding.latTypePhotoView.layoutParams = FrameLayout.LayoutParams(dpToPx(34f),dpToPx(34f))
                    binding.statusLivePhotoView.isHidden = true
                    binding.latTypePhotoView.isHidden = false
                    //binding.latTypePhotoView.setAnimation(R.raw.square_anim)

                    Glide.with(holder.itemView.context)
                        .asGif()
                        .load(R.raw.square_animation)
                        .into(binding.latTypePhotoView)
                }
                else -> {
                    binding.statusLivePhotoView.isHidden = true
                    binding.latTypePhotoView.isHidden = true
                }
            }

            if (model.isFavorite) {
                binding.favoriteStatus.setImageResource(R.drawable.ic_fav_re)
            } else {
                binding.favoriteStatus.setImageResource(R.drawable.ic_no_fav_re)
            }

            binding.favoriteStatus.setOnClickListener {
                if (!model.isFavorite) {
                    onClickTopButtonCallback?.invoke(model)
                    binding.favoriteStatus.setImageResource(R.drawable.ic_fav_re)
                    if (screenType != AppScreen.DOWNLOAD && screenType != AppScreen.FAVORITE) {
                        holder.startAnimationFavorite()
                    }
                } else {
                    onClickTopButtonCallback?.invoke(model)
                    binding.favoriteStatus.setImageResource(R.drawable.ic_no_fav_re)
                    holder.cancelAnimationFavorite()
                }
            }

            binding.ivDelete.setOnClickListener {
                onClickDeleteButtonCallback?.invoke(model)
            }

            binding.cardView.setOnClickListener {
                onClickedItemCallback?.invoke(binding.root, position, model)
            }
            val tempUrl = model.toUrl()
            val url: Any = if (model.isFromLocalStorage) tempUrl else tempUrl.toGlideUrl
            requestManagerInstance.load(Uri.parse(tempUrl))
                .apply(
                    if (model.canCropImage) {
                        RequestOptions.bitmapTransform(
                            CropBitmapTransformation(
                                widthOfItem, heightOfItem,
                                CropBitmapTransformation.CropType.CENTER
                            )
                        )
                    } else requestOptions
                )
                .error(requestManagerInstance.load(model.getUrlFailMin(tempUrl)))
                .into(binding.imageView)
        } else if (holder is NativeAdViewHolder) {
            val model = nativeAdModel
            val params = holder.dataBinding.rootView.layoutParams

            if (model != null) {
                model.amount += 1
                holder.dataBinding.rootView.isHidden = false
                if (params.height != ViewGroup.LayoutParams.WRAP_CONTENT)
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                holder.onBind(model.nativeAd)
            } else {
                holder.dataBinding.rootView.isHidden = true
                if (params.height != 0) params.height = 0
            }
            Logger.d("UnifiedNativeAdViewHolder", model?.amount)
        }
    }

    protected fun setBackgroundColor(view: CardView) {
        val color = Color.argb(180, random.nextInt(256), random.nextInt(256), random.nextInt(256))
        view.setCardBackgroundColor(color)
    }

    override fun getData(position: Int): WallpaperModel {
        return super.getData(position - getAdVisible(position))
    }

    override fun getDataOrNull(position: Int): WallpaperModel? {
        return super.getDataOrNull(getPositionInListData(position))
    }

    fun getPositionInListData(positionInRecyclerView: Int): Int {
        val viewType = getItemViewType(positionInRecyclerView)
        return if (viewType == ITEM_CODE || viewType == FOUR_IMAGES_CODE || viewType == COLLECTION_CODE)
            positionInRecyclerView - getAdVisible(positionInRecyclerView)
        else -getAdVisible(positionInRecyclerView)
    }

    private fun getAdVisible(positionInRecyclerView: Int): Int {
        if (shouldHiddenAd) return 0
        val pos = positionInRecyclerView - posAdStart
        return if (pos < 0) 0 else pos / (distanceAd + 1) + 1
    }

    private fun findPositionAdView(): Int {
        val lastPos = getLayoutManager().findLastVisibleItemPosition()
        var res = lastPos - posAdStart
        return if (res <= 0) posAdStart
        else {
            res -= res % (distanceAd + 1)
            res + posAdStart
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        try {
            if (holder is MainFragmentViewHolder) {
                requestManagerInstance.clear(holder.dataBinding.imageView)
                holder.dataBinding.imageView.setImageDrawable(null)
                holder.dataBinding.latTypePhotoView.isHidden = true
                holder.exoPlayVideo?.removeFromParent()
                holder.exoPlayVideo = null
                holder.cancelAnimationFavorite()
            }
        } catch (e: Exception) {

        }
    }

    private fun findViewHolder(positionInRecyclerView: Int): MainFragmentViewHolder? {
        return getRecyclerView()?.findViewHolderForAdapterPosition(positionInRecyclerView) as? MainFragmentViewHolder
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val count = RemoteConfig.commonData.nativeAdCount.toInt()
        distanceAd = if (count > spanCount) count else 8 * spanCount
        onScrollListener = scrollListener
        setupVideo(recyclerView.context)
    }

    fun refreshAdapterIfNeed() {
        if (getRecyclerView() != null) {
            val isHiddenAd = AdsManager.isVipUser || !RemoteConfig.commonData.supportNative
            val count = RemoteConfig.commonData.nativeAdCount.toInt()
            val distanceAds = if (count > spanCount) count else 8 * spanCount
            if (isHiddenAd != this.shouldHiddenAd || distanceAds != this.distanceAd) {
                this.shouldHiddenAd = isHiddenAd
                this.distanceAd = distanceAds
                notifyDataSetChanged()
            }
        }
    }

    override fun release() {
        handler.removeCallbacksAndMessages(null)
        removeAll()
        releaseVideo()
        nativeAdModel?.nativeAd?.destroy()
        disposableAds?.dispose()
        nativeAdModel = null
        super.release()
    }

    override fun scrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        downloadVideo(800)
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            loadAds()
        }
    }

    class MainFragmentViewHolder(bd: ItemRecyclerViewMainBinding) :
        BaseAdapterList.BaseViewHolder<WallpaperModel, ItemRecyclerViewMainBinding>(bd) {
        var exoPlayVideo: ExoPlayVideo? = null

        fun cancelAnimationFavorite() {
            dataBinding.ivFavoriteAnimation.cancelAnimation()
            dataBinding.ivFavoriteAnimation.isHidden = true
        }

        fun startAnimationFavorite() {
            dataBinding.ivFavoriteAnimation.visibility = View.VISIBLE
            dataBinding.ivFavoriteAnimation.playAnimation()
            dataBinding.ivFavoriteAnimation.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    handleFadeOutImage()
                }

                override fun onAnimationCancel(animation: Animator) {
                }

                override fun onAnimationRepeat(animation: Animator) {
                }
            })
        }

        private fun handleFadeOutImage() {
            val anim = AlphaAnimation(1.0f, 0.0f)
            anim.duration = 1000
            anim.repeatMode = Animation.REVERSE
            dataBinding.ivFavoriteAnimation.startAnimation(anim)
            dataBinding.ivFavoriteAnimation.visibility = View.GONE
        }
    }

    override fun removeAll() {
        lastVisibleItemIndex = 0
        super.removeAll()
    }

    private val canLoadAds: Boolean get() = if (shouldHiddenAd) false
        else nativeAdModel == null || nativeAdModel!!.amount > RemoteConfig.commonData.numberOfAdImpressions

    fun loadAds() {
        return
        if (loadingAd || !canLoadAds) return
        loadingAd = true
        loadedNativeAdCount++
        disposableAds?.dispose()
        disposableAds = AdsManager
            .loadNativeAd(AdsManager.KEY_NATIVE_AD)?.let { rx ->
                rx.doFinally {
                    loadingAd = false
                    //Nếu tải thất bại thì reload ads 2 lần
                    if (loadedNativeAdCount < 3 && nativeAdModel == null) {
                        loadAds()
                    } else if (!loadedInterstitialAd) {
                        loadedInterstitialAd = true
                        AdsManager.loadInterstitialAd()
                    }
                }.subscribe({
                        val model = nativeAdModel
                        nativeAdModel = NativeAdModel(it, id = System.currentTimeMillis())
                        notifyItemChanged(findPositionAdView())
                        model?.nativeAd?.destroy()
                        Logger.d("loadAds succeed = $it")
                    }, {
                        Logger.d("loadAds error")
                    })
            }

    }

    override fun setData(data: List<WallpaperModel>) {
        super.setData(data)
        isPlayFirstVideo = false
        if (data.size >= posAdStart - 1) loadAds()
    }

    interface OnScrollListener : BaseGridAdapter.OnScrollListener {
        fun downloadVideo(model: WallpaperModel) {

        }
    }

    //region Play Video

    fun downloadVideo(delay: Long = 0) {
        if (this::exoPlayer.isInitialized && isAutoPlayVideo) {
            removeVideoView()
            allowPlayVideo = true
            handler.removeCallbacks(downloadVideoRunnable)
            handler.postDelayed(downloadVideoRunnable, delay)
        }
    }

    fun playVideo() {
        if (isAutoPlayVideo && allowPlayVideo) {
            handler.removeCallbacks(playVideoRunnable)
            handler.postDelayed(playVideoRunnable, 300)
        }
    }

    fun stopVideo(animate: Boolean = true) {
        allowPlayVideo = false
        if (this::exoPlayer.isInitialized) {
            handler.removeCallbacks(playVideoRunnable)
            handler.removeCallbacks(downloadVideoRunnable)
            exoPlayer.forEach { it.removeFromParent(animate) }
        }
    }

    private fun releaseVideo() {
        if (this::exoPlayer.isInitialized) {
            exoPlayer.forEach { it.release() }
        }
    }

    private fun setupVideo(context: Context) {
        if (!WallpaperHelper.isSupportLiveWallpaper || maximumVideoOnScreen <= 0) return
        if (this::exoPlayer.isInitialized || screenType != AppScreen.HOME) return
        exoPlayer = Array(maximumVideoOnScreen) {  ExoPlayVideo(context) }
    }

    private fun findPositionVideo(recyclerView: RecyclerView, isLoadData: Boolean) : List<Pair<WallpaperModel, MainFragmentAdapter.MainFragmentViewHolder>> {
        val listPos = mutableListOf<Pair<WallpaperModel, MainFragmentViewHolder>>()
        var lastPos = getLayoutManager().findLastVisibleItemPosition()
        var firstPos = getLayoutManager().findFirstVisibleItemPosition()
        if (lastPos == RecyclerView.NO_POSITION
            || firstPos == RecyclerView.NO_POSITION) return listPos
        if (scrolledToBottom) {
            lastPos = if (isLoadData) min(amount, lastPos + spanCount * 2) else lastPos
            while (lastPos >= firstPos) {
                val model = getDataOrNull(firstPos)
                if (model?.isVideo == true) {
                    if (isLoadData) {
                        scrollListener?.downloadVideo(model)
                    }
                    val vh = findViewHolder(firstPos)
                    if (vh != null && canPlayVideo(recyclerView, vh.itemView)) {
                        if (listPos.firstOrNull { it.second.exoPlayVideo == vh.exoPlayVideo } != null) {
                            vh.exoPlayVideo = null
                        }
                        listPos.add(model to vh)
                    }
                }
                firstPos ++
            }
        } else {
            firstPos = if (isLoadData) max(0, firstPos - spanCount * 2) else firstPos
            while (firstPos <= lastPos) {
                val model = getDataOrNull(lastPos)
                if (model?.isVideo == true) {
                    if (isLoadData) {
                        scrollListener?.downloadVideo(model)
                    }
                    val vh = findViewHolder(lastPos)
                    if (vh != null && canPlayVideo(recyclerView, vh.itemView)) {
                        if (listPos.firstOrNull { it.second.exoPlayVideo == vh.exoPlayVideo } != null) {
                            vh.exoPlayVideo = null
                        }
                        listPos.add(model to vh)
                    }
                }
                lastPos --
            }
        }
        return listPos
    }

    fun logTrackingTotalWall(screenType: AppScreen, isSearchHashtag: Boolean = false) {
        if (highestPos <= 0) return
        val pairData = if (isSearchHashtag) {
            "screenType" to "hashtag"
        } else "screenType" to screenType.name
        highestPos = 0
    }

    private fun canPlayVideo(itemView: View): Boolean {
        val recyclerView = getRecyclerView() ?: return false
        return canPlayVideo(recyclerView, itemView)
    }

    private fun canPlayVideo(recyclerView: RecyclerView, itemView: View): Boolean {
        val locationOnScreen = IntArray(2)
        itemView.getLocationOnScreen(locationOnScreen)
        val bottom = locationOnScreen[1] + itemView.height / 2
        return bottom <= recyclerView.height - thresholdDistance && locationOnScreen[1] >= thresholdDistance
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (!isPlayFirstVideo && isAutoPlayVideo && allowPlayVideo) {
            isPlayFirstVideo = true
            downloadVideo(200)
        }
    }

    private val downloadVideoRunnable = Runnable {
        val recyclerView = getRecyclerView()
        if (isAutoPlayVideo && recyclerView != null) {
            val scrolled = recyclerView.computeVerticalScrollOffset()
            scrolledToBottom = scrolled >= latestScrollOffset
            latestScrollOffset = scrolled
            val array = findPositionVideo(recyclerView, true)
            playListVideos(array)
        }
    }

    private val playVideoRunnable = Runnable {
        val recyclerView = getRecyclerView()
        if (this::exoPlayer.isInitialized && recyclerView != null) {
            val array = findPositionVideo(recyclerView, false)
            playListVideos(array)
        }
    }

    private fun playListVideos(array : List<Pair<WallpaperModel, MainFragmentViewHolder>>) {
        if (!isAutoPlayVideo || !allowPlayVideo) return
        val size = array.size
        if (size > 0) {
            val to = max(size - maximumVideoOnScreen, 0)
            for (i in size - 1 downTo to) {
                val data = array[i]
                val path = data.first.pathCacheVideo
                if (path != null) {
                    val file = File(path)
                    val vh = data.second
                    val view = vh.dataBinding.cardView
                    var exoPlay = vh.exoPlayVideo
                    if (exoPlay != null) {
                        exoPlay.play(view, file)
                    } else {
                        exoPlay = exoPlayer.firstOrNull { it.parentView == null }
                        exoPlay?.play(view, file)
                        vh.exoPlayVideo = exoPlay
                    }
                }
            }
        }
    }

    fun scrollToItemPosition(position: Int) {
        val pos = getPositionInRecyclerView(position)
        if (pos >= 0)
            getRecyclerView()?.scrollToPosition(pos)
    }

    private fun getPositionInRecyclerView(positionInListData: Int): Int {
        if (shouldHiddenAd) return positionInListData
        val last = positionInListData - posAdStart
        return when {
            positionInListData == 0 -> 0
            last < 0 -> positionInListData
            else -> positionInListData + last / distanceAd + 1
        }
    }

    fun findItemPosition(item: WallpaperModel): Int {
        return listData.lastIndexOf(item)
    }

    private fun findItemPositionInListData(item: WallpaperModel): Int {
        return listData.indexOfFirst {
            it.id == item.id
        }
    }

    fun updateFavoriteWallpaper(wallpaperModel: WallpaperModel) {
        val posInListData = findItemPositionInListData(wallpaperModel)
        val posInRecyclerView = getPositionInRecyclerView(posInListData)
        if (posInListData < 0 || posInRecyclerView < 0) return
        listData[posInListData] = wallpaperModel
        val holder = findViewHolder(posInRecyclerView)
        if (wallpaperModel.isFavorite) {
            holder?.dataBinding?.favoriteStatus?.setImageResource(R.drawable.ic_fav_re)
        } else {
            holder?.dataBinding?.favoriteStatus?.setImageResource(R.drawable.ic_no_fav_re)
        }
    }

    fun findPositionItemInRecyclerView(wallpaperModel: WallpaperModel): Int {
        val posInListData = findItemPositionInListData(wallpaperModel)
        val posInRecyclerView = getPositionInRecyclerView(posInListData)
        if (posInListData < 0 || posInRecyclerView < 0) return -1
        return posInRecyclerView
    }

    private fun removeVideoView() {
        val rv = getRecyclerView() ?: return
        exoPlayer.forEach {
            it.parentView?.also {view ->
                if (!canPlayVideo(rv, view)) {
                    it.removeFromParent()
                    val vh = rv.findContainingViewHolder(view) as? MainFragmentViewHolder
                    vh?.exoPlayVideo = null
                }
            }
        }
    }

    //endregion

}