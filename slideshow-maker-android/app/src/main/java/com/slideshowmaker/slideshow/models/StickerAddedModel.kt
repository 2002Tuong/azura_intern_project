package com.slideshowmaker.slideshow.models

import android.graphics.Bitmap
import java.io.Serializable

class StickerAddedModel(val bitmap: Bitmap, var onEdit:Boolean, var startTimeInMilSec:Int, var endTimeInMilSec:Int, val stickerViewId:Int):Serializable {


}