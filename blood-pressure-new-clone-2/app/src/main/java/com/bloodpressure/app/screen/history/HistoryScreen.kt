package com.bloodpressure.app.screen.history

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ads.LargeNativeAd
import com.bloodpressure.app.data.model.Record
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.screen.alarm.SetAlarmDialog
import com.bloodpressure.app.screen.bloodpressure.DateItem
import com.bloodpressure.app.screen.bloodpressure.RecordItem
import com.bloodpressure.app.screen.heartrate.detail.DateRangeSelection
import com.bloodpressure.app.ui.component.TopAppBarAction
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalRemoteConfig
import com.bloodpressure.app.utils.Logger
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onNavigateToRecordDetail: (Long) -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adsManager = LocalAdsManager.current

    if (uiState.showSetAlarmDialog) {
        SetAlarmDialog(
            alarmType = AlarmType.BLOOD_PRESSURE,
            onDismissRequest = { viewModel.showSetReminder(false) },
            onSave = { alarmRecord ->
                viewModel.insertRecord(alarmRecord)
                viewModel.showSetReminder(false)
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
                Text(text = stringResource(id = R.string.cw_history))
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            actions = {
                TopAppBarAction(
                    isPurchased = uiState.isPurchased,
                    onSetAlarmClick = { viewModel.showSetReminder(true) },
                    onNavigateToPremium = onNavigateToPremium
                )
            },
            navigationIcon = {
                IconButton(onClick = { onNavigateUp() }) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.ArrowBack),
                        contentDescription = null
                    )
                }
            }
        )

        HistoryContent(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            records = uiState.filteredRecords,
            onItemClick = { id ->
                adsManager.showClickHistoryItemAd {
                    onNavigateToRecordDetail(id)
                }
            },
            onDateRangeChanged = { startDate, endDate ->
                viewModel.setFilteredRecords(
                    startDate, endDate
                )
            },
            sampleRecordId = uiState.sampleRecordId,
            uiState = uiState
        )
    }
}

@Composable
fun HistoryContent(
    modifier: Modifier = Modifier,
    onItemClick: (Long) -> Unit,
    records: Map<String, List<Record>>,
    onDateRangeChanged: (Long, Long) -> Unit,
    sampleRecordId: Long,
    uiState: HistoryViewModel.UiState
) {
    val adsManager = LocalAdsManager.current
    val nativeAd by adsManager.historyNativeAd.collectAsStateWithLifecycle()
    val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()
    val shouldShowNative = LocalRemoteConfig.current.adsConfig.shouldShowHistoryNativeAd
    LaunchedEffect(Unit) {
        adsManager.loadHistoryNativeAd()
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        HistoryRecordList(
            records = records,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            onItemClick = onItemClick,
            onDateRangeChanged = onDateRangeChanged,
            sampleRecordId = sampleRecordId
        )

        if (!uiState.isPurchased) {
            if (shouldShowNative) {
                if (nativeAd != null) {
                    LargeNativeAd(nativeAd = nativeAd!!, modifier = Modifier.fillMaxWidth())
                }
            } else {
                if (adView != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPaddingIfNeed(),
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
    }
}

@Composable
fun HistoryRecordList(
    modifier: Modifier = Modifier,
    records: Map<String, List<Record>>,
    onItemClick: (Long) -> Unit,
    onDateRangeChanged: (Long, Long) -> Unit,
    sampleRecordId: Long
) {
    DateRangeSelection(onDateRangeChanged = onDateRangeChanged)
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
            )
        }

        records.forEach { entry ->
            item {
                DateItem(date = entry.key)
            }
            items(entry.value) { record ->
                RecordItem(
                    record = record,
                    onClick = onItemClick,
                    isSampleRecord = record.createdAt == sampleRecordId
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
            )
        }
    }
}
