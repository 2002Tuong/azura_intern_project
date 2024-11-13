package com.bloodpressure.app.screen.home

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bloodpressure.app.BuildConfig
import com.bloodpressure.app.R
import com.bloodpressure.app.screen.home.info.HomeInfoType
import com.bloodpressure.app.screen.home.tracker.TrackerType
import com.bloodpressure.app.screen.updateapp.AppUpdateChecker
import com.bloodpressure.app.screen.updateapp.AppUpdateDialog
import com.bloodpressure.app.ui.component.RatingDialog
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalShareController
import com.bloodpressure.app.utils.Logger
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onNavigateToThankYou: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onTrackerClick: (TrackerType) -> Unit,
    onHomeInfoItemClick: (HomeInfoType) -> Unit,
    onAlarmClick: () -> Unit,
    onAboutMeClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.shouldShowPremium) {
        if (uiState.shouldShowPremium && !uiState.isPurchased) {
            onNavigateToPremium()
            viewModel.onPremiumShown()
        }
    }

    val adsManager = LocalAdsManager.current
    LaunchedEffect(uiState.shouldLoadAds) {
        if (uiState.shouldLoadAds) {
            adsManager.loadInterAds()
            adsManager.loadFeatureNative()
            adsManager.loadAddRecordFeatureInter(TrackerType.WATER_REMINDER)
            adsManager.loadInfoNativeAd()
            viewModel.onAdsLoaded()
        }
    }
    RequestPostNotificationIfNeeded {
        viewModel.onPermissionGranted()
    }


    val shareController = LocalShareController.current
    var showConfirmExitDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    if (showConfirmExitDialog) {
        val exitAppNativeAd by adsManager.exitAppNativeAd.collectAsStateWithLifecycle()
        ConfirmExitDialog(
            nativeAd = exitAppNativeAd,
            onDismissRequest = { showConfirmExitDialog = false },
            onConfirmButtonClick = { showRatingDialog = true },
            onDismissButtonClick = {
                if (uiState.isPurchased) {
                    onNavigateUp()
                } else {
                    adsManager.showExitAppAd { isAdShown ->
                        if (isAdShown) {
                            onNavigateToThankYou()
                        } else {
                            onNavigateUp()
                        }
                    }
                }
            }
        )
    }
    if (showRatingDialog) {
        RatingDialog(
            onDismissRequest = { showRatingDialog = false },
            onRateClick = { rate ->
                if (rate > 4) {
                    shareController.openStore()
                } else {
                    shareController.sendFeedback()
                }
            }
        )
    }
    val onBack = {
        showConfirmExitDialog = true
    }
    BackHandler(true, onBack)

    val updateType by viewModel.updateType.collectAsStateWithLifecycle(
        minActiveState = Lifecycle.State.RESUMED
    )

    if (updateType is AppUpdateChecker.UpdateType.RequireUpdate) {
        AppUpdateDialog(
            title = (updateType as AppUpdateChecker.UpdateType.RequireUpdate).title,
            message = (updateType as AppUpdateChecker.UpdateType.RequireUpdate).message,
            isCancellable = !(updateType as AppUpdateChecker.UpdateType.RequireUpdate).forceUpdate,
            onDismissRequest = {
                viewModel.clearUpdateType()
            },
            onConfirmClick = {
                shareController.openAppStore((updateType as AppUpdateChecker.UpdateType.RequireUpdate).url)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
    ) {
        val navController = rememberNavController()
        HomeNavigationHost(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            navController = navController,
            onNavigateToLanguage = onNavigateToLanguage,
            onNavigateToPremium = onNavigateToPremium,
            onTrackerClick = { trackerType ->
                if (trackerType == TrackerType.WATER_REMINDER) {
                    adsManager.showAddFeatureInterAds(TrackerType.WEIGHT_BMI) {
                        onTrackerClick.invoke(trackerType)
                    }
                } else {
                    onTrackerClick.invoke(trackerType)
                }
            },
            onHomeInfoItemClick = onHomeInfoItemClick,
            onAlarmClick = onAlarmClick,
            onAboutMeClick = onAboutMeClick
        )

        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPaddingIfNeed(),
            windowInsets = WindowInsets(0.dp)
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            navigationItems.forEach { item ->
                val isSelected =
                    currentDestination?.hierarchy?.any { it.route == item.route } == true
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(
                                id = if (isSelected) item.selectedIcon else item.icon
                            ),
                            contentDescription = stringResource(id = item.label)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(id = item.label),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selected = isSelected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                            LocalAbsoluteTonalElevation.current
                        ),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
        val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()
        if (!uiState.isPurchased && adView != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = {
                        adView!!.apply {
                            (parent as? ViewGroup)?.removeView(this)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RequestPostNotificationIfNeeded(onPermissionsGranted: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { isGranted ->
        if (isGranted.all { it.value }) {
            onPermissionsGranted()
        }
        // TODO: Pending
        // requestFullScreenIntent(context)
    }
    LaunchedEffect(key1 = Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isPostNotificationGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            val scheduleAlarmPermissionGranted =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.SCHEDULE_EXACT_ALARM
                    ) == PackageManager.PERMISSION_GRANTED
                } else true
            if (!isPostNotificationGranted || !scheduleAlarmPermissionGranted) {
                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.SCHEDULE_EXACT_ALARM
                    )
                else arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                launcher.launch(
                    permissions
                )
            } else {
                onPermissionsGranted()
            }
        } else {
            onPermissionsGranted()
        }
    }
}

private fun requestFullScreenIntent(context: Context) {
    // TODO: Add dialog explain + ask permission
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (notificationManager?.canUseFullScreenIntent() == false) {
            val intent = Intent(
                android.provider.Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Logger.e(Throwable("cannot start settings screen: ${e.message}"))
            }
        }
    }
}

data class NavigationItemData(
    val route: String,
    val icon: Int,
    val selectedIcon: Int,
    val label: Int
)

private val navigationItems = listOf(
    NavigationItemData(
        route = HomeRoute.TRACKER,
        icon = R.drawable.ic_tab_tracker,
        selectedIcon = R.drawable.ic_tab_tracker_active,
        label = R.string.bottom_navigation_tracker_title
    ),
    NavigationItemData(
        route = HomeRoute.INFO,
        icon = R.drawable.ic_tab_info,
        selectedIcon = R.drawable.ic_tab_info_active,
        label = R.string.bottom_navigation_item_info_title
    ),
    NavigationItemData(
        route = HomeRoute.SETTING,
        icon = R.drawable.ic_tab_setting,
        selectedIcon = R.drawable.ic_tab_setting_active,
        label = R.string.bottom_navigation_item_settings_title
    )
)
