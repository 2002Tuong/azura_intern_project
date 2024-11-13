package com.parallax.hdvideo.wallpapers.di.network

import com.parallax.hdvideo.wallpapers.BuildConfig
import com.parallax.hdvideo.wallpapers.data.db.CacheModel
import com.parallax.hdvideo.wallpapers.data.model.FailedResponse
import com.parallax.hdvideo.wallpapers.di.storage.database.dao.CacheDao
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.toHex
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import com.parallax.hdvideo.wallpapers.utils.AppConstants.BASE_URL
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.network.NetworkUtils
import com.parallax.hdvideo.wallpapers.utils.other.AES
import com.parallax.hdvideo.wallpapers.utils.rx.RxBus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Invocation
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    const val TIME_OUT_IN_SEC: Long = 15
    const val EXPIRED_TIME_CACHE_IN_MILLIS: Long = 1 * 1000 * 3600 * 24 // 1 DAYS
    val userAgent = "TPcom/3.0 " + System.getProperty("http.agent")

    @Singleton
    @Provides
    fun providePostApi(retrofit: Retrofit): ApiClient {
        return retrofit.create(ApiClient::class.java)
    }

    @Singleton
    @Provides
    fun provideRetrofitInterface(httpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().client(httpClient)
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
    }

    @Provides
    fun provideOkHttpClient(interceptor: HttpLoggingInterceptor, request: Interceptor): OkHttpClient =
        OkHttpClient.Builder()
//            .cache(Cache(FileUtils.cacheDir, (10 * 1024 * 1024).toLong())) // 10MB
            .addInterceptor(interceptor)
            .addInterceptor(request)
            .followRedirects(true)
            .connectTimeout(TIME_OUT_IN_SEC, TimeUnit.SECONDS)
            .callTimeout(TIME_OUT_IN_SEC, TimeUnit.SECONDS)
            .build()

    @Provides
    fun provideLoggingInterceptor() =
        HttpLoggingInterceptor().apply { level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE }

    @Provides
    fun listenerResponse(storage: LocalStorage?, cacheDao: CacheDao?) = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val build = chain.request().newBuilder().
                header("Content-Type", "application/json")
                .header("User-Agent", userAgent)

            val authorization = storage?.authorization
            if (authorization != null) {
                build.header("Authorization", authorization)
            }
            val request = build.build()
            val url = request.url
            val host = url.host
            try {
                var check = true
                val paramExpired = url.queryParameter(AppConstants.EXPIRATION)?.toLong()?.also { check = it > 0 }
                var id = ""
                if (cacheDao != null && check) {
                    val invocation = request.tag(Invocation::class.java)
                    val cacheResponse = invocation?.run { method().getAnnotation(CacheResponse::class.java) }
                    if (cacheResponse != null) {
                        val patterns = Pattern.compile(".+${url.host}(.+?)&token.+").matcher(url.toString())
                        id = if (patterns.find()) {
                            patterns.group(1) ?: url.toString()
                        } else {
                            url.toString()
                        }.replace("&mobileid=.+&sex=".toRegex(), "")
                            .replace(AppConstants.SEARCH_IGNORE_CHAR.toRegex(), "")
                            .toLowerCase(Locale.ENGLISH)
                            .toHex(pass = host)
                        val oldData = cacheDao.findCache(id)
                        if (oldData != null && oldData.data.isNotEmpty()) {
                            val expired = AES.dec(oldData.expired, host)?.toLong() ?: 0
                            val currentTime = System.currentTimeMillis()
                            if (expired > currentTime) {
                                val res = AES.dec(oldData.data, host)
                                if (!res.isNullOrEmpty()) {
                                    Logger.d("OkHttp", "from cache")
                                    return getCachedResponse(res, request)
                                }
                            } else {
                                val list = cacheDao.selectExpiredTime().filter { (AES.dec(it.expired, host)?.toLong() ?: 0) < currentTime }
                                cacheDao.delete(list)
                            }
                        }
                    }
                }
                val response = connect(chain, request)
                val body = response.body
                if (!response.isSuccessful || body == null) {
                    val code = response.code
                    RxBus.push(FailedResponse(code, null, null, online = NetworkUtils.isNetworkConnected(), url = url.toString()))
                } else {
                    if (id.isNotEmpty()) {
                        val res = body.string()
                        if (!res.isNullOrEmpty()) {
                            val lastUpdated = System.currentTimeMillis()
                            val expiredTime = lastUpdated + (paramExpired ?: EXPIRED_TIME_CACHE_IN_MILLIS)
                            cacheDao?.insert(CacheModel(id,
                                AES.enc(res, host) ?: "",
                                AES.enc(lastUpdated.toString(), host) ?: "",
                                AES.enc(expiredTime.toString(), host) ?: ""))
                        }
                        return getCachedResponse(res, request)
                    }
                }
                return response
            } catch (e: Exception) {
                val newError = FailedResponse(-1, e.cause, e.message, NetworkUtils.isNetworkConnected(), url = url.toString())
                RxBus.push(newError)
                throw newError
            }
        }
    }

    private fun connect(chain: Interceptor.Chain, request: Request) : Response {
        var errorr: Exception? = null
        try {
            val res = chain.proceed(request)
            if (res.isSuccessful) return res
        } catch (e: Exception) {
            errorr = e
        }
        val hasNetwork = NetworkUtils.isNetworkConnected()
        val message = errorr?.message?.toLowerCase() ?: ""
        if (!hasNetwork || message.contains("canceled")) {
            throw FailedResponse(code = -1, message = message, cause = errorr?.cause, online = hasNetwork)
        } else {
            val url = request.url.toString()
            val newUrl = getFailedDomain(url)
            if (newUrl.isNotEmpty()) {
                return chain.proceed(request.newBuilder().url(newUrl).build())
            } else throw FailedResponse(code = -1, message = message, cause = errorr?.cause, online = hasNetwork)
        }
    }

    private fun getFailedDomain(url: String): String {
        if (url.contains("rest/")) {
            return url.replaceFirst("(http.*)rest/".toRegex(), WallpaperURLBuilder.shared.wallpaperServerFailed) + "&error=true"
        } else if (url.contains("wallpaper/restcache/")) {
            return url.replaceFirst("(http.*)restcache/".toRegex(), WallpaperURLBuilder.shared.videoServerFailed) + "&error=true"
        }
        return ""
    }

    private fun getCachedResponse(cachedData: String, request: Request): Response {
        return Response.Builder().code(200)
            .body(ResponseBody.create("application/json".toMediaTypeOrNull(), cachedData))
            .message("Response")
            .addHeader("Content-Type", "application/json")
            .request(request)
            .protocol(Protocol.HTTP_2)
            .build()
    }

    fun get(url: String, response: ((String) -> Unit), failure: ((Int) -> Unit)? = null) {
        val request = Request.Builder().url(url).addHeader("user-agent", userAgent).build()
        val client = provideOkHttpClient(provideLoggingInterceptor(), listenerResponse(null, null))
        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                failure?.invoke(-1)
            }

            override fun onResponse(call: Call, res: Response) {
                val code = res.code
                val body = res.body
                if (res.isSuccessful && body != null) {
                    response.invoke(body.string())
                } else {
                    failure?.invoke(code)
                }
            }
        })
    }

}