package com.bloodpressure.app.screen

import android.content.Intent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.BMIOptionType
import com.bloodpressure.app.data.model.BloodPressureOption
import com.bloodpressure.app.data.model.BloodSugarOption
import com.bloodpressure.app.data.model.HeartRateOptionType
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.screen.aboutme.AboutMeScreen
import com.bloodpressure.app.screen.alarm.AlarmScreen
import com.bloodpressure.app.screen.barcodescan.BarcodeScanScreen
import com.bloodpressure.app.screen.bloodpressure.BloodPressureFeatureScreen
import com.bloodpressure.app.screen.bloodpressure.BloodPressureScreen
import com.bloodpressure.app.screen.bloodsugar.BloodSugarScreen
import com.bloodpressure.app.screen.bloodsugar.add.BloodSugarDetailScreen
import com.bloodpressure.app.screen.bloodsugar.add.BloodSugarTargetsScreen
import com.bloodpressure.app.screen.bloodsugar.history.BloodSugarHistoryScreen
import com.bloodpressure.app.screen.bloodsugar.history.BloodSugarStatisticScreen
import com.bloodpressure.app.screen.bmi.BMIScreen
import com.bloodpressure.app.screen.bmi.add.AddBmiScreen
import com.bloodpressure.app.screen.bmi.history.BMIHistoryScreen
import com.bloodpressure.app.screen.bmi.historyandstatistics.BMIHistoryAndStatisticsScreen
import com.bloodpressure.app.screen.comingsoon.ComingSoonScreen
import com.bloodpressure.app.screen.heartrate.HeartRateScreen
import com.bloodpressure.app.screen.heartrate.add.AddHeartRateScreen
import com.bloodpressure.app.screen.heartrate.detail.HeartRateDetailScreen
import com.bloodpressure.app.screen.heartrate.history.HeartRateHistoryScreen
import com.bloodpressure.app.screen.heartrate.measure.HeartRateMeasureScreen
import com.bloodpressure.app.screen.heartrate.result.HeartRateResultScreen
import com.bloodpressure.app.screen.history.HistoryScreen
import com.bloodpressure.app.screen.home.HomeScreen
import com.bloodpressure.app.screen.home.info.HomeInfoType
import com.bloodpressure.app.screen.home.info.InfoDetailScreen
import com.bloodpressure.app.screen.home.info.InfoItemType
import com.bloodpressure.app.screen.home.info.InfoScreen
import com.bloodpressure.app.screen.home.tracker.TrackerType
import com.bloodpressure.app.screen.language.EditLanguageScreen
import com.bloodpressure.app.screen.language.LanguageSelectionScreen
import com.bloodpressure.app.screen.onboarding.OnboardingScreen
import com.bloodpressure.app.screen.premium.PremiumScreen
import com.bloodpressure.app.screen.record.AddRecordScreen
import com.bloodpressure.app.screen.record.note.EditNoteScreen
import com.bloodpressure.app.screen.splash.SplashScreen
import com.bloodpressure.app.screen.waterreminder.WaterAlarmScreen
import com.bloodpressure.app.screen.waterreminder.WaterReminderScreen
import com.bloodpressure.app.screen.waterreminder.history.WaterFullHistoryScreen
import com.bloodpressure.app.screen.waterreminder.history.WaterHistoryScreen
import com.bloodpressure.app.utils.JsonProvider
import com.bloodpressure.app.utils.LocalAdsManager
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun MainNavigationHost(
    navController: NavHostController,
    onExitApp: () -> Unit,
    hideNavigationBar: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val adsManager = LocalAdsManager.current
    LaunchedEffect(key1 = navBackStackEntry, block = {
        adsManager.reloadBannerAds()
    })
    NavHost(
        navController = navController,
        startDestination = MainRoute.SPLASH,
        enterTransition = { defaultEnterTransition() },
        exitTransition = { defaultExitTransition() },
        popEnterTransition = { defaultPopEnterTransition() },
        popExitTransition = { defaultPopExitTransition() },
    ) {
        composable(
            MainRoute.SPLASH,
            deepLinks = listOf(
                navDeepLink { uriPattern = UriPattern.BLOOD_PRESSURE },
                navDeepLink { uriPattern = UriPattern.HEART_RATE },
                navDeepLink { uriPattern = UriPattern.ADD_RECORD },
                navDeepLink { uriPattern = UriPattern.MEASURE_HEART_RATE },
                navDeepLink { uriPattern = UriPattern.BLOOD_SUGAR },
                navDeepLink { uriPattern = UriPattern.BMI },
                navDeepLink { uriPattern = UriPattern.HISTORY },
                navDeepLink { uriPattern = UriPattern.WATER_REMINDER },
                navDeepLink {
                    uriPattern = "${UriPattern.BP_INFO}?feature={feature}"
                }

            )
        ) {
            val deeplinkIntent = it.arguments?.get(
                "android-support-nav:controller:deepLinkIntent"
            ) as? Intent
            val uri = deeplinkIntent?.data?.toString()
            SplashScreen(onNavigateNext = { route ->
                navController.navigate(route) {
                    popUpTo(MainRoute.SPLASH) { inclusive = true }
                }
                if (route == MainRoute.HOME && uri != null) {
                    val nextRoute = UriPattern.getRouteFromUri(uri)
                    navController.navigate(nextRoute)
                }
            })
        }

        composable(MainRoute.LANGUAGE_SELECTION) {
            LanguageSelectionScreen(
                goNext = { route ->
                    navController.navigate(route) {
                        popUpTo(MainRoute.LANGUAGE_SELECTION) { inclusive = true }
                    }
                },
                onNavigateUp = { onExitApp() },
                hideNavigationBar = hideNavigationBar
            )
        }

        composable(MainRoute.HOME) {
            HomeScreen(
                onNavigateUp = { onExitApp() },
                onNavigateToThankYou = {
                    navController.navigate(MainRoute.THANK_YOU)
                },
                onNavigateToLanguage = {
                    navController.navigate(MainRoute.LANGUAGE_UPDATE)
                },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                },
                onTrackerClick = {

                    when (it) {
                        TrackerType.BLOOD_PRESSURE -> {
                            navController.navigate(MainRoute.BLOOD_PRESSURE)
                        }

                        TrackerType.HEART_RATE -> {
                            navController.navigate(MainRoute.HEART_RATE)
                        }

                        TrackerType.WEIGHT_BMI -> {
                            navController.navigate(MainRoute.BARCODE_SCAN)
                        }

                        TrackerType.WATER_REMINDER -> {
                            navController.navigate(MainRoute.WATER_REMINDER)
                        }

                        TrackerType.BLOOD_SUGAR -> {
                            navController.navigate(MainRoute.BLOOD_SUGAR)
                        }

                        else -> {
                            navController.navigate("${MainRoute.COMING_SOON}?titleResId=${it.titleRes}")
                        }
                    }
                },
                onHomeInfoItemClick = {
                    when (it) {
                        HomeInfoType.BLOOD_PRESSURE -> {
                            navController.navigate("${MainRoute.BLOOD_PRESSURE_INFO}?feature=${HomeInfoType.BLOOD_PRESSURE}")
                        }

                        HomeInfoType.HEART_RATE -> {
                            navController.navigate("${MainRoute.BLOOD_PRESSURE_INFO}?feature=${HomeInfoType.HEART_RATE}")
                        }

                        HomeInfoType.WEIGHT_BMI -> {
                            navController.navigate("${MainRoute.BLOOD_PRESSURE_INFO}?feature=${HomeInfoType.WEIGHT_BMI}")
                        }

                        HomeInfoType.BLOOD_SUGAR -> {
                            navController.navigate("${MainRoute.BLOOD_PRESSURE_INFO}?feature=${HomeInfoType.BLOOD_SUGAR}")
                        }

                        HomeInfoType.WATER_REMINDER -> {
                            navController.navigate("${MainRoute.BLOOD_PRESSURE_INFO}?feature=${HomeInfoType.WATER_REMINDER}")
                        }

                        else -> {
                            navController.navigate("${MainRoute.BLOOD_PRESSURE_INFO}?feature=${HomeInfoType.HEART_RATE}")
                        }
                    }
                },
                onAlarmClick = {
                    navController.navigate(MainRoute.ALARM)
                },
                onAboutMeClick = {
                    navController.navigate(MainRoute.ABOUT_ME)
                }
            )
        }

        composable(
            route = MainRoute.ADD_RECORD,
        ) {
            AddRecordScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToAddNote = {
                    navController.navigate(MainRoute.EDIT_NOTE)
                },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                },
                title = stringResource(id = R.string.add_record),
                onSaveRecord = {
                    navController.navigate(MainRoute.BLOOD_PRESSURE_FEATURE)
                }
            )
        }

        composable(route = MainRoute.BLOOD_SUGAR) {
            BloodSugarScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                },
                onItemOptionClick = { type ->
                    when (type) {
                        BloodSugarOption.ADD -> {
                            navController.navigate("${MainRoute.BLOOD_SUGAR_ADD_ANALYZE}?id=-1")
                        }

                        BloodSugarOption.HISTORY -> {
                            navController.navigate(route = MainRoute.BLOOD_SUGAR_HISTORY)
                        }

                        BloodSugarOption.TRENDS -> {
                            navController.navigate(route = MainRoute.BLOOD_SUGAR_STATISTIC)
                        }
                    }
                },
            )
        }

        composable(
            route = "${MainRoute.BLOOD_SUGAR_ADD_ANALYZE}?id={id}&isEditing={isEditing}",
            arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("isEditing") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id")
            val isEdit = backStackEntry.arguments?.getBoolean("isEditing") ?: false
            BloodSugarDetailScreen(
                recordId = id,
                onNavigateUp = { navController.navigateUp() },
                title = if (isEdit) stringResource(id = R.string.edit) else stringResource(id = R.string.add_record),
                onTargetSelected = {
                    navController.navigate(
                        route = MainRoute.BLOOD_SUGAR_DETAIL
                    )
                },
                onSaveRecord = {
                    navController.navigateUp()
                    if (!isEdit) {
                        navController.navigate(
                            route = MainRoute.BLOOD_SUGAR_STATISTIC
                        )
                    }
                },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                },
                onNavigateToAddNote = { navController.navigate(MainRoute.EDIT_NOTE) },
            )
        }

        composable(MainRoute.BLOOD_SUGAR_STATISTIC) {
            BloodSugarStatisticScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToRecordDetail = { recordId ->
                    navController.navigate("${MainRoute.BLOOD_SUGAR_ADD_ANALYZE}?id=$recordId&isEditing=${true}")
                },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                },
                onNavigateToHistory = {
                    navController.navigate(MainRoute.BLOOD_SUGAR_HISTORY)
                }
            )
        }

        composable(MainRoute.BLOOD_SUGAR_HISTORY) {
            BloodSugarHistoryScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToRecordDetail = { recordId ->
                    navController.navigate("${MainRoute.BLOOD_SUGAR_ADD_ANALYZE}?id=$recordId&isEditing=${true}")
                },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                }
            )
        }

        composable(
            MainRoute.BLOOD_SUGAR_DETAIL
        ) {
            BloodSugarTargetsScreen(
                onNavigateUp = {
                    navController.navigateUp()
                },
                title = stringResource(id = R.string.edit_target_range)
            )
        }

        composable(MainRoute.LANGUAGE_UPDATE) {
            EditLanguageScreen(onNavigateUp = { navController.navigateUp() })
        }

        composable(
            route = "${MainRoute.INFO_DETAIL}/{id}?feature={feature}",
            arguments = listOf(navArgument("feature") {
                type = NavType.EnumType(InfoItemType::class.java)
            }),
        ) {
            InfoDetailScreen(onNavigateUp = { navController.navigateUp() })
        }

        composable(
            route = MainRoute.HISTORY,
            deepLinks = listOf()
        ) {
            HistoryScreen(onNavigateUp = { navController.navigateUp() },
                onNavigateToRecordDetail = { id ->
                    navController.navigate(
                        route = "${MainRoute.RECORD_DETAIL}/$id"
                    )
                },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                }
            )
        }

        composable(
            route = "${MainRoute.RECORD_DETAIL}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) {
            AddRecordScreen(
                onNavigateUp = {
                    navController.navigateUp()
                },
                onNavigateToAddNote = {
                    navController.navigate(MainRoute.EDIT_NOTE)
                },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                },
                title = stringResource(id = R.string.update_record)
            )
        }

        composable(route = MainRoute.EDIT_NOTE) {
            EditNoteScreen(onNavigateUp = { navController.navigateUp() })
        }

        composable(route = MainRoute.PREMIUM) {
            PremiumScreen(onNavigateUp = { navController.navigateUp() })
        }

        composable(route = MainRoute.ONBOARDING) {
            OnboardingScreen(
                onNavigateToMain = {
                    navController.navigate(MainRoute.HOME) {
                        popUpTo(MainRoute.ONBOARDING) { inclusive = true }
                    }
                },
                onNavigateUp = { onExitApp() },
                hideNavigationBar = hideNavigationBar
            )
        }

        composable(route = MainRoute.THANK_YOU) {
            ThankYouScreen(onNavigateUp = { onExitApp() })
        }

        composable(route = MainRoute.BLOOD_PRESSURE) {
            BloodPressureFeatureScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                },
                onItemOptionClick = { type ->
                    when (type) {
                        BloodPressureOption.TRENDS -> {
                            navController.navigate(MainRoute.BLOOD_PRESSURE_FEATURE)
                        }

                        BloodPressureOption.ADD -> {
                            navController.navigate(MainRoute.ADD_RECORD)
                        }

                        BloodPressureOption.HISTORY -> {
                            navController.navigate(MainRoute.HISTORY)
                        }
                    }
                },
            )
        }

        composable(
            route = MainRoute.BLOOD_PRESSURE_FEATURE,
        ) {
            BloodPressureScreen(
                onNavigateUp = { navController.navigateUp() },
                onItemClick = { id ->
                    navController.navigate(
                        route = "${MainRoute.RECORD_DETAIL}/$id"
                    )
                },
                onNavigateToPremium = { navController.navigate(MainRoute.PREMIUM) },
                onNavigateToHistory = {
                    navController.navigate(MainRoute.HISTORY)
                }
            )
        }

        composable(
            route = MainRoute.HEART_RATE,
        ) {
            HeartRateScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                },
                onHeartRateOptionClick = { heartRateOption ->

                    when (heartRateOption) {
                        HeartRateOptionType.MEASURE_NOW -> {
                            navController.navigate(MainRoute.MEASURE_HEART_RATE)
                        }

                        HeartRateOptionType.ADD_MANUALLY -> {
                            navController.navigate("${MainRoute.ADD_RATE_MEASURE}?heartrate=74")
                        }

                        HeartRateOptionType.TRENDS -> {
                            navController.navigate(MainRoute.HEART_RATE_DETAIL)
                        }

                        HeartRateOptionType.HISTORY -> {
                            navController.navigate(MainRoute.HEART_RATE_HISTORY)
                        }
                    }

                })
        }

        composable(route = MainRoute.WATER_REMINDER) {
            WaterReminderScreen(
                onNavigateUp = { navController.navigateUp() },
                onHistoryClick = {
                    navController.navigate(MainRoute.WATER_HISTORY)
                },
                onReminderItemClick = {
                    navController.navigate(MainRoute.WATER_ALARM)
                },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                },
            )
        }

        composable(route = MainRoute.WATER_HISTORY) {
            WaterHistoryScreen(
                onNavigateUp = { navController.navigateUp() },
                onViewAllHistory = {navController.navigate(MainRoute.WATER_FULL_HISTORY)},
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                },
                onReminderItemClick = {
                    navController.navigate(MainRoute.WATER_ALARM)
                },
            )
        }

        composable(route = MainRoute.WATER_ALARM) {
            WaterAlarmScreen(
                onNavigateUp = { navController.navigateUp() },
            )
        }

        composable(route = MainRoute.WATER_FULL_HISTORY) {
            WaterFullHistoryScreen(
                onNavigateUp = { navController.navigateUp() },
            )
        }


        composable(route = MainRoute.MEASURE_HEART_RATE) {
            HeartRateMeasureScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                },
                onFinishMeasure = { heartRate, heartRateRecord ->
                    navController.navigateUp()
                    if (heartRateRecord != null) {

                        val heartRateRecordString = Json.encodeToString(heartRateRecord)

                        navController.navigate("${MainRoute.HEART_RATE_RESULT}?record=${heartRateRecordString}")

                    } else {
                        navController.navigate("${MainRoute.ADD_RATE_MEASURE}?heartrate=$heartRate")
                    }

                }
            )
        }

        composable(
            route = "${MainRoute.ADD_RATE_MEASURE}?heartrate={heartrate}",
            arguments = listOf(navArgument("heartrate") { type = NavType.IntType })
        ) {

            it.arguments?.getInt("heartrate").let { heartRate ->
                if (heartRate != null) {
                    AddHeartRateScreen(
                        heartRate = heartRate,
                        onNavigateUp = { navController.navigateUp() },
                        onNavigateToPremium = {
                            navController.navigate(MainRoute.PREMIUM)
                        },
                        onNavigateToAddNote = { navController.navigate(MainRoute.EDIT_NOTE) },
                        onSaveRecord = { heartRateRecord ->
                            val heartRateRecordString = Json.encodeToString(heartRateRecord)
                            navController.navigateUp()
                            navController.navigate("${MainRoute.HEART_RATE_RESULT}?record=${heartRateRecordString}")
                        }
                    )
                }
            }
        }

        composable(route = MainRoute.HEART_RATE_HISTORY) {
            HeartRateHistoryScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                },
                onNavigateToRecordDetail = { recordId ->
                    navController.navigate("${MainRoute.HEART_RATE_RESULT_VIEW}/${recordId}")
                }
            )
        }

        composable(
            route = "${MainRoute.HEART_RATE_RESULT}?record={record}", arguments = listOf(
                navArgument("record") { type = NavType.StringType },
            )
        ) {

            it.arguments?.getString("record").let { heartRateRecordString ->
                if (heartRateRecordString != null) {
                    val heartRateRecord =
                        JsonProvider.json.decodeFromString<HeartRateRecord>(heartRateRecordString)

                    HeartRateResultScreen(
                        heartRateRecord = heartRateRecord,
                        onNavigateUp = {
                            navController.navigateUp()
                            navController.navigate("${MainRoute.ADD_RATE_MEASURE}?heartrate=${heartRateRecord.heartRate}")
                        },
                        onNavigateToDetail = {
                            navController.navigateUp()
                            navController.navigate(MainRoute.HEART_RATE_DETAIL)
                        },
                        onNavigateToHistory = {
                            navController.navigate(MainRoute.HEART_RATE_HISTORY)
                        }
                    )
                }
            }
        }

        composable(
            route = "${MainRoute.HEART_RATE_RESULT_VIEW}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) {
            HeartRateResultScreen(onNavigateUp = { navController.navigateUp() },
                onNavigateToDetail = {},
                onNavigateToHistory = {
                    navController.navigate(MainRoute.HEART_RATE_HISTORY)
                })
        }

        composable(route = MainRoute.HEART_RATE_DETAIL) {
            HeartRateDetailScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToHistory = { navController.navigate(MainRoute.HEART_RATE_HISTORY) },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                },
                onNavigateToRecordDetail = { recordId ->
                    navController.navigate("${MainRoute.HEART_RATE_RESULT_VIEW}/${recordId}")
                }
            )
        }

        composable(
            route = "${MainRoute.COMING_SOON}?titleResId={titleResId}", arguments = listOf(
                navArgument("titleResId") { type = NavType.IntType },
            )
        ) {

            it.arguments?.getInt("titleResId").let { titleResId ->
                ComingSoonScreen(titleResId = titleResId) {
                    navController.navigateUp()
                }
            }

        }

        composable(
            route = "${MainRoute.BLOOD_PRESSURE_INFO}?feature={feature}",
            arguments = listOf(navArgument("feature") {
                type = NavType.EnumType(InfoItemType::class.java)
            }),
        ) {
            val feature =
                NavType.EnumType(InfoItemType::class.java)[it.arguments ?: bundleOf(), "feature"]
                    ?: InfoItemType.BLOOD_PRESSURE
            InfoScreen(onNavigateToInfoDetail = { id ->
                navController.navigate(
                    route = "${MainRoute.INFO_DETAIL}/$id?feature=$feature"
                )
            }, infoType = feature, onNavigateUp = { navController.navigateUp() })
        }

        composable(route = MainRoute.ALARM) {
            AlarmScreen(
                onNavigateUp = { navController.navigateUp() },
                onWaterReminderClick = {
                    navController.navigate(MainRoute.WATER_ALARM)
                }
            )
        }

        composable(route = MainRoute.ABOUT_ME) {
            AboutMeScreen(onNavigateUp = { navController.navigateUp() })
        }

        composable(route = MainRoute.BMI) {
            BMIScreen(
                onNavigateUp = {navController.navigateUp()},
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                },
                onItemClick = {type ->
                    when(type) {
                        BMIOptionType.ANALYZE -> {navController.navigate(MainRoute.ADD_BMI)}
                        BMIOptionType.HISTORY -> {
                            navController.navigate(MainRoute.HISTORY_BMI)
                        }
                        BMIOptionType.TRENDS -> {navController.navigate(MainRoute.HISTORY_STATISTICS_BMI)}
                    }
                }
            )
        }

        composable(MainRoute.ADD_BMI) {
            AddBmiScreen(
                onNavigateUp = { navController.navigateUp() },
                onComplete = { navController.navigate(MainRoute.HISTORY_STATISTICS_BMI) {
                    popUpTo(MainRoute.ADD_BMI) {
                        inclusive = true
                    }
                } },
                onNavigateToAddNote = {navController.navigate(MainRoute.EDIT_NOTE)},
                title = stringResource(R.string.add_bmi_weight)
            )
        }

        composable(MainRoute.HISTORY_STATISTICS_BMI) {
            BMIHistoryAndStatisticsScreen(
                onNavigateUp = { navController.navigateUp() },
                onAddRecordClick = {navController.navigate(MainRoute.ADD_BMI) {
                    popUpTo(MainRoute.HISTORY_STATISTICS_BMI) {
                        inclusive = true
                    }
                } },
                onNavigateToPremium = {
                    navController.navigate(MainRoute.PREMIUM)
                },
                onNavigateToHistory = {
                    navController.navigate(MainRoute.HISTORY_BMI)
                },
                onItemClick = {id ->
                    navController.navigate("${MainRoute.RECORD_DETAIL_BMI}/$id")
                }
            )
        }

        composable(MainRoute.HISTORY_BMI) {
            BMIHistoryScreen(
                onNavigateUp = { navController.navigateUp() },
                onItemCLick = { id ->
                    navController.navigate("${MainRoute.RECORD_DETAIL_BMI}/$id")
                }
            )
        }

        composable(
            route = "${MainRoute.RECORD_DETAIL_BMI}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) {
            AddBmiScreen(
                onNavigateUp = { navController.navigateUp() },
                onComplete = {
                    navController.navigate(MainRoute.HISTORY_STATISTICS_BMI) {
                        popUpTo( MainRoute.HISTORY_STATISTICS_BMI) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToAddNote = {navController.navigate(MainRoute.EDIT_NOTE)},
                title = stringResource(R.string.update_record)
            )
        }

        composable(
            route = MainRoute.BARCODE_SCAN
        ) {
            BarcodeScanScreen(
                onNavigateUp = {navController.navigateUp()}
            )
        }
    }
}

private fun AnimatedContentTransitionScope<*>.defaultEnterTransition(): EnterTransition {
    return slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start)
}

private fun AnimatedContentTransitionScope<*>.defaultExitTransition(): ExitTransition {
    return slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start)
}

private fun AnimatedContentTransitionScope<*>.defaultPopEnterTransition(): EnterTransition {
    return slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End)
}

private fun AnimatedContentTransitionScope<*>.defaultPopExitTransition(): ExitTransition {
    return slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End)
}

object MainRoute {
    const val HOME = "home"
    const val SPLASH = "splash"
    const val LANGUAGE_SELECTION = "language_selection"
    const val LANGUAGE_UPDATE = "language_update"
    const val ADD_RECORD = "add_record"
    const val INFO_DETAIL = "info_detail"
    const val HISTORY = "history"
    const val RECORD_DETAIL = "record_detail"
    const val EDIT_NOTE = "edit_note"
    const val PREMIUM = "premium"
    const val ONBOARDING = "onboarding"
    const val THANK_YOU = "thank_you"
    const val BLOOD_PRESSURE = "blood_pressure"
    const val BLOOD_PRESSURE_FEATURE = "blood_pressure_feature"
    const val BLOOD_PRESSURE_HISTORY = "blood_pressure_history"
    const val BLOOD_SUGAR = "blood_sugar"
    const val BLOOD_SUGAR_ADD_ANALYZE = "blood_sugar_add_analyze"
    const val BLOOD_SUGAR_DETAIL = "blood_sugar_detail"
    const val BLOOD_SUGAR_STATISTIC = "blood_sugar_statistic"
    const val BLOOD_SUGAR_HISTORY = "blood_sugar_history"
    const val HEART_RATE = "heart_rate"
    const val MEASURE_HEART_RATE = "measure_heart_rate"
    const val ADD_RATE_MEASURE = "add_heart_rate"
    const val HEART_RATE_HISTORY = "heart_rate_history"
    const val HEART_RATE_RESULT = "heart_rate_result"
    const val HEART_RATE_RESULT_VIEW = "heart_rate_result_view"
    const val HEART_RATE_DETAIL = "heart_rate_detail"
    const val COMING_SOON = "coming_soon"
    const val BLOOD_PRESSURE_INFO = "blood_pressure_info"
    const val ALARM = "alarm"
    const val ABOUT_ME = "about_me"
    const val BMI = "bmi"
    const val ADD_BMI = "add bmi"
    const val HISTORY_STATISTICS_BMI = "history_statistics_bmi"
    const val HISTORY_BMI = "history_bmi"
    const val RECORD_DETAIL_BMI = "record detail bmi"
    const val WATER_REMINDER = "water_reminder"
    const val WATER_HISTORY = "water_history"
    const val WATER_ALARM = "water_alarm"
    const val WATER_FULL_HISTORY = "water_full_history"
    const val BARCODE_SCAN = "barcode_scan"
}

object UriPattern {
    const val ADD_RECORD = "bpapp://addrecord"
    const val MEASURE_HEART_RATE = "bpapp://measure_heartrate"
    const val HEART_RATE = "bpapp://heartrate"
    const val BLOOD_SUGAR = "bpapp://bloodsugar"
    const val BMI = "bpapp://bmi"
    const val WATER_REMINDER = "bpapp://water"
    const val BLOOD_PRESSURE = "bpapp://bloodpressure"
    const val HISTORY = "bpapp://history"
    const val BP_INFO = "bpapp://bloodpressureinfo"
    const val HOME = "bpapp://home"

    fun getRouteFromUri(uri: String): String {
        return when (uri) {
            ADD_RECORD -> MainRoute.ADD_RECORD
            HEART_RATE -> MainRoute.HEART_RATE
            BLOOD_SUGAR -> MainRoute.BLOOD_SUGAR
            BMI -> MainRoute.BMI
            WATER_REMINDER -> MainRoute.WATER_REMINDER
            BLOOD_PRESSURE -> MainRoute.BLOOD_PRESSURE
            BP_INFO -> MainRoute.BLOOD_PRESSURE
            MEASURE_HEART_RATE -> MainRoute.MEASURE_HEART_RATE
            else -> MainRoute.HOME
        }
    }
}