package com.slideshowmaker.slideshow.models

import com.slideshowmaker.slideshow.data.models.MediaInfo

class MediaAlbumModel {
    val mediaItemPaths = arrayListOf<MediaInfo>()
    val folderId:String
    val albumName:String

    constructor(albumName:String, folderId:String) {
        this.folderId = folderId
        this.albumName = albumName
    }

}