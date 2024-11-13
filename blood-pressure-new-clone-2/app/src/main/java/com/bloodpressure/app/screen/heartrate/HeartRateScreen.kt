package com.bloodpressure.app.screen.heartrate

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ads.CardItemNativeAd
import com.bloodpressure.app.data.model.HeartRateOptionType
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.screen.alarm.SetAlarmDialog
import com.bloodpressure.app.screen.home.tracker.TrackerType
import com.bloodpressure.app.ui.component.AskSetAlarmDialog
import com.bloodpressure.app.ui.component.TopAppBarAction
import com.bloodpressure.app.ui.theme.Blue800
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onHeartRateOptionClick: (HeartRateOptionType) -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: HeartRateViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adsManager = LocalAdsManager.current

    LaunchedEffect(Unit) {
        adsManager.loadAddRecordNativeAd()
        adsManager.loadAddRecordFeatureInter(TrackerType.HEART_RATE)
    }

    if (uiState.showSetAlarmDialog) {
        SetAlarmDialog(
            alarmType = AlarmType.HEART_RATE,
            onDismissRequest = { viewModel.showSetAlarmDialog(false) },
            onSave = { alarmRecord ->
                viewModel.insertRecord(alarmRecord)
                viewModel.showSetAlarmDialog(false)
            }
        )
    }

    if (uiState.showAskSetAlarmDialog) {
        AskSetAlarmDialog(
            onDismissRequest = { viewModel.showSetAlarmDialog(false) },
            onCancel = {
                viewModel.showAskSetAlarmDialog(false)
                onNavigateUp()
            },
            onAgreeSetAlarm = {
                viewModel.showSetAlarmDialog(true)
                viewModel.showAskSetAlarmDialog(false)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.heart_rate),
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                    )
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            navigationIcon = {
                IconButton(
                    onClick = {
                        if (uiState.hasHeartRateAlarm) {
                            onNavigateUp()
                        } else {
                            viewModel.showAskSetAlarmDialog(true)
                        }
                    }
                ) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.ArrowBack),
                        contentDescription = null
                    )
                }
            },
            actions = {
                TopAppBarAction(
                    isPurchased = uiState.isPurchased,
                    onSetAlarmClick = { viewModel.showSetAlarmDialog(true) },
                    onNavigateToPremium = onNavigateToPremium
                )
            }
        )

        HeartRateContent(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            uiState = uiState,
            onHeartRateOptionClick = { option ->
            if (option == HeartRateOptionType.MEASURE_NOW) {
                adsManager.showAddFeatureInterAds(TrackerType.HEART_RATE) {
                    onHeartRateOptionClick(option)
                }
            } else {
                onHeartRateOptionClick(option)
            }
        })
    }
}

@Composable
fun HeartRateContent(
    modifier: Modifier = Modifier,
    onHeartRateOptionClick: (HeartRateOptionType) -> Unit,
    uiState: HeartRateViewModel.UiState
) {

    val adsManager = LocalAdsManager.current
    val nativeAds by adsManager.heartRateNativeAd.collectAsState()

    Box(modifier = modifier.padding(16.dp)) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            nativeAds?.let {
                item {
                    CardItemNativeAd(nativeAd = it)
                }
            }
            items(uiState.heartRateOptionList.take(2)) { option ->
                HeartRateOptionItem(
                    heartRateOptionType = option,
                    onHeartRateOptionClick = onHeartRateOptionClick
                )
            }

            if (uiState.isHistoryAvailable) {
                item {
                    HeartRateOptionItem(
                        heartRateOptionType = HeartRateOptionType.TRENDS,
                        onHeartRateOptionClick = onHeartRateOptionClick
                    )
                }
                item {
                    HeartRateOptionItem(
                        heartRateOptionType = HeartRateOptionType.HISTORY,
                        onHeartRateOptionClick = onHeartRateOptionClick
                    )
                }
            }
        }
    }
}

@Composable
fun HeartRateOptionItem(
    modifier: Modifier = Modifier,
    heartRateOptionType: HeartRateOptionType,
    onHeartRateOptionClick: (HeartRateOptionType) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row {
                Image(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(id = heartRateOptionType.iconRes),
                    contentDescription = "",
                    contentScale = ContentScale.None
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(id = heartRateOptionType.titleRes),
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight(400),
                        color = GrayScale900,
                    )
                )
            }

            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Blue800, shape = RoundedCornerShape(size = 8.dp)),
                onClick = { onHeartRateOptionClick(heartRateOptionType) }) {
                Text(
                    text = stringResource(id = heartRateOptionType.nameRes),
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight(700),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                )
            }
        }
    }
}