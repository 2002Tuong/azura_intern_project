package com.example.videoart.batterychargeranimation.di

import com.example.videoart.batterychargeranimation.helper.BatteryHelper
import com.example.videoart.batterychargeranimation.helper.audio_manager.AudioManager
import com.example.videoart.batterychargeranimation.helper.audio_manager.AudioManagerImpl
import com.example.videoart.batterychargeranimation.notification.NotificationController
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.slideshowmaker.slideshow.data.remote.AnimationServerInterface
import com.slideshowmaker.slideshow.modules.music_player.MusicPlayer
import com.slideshowmaker.slideshow.modules.music_player.MusicPlayerImpl
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val localModule = module {
    single<AudioManager> {AudioManagerImpl()}
    single<MusicPlayer> { MusicPlayerImpl() }
    single<BatteryHelper> { BatteryHelper() }
    single<AnimationServerInterface>{
        val retrofit = Retrofit.Builder()
            .client(OkHttpClient.Builder().build())
            .baseUrl("https://charging-battery.ap-south-1.linodeobjects.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
        retrofit.create(AnimationServerInterface::class.java)
    }

    single<NotificationController>{ NotificationController(androidContext()) }
}