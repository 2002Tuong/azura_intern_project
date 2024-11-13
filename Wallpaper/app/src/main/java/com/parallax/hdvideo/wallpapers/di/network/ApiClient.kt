package com.parallax.hdvideo.wallpapers.di.network

import com.parallax.hdvideo.wallpapers.data.api.ResponseModel
import com.parallax.hdvideo.wallpapers.data.model.*
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url


interface ApiClient {

    @GET
    fun getString(@Url url: String): Single<String>

    @GET
    fun getSimpleString(@Url url: String): Call<String>

    @GET
    fun getStringResponse(@Url url: String): Single<Response<String>>

    @GET
    fun getConfig(@Url url: String): Single<ConfigInfo>

    @GET
    fun getListWallpapers(@Url url: String): Single<ServerInfoModel>

    @GET
    fun getListWallpapersFromCategory(@Url url: String): Single<ResponseModel<WallpaperModel>>

    @CacheResponse
    @GET
    fun getListWallpapersFromCategory(@Url url: String, @Query(AppConstants.EXPIRATION) expired: Long): Single<ResponseModel<WallpaperModel>>

    @CacheResponse
    @GET
    fun getListCategoryModels(@Url url: String): Single<ResponseModel<Category>>

    @GET
    fun getMoreApp(@Url url: String): Single<ResponseModel<MoreAppModel>>

    @CacheResponse
    @GET
    fun getHashTags(@Url url: String): Single<ResponseModel<HashTag>>

    @GET
    fun getOriginStorage(@Url url: String): Observable<OriginalStorage>

    @Streaming
    @GET
    fun downloadFile(@Url fileUrl: String): Single<ResponseBody>

    @GET
    fun downloadFiles(@Url fileUrl: String): Call<ResponseBody>

    @CacheResponse
    @GET
    fun getWallpapersFromCategory(@Url url: String, @Query(AppConstants.EXPIRATION) expired: Long): Call<ResponseModel<WallpaperModel>>
}