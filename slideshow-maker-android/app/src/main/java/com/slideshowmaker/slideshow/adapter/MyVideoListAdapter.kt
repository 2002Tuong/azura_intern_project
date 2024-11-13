package com.slideshowmaker.slideshow.adapter

import android.annotation.SuppressLint
import android.view.View
import coil.decode.VideoFrameDecoder
import coil.load
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.models.MyVideoModel
import com.slideshowmaker.slideshow.ui.base.BaseAdapter
import com.slideshowmaker.slideshow.ui.base.BaseViewHolder
import com.slideshowmaker.slideshow.utils.Utils
import kotlinx.android.synthetic.main.item_view_all_my_video.view.checkbox
import kotlinx.android.synthetic.main.item_view_all_my_video.view.dateLabel
import kotlinx.android.synthetic.main.item_view_all_my_video.view.durationLabel
import kotlinx.android.synthetic.main.item_view_all_my_video.view.icOpenMenu
import kotlinx.android.synthetic.main.item_view_all_my_video.view.imageThumb
import kotlinx.android.synthetic.main.item_view_all_my_video.view.nameLabel
import kotlinx.android.synthetic.main.item_view_all_my_video.view.sizeLabel
import java.io.File
import kotlin.math.roundToInt

class MyVideoListAdapter : BaseAdapter<MyVideoModel>() {

    var onSelectChangeCallback: ((Boolean) -> Unit)? = null
    var onLongPressCallback: (() -> Unit)? = null
    var onClickItemCallback: ((MyVideoModel) -> Unit)? = null
    var selectedMode = false

    var onClickOpenMenuCallback: ((View, MyVideoModel) -> Unit)? = null

    override fun doGetViewType(position: Int): Int {
        return if (_itemArray[position].filePath == "ads") {
            R.layout.item_view_native_ads_in_my_studio
        } else {
            R.layout.item_view_all_my_video
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val itemView = holder.itemView
        val myVideoItem = _itemArray[position]
        if (myVideoItem.filePath.isNotEmpty() && myVideoItem.filePath != "ads") {
            itemView.imageThumb.load(myVideoItem.filePath) {
                decoderFactory { result, options, _ -> VideoFrameDecoder(result.source, options) }
                placeholder(R.drawable.icon_load_thumb)
            }
        }
        if (getItemViewType(position) == R.layout.item_view_all_my_video) {
            // if(item.filePath.toLowerCase().contains(".mp4"))
            itemView.durationLabel.text =
                Utils.convertSecToTimeString((myVideoItem.duration.toFloat() / 1000).roundToInt())
            itemView.nameLabel.text =
                myVideoItem.filePath.split("/").lastOrNull()?.split(".")?.firstOrNull().orEmpty()
            itemView.dateLabel.text = myVideoItem.formattedDateAdded()
            itemView.sizeLabel.text = "${myVideoItem.fileSizeMb()} MB"
            itemView.checkbox.isSelected = myVideoItem.checked

            if (selectedMode) {
                itemView.checkbox.visibility = View.VISIBLE
            } else {
                itemView.checkbox.visibility = View.GONE
            }

            itemView.checkbox.setOnClickListener {
                myVideoItem.checked = !myVideoItem.checked
                itemView.checkbox.isSelected = myVideoItem.checked
                onSelectChangeCallback?.invoke(myVideoItem.checked)
            }


            itemView.icOpenMenu.setOnClickListener {
                if (!selectedMode) {
                    onClickOpenMenuCallback?.invoke(itemView.icOpenMenu, myVideoItem)
                }
            }
            itemView.setOnLongClickListener {
                onLongPressCallback?.invoke()
                return@setOnLongClickListener true
            }
            itemView.setOnClickListener {
                if (selectedMode) {
                    itemView.checkbox.performClick()
                } else {
                    onClickItemCallback?.invoke(myVideoItem)
                }
            }
            if (selectedMode) {
                itemView.icOpenMenu.alpha = 0.2f
            } else {
                itemView.icOpenMenu.alpha = 1f
            }
        } else if (getItemViewType(position) == R.layout.item_view_native_ads_in_my_studio) {
        }

    }

    override fun setItemList(arrayList: ArrayList<MyVideoModel>) {
        _itemArray.clear()
        if (arrayList.size < 1) return
        val finalItems = arrayListOf<MyVideoModel>()
        finalItems.add(arrayList[0])
        for (index in 1 until arrayList.size) {
            val item = arrayList[index]
            finalItems.add(item)
        }
        _itemArray.clear()
        _itemArray.addAll(finalItems)
    }

    fun selectAll() {
        for (item in _itemArray) {
            if (item.filePath.length > 5)
                item.checked = true
        }
        notifyDataSetChanged()
    }

    fun setOffAll() {
        for (item in _itemArray) {
            item.checked = false
        }
        notifyDataSetChanged()
    }

    fun getNumberItemSelected(): Int {
        var count = 0
        for (item in _itemArray) {
            if (item.checked && item.filePath.isNotEmpty()) ++count
        }
        return count
    }

    fun getTotalItem(): Int {
        var count = 0
        for (item in _itemArray) {
            if (item.filePath.length > 5) ++count
        }
        return count
    }

    fun onDeleteItem(path: String) {

        for (index in 0 until _itemArray.size) {
            val item = _itemArray[index]
            if (item.filePath == path) {
                _itemArray.removeAt(index)
                notifyItemRemoved(index)
                break
            }
        }

        deleteEmptyDay()

    }

    fun deleteEmptyDay() {
        for (index in 0 until _itemArray.size) {
            val item = _itemArray[index]
            if (item.filePath.isEmpty()) {
                if (index == _itemArray.size - 1) {
                    _itemArray.removeAt(index)
                    notifyItemRemoved(index)
                    return
                } else {
                    val nextItem = _itemArray[index + 1]
                    if (nextItem.filePath.isEmpty()) {
                        _itemArray.removeAt(index)
                        notifyItemRemoved(index)
                        return
                    }
                }
            }
        }
    }

    fun checkDeleteItem() {
        for (index in 0 until _itemArray.size) {
            val item = _itemArray[index]
            if (!File(item.filePath).exists()) {
                _itemArray.removeAt(index)
                // notifyItemRemoved(index)
            }
        }
    }
}