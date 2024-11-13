package com.slideshowmaker.slideshow.data.models

data class FrameLinkInfo constructor(
    val fileName: String,
    val link: String,
    val isPro: Boolean,
    val id: String,
    val thumb: String,
    val frameFileName: String,
    private val packLink: String = "",
    private val ratioExtension: String? = LinkDataUtils.DEFAULT_RATIO_EXTENSION
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

    private fun getRatioExtension(): String = ratioExtension ?: LinkDataUtils.DEFAULT_RATIO_EXTENSION

}

fun FrameLinkInfo.isNone() = link == "none"
