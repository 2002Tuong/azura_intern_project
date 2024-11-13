package com.slideshowmaker.slideshow.data.remote

import android.os.Build
import com.slideshowmaker.slideshow.BuildConfig
import com.slideshowmaker.slideshow.data.response.IpInfoModelSetting
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import java.util.*

interface VideoMakerServerInterface {
    @POST("https://gamemobileglobal.com/api/apps/register_token.php")
    @FormUrlEncoded
    fun registerTokenAsync(
        @Field(value = "deviceID") deviceId: String,
        @Field(value = "token_id") token: String = "",
        @Field(value = "code") code: String = "65458",
        @Field(value = "package") packageName: String = "ads",
        @Field(value = "os_version") osVersion: String = Build.VERSION.RELEASE,
        @Field(value = "version") version: String = BuildConfig.VERSION_NAME,
        @Field(value = "phone_name") phoneName: String = Build.MODEL,
        @Field(value = "country") country: String = Locale.getDefault().country,
        @Field(value = "referrer") referrer: String = "",
        @Field(value = "install_from_store") installFromStore: Boolean = false,
    ): Deferred<Response<Unit>>

    @POST("https://gamemobileglobal.com/api/apps/transaction.php")
    @FormUrlEncoded
    fun acknowledgeTransactionAsync(
        @Field("deviceID") deviceId: String,
        @Field("os") os: String = "android",
        @Field("sub_id") subId: String,
        @Field("country") country: String = Locale.getDefault().country,
        @Field("current_time") currentTime: Long = System.currentTimeMillis() / 1000L,
        @Field("phone_name") phoneName: String = Build.MODEL,
        @Field("os_version") osVersion: String = Build.VERSION.RELEASE,
        @Field("version") version: String = BuildConfig.VERSION_NAME,
        @Field("extend") extend: Int,
        @Field("signature") signature: String,
    ): Deferred<Response<Unit>>

    @GET("https://ipinfo.io/json")
    fun getIpInfoAsync(): Deferred<Response<IpInfoModelSetting>>

    @Streaming
    @GET()
    suspend fun downloadLargeFile(@Url url: String): ResponseBody

}