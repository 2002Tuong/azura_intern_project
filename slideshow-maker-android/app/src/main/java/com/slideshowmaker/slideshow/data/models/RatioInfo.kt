package com.slideshowmaker.slideshow.data.models

data class RatioInfo (val title:String, val iconId:Int, val h:Int, val v:Int) {
    constructor():this("",1,1,1)
}
