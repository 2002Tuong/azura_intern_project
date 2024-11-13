package com.example.claptofindphone.presenter.common

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.example.claptofindphone.R
import com.example.claptofindphone.data.local.PreferenceSupplier
import com.example.claptofindphone.databinding.LayoutItemSoundSelectedSettingBinding
import javax.inject.Inject

@SuppressLint("CustomViewStyleable")
class ItemSoundSetting @JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val itemSoundSetting =
        LayoutItemSoundSelectedSettingBinding.inflate(LayoutInflater.from(context), this, true)
    private var isFlash = false
    private var isVibration = false
    private var isSound = false
    private var isVolume = false

    @Inject
    lateinit var preferenceSupplier: PreferenceSupplier

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ItemSoundSetting).apply {
            isFlash = getBoolean(R.styleable.ItemSoundSetting_isFlash, false)
            isVibration = getBoolean(R.styleable.ItemSoundSetting_isVibration, false)
            isSound = getBoolean(R.styleable.ItemSoundSetting_isSound, false)
            isVolume = getBoolean(R.styleable.ItemSoundSetting_isVolume, false)
            recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setItemIcon()
        setSupportTitle()
        shouldDisplaySeekbarSoundSetting()
        shouldSwitchButton()
        setUpSeekBarVolume()
    }

    private fun setItemIcon() {
        if (isFlash) {
            itemSoundSetting.imgSettingIcon.setImageResource(R.drawable.icon_flash)
        } else if (isVibration) {
            itemSoundSetting.imgSettingIcon.setImageResource(R.drawable.icon_vibration)
        } else if (isSound) {
            itemSoundSetting.imgSettingIcon.setImageResource(R.drawable.icon_sound)
        } else if (isVolume) {
            itemSoundSetting.imgSettingIcon.setImageResource(R.drawable.icon_volume)
        }

    }

    private fun setSupportTitle() {
        if (isFlash) {
            itemSoundSetting.txtSettingTitle.text = context.resources.getString(R.string.flash)
        } else if (isVibration) {
            itemSoundSetting.txtSettingTitle.text = context.resources.getString(R.string.vibration)
        } else if (isSound) {
            itemSoundSetting.txtSettingTitle.text = context.resources.getString(R.string.sound)
        } else if (isVolume) {
            itemSoundSetting.txtSettingTitle.text = context.resources.getString(R.string.volume)
        }
    }

    private fun shouldDisplaySeekbarSoundSetting() {
        itemSoundSetting.seekBarSoundVolume.visibility = if (isVolume) {
            VISIBLE
        } else GONE
    }

    private fun shouldSwitchButton() {
        itemSoundSetting.swSoundSetting.visibility = if (isVolume) {
            GONE
        } else VISIBLE
    }

    fun getSwitchButton() = itemSoundSetting.swSoundSetting

    private fun setUpSeekBarVolume() {
        itemSoundSetting.seekBarSoundVolume.max = 10
        itemSoundSetting.seekBarSoundVolume.min = 0
    }

    fun getSeekBarVolume() = itemSoundSetting.seekBarSoundVolume

}