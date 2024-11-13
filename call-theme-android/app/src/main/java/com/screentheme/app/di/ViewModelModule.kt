package com.screentheme.app.di

import com.calltheme.app.notification.FullScreenReminderViewModel
import com.calltheme.app.ui.mydesign.ApplyThemeViewModel
import com.calltheme.app.ui.mydesign.MyDesignViewModel
import com.calltheme.app.ui.pickavatar.PickAllAvatarsViewModel
import com.calltheme.app.ui.pickavatar.PickAvatarViewModel
import com.calltheme.app.ui.pickbackground.PickBackgroundViewModel
import com.calltheme.app.ui.picklanguage.PickLanguageViewModel
import com.calltheme.app.ui.pickringtone.PickRingtonesViewModel
import com.calltheme.app.ui.previewcall.PreviewCallViewModel
import com.calltheme.app.ui.setcalltheme.SaveThemeSuccessfullyViewModel
import com.calltheme.app.ui.setcalltheme.SetCallThemeViewModel
import com.calltheme.app.ui.splash.SplashViewModel
import com.calltheme.app.ui.theme.SaveRemoteThemeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { SaveRemoteThemeViewModel() }
    viewModel { SplashViewModel(androidContext()) }
    viewModel { SetCallThemeViewModel() }
    viewModel { SaveThemeSuccessfullyViewModel() }
    viewModel { PreviewCallViewModel() }
    viewModel { PickRingtonesViewModel(get()) }
    viewModel { PickBackgroundViewModel(androidContext()) }
    viewModel { PickAvatarViewModel(androidContext()) }
    viewModel { PickAllAvatarsViewModel(get()) }
    viewModel { MyDesignViewModel(get()) }
    viewModel { ApplyThemeViewModel() }
    viewModel { FullScreenReminderViewModel() }
}