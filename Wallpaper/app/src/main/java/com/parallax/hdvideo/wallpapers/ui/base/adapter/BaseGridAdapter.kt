package com.parallax.hdvideo.wallpapers.ui.base.adapter

import android.view.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.ItemListBinding
import com.parallax.hdvideo.wallpapers.ui.collection.CollectionV2Adapter
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.dpToPx
import java.lang.ref.WeakReference
import kotlin.math.ceil

abstract class BaseGridAdapter<D: Any>: BaseAdapter<D>() {

    val ITEM_CODE = 100
    val LOADING_CODE = 300
    val FOUR_IMAGES_CODE = 600
    val COLLECTION_CODE = 500
    val TITLE_CODE = 200
    val SCROLL_VIEW_CODE = 700
    val ADVERTISE_CODE = 400

    private var weakRecyclerView: WeakReference<RecyclerView>? = null
    private lateinit var gridLayoutManagerInstance: GridLayoutManager

    protected var widthOfItem = 0
    protected var heightOfItem = 0
    var maximumItemOnScreen = 0
    var lastVisibleItemIndex = 0
    var spanCount = 1
    var loading = false
    protected var shouldHiddenProgressBar = false
    open var ratioHeight = AppConfiguration.aspectRatio
    var marginOfItem: Int = 0
    private var goToTopBtn: FloatingActionButton? = null
    private var shouldShowButton = true
    open var onScrollListener: OnScrollListener? = null
    val requestOptions: RequestOptions by lazy {
        RequestOptions().apply {
            diskCacheStrategy(DiskCacheStrategy.DATA)
            dontTransform()
            if (widthOfItem <= 0) {
                override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
            } else {
                override(widthOfItem, heightOfItem)
            }
        }
    }

    lateinit var requestManagerInstance: RequestManager

    abstract fun onCreateView(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    abstract fun onBindView(holder: RecyclerView.ViewHolder, position: Int)

    open fun getSpanSizeLookup(pos: Int) = spanCount

    open val amount: Int
        get() = listData.size
    override fun getItemCount(): Int {
        return amount + if (loading && !shouldHiddenProgressBar) 1 else 0
    }
    open var updateScrollButtonPos: (() -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        return if (position == amount && !shouldHiddenProgressBar) {
            LOADING_CODE
        } else {
            ITEM_CODE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_CODE -> {
                val viewHolder = onCreateView(parent, viewType)
                val params = viewHolder.itemView.layoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = heightOfItem
                viewHolder.itemView.layoutParams = params
                viewHolder
            }
            LOADING_CODE -> {
                val layout = FrameLayout(parent.context)
                layout.layoutParams =
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(80f))
                val bar = ProgressBar(
                    ContextThemeWrapper(parent.context, R.style.ProgressBarStyle),
                    null,
                    0
                )
                val params = FrameLayout.LayoutParams(dpToPx(44f), dpToPx(44f))
                params.gravity = Gravity.CENTER
                layout.addView(bar, params)
                LoadingViewHolder(layout)
            }
            COLLECTION_CODE -> {
                val viewHolder = CollectionV2Adapter.CollectionViewHolder(
                    ItemListBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
                val param = viewHolder.itemView.layoutParams
                param.height = (AppConfiguration.widthScreenValue * 0.55f).toInt()
                viewHolder.itemView.layoutParams = param
                viewHolder
            }
            else -> {
                val viewHolder = onCreateView(parent, viewType)
                if (viewType == FOUR_IMAGES_CODE || viewType == COLLECTION_CODE) {
                    val params = viewHolder.itemView.layoutParams
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT
                    params.height = heightOfItem
                    viewHolder.itemView.layoutParams = params
                }
                viewHolder
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder.itemViewType != LOADING_CODE) {
            onBindView(holder, position)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        weakRecyclerView = WeakReference(recyclerView)
        gridLayoutManagerInstance = recyclerView.layoutManager as GridLayoutManager
        spanCount = gridLayoutManagerInstance.spanCount
        gridLayoutManagerInstance.spanSizeLookup = spanSizeLookup
        recyclerView.removeOnLayoutChangeListener(onLayoutChangeListener)
        recyclerView.addOnLayoutChangeListener(onLayoutChangeListener)
        recyclerView.addOnScrollListener(onScroll)
    }

    private val onLayoutChangeListener =
        View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            calculateSizeItem(getRecyclerView()!!)
            notifyDataSetChanged()
        }

    private fun calculateSizeItem(recyclerView: RecyclerView) {
        recyclerView.removeOnLayoutChangeListener(onLayoutChangeListener)
        recyclerView.setPadding(marginOfItem, recyclerView.paddingTop, 0, recyclerView.paddingBottom)
        val paddingOfItem = marginOfItem * (spanCount - 1) / spanCount
        val widthItem = recyclerView.width / spanCount - paddingOfItem
        widthOfItem = widthItem
        heightOfItem = (widthItem * ratioHeight).toInt()
        maximumItemOnScreen = ceil(recyclerView.height * 1.0 / heightOfItem).toInt() * spanCount
    }

    private val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(pos: Int): Int {
            return when (getItemViewType(pos)) {
                ITEM_CODE -> {
                    return  1
                }
                LOADING_CODE -> {
                    spanCount
                }
                else -> {
                    getSpanSizeLookup(pos)
                }
            }
        }
    }

    private val onScroll = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

            val lastItem = gridLayoutManagerInstance.findLastVisibleItemPosition()
            if (lastItem == RecyclerView.NO_POSITION) return
            lastVisibleItemIndex = lastItem
            if (dy >= 0 && !shouldHiddenProgressBar) {
                if (amount - lastItem < maximumItemOnScreen) {
                    loadNextItems()
                }
            }

            if (shouldShowButton) {
                if (lastItem > maximumItemOnScreen) {
                    if (dy < 0) {
                        updateScrollButtonPos?.invoke()
                        goToTopBtn?.show()
                    }
                    else goToTopBtn?.hide()
                } else {
                    goToTopBtn?.hide()
                }
            }

            scrolled(recyclerView, lastItem, dy)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) shouldShowButton = true
            scrollStateChanged(recyclerView, newState)
        }
    }

    open fun updateGoToTopBtnPos() {

    }

    open fun scrolled(recyclerView: RecyclerView, lastItem: Int, dy: Int) {

    }

    open fun scrollStateChanged(recyclerView: RecyclerView, newState: Int) {

    }

    fun loadNextItems() {
        if (!loading) {
            loading = true
            val count1 = itemCount
            if (amount == count1 - 1 && onScrollListener != null) {
                notifyItemInserted(amount)
                onScrollListener!!.loadMoreData()
            } else loading = false
        }
    }

    fun onNextItemsLoaded() {
        if (loading) {
            val count = itemCount
            loading = false
            if (amount == count - 1)
                notifyItemRemoved(amount)
        }
    }

    open fun canLoadMoreData(isCan: Boolean) {
        val isHidden = !isCan
        if (shouldHiddenProgressBar != isHidden) {
            if (isHidden) {
                val count1 = itemCount
                shouldHiddenProgressBar = isHidden
                if (amount == count1 - 1)
                    notifyItemRemoved(amount)
            } else {
                shouldHiddenProgressBar = isHidden
                val count1 = itemCount
                if (amount == count1 - 1) {
                    notifyItemInserted(amount)
                }
                loading = false
            }
        }
    }

    override fun addData(data: List<D>) {
        onNextItemsLoaded()
        if (data.isNotEmpty()) {
            listData.addAll(data)
            notifyItemRangeInserted(amount, data.size)
        }
    }

    override fun addData(vararg data: D) {
        onNextItemsLoaded()
        if (data.isNotEmpty()) {
            listData.addAll(data)
            notifyItemRangeInserted(amount, data.size)
        }
    }

    override fun setData(data: List<D>) {
        loading = false
        super.setData(data)
    }


    open fun release() {
        weakRecyclerView?.clear()
        weakRecyclerView = null
        onScrollListener = null
        listData.clear()
    }

    fun getRecyclerView() = weakRecyclerView?.get()

    fun getLayoutManager() = gridLayoutManagerInstance

    private class LoadingViewHolder(view: View): RecyclerView.ViewHolder(view)

    interface OnScrollListener {
        fun loadMoreData()
    }

    fun setGoToTopButton(button: FloatingActionButton) {
        goToTopBtn = button
        button.setOnClickListener {
            shouldShowButton = false
            goToTopBtn?.hide()
            getRecyclerView()?.smoothScrollToPosition(0)
        }
        button.hide()
    }
}