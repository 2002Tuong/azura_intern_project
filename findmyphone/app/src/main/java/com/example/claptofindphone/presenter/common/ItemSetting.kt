package com.example.claptofindphone.presenter.common

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.LayoutItemOptionSettingBinding

@SuppressLint("CustomViewStyleable")
class ItemSetting @JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val itemSupport =
        LayoutItemOptionSettingBinding.inflate(LayoutInflater.from(context), this, true)
    private var isFlashModeDefault = false
    private var isFlashModeDisco = false
    private var isFlashModeSOS = false
    private var isVibrationDefault = false
    private var isVibrationStrong = false
    private var isVibrationHeartBeat = false
    private var isVibrationTicktock = false
    private var isSettingLanguage = false

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ItemSettingRadioButton).apply {
            isFlashModeDefault =
                getBoolean(R.styleable.ItemSettingRadioButton_isDefaultFlash, false)
            isFlashModeDisco = getBoolean(R.styleable.ItemSettingRadioButton_isDisco, false)
            isFlashModeSOS = getBoolean(R.styleable.ItemSettingRadioButton_isSOS, false)
            isVibrationDefault =
                getBoolean(R.styleable.ItemSettingRadioButton_isDefaultVibration, false)
            isVibrationStrong =
                getBoolean(R.styleable.ItemSettingRadioButton_isStrongVibration, false)
            isVibrationHeartBeat =
                getBoolean(R.styleable.ItemSettingRadioButton_isHeartBeat, false)
            isVibrationTicktock = getBoolean(R.styleable.ItemSettingRadioButton_isTicktock, false)
            isSettingLanguage =
                getBoolean(R.styleable.ItemSettingRadioButton_isSettingLanguage, false)
            recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setSupportTitle()
        setUpViewArrowRight()
        setUpRadioButton()
    }

    private fun setSupportTitle() {
        itemSupport.txtMode.text = if (isFlashModeDefault) {
            context.resources.getString(R.string.default_mode)
        } else (if (isFlashModeDisco) {
            context.resources.getString(R.string.disco_mode)
        } else if (isFlashModeSOS) {
            context.resources.getString(R.string.sos_mode)
        } else if (isVibrationDefault) {
            context.resources.getString(R.string.default_mode)
        } else if (isVibrationStrong) {
            context.resources.getString(R.string.strong_vibration)
        } else if (isVibrationHeartBeat) {
            context.resources.getString(R.string.heart_beat)
        } else if (isVibrationTicktock) {
            context.resources.getString(R.string.ticktock)
        } else {
            context.resources.getString(R.string.language)
        }).toString()
    }

    private fun setUpViewArrowRight() {
        itemSupport.imgOpenNext.visibility = if (isSettingLanguage) {
            VISIBLE
        } else {
            GONE
        }
    }

    private fun setUpRadioButton() {
        itemSupport.rbMode.visibility = if (isSettingLanguage) {
            GONE
        } else {
            VISIBLE
        }
    }

    fun getRadioButton() = itemSupport.rbMode

}