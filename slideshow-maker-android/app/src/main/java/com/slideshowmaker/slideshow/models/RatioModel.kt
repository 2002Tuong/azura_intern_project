package com.slideshowmaker.slideshow.models

import com.slideshowmaker.slideshow.data.models.RatioInfo

class RatioModel (ratioData: RatioInfo) {
    val title=ratioData.title;
    val iconId=ratioData.iconId;
    val h=ratioData.h;
    val v=ratioData.v;
}
