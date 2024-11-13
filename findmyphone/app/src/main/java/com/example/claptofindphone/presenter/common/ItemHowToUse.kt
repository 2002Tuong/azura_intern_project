package com.example.claptofindphone.presenter.common

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.LayoutItemViewHowToUseBinding

@SuppressLint("CustomViewStyleable")
class ItemHowToUse @JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val itemSupport =
        LayoutItemViewHowToUseBinding.inflate(LayoutInflater.from(context), this, true)
    private var isHowToUse1 = false
    private var isHowToUse2 = false
    private var isHowToUse3 = false
    private var isHowToUse4 = false

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ItemHowToUse).apply {
            isHowToUse1 = getBoolean(R.styleable.ItemHowToUse_isHowToUse_1, false)
            isHowToUse2 = getBoolean(R.styleable.ItemHowToUse_isHowToUse_2, false)
            isHowToUse3 = getBoolean(R.styleable.ItemHowToUse_isHowToUse_3, false)
            isHowToUse4 = getBoolean(R.styleable.ItemHowToUse_isHowToUse_4, false)
            recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setItemIcon()
        setSupportTitle()
    }

    private fun setItemIcon() {
        if (isHowToUse1) {
            itemSupport.imgSupport.setImageResource(R.drawable.icon_how_to_use_1)
        } else if (isHowToUse2) {
            itemSupport.imgSupport.setImageResource(R.drawable.icon_how_to_use_2)
        } else if (isHowToUse3) {
            itemSupport.imgSupport.setImageResource(R.drawable.icon_how_to_use_3)
        } else if (isHowToUse4) {
            itemSupport.imgSupport.setImageResource(R.drawable.icon_how_to_use_4)
        }

    }

    private fun setSupportTitle() {
        if (isHowToUse1) {
            itemSupport.txtSupportTitle.text = context.resources.getString(R.string.how_to_use_instruction_1)
        } else if (isHowToUse2) {
            itemSupport.txtSupportTitle.text = context.resources.getString(R.string.how_to_use_instruction_2)
        } else if (isHowToUse3) {
            itemSupport.txtSupportTitle.text = context.resources.getString(R.string.how_to_use_instruction_3)
        } else if (isHowToUse4) {
            itemSupport.txtSupportTitle.text = context.resources.getString(R.string.how_to_use_instruction_4)
        }

    }

}