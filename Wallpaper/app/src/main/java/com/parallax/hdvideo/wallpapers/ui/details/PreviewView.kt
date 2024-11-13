package com.parallax.hdvideo.wallpapers.ui.details

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BasePagerAdapter
import com.parallax.hdvideo.wallpapers.ui.custom.BaseViewPager
import java.util.*

class PreviewView(val context: Context) {

    init {
        setup()
    }
    private lateinit var viewPager: BaseViewPager
    private lateinit var previewAdapter: Adapter
    private val patternDate = "E dd MMM"
    private val patternHour = "HH:mm"
    private val patternYear = "EEEE, dd MMM"
    private var currentPos = 0

    private fun setup() {
        viewPager = BaseViewPager(context)
        viewPager.hasTouched = false
        previewAdapter = Adapter()
        viewPager.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        previewAdapter.enabled = false
        viewPager.adapter = previewAdapter
        previewAdapter.addData(0, 1)
    }

    fun getView() : ViewPager {
        return viewPager
    }

    fun setCurrentItem(position: Int) {
        if (this::viewPager.isInitialized) {
            viewPager.setCurrentItem(position, true)
        }
    }

    fun switch() {
        var position = viewPager.currentItem + 1
        if (position == previewAdapter.count)
            position = 0
        setCurrentItem(position)
    }

    val isFirstPosition get() = viewPager.currentItem == 0

    var isHidden: Boolean
        get() = this::viewPager.isInitialized && this.viewPager.isHidden
        set(value) {
            if (this::viewPager.isInitialized)
                this.viewPager.isHidden = value
        }

    fun setText(cal: Calendar) {
        var itemView = previewAdapter.getViewHolder(0)?.itemView ?: return
        itemView.findViewById<TextView>(R.id.previewHour).text = DateFormat.format(patternHour, cal)
        itemView.findViewById<TextView>(R.id.previewDate).text = DateFormat.format(patternDate, cal)
        itemView = previewAdapter.getViewHolder(1)?.itemView ?: return
    }

    private class Adapter: BasePagerAdapter<Int, BasePagerAdapter.ViewHolder>() {

        override fun getItemViewType(position: Int): Int {
            return position
        }
        override fun onCreateViewHolder(container: ViewGroup, viewType: Int): ViewHolder {
            val idLayout = if (viewType == 0) R.layout.preview_lock_screen else R.layout.layout_preview
            return ViewHolder(LayoutInflater.from(container.context).inflate(idLayout, container, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        }
    }
}