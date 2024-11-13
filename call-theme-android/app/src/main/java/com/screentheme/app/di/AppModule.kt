package com.screentheme.app.di

import com.calltheme.app.ui.picklanguage.LanguageProvider
import com.screentheme.app.notification.NotificationManager
import com.screentheme.app.utils.helpers.CountTimeHelper
import com.screentheme.app.utils.helpers.FlashController
import com.screentheme.app.utils.helpers.RingtoneController
import com.screentheme.app.utils.helpers.ThemeManager
import com.screentheme.app.utils.helpers.VibrateController
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { FlashController(androidContext()) }
    single { RingtoneController(androidContext()) }
    single { ThemeManager(androidContext())}
    single { VibrateController(androidContext()) }
    single { NotificationManager(androidContext()) }
    single { LanguageProvider(androidContext()) }
    single { CountTimeHelper() }
}