package com.example.videoart.batterychargeranimation.di

import com.example.videoart.batterychargeranimation.ui.info.BatteryInfoViewModel
import com.example.videoart.batterychargeranimation.ui.mytheme.MyThemeViewModel
import com.example.videoart.batterychargeranimation.ui.preview.PreviewViewModel
import com.example.videoart.batterychargeranimation.ui.reminder.FullScreenReminderViewModel
import com.example.videoart.batterychargeranimation.ui.splash.SplashViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { SplashViewModel(androidContext()) }
    viewModel { MyThemeViewModel(androidContext()) }
    viewModel { PreviewViewModel(get()) }
    viewModel { BatteryInfoViewModel() }
    viewModel { FullScreenReminderViewModel() }
}