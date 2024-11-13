package com.wifi.wificharger.di

import com.wifi.wificharger.data.local.AppDatabase
import com.wifi.wificharger.data.repository.WifiRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { WifiRepository(get()) }
    single { AppDatabase.getInstance(get()) }
    single { AppDatabase.getInstance(get()).wifiDao() }
}