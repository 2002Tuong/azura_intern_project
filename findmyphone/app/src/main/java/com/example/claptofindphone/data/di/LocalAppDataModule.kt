package com.example.claptofindphone.data.di

import android.content.Context
import android.content.SharedPreferences
import com.example.claptofindphone.ads.BannerAdsController
import com.example.claptofindphone.data.local.PreferenceSupplier
import com.example.claptofindphone.data.remote.RemoteConfigProvider
import com.example.claptofindphone.presenter.result.ActiveManager
import com.example.claptofindphone.utils.AdsHelper
import com.example.claptofindphone.utils.LanguageSupporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocalAppDataModule {

    @Singleton
    @Provides
    fun getSharedPreference(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(PreferenceSupplier.KEY_FILE, Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideLanguageUtils(preferenceSupplier: PreferenceSupplier): LanguageSupporter = LanguageSupporter(preferenceSupplier)

    @Singleton
    @Provides
    fun provideAdsUtils(preferenceSupplier: PreferenceSupplier, @ApplicationContext context: Context, bannerAdsController: BannerAdsController): AdsHelper = AdsHelper(preferenceSupplier, bannerAdsController, context)

    @Singleton
    @Provides
    fun provideRemoteConfig(preferenceSupplier: PreferenceSupplier) = RemoteConfigProvider(preferenceSupplier)

    @Singleton
    @Provides
    fun provideActiveController(preferenceSupplier: PreferenceSupplier) = ActiveManager(preferenceSupplier)

    @Singleton
    @Provides
    fun provideBannerManager(@ApplicationContext context: Context) = BannerAdsController(context)

}