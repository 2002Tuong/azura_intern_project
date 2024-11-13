package com.slideshowmaker.slideshow.data.models

import com.slideshowmaker.slideshow.data.models.LinkDataUtils.DEFAULT_RATIO_EXTENSION

data class FilterLinkInfo constructor(
    val name : String,
    val link : String,
    val isPro: Boolean,
    val id: String,
    val thumb: String,
    val filterFileName: String,
    private val packLink: String = "",
    private val ratioExtension: String? = DEFAULT_RATIO_EXTENSION
) {
    var ratio: Float = 1f
    private val isPackLinkAvailable: Boolean get() = packLink.isNotBlank()

    fun getDownloadLinkAndName(): Pair<String, String> {
        return if (isPackLinkAvailable) {
            Pair(packLink, "$id.zip")
        } else {
            Pair(link, getRatioFileName())
        }
    }

    fun getRatioFileName(): String {
        if (isNone()) return id
        if (isPackLinkAvailable.not()) return id
        return id + LinkDataUtils.getRatioStr(ratio) + getRatioExtension()
    }

    private fun getRatioExtension(): String = ratioExtension ?: DEFAULT_RATIO_EXTENSION

}

fun FilterLinkInfo.isNone() = link == "none"