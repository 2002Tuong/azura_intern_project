package com.artgen.app.di

import android.content.Context
import coil.ImageLoader
import coil.request.CachePolicy
import com.artgen.app.BuildConfig
import com.artgen.app.R
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.data.remote.interceptor.AuthorizationAuthenticator
import com.artgen.app.data.remote.interceptor.AuthorizationInterceptor
import com.artgen.app.data.remote.interceptor.TokenRepository
import com.lyft.kronos.KronosClock
import java.io.File
import java.util.concurrent.TimeUnit
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val httpModule = module {
    factory { provideFile(androidContext()) }
    factory { provideCache(get()) }
    factory { provideHttpLogging() }
    factory { provideTokenRepository(get(), androidApplication()) }
    factory { provideAuthorizationInterceptor(get(), androidApplication(), get()) }
    factory { provideAuthenticator(get(), get()) }
    factory { provideUserAgentHeaderInterceptor(androidApplication()) }
    factory { provideOkHttpClient(get(), get(), get(), get(), get()) }
    factory { provideImageLoader(androidApplication()) }
}

fun provideImageLoader(context: Context) = ImageLoader.Builder(context)
    .diskCachePolicy(CachePolicy.ENABLED)
    .respectCacheHeaders(false)
    .build()

fun provideOkHttpClient(
    cache: Cache,
    loggingInterceptor: HttpLoggingInterceptor,
    authorizationInterceptor: AuthorizationInterceptor,
    authenticator: AuthorizationAuthenticator,
    userAgentInterceptor: Interceptor,
): OkHttpClient {
    val builder = OkHttpClient.Builder().apply {
        cache(cache)
        addNetworkInterceptor(loggingInterceptor)
        addInterceptor(userAgentInterceptor)
        addInterceptor(authorizationInterceptor)
        authenticator(authenticator)
        connectTimeout(DEFAULT_REQUEST_TIMEOUT_IN_SECOND, TimeUnit.SECONDS)
        readTimeout(DEFAULT_REQUEST_TIMEOUT_IN_SECOND, TimeUnit.SECONDS)
        writeTimeout(DEFAULT_REQUEST_TIMEOUT_IN_SECOND, TimeUnit.SECONDS)
    }
    return builder.build()
}

fun provideTokenRepository(kronosClock: KronosClock, context: Context) =
    TokenRepository(kronosClock, context)

fun provideAuthorizationInterceptor(
    tokenRepository: TokenRepository,
    context: Context,
    dataStore: AppDataStore
) = AuthorizationInterceptor(tokenRepository, context, dataStore)

fun provideAuthenticator(tokenRepository: TokenRepository, dataStore: AppDataStore) =
    AuthorizationAuthenticator(tokenRepository, dataStore)

fun provideUserAgentHeaderInterceptor(context: Context): Interceptor {
    return Interceptor {
        val builder = it.request().newBuilder()
        builder.addHeader(
            HEADER_USER_AGENT,
            String.format(
                USER_AGENT_FORMAT,
                context.getString(R.string.app_name),
                BuildConfig.VERSION_NAME,
                context.packageName,
                "${BuildConfig.VERSION_CODE}",
                android.os.Build.VERSION.RELEASE,
                okhttp3.OkHttp.VERSION,
            ),
        )
        it.proceed(builder.build())
    }
}

fun provideCache(cachePath: File): Cache {
    return Cache(cachePath, CACHE_SIZE_IN_MB * 1000 * 1000)
}

fun provideFile(context: Context): File {
    return File(context.cacheDir, CACHE_FOLDER_NAME).apply {
        mkdir()
    }
}

fun provideHttpLogging(): HttpLoggingInterceptor {
    val logger = HttpLoggingInterceptor()
    if (BuildConfig.DEBUG) {
        logger.setLevel(HttpLoggingInterceptor.Level.BODY)
    } else {
        logger.setLevel(HttpLoggingInterceptor.Level.NONE)
    }
    return logger
}

private const val HEADER_USER_AGENT = "User-Agent"
private const val USER_AGENT_FORMAT = "%s/%s (%s;build:%s;Android %s) okhttp/%s"
private const val CACHE_SIZE_IN_MB = 10L
private const val CACHE_FOLDER_NAME = "HttpCache"
private const val DEFAULT_REQUEST_TIMEOUT_IN_SECOND = 60L
