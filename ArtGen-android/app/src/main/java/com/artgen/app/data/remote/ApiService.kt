package com.artgen.app.data.remote

import com.artgen.app.data.remote.response.ArtGenResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("api/artgen/v1/gen_image")
    suspend fun generateArt(
        @Part imageId: MultipartBody.Part?,
        @Part inputImage: MultipartBody.Part?,
        @Part style: MultipartBody.Part,
        @Part text: MultipartBody.Part?
    ): Response<ArtGenResponse>
}
