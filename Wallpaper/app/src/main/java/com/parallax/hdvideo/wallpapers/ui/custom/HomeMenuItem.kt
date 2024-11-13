package com.parallax.hdvideo.wallpapers.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.extension.setGradientTextView

class HomeMenuItem : FrameLayout {

    var title : TextView ? = null
    var icon : ImageView ? = null
    var resIconSelected = 0
    var resIconUnSelected = 0

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView()
    }

    private fun initView() {
        inflate(context, R.layout.item_menu_home, this)
        title = findViewById(R.id.tvTitle)
        icon = findViewById(R.id.ivTypeMenu)
    }

    fun setData(@StringRes tittle : Int,@DrawableRes resIconSelect : Int,@DrawableRes resIconUnSelect : Int): HomeMenuItem {
        this.resIconSelected = resIconSelect
        this.resIconUnSelected = resIconUnSelect
        title?.setText(tittle)
        icon?.setImageResource(resIconUnSelect)
        return this
    }

    fun selected(): HomeMenuItem {
        setBackgroundResource(R.drawable.home_menu_background_oval_re)
        icon?.setImageResource(this.resIconSelected)
        title?.setGradientTextView(true)

        return this
    }

    fun unSelected(): HomeMenuItem {
        setBackgroundResource(R.drawable.home_menu_background_oval_no_choose_re)
        icon?.setImageResource(this.resIconUnSelected)
        title?.setGradientTextView(false)
        return this
    }

}