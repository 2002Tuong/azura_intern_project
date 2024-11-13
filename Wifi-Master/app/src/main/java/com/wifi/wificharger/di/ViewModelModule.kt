package com.wifi.wificharger.di

import com.wifi.wificharger.ui.home.HomeViewModel
import com.wifi.wificharger.ui.main.MainViewModel
import com.wifi.wificharger.ui.onboarding.WalkThroughViewModel
import com.wifi.wificharger.ui.picklanguage.PickLanguageViewModel
import com.wifi.wificharger.ui.splash.SplashViewModel
import com.wifi.wificharger.ui.wifis.ListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
//    viewModel { FullScreenReminderViewModel(get(), get()) }
    viewModel { SplashViewModel(get()) }
    viewModel { PickLanguageViewModel(get()) }
    viewModel { WalkThroughViewModel(get()) }
    viewModel { ListViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
//    viewModel { GenArtViewModel(get(), get(), get()) }
//    viewModel { EditPhotoViewModel(get(), get(), get()) }
//    viewModel { ResultViewModel(get(), get(), get()) }
//    viewModel { ImageSelectionViewModel(get(), get()) }
//    viewModel { GenArtSuccessViewModel(get(), get()) }
//    viewModel { SubscriptionViewModel(get(), get(), get()) }
}
