package com.parallax.hdvideo.wallpapers.ui.details

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.parallax.hdvideo.wallpapers.data.model.ParallaxModelWrapper
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.databinding.ItemDetailsViewPagerBinding
import com.parallax.hdvideo.wallpapers.databinding.ItemParallaxViewBinding
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.extension.toGlideUrl
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.ui.custom.ExoPlayVideo
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.dpToPx
import com.parallax.hdvideo.wallpapers.utils.file.FileUtils
import com.parallax.hdvideo.wallpapers.utils.image.CropBitmapTransformation
import com.parallax.hdvideo.wallpapers.utils.other.RequestListenerModel
import com.techpro.parallax.wallpaper.gl.ParallaxSurfaceView
import com.techpro.parallax.wallpaper.gl.Set4DImageCallBack
import com.techpro.parallax.wallpaper.model.ParallaxModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.set

@SuppressLint("NotifyDataSetChanged")
class DetailV2Adapter(
    var downloadVideo: (position: Int, model: WallpaperModel) -> Disposable?,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val listWall = mutableListOf<WallpaperModel>()
    private val attached = mutableMapOf<Int, ViewHolderCommon>()
    var keepPos = -1
    var currentPos = 0
    private var _context: Context? = null
    var onRenderFirstFrameCallback: (() -> Unit)? = null
    var startPos = 0
    private val handler = Handler(Looper.getMainLooper())
    lateinit var requestManager: RequestManager
    private var canPlay = false
    private val calendar = Calendar.getInstance()
    var itemWidthValue = AppConfiguration.widthScreenValue
    var itemHeightValue = AppConfiguration.heightScreenValue
    private var thumbnailWidthValue = 0
    private var thumbnailHeightValue = 0
    private var exoPlayerVideo: ExoPlayVideo? = null
    private var disposableAd: Disposable? = null
    var firstTimeIntroShowed = false
    private var intentReceiver: BroadcastReceiver? = null
    var currentScreenState = PopupWindowAdapter.ScreenState.PREVIEW_IMAGE
    var onClickedItemCallback: ((Int, WallpaperModel) -> Unit)? = null
    private val totalViewHolders: List<ViewHolderCommon> get() = attached.values.toList()
    val context get() = _context

    private lateinit var load4DImageSuccessCallback: (boolean: Boolean) -> Unit
    var doingLoad4DImage = false

    init {
        thumbnailWidthValue = dpToPx(116f)
        thumbnailHeightValue = (thumbnailWidthValue * AppConfiguration.aspectRatio).toInt()
    }

    override fun getItemViewType(position: Int): Int {
        return if (getData(position).is4DImage) ITEM_VIEW_PARALLAX else ITEM_VIEW_IMAGE_VIDEO
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == ITEM_VIEW_IMAGE_VIDEO) {
            val binding = ItemDetailsViewPagerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            DetailV2ViewHolder(binding)
        } else {
            val binding =
                ItemParallaxViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ParallaxViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getData(position).is4DImage) {
            (holder as ParallaxViewHolder).bindView(position)
        } else {
            (holder as DetailV2ViewHolder).bindView(position)
        }
    }

    fun visibleParallaxView() {
        val viewHolder = attached[currentPos] ?: return
        viewHolder as ParallaxViewHolder
        viewHolder.dataBinding.parallaxViewContainer.isHidden = false
        viewHolder.dataBinding.imageView.isHidden = false
        viewHolder.dataBinding.progressBar.isHidden = false
        viewHolder.dataBinding.parallaxViewContainer.removeAllViews()

        val parallaxView = ParallaxSurfaceView(context)
        val wallModel = getData(currentPos)

        doingLoad4DImage = true

        loadParallax(wallModel, viewHolder.listImage)
        parallaxView.setImage(viewHolder.listImage, false)

        viewHolder.dataBinding.parallaxViewContainer.addView(parallaxView)
        parallaxView.set4DImageSuccessListener(object : Set4DImageCallBack {
            override fun loadImageSuccess() {
                doingLoad4DImage = false

                CoroutineScope(Dispatchers.Main).launch {
                    parallaxView.onResume()
                    load4DImageSuccessCallback.invoke(true)
                    viewHolder.dataBinding.progressBar.isHidden = true
                }
            }

            override fun loadImageError() {
                doingLoad4DImage = false

                CoroutineScope(Dispatchers.Main).launch {
                    viewHolder.dataBinding.parallaxViewContainer.isHidden = true
                    load4DImageSuccessCallback.invoke(false)
                    parallaxView.onPause()

                    viewHolder.dataBinding.imageView.isHidden = false
                    viewHolder.dataBinding.progressBar.isHidden = true
                }
            }
        })

        remove4DBehindAndBeforeCurrentItem()
    }

    fun onLoad4DImageSuccessListener(load4DImageSuccess: (boolean: Boolean) -> Unit) {
        this.load4DImageSuccessCallback = load4DImageSuccess
    }

    fun cancelLoad4D(){
        val viewHolder1 = attached[currentPos-1] ?: return
        if (viewHolder1 is ParallaxViewHolder) {
            val view = viewHolder1.dataBinding.parallaxViewContainer.getChildAt(0)
            if (view is ParallaxSurfaceView) {
       //         view.cancelLoad4D()
            }
        }
    }

    private fun loadParallax(model: WallpaperModel, listImages: MutableList<ParallaxModel>) {
        val count = model.count?.toIntOrNull() ?: return
        val configParam = model.configParam!!.split(",")

        listImages.clear()
        val domainParallax = getDomainParallax4D(model)
        val bgModel = domainParallax + AppConstants.IMAGE_BACKGROUND_NAME

        listImages.add(ParallaxModel(bgModel))
        (1..count).forEach {
            val url = domainParallax + "${AppConstants.IMAGE_LAYER}$it.png"
            val parallaxModel = ParallaxModelWrapper(url)
            parallaxModel.setValue(configParam[it - 1].toFloat())
            listImages.add(parallaxModel)
        }
    }

     fun getDomainParallax4D(model: WallpaperModel): String {
        return try {
            val originBackgroundUrl =
                WallpaperURLBuilder.shared.getFullStorage().plus("parallax/").plus(model.url)
                    .plus(AppConstants.IMAGE_BACKGROUND_NAME)
            val bitmap = Glide.with(context!!)
                .asBitmap()
                .load(originBackgroundUrl)
                .submit()
                .get()
            if (bitmap != null)
                WallpaperURLBuilder.shared.getFullStorage(false).plus("parallax/").plus(model.url)
            else WallpaperURLBuilder.shared.getFullStorage().plus("parallax/").plus(model.url)
        } catch (e: Exception) {
            WallpaperURLBuilder.shared.getFullStorage().plus("parallax/").plus(model.url)
        }
    }

    fun remove4DBehindAndBeforeCurrentItem() {
        val viewHolder1 = attached[currentPos - 1] ?: return
        if (viewHolder1 is ParallaxViewHolder) {

            viewHolder1.dataBinding.parallaxViewContainer.removeAllViews()
            viewHolder1.dataBinding.imageView.isHidden = false

//            CoroutineScope(Dispatchers.IO).launch {
//                val view = vh1.dataBinding.parallaxViewContainer.getChildAt(0)
//                if (view is ParallaxSurfaceView) {
//                    view.clearSensorListener()
//                }
//                Log.d("ParallaxSufface", "remove Listener")
//                vh1.dataBinding.parallaxViewContainer.removeAllViews()
//                vh1.dataBinding.imageView.isHidden = false
//            }
        }

        val viewHolder2 = attached[currentPos + 1] ?: return
        if (viewHolder2 is ParallaxViewHolder) {
            viewHolder2.dataBinding.parallaxViewContainer.removeAllViews()
            viewHolder2.dataBinding.imageView.isHidden = false

//            CoroutineScope(Dispatchers.IO).launch {
//                val view = vh2.dataBinding.parallaxViewContainer.getChildAt(0)
//                if (view is ParallaxSurfaceView) {
//                    view.clearSensorListener()
//                }
//                Log.d("ParallaxSufface", "remove Listener")
//                vh2.dataBinding.parallaxViewContainer.removeAllViews()
//                vh2.dataBinding.imageView.isHidden = false
//            }
        }
    }

    fun unRegisterSensor() {
        val viewHolder = attached[currentPos] ?: return
        if (viewHolder is ParallaxViewHolder) {
            val view = viewHolder.dataBinding.parallaxViewContainer.getChildAt(0)
            if (view is ParallaxSurfaceView) {
                view.onPause()
            }
        }
    }

    fun clearViewParallax4D() {
        val viewHolder = attached[currentPos] ?: return
        viewHolder as ParallaxViewHolder
        viewHolder.dataBinding.parallaxViewContainer.removeAllViews()
        viewHolder.dataBinding.imageView.isHidden = false
    }

    fun playCurrentParallax(model: WallpaperModel) {
        val viewHolder = getViewHolderCard(currentPos) ?: return
        val listImages = model.pathCacheParallaxPath?.let { FileUtils.getAllImages(it) }
        val listImagesModel = mutableListOf<ParallaxModel>()
        listImages?.forEach {
            listImagesModel.add(ParallaxModel(it))
        }
        if (listImagesModel.size == model.count?.toInt()) {
            viewHolder.dataBinding.imageView.isHidden = true
//            vh.dataBinding.parallaxView.isHidden = false
//            vh.dataBinding.parallaxView.setImage(listImagesModel)
        }
    }

    private val currentVideoDir: String?
        get() {
            val model = getDataOrNull(currentPos) ?: return null
            if (!model.isVideo) return null
            val fromLocalStorage = model.isFromLocalStorage
            return if (fromLocalStorage) model.toUrl(isVideo = true) else model.pathCacheFullVideo ?: model.pathCache
        }

    private val playRunnable = Runnable {
        val url = currentVideoDir
        val vh = getViewHolderCard(currentPos)
        if (canPlay && url != null && vh != null)
            exoPlayerVideo?.play(
                vh.dataBinding.containerVideo, url,
                getDataOrNull(currentPos)!!.isFromLocalStorage
            )
    }

    @Synchronized
    fun playVideoCurrent(isForced: Boolean = false) {
        if (!canPlay && !isForced) return
        canPlay = true
        val context = context
        if (exoPlayerVideo == null && context != null) {
            val exoPlay = ExoPlayVideo(context)
            exoPlay.addEventListener(object : ExoPlayVideo.EventListener {
                override fun onRenderedFirstFrame() {
                    if (exoPlayerVideo?.currentPath == currentVideoDir) {
                        getViewHolderCard(currentPos)?.apply {
                            (itemView.parent as? View)?.performHapticFeedback(
                                HapticFeedbackConstants.LONG_PRESS,
                                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                            )
                            cancelAnimate()
                        }
                    }
                    onRenderFirstFrameCallback?.invoke()
                }
            })
            exoPlayerVideo = exoPlay
        }
        handler.removeCallbacks(playRunnable)
        if (getDataOrNull(currentPos)?.isVideo == true) {
            handler.postDelayed(playRunnable, 300)
        } else {
            exoPlayerVideo?.removeFromParent()
        }
        onTimeChanged()
    }

    override fun getItemCount() = listWall.size

    fun setThumbnailSize(width: Int, height: Int) {
        val r1 = itemHeightValue.toFloat() / itemWidthValue
        val r2 = height.toFloat() / width
        thumbnailHeightValue = height
        thumbnailWidthValue = width
        if (r2 > r1) {
            thumbnailHeightValue = (width * r1).toInt()
        } else {
            thumbnailWidthValue = (height / r1).toInt()
        }
    }

    fun getCountSize() = listWall.size

    fun getData(position: Int): WallpaperModel {
        return listWall[position]
    }

    private fun loadImage(
        image: ImageView,
        photo: WallpaperModel,
        listener: RequestListenerModel
    ) {
        val minUrl = photo.toUrl()
        val request: RequestBuilder<Drawable> =
            requestManager
                .load(minUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .apply(
                    if (photo.canCropImage)
                        RequestOptions.bitmapTransform(
                            CropBitmapTransformation(
                                thumbnailWidthValue,
                                thumbnailHeightValue,
                                CropBitmapTransformation.CropType.CENTER
                            )
                        )
                    else
                        RequestOptions.overrideOf(thumbnailWidthValue, thumbnailHeightValue)
                ).error(requestManager.load(photo.getUrlFailMin(minUrl)))

        val failUrl = photo.toUrl(isMin = false, isThumb = false)
        val dataUrl: Any = if (photo.isFromLocalStorage) failUrl else failUrl.toGlideUrl
        requestManager.load(Uri.parse(photo.toUrl()))
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .apply(
                if (photo.canCropImage)
                    RequestOptions.bitmapTransform(
                        CropBitmapTransformation(
                            itemWidthValue,
                            itemHeightValue,
                            CropBitmapTransformation.CropType.CENTER
                        )
                    )
                else
                    RequestOptions.overrideOf(itemWidthValue, itemHeightValue)
            )
            .error(requestManager.load(photo.getUrlFailThumb(failUrl)))
            .thumbnail(request)
            .listener(listener)
            .into(image)
    }

    fun setData(data: List<WallpaperModel>) {
        listWall.clear()
        listWall.addAll(data)
        notifyDataSetChanged()
    }

    fun addData(data: List<WallpaperModel>) {
        if (data.isEmpty()) return
        listWall.addAll(data)

//        notifyDataSetChanged()
        notifyDataChangeIgnoreCurrentPosition(currentPos)
    }

    private fun notifyDataChangeIgnoreCurrentPosition(position: Int) {
        notifyItemChanged(0, position)
        notifyItemChanged(position + 1, listWall.size - position)
    }

    fun remove(position: Int) {
        listWall.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getDataOrNull(positionInViewPager: Int): WallpaperModel? {
        return if (positionInViewPager < listWall.size) listWall[positionInViewPager]
        else null
    }

    fun removeAll() {
        listWall.clear()
        notifyDataSetChanged()
    }

    fun showOrHiddenPreview(show: Boolean, animate: Boolean = true) {
        var curtView: View? = null
        val alpha = if (show) 1f else 0f
        if (animate) {
            curtView = attached[currentPos]?.previewView
            curtView?.alpha = 1 - alpha
        }
        totalViewHolders.forEach {
            it.previewView?.isHidden = !show
        }
        curtView?.apply { animate().alpha(alpha).setDuration(200).start() }
    }

    fun getViewHolderCard(positionInViewPager: Int): DetailV2ViewHolder? {
        return if (attached[positionInViewPager] != null) attached[positionInViewPager] as DetailV2ViewHolder else null
    }

    fun setPreview(state: PopupWindowAdapter.ScreenState) {
        totalViewHolders.forEach {
            if (it is DetailV2ViewHolder) {
                it.addPreviewView(state, it.dataBinding.cardView)
            }
            if (it is ParallaxViewHolder) {
                it.addPreviewView(state, it.dataBinding.cardView)
            }
        }
        onTimeChanged()
        currentScreenState = state
    }

    fun pauseVideo() {
        handler.removeCallbacks(playRunnable)
        canPlay = false
        exoPlayerVideo?.removeFromParent()
    }

    fun registerTimer(context: Context) {
        if (RemoteConfig.commonData.isSupportedDevice && intentReceiver == null) {
            intentReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    onTimeChanged()
                }
            }
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_TIME_TICK)
            filter.addAction(Intent.ACTION_TIME_CHANGED)
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
            context.registerReceiver(intentReceiver, filter)
        }
    }

    fun unregisterTimer(context: Context) {
        if (intentReceiver != null) {
            context.unregisterReceiver(intentReceiver)
        }
        intentReceiver = null
    }

    fun onTimeChanged() {
        val currentTIme = System.currentTimeMillis()
        if (calendar.timeInMillis / 1000 == currentTIme / 1000) return
        calendar.timeInMillis = currentTIme

        totalViewHolders.forEach {
            it.setTimeTextView(it)
        }
    }

    fun release() {
        disposableAd?.dispose()
        totalViewHolders.forEach {
            (it as? DetailV2ViewHolder)?.clear()
        }
        handler.removeCallbacksAndMessages(null)
        exoPlayerVideo?.release()
        exoPlayerVideo = null
    }

    //----------------------------------------------------------------------------------------------
    inner class DetailV2ViewHolder(val dataBinding: ItemDetailsViewPagerBinding) :
        ViewHolderCommon(dataBinding), ExoPlayVideo.EventListener {
        private var model: WallpaperModel? = null
        private val composite = CompositeDisposable()

        init {
            itemView.setOnClickListener {
                onClickedItemCallback?.invoke(adapterPosition, listWall[adapterPosition])
            }
        }

        fun bindView(position: Int) {
            _context = this.itemView.context
            val wallModel = listWall[position]
            attached[position] = this

            dataBinding.imageView.isHidden = false
            dataBinding.containerVideo.isHidden = false

            loadImage(dataBinding.imageView, wallModel,
                object : RequestListenerModel(wallModel) {

                    override fun onFinal() {
                        if (wallpaper?.isVideo != true)
                            cancelAnimate()
                    }

                })
            onBind(wallModel)
            previewView?.isHidden = !firstTimeIntroShowed
            addPreviewView(currentScreenState, dataBinding.cardView)
            setTimeTextView(this)

            if (wallModel.isVideo) {
                val disposable = downloadVideo.invoke(position, wallModel)
                if (disposable == null) {
                    cancelAnimate()
                }
                addDisposable(disposable)
            }
        }

        fun cancelAnimate() {
            dataBinding.progressBar.isHidden = true
        }

        private fun startAnimate() {
            dataBinding.progressBar.isHidden = false
        }

        private fun onBind(model: WallpaperModel) {
            this.model = model
        }

        override fun stateBuffering() {
            startAnimate()
        }

        override fun onStart() {
            startAnimate()
        }

        override fun onRenderedFirstFrame() {
            cancelAnimate()
        }

        override fun onPlay(playWhenReady: Boolean) {
            if (playWhenReady) cancelAnimate()
        }

        fun clear() {
            composite.clear()
            dataBinding.ivIconSound.isHidden = true
            dataBinding.progressBar.isHidden = false
            dataBinding.containerVideo.removeAllViews()
            dataBinding.imageView.setImageDrawable(null)
        }

        fun addDisposable(disposable: Disposable?) {
            disposable?.let { composite.add(it) }
        }
    }

    //----------------------------------------------------------------------------------------------
    inner class ParallaxViewHolder(val dataBinding: ItemParallaxViewBinding) :
        ViewHolderCommon(dataBinding) {
        init {
            itemView.setOnClickListener {
                onClickedItemCallback?.invoke(adapterPosition, listWall[adapterPosition])
            }
        }

        var listImage = mutableListOf<ParallaxModel>()

        fun bindView(position: Int) {
            _context = this.itemView.context
            val wallModel = listWall[position]
            attached[position] = this

            loadImage(dataBinding.imageView, wallModel,
                object : RequestListenerModel(wallModel) {
                    override fun onFinal() {
                        if (wallpaper?.isVideo != true)
                            dataBinding.progressBar.isHidden = true
                    }
                })

            previewView?.isHidden = !firstTimeIntroShowed
            addPreviewView(currentScreenState, dataBinding.cardView)
            setTimeTextView(this)
        }
    }

    companion object {
        private const val ITEM_VIEW_IMAGE_VIDEO = 0
        private const val ITEM_VIEW_PARALLAX = 1
    }

}