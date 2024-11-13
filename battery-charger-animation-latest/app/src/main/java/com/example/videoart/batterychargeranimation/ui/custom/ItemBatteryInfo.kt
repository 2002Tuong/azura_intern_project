package com.example.videoart.batterychargeranimation.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.databinding.ItemBatteryInfoBinding

class ItemBatteryInfo @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {
    private val layout = ItemBatteryInfoBinding.inflate(LayoutInflater.from(context), this, true)
    var title = ""
    var info = ""
        set(value) {
            field = value
            layout.infomation.text = field
        }
    var icon = -1

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ItemBatteryInfo).apply {
            title = getString(R.styleable.ItemBatteryInfo_titleText) ?: ""
            info = getString(R.styleable.ItemBatteryInfo_infoText) ?: ""
            icon = getResourceId(R.styleable.ItemBatteryInfo_iconThumbnail, -1)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if(icon > 0) {
            layout.icon.setImageResource(icon)
        }

        layout.title.text = title
        layout.infomation.text = info
    }
}