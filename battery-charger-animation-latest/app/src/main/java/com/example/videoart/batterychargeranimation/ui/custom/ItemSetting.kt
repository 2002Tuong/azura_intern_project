package com.example.videoart.batterychargeranimation.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.databinding.ItemSettingBinding

class ItemSetting @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs){
    private val layout = ItemSettingBinding.inflate(LayoutInflater.from(context), this, true)
    private var headIcon: Int= -1
    private var tailIcon: Int = -1
    private var label = ""

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ItemSetting).apply {
            headIcon = getResourceId(R.styleable.ItemSetting_headIcon, -1)
            tailIcon = getResourceId(R.styleable.ItemSetting_tailIcon, -1)
            label = getString(R.styleable.ItemSetting_text) ?: ""
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if(headIcon > 0) {
            layout.headIcon.setImageResource(headIcon)
        }

        if(tailIcon > 0) {
            layout.tailIcon.setImageResource(tailIcon)
        }
        layout.label.text = label
    }
}