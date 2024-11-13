package com.slideshowmaker.slideshow.data.remote

import okhttp3.ResponseBody
import retrofit2.http.*

interface AnimationServerInterface {

    @Streaming
    @GET()
    suspend fun downloadLargeFile(@Url url: String): ResponseBody

}