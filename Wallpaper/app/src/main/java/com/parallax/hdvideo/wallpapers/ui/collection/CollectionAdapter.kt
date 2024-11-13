package com.parallax.hdvideo.wallpapers.ui.collection

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.data.model.Category
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.databinding.ItemHorizontalScrollImagesListBinding
import com.parallax.hdvideo.wallpapers.databinding.ItemListRowImageBinding
import com.parallax.hdvideo.wallpapers.databinding.ItemListViewBinding
import com.parallax.hdvideo.wallpapers.extension.margin
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseAdapterList
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseGridAdapter
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import kotlin.math.min

class CollectionAdapter : BaseGridAdapter<Category>() {

    private val random = java.util.Random()
    private fun isPositionTitle(position: Int) = position % 2 == 0
    var onClickItemCallback: ((Int, Category, View) -> Unit)? = null

    override val amount: Int
        get() = super.amount * 2

    override fun getItemViewType(position: Int): Int {
        return if (isPositionTitle(position)) TITLE_CODE else SCROLL_VIEW_CODE
    }

    override fun onCreateView(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TITLE_CODE -> {
                TitleViewHolder(
                    ItemListViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                HorizontalScrollListVH(
                    ItemHorizontalScrollImagesListBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindView(holder: RecyclerView.ViewHolder, position: Int) {
        val halfCatePos = position / 2
        if (holder is TitleViewHolder) {
            val category = listData[halfCatePos]
            val binding = holder.dataBinding
            binding.tvTitle.text = category.name
            holder.itemView.setOnClickListener {
                onClickedItemCallback?.invoke(it, position, category)
            }
        } else if (holder is HorizontalScrollListVH) {
            val binding = holder.dataBinding
            val horizontalRvAdapter = HorizontalRvAdapter()
            binding.horizontalRecyclerView.adapter = horizontalRvAdapter
            binding.horizontalRecyclerView.layoutManager =
                LinearLayoutManager(WallpaperApp.instance, LinearLayoutManager.HORIZONTAL, false)
            horizontalRvAdapter.setData(listData[halfCatePos])
        }
    }

    private fun setBackgroundColor(view: CardView) {
        val colorFromArgb = Color.argb(180, random.nextInt(256), random.nextInt(256), random.nextInt(256))
        view.setCardBackgroundColor(colorFromArgb)
    }

    class TitleViewHolder constructor(binding: ItemListViewBinding) :
        BaseAdapterList.BaseViewHolder<Category, ItemListViewBinding>(binding)

    class HorizontalScrollListVH constructor(binding: ItemHorizontalScrollImagesListBinding) :
        BaseAdapterList.BaseViewHolder<List<WallpaperModel>, ItemHorizontalScrollImagesListBinding>(
            binding
        )

    inner class HorizontalRvAdapter :
        BaseAdapterList<WallpaperModel, ItemListRowImageBinding>(
            layoutId = { R.layout.item_list_row_image }
        ) {
        var category = Category()
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BaseViewHolder<WallpaperModel, ItemListRowImageBinding> {
            val viewHolder = super.onCreateViewHolder(parent, viewType)
            val params = viewHolder.itemView.layoutParams
            params.width = (AppConfiguration.widthScreenValue / 4)
            params.height = params.width * 200 / 116
            viewHolder.itemView.layoutParams = params
            viewHolder.itemView.margin(right = 8f)
            setBackgroundColor(viewHolder.itemView.findViewById(R.id.cardView))
            return viewHolder
        }

        fun setData(data: Category) {
            val size = data.walls.size
            category = data
            if (size != 0) {
                super.setData(data.walls.subList(0, min(size, 5)).toList())
            }
        }

        override fun onBindViewHolder(
            holder: BaseViewHolder<WallpaperModel, ItemListRowImageBinding>,
            position: Int
        ) {
            val isLastItem = position == dataSize - 1
            val binding = holder.dataBinding
            val model = getData(position)
            if (isLastItem) {
                binding.coverView.visibility = View.VISIBLE
                binding.tvPressToSeeAll.visibility = View.VISIBLE
            }
            val url = model.toUrl()
            requestManagerInstance.load(url).apply(requestOptions)
                .error(requestManagerInstance.load(model.getUrlFailMin(url)))
                .into(binding.imageView)
            holder.itemView.setOnClickListener {
                onClickItemCallback?.invoke(position, category, it)
            }
        }
    }
}