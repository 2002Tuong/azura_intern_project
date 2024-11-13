package com.bloodpressure.app.di

import com.bloodpressure.app.screen.MainViewModel
import com.bloodpressure.app.screen.aboutme.AboutMeViewModel
import com.bloodpressure.app.screen.alarm.AlarmViewModel
import com.bloodpressure.app.screen.barcodescan.BarcodeScanViewModel
import com.bloodpressure.app.screen.bloodpressure.BloodPressureFeatureViewModel
import com.bloodpressure.app.screen.bloodpressure.BloodPressureViewModel
import com.bloodpressure.app.screen.bloodsugar.BloodSugarFeatureViewModel
import com.bloodpressure.app.screen.bloodsugar.SCOPE_NAME
import com.bloodpressure.app.screen.bloodsugar.add.BloodSugarDetailViewModel
import com.bloodpressure.app.screen.bloodsugar.history.BloodSugarStatisticViewModel
import com.bloodpressure.app.screen.bmi.add.AddBmiScreenViewModel
import com.bloodpressure.app.screen.bmi.history.BMIHistoryViewModel
import com.bloodpressure.app.screen.bmi.historyandstatistics.BMIHistoryAndStatisticsViewModel
import com.bloodpressure.app.screen.bmi.listfeature.BMIListFeatureViewModel
import com.bloodpressure.app.screen.bmi.statistics.BMIStatisticsViewModel
import com.bloodpressure.app.screen.fullscreenreminder.FullScreenReminderViewModel
import com.bloodpressure.app.screen.heartrate.HeartRateViewModel
import com.bloodpressure.app.screen.heartrate.add.AddHeartRateViewModel
import com.bloodpressure.app.screen.heartrate.detail.HeartRateDetailViewModel
import com.bloodpressure.app.screen.heartrate.history.HeartRateHistoryViewModel
import com.bloodpressure.app.screen.heartrate.measure.HeartRateMeasureViewModel
import com.bloodpressure.app.screen.heartrate.result.HeartRateResultViewModel
import com.bloodpressure.app.screen.heartrate.statistics.HeartRateStatisticsViewModel
import com.bloodpressure.app.screen.heartrate.trends.HeartRateTrendsViewModel
import com.bloodpressure.app.screen.history.HistoryViewModel
import com.bloodpressure.app.screen.home.HomeViewModel
import com.bloodpressure.app.screen.home.info.HomeInfoViewModel
import com.bloodpressure.app.screen.home.info.InfoDetailViewModel
import com.bloodpressure.app.screen.home.info.InfoViewModel
import com.bloodpressure.app.screen.home.settings.SettingViewModel
import com.bloodpressure.app.screen.home.tracker.TrackerViewModel
import com.bloodpressure.app.screen.language.LanguageSelectionViewModel
import com.bloodpressure.app.screen.onboarding.OnboardingViewModel
import com.bloodpressure.app.screen.premium.PremiumViewModel
import com.bloodpressure.app.screen.record.AddRecordViewModel
import com.bloodpressure.app.screen.record.note.NoteViewModel
import com.bloodpressure.app.screen.splash.SplashScreenViewModel
import com.bloodpressure.app.screen.waterreminder.WaterAlarmViewModel
import com.bloodpressure.app.screen.waterreminder.WaterReminderViewModel
import com.bloodpressure.app.screen.waterreminder.history.WaterHistoryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { SplashScreenViewModel(get(), get(), get(), get()) }
    viewModel { LanguageSelectionViewModel(get(), get()) }
    viewModel { SettingViewModel(get(), get()) }
    viewModel { _ -> InfoViewModel(get(), get(), get()) }
    viewModel { TrackerViewModel(get(), get(), get()) }
    viewModel { AddRecordViewModel(get(), get(), get(), get(), get()) }
    viewModel { HistoryViewModel(get(), get(), get()) }
    viewModel { NoteViewModel(get()) }
    viewModel { InfoDetailViewModel(get(), get(), get(), get(), get()) }
    viewModel { PremiumViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
    viewModel { OnboardingViewModel(get()) }
    viewModel { MainViewModel(get(), get()) }
    viewModel { BloodPressureViewModel(get(), get(), get(), get()) }
    viewModel { HeartRateViewModel(get(), get(), get()) }
    viewModel { HeartRateMeasureViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { AddHeartRateViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { HeartRateHistoryViewModel(get(), get(), get(), get()) }
    viewModel { HeartRateTrendsViewModel(get(), get()) }
    viewModel { HeartRateStatisticsViewModel(get()) }
    viewModel { HeartRateResultViewModel(get(), get(), get(), get(), get()) }
    viewModel { HeartRateDetailViewModel(get(), get(), get(), get(), get()) }
    viewModel { HomeInfoViewModel(get()) }
    viewModel { AlarmViewModel(get(), get()) }
    viewModel { AboutMeViewModel(get()) }
    viewModel { BMIListFeatureViewModel(get(), get(), get(), get()) }
    viewModel { AddBmiScreenViewModel(get(), get(), get(), get(), get()) }
    viewModel { BMIHistoryViewModel(get(), get(), get()) }
    viewModel { BMIHistoryAndStatisticsViewModel(get(), get(), get())}
    viewModel { BMIStatisticsViewModel(get(), get())}
    viewModel { FullScreenReminderViewModel(get(), get()) }
    viewModel { WaterReminderViewModel(get(), get(), get(), get())}
    viewModel { WaterHistoryViewModel(get(), get(), get())}
    viewModel { BloodSugarStatisticViewModel(get(), get(), get()) }
    viewModel { WaterAlarmViewModel(get(), get())}
    viewModel { BloodSugarFeatureViewModel(get(), get(), get()) }
    viewModel { BloodPressureFeatureViewModel(get(), get(), get()) }
    viewModel {BarcodeScanViewModel(get())}
    scope(named(SCOPE_NAME)) {
        scoped { BloodSugarDetailViewModel(get(), get(), get(), get()) }
    }
}
