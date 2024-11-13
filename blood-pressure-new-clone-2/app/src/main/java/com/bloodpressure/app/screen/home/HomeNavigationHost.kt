package com.bloodpressure.app.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bloodpressure.app.screen.home.info.HomeInfoScreen
import com.bloodpressure.app.screen.home.info.HomeInfoType
import com.bloodpressure.app.screen.home.settings.SettingScreen
import com.bloodpressure.app.screen.home.tracker.TrackerScreen
import com.bloodpressure.app.screen.home.tracker.TrackerType

@Composable
fun HomeNavigationHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onNavigateToLanguage: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onTrackerClick: (TrackerType) -> Unit,
    onHomeInfoItemClick: (HomeInfoType) -> Unit,
    onAlarmClick: () -> Unit,
    onAboutMeClick: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute.TRACKER,
        modifier = modifier
    ) {
        composable(HomeRoute.TRACKER) {
            TrackerScreen(
                onPremiumClick = onNavigateToPremium,
                onTrackerClick = onTrackerClick,
                onAlarmClick = onAlarmClick
            )
        }

        composable(HomeRoute.INFO) {
            HomeInfoScreen(
                onPremiumClick = onNavigateToPremium,
                onHomeInfoItemClick = onHomeInfoItemClick,
                onAlarmClick = onAlarmClick
            )
        }

        composable(HomeRoute.SETTING) {
            SettingScreen(
                onLanguageClick = onNavigateToLanguage,
                onPremiumBannerClick = onNavigateToPremium,
                onAlarmClick = onAlarmClick,
                onAboutMeClick = onAboutMeClick
            )
        }
    }
}

object HomeRoute {
    const val TRACKER = "tracker"
    const val INFO = "info"
    const val SETTING = "setting"
}
