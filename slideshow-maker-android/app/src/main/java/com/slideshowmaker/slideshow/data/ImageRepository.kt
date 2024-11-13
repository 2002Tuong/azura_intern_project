package com.slideshowmaker.slideshow.data

import android.content.Context
import com.slideshowmaker.slideshow.data.local.LocalImageDataSource
import com.slideshowmaker.slideshow.data.local.fetchPagePicture
import com.slideshowmaker.slideshow.data.local.fetchPictureAlbum
import com.slideshowmaker.slideshow.data.models.enum.MediaType
import com.slideshowmaker.slideshow.data.response.PhotoAlbum


class ImageRepository(private val context: Context) {
    fun getPicturePagingSource(
        album: String,
        mediaKind: MediaType = MediaType.PHOTO
    ): LocalImageDataSource {
        return LocalImageDataSource { limit, offset ->
            context.fetchPagePicture(album, limit, offset, mediaKind)
        }
    }

    fun getAlbums(mediaKind: MediaType = MediaType.PHOTO): List<PhotoAlbum> {
        return context.fetchPictureAlbum(mediaKind)
    }
}
