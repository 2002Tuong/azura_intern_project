package com.artgen.app.di

import android.content.Context
import com.artgen.app.R
import com.artgen.app.data.remote.ApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.lyft.kronos.AndroidClockFactory
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    factory { provideGSon() }
    factory { provideGSonConverter(get()) }
    single { provideRetrofit(androidApplication(), get(), get()) }
    single { provideKronosClock(androidApplication()) }
    single { provideAPIService(get()) }
}

fun provideAPIService(retrofit: Retrofit): ApiService {
    return retrofit.create(ApiService::class.java)
}

fun provideRetrofit(
    context: Context,
    okHttpClient: OkHttpClient,
    gSonConverterFactory: GsonConverterFactory,
): Retrofit {
    return Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(context.getString(R.string.base_endpoint))
        .addConverterFactory(gSonConverterFactory)
        .build()
}

fun provideGSon(): Gson = GsonBuilder().create()

fun provideGSonConverter(gSon: Gson): GsonConverterFactory = GsonConverterFactory.create(gSon)

fun provideKronosClock(context: Context) = AndroidClockFactory.createKronosClock(context)
