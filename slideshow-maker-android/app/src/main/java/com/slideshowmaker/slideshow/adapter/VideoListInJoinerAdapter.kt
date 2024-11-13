package com.slideshowmaker.slideshow.adapter

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.models.VideoForJoinModel
import com.slideshowmaker.slideshow.ui.base.BaseAdapter
import com.slideshowmaker.slideshow.ui.base.BaseViewHolder
import com.slideshowmaker.slideshow.utils.MediaHelper
import com.slideshowmaker.slideshow.utils.Utils
import kotlinx.android.synthetic.main.item_view_video_in_joiner.view.*
import kotlin.math.roundToInt

class VideoListInJoinerAdapter : BaseAdapter<VideoForJoinModel>() {
    private val imageSize: Float =
        VideoMakerApplication.getContext().resources.displayMetrics.density * 76

    override fun doGetViewType(position: Int): Int = R.layout.item_view_video_in_joiner
    var itemClickCallback: ((VideoForJoinModel) -> Unit)? = null
    private var mCurPosition = -1
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val videoItem = _itemArray[position]
        val itemView = holder.itemView
        Glide.with(itemView.context).load(videoItem.filePath).apply(RequestOptions().override(imageSize.toInt()))
            .into(itemView.mediaThumb)
        itemView.durationLabel.text = Utils.convertSecToTimeString(
            (MediaHelper.getVideoDuration(videoItem.filePath).toFloat() / 1000).roundToInt()
        )

        if (videoItem.isSelect) {
            itemView.strokeBg.visibility = View.VISIBLE
        } else {
            itemView.strokeBg.visibility = View.GONE
        }

        itemView.setOnClickListener {
            if (mCurPosition >= 0) _itemArray[mCurPosition].isSelect = false
            mCurPosition = position
            _itemArray[mCurPosition].isSelect = true
            notifyDataSetChanged()
            itemClickCallback?.invoke(videoItem)
        }

    }

    fun highlightItem(id: Int) {
        for (item in _itemArray) {
            item.isSelect = item.id == id
        }
        notifyDataSetChanged()
    }
}