package com.parallax.hdvideo.wallpapers.data.model

import com.google.gson.annotations.SerializedName
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder

class MoreAppModel: Category() {
    @SerializedName("icon")
    var icon: String? = null
    @SerializedName("images")
    var image: String? = null

    val thumbnail get() = WallpaperURLBuilder.shared.getFullStorage().plus(image)

    override fun toUrl(isMin: Boolean , isThumb: Boolean ): String {
        return WallpaperURLBuilder.shared.getFullStorage()
            .plus("moreapps/")
            .plus(id)
            .plus("/")
            .plus(RemoteConfig.languageCode).plus(".jpg")
    }

}