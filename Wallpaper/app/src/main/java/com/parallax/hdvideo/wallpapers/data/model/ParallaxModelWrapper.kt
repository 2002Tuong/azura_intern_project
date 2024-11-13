package com.parallax.hdvideo.wallpapers.data.model

import android.graphics.Bitmap
import com.techpro.parallax.wallpaper.model.ParallaxModel

class ParallaxModelWrapper: ParallaxModel {

    constructor(url: String) : super(url)

    constructor(bitmap: Bitmap) : super(bitmap)

    @Transient
    val maxProgress = 100

    fun toIntValue() : Int = ((powerX * maxProgress + maxProgress) / 2).toInt()

    fun setIntValue(value: Int) {
        setValue((value * 2f  - maxProgress) / maxProgress)
    }

//    fun updateValue() {
//        val binding = view ?: return
//        var value = binding.editText.text.toString().toFloatOrNull()
//        if (value != null) {
//            value = min(max(value, -1f), 1f)
//            setValue(value)
//            binding.progress.progress = toIntValue()
//            binding.editText.setText(powerX.toString())
//        }
//    }
}