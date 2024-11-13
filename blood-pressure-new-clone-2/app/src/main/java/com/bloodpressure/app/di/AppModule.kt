package com.bloodpressure.app.di

import com.bloodpressure.app.ads.AdsInitializer
import com.bloodpressure.app.ads.BillingClientLifecycle
import com.bloodpressure.app.ads.InterAdsManager
import com.bloodpressure.app.ads.NativeAdsManager
import com.bloodpressure.app.ads.OpenAdsManager
import com.bloodpressure.app.camera.CameraNew
import com.bloodpressure.app.camera.CameraSupport
import com.bloodpressure.app.camera.HeartRate
import com.bloodpressure.app.camera.Processing
import com.bloodpressure.app.camera.ProcessingSupport
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.local.BpRoomDatabase
import com.bloodpressure.app.data.local.BpRoomDatabaseProvider
import com.bloodpressure.app.data.local.BpTypeConverters
import com.bloodpressure.app.data.local.DatabaseExporter
import com.bloodpressure.app.data.local.NoteConverters
import com.bloodpressure.app.data.local.SampleRecordProvider
import com.bloodpressure.app.data.remote.RemoteConfig
import com.bloodpressure.app.data.repository.AlarmRepository
import com.bloodpressure.app.data.repository.BMIRepository
import com.bloodpressure.app.data.repository.bloodsugar.BloodSugarRepository
import com.bloodpressure.app.data.repository.BpRepository
import com.bloodpressure.app.data.repository.HeartRateRepository
import com.bloodpressure.app.data.repository.WaterReminderRepository
import com.bloodpressure.app.data.repository.bloodsugar.IBloodSugarRepository
import com.bloodpressure.app.fcm.NotificationController
import com.bloodpressure.app.screen.barcodescan.ScannerSupport
import com.bloodpressure.app.screen.home.info.InfoFactory
import com.bloodpressure.app.screen.language.LanguageProvider
import com.bloodpressure.app.screen.updateapp.AppUpdateChecker
import com.bloodpressure.app.utils.AlarmingManager
import com.bloodpressure.app.utils.DefaultReminderManager
import com.bloodpressure.app.utils.LanguageManager
import com.bloodpressure.app.utils.SoundManager
import com.bloodpressure.app.utils.TextFormatter
import com.bloodpressure.app.utils.VibrationManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { BillingClientLifecycle(androidContext(), get()) }
    single { RemoteConfig(androidContext()) }
    single { AppDataStore(androidContext()) }
    factory { LanguageManager() }
    single { InfoFactory() }
    factory { TextFormatter(androidContext()) }
    single { AdsInitializer() }
    single { OpenAdsManager(androidContext(), get(), get()) }
    factory { InterAdsManager(androidContext(), get(), get()) }
    factory { NativeAdsManager(androidContext(), get()) }
    single { LanguageProvider() }
    single { AppUpdateChecker(get(), get()) }
    factory { SampleRecordProvider(get()) }
    single { NotificationController(androidContext(), get(), get()) }
    single<ProcessingSupport> { Processing() }
    single<CameraSupport> { CameraNew(androidContext(), get()) }
    single { HeartRate(get()) }
    single { VibrationManager(androidContext()) }
    single { SoundManager(androidContext()) }
    single { DefaultReminderManager(androidContext(), get()) }
    single {ScannerSupport(get())}
}

val databaseModule = module {
    single { BpTypeConverters() }
    single { NoteConverters() }
    single { BpRoomDatabaseProvider(androidContext()) }
    single { get<BpRoomDatabaseProvider>().provide() }
    single { get<BpRoomDatabase>().recordDao() }
    single { get<BpRoomDatabase>().noteDao() }
    single { get<BpRoomDatabase>().alarmDao() }
    single { BpRepository(get(), get(), get(), get()) }
    single { DatabaseExporter(androidContext(), get(), get(), get()) }
    single { get<BpRoomDatabase>().heartRateDao() }
    single { HeartRateRepository(get(), get(), get(), get()) }
    single { AlarmingManager(androidContext(), get()) }
    single { AlarmRepository(get()) }
    single { WaterReminderRepository(get(), get()) }
    single { get<BpRoomDatabase>().waterReminderDao() }
    single { get<BpRoomDatabase>().bmiRecordDao() }
    single { BMIRepository(get(), get(), get(), get()) }
    single { get<BpRoomDatabase>().bloodSugarRecordDao() }
    single<IBloodSugarRepository> { BloodSugarRepository(get(), get()) }
}
