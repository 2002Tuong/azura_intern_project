package com.artgen.app.di

import com.artgen.app.ui.MainViewModel
import com.artgen.app.ui.screen.cropphoto.CropPhotoViewModel
import com.artgen.app.ui.screen.genart.ArtGeneratorViewModel
import com.artgen.app.ui.screen.genart.GenArtSuccessViewModel
import com.artgen.app.ui.screen.imagepicker.ImagePickerViewModel
import com.artgen.app.ui.screen.imagepicker.ImageRepository
import com.artgen.app.ui.screen.language.LanguageSelectionViewModel
import com.artgen.app.ui.screen.main.StylePickerViewModel
import com.artgen.app.ui.screen.onboarding.OnboardingViewModel
import com.artgen.app.ui.screen.premium.PremiumViewModel
import com.artgen.app.ui.screen.reminder.FullScreenReminderViewModel
import com.artgen.app.ui.screen.result.ResultViewModel
import com.artgen.app.ui.screen.settings.SettingViewModel
import com.artgen.app.ui.screen.splash.SplashScreenViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { SplashScreenViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { LanguageSelectionViewModel(get(), get()) }
    viewModel { OnboardingViewModel(get()) }
    viewModel { StylePickerViewModel(get(), get(), get(), get()) }
    viewModel { CropPhotoViewModel(get(), get(), get()) }
    factory { ImageRepository(androidContext()) }
    viewModel { ImagePickerViewModel(get()) }
    viewModel { SettingViewModel() }
    viewModel { ArtGeneratorViewModel(get(), get(), get()) }
    viewModel { ResultViewModel(get(), get(), get(), get()) }
    viewModel { FullScreenReminderViewModel(get(), get()) }
    viewModel { GenArtSuccessViewModel(get(), get())}
    viewModel {PremiumViewModel(get(), get(), get(), get())}
    viewModel {MainViewModel(get(), get())}
}
