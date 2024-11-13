package com.slideshowmaker.slideshow.modules.transition.transition

import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.RemoteConfigRepository
import com.slideshowmaker.slideshow.utils.extentions.isUrl
import java.io.Serializable

open class GSTransition(
    val transitionCodeId: Int = R.raw.transition_code_none,
    val transitionName: String = "Random",
    val id: String = "random_1",
    var isPro: Boolean = false,
    var thumbnailUrl: String = "file:///android_asset/Random.jpg",
    var isWatchAds: Boolean = false
) : Serializable {
    override fun equals(other: Any?): Boolean {
        return (other as? GSTransition)?.id == id
    }

    fun getThumbnail(): String = if (thumbnailUrl.startsWith("file")) thumbnailUrl else
        if (thumbnailUrl.isUrl()) thumbnailUrl
        else
            RemoteConfigRepository.appConfig?.cdnUrl.orEmpty().plus(thumbnailUrl)

    companion object {
        val RANDOM_IDS = listOf("random_1", "random_2")
    }
}