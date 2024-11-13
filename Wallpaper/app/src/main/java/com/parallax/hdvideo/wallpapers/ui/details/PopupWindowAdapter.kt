package com.parallax.hdvideo.wallpapers.ui.details

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import android.widget.TextView
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.extension.margin


/**
 * Check DWT-787 for design
 * State 0: default option , user viewing lock screen preview
 * State 1: user viewing home screen preview
 * State 2: user viewing only image preview
 */

class PopupWindowAdapter(
    context: Context,
    data: MutableList<HashMap<String, Any>> = ArrayList(),
    resource: Int = R.layout.item_detail_fragment_menu,
    columnsName: Array<String> = arrayOf("IMAGE", "TEXT"),
    viewIds: IntArray = intArrayOf(R.id.ivOptionImage, R.id.tvOptionName)
): SimpleAdapter(context, data,resource, columnsName, viewIds) {

    private val mData = data
    private val hmap01 = hashMapOf<String, Any>()
    private val hmap02 = hashMapOf<String, Any>()
    private val hmap03 = hashMapOf<String, Any>()
    var currentScreenState = ScreenState.PREVIEW_LOCK
    init {
        hmap01["IMAGE"] =  R.drawable.ic_home_screen_white_re
        hmap01["TEXT"] = context.getString(R.string.set_wallpaper_home_screen)
        hmap02["IMAGE"] = R.drawable.ic_image_square_re
        hmap02["TEXT"] = context.getString(R.string.image)
        hmap03["IMAGE"] = R.drawable.ic_lock_screen_white_re
        hmap03["TEXT"] = context.getString(R.string.set_wallpaper_lock_screen)
        data.add(hmap01)
        data.add(hmap02)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val superView = super.getView(position, convertView, parent)
        if (position == 1) {
            val tvOption = superView.findViewById<TextView>(R.id.tvOptionName)
            tvOption.margin(bottom = 24f)
        }
        return superView
    }

    fun setState(state: ScreenState) {
        currentScreenState = state
        mData.clear()
        when (currentScreenState) {
            ScreenState.PREVIEW_LOCK -> {
                mData.add(hmap01)
                mData.add(hmap02)
            }
            ScreenState.PREVIEW_HOME -> {
                mData.add(hmap03)
                mData.add(hmap02)
            }
            ScreenState.PREVIEW_IMAGE -> {
                mData.add(hmap01)
                mData.add(hmap03)
            }
        }
        notifyDataSetChanged()
    }

    enum class ScreenState {
        PREVIEW_LOCK,PREVIEW_HOME,PREVIEW_IMAGE
    }
}