package com.bloodpressure.app.screen.heartrate.detail

import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.screen.alarm.SetAlarmDialog
import com.bloodpressure.app.screen.heartrate.history.EmptyHeartRateRecords
import com.bloodpressure.app.screen.home.settings.CreateCsvContract
import com.bloodpressure.app.ui.component.AskSetAlarmDialog
import com.bloodpressure.app.ui.component.TopAppBarAction
import com.bloodpressure.app.ui.theme.GrayScale600
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalShareController
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateDetailScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToRecordDetail: (Long) -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: HeartRateDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shareController = LocalShareController.current

    val launcher = rememberLauncherForActivityResult(
        contract = CreateCsvContract(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.exportData(uri)
            }
        }
    )

    uiState.shareUri?.let { shareUri ->
        LaunchedEffect(shareUri) {
            shareController.shareFile(shareUri)
            viewModel.clearShareUri()
        }
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

    if (uiState.showSetAlarmDialog) {
        SetAlarmDialog(
            alarmType = AlarmType.HEART_RATE,
            onDismissRequest = { viewModel.showSetAlarmDialog(false) },
            onSave = { alarmRecord ->
                viewModel.insertRecord(alarmRecord)
                viewModel.showSetAlarmDialog(false)
                onNavigateUp()
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
                    text = stringResource(R.string.heart_rate_detail),
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                    )
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            navigationIcon = {
                IconButton(onClick = {
                    onNavigateUp()
                }) {
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

        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(GrayScale600)
        )

        HeartRateDetailContent(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            uiState = uiState,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToRecordDetail = onNavigateToRecordDetail,
            onDateRangeChanged = { startDate, endDate ->
                viewModel.setFilteredHeartRateRecords(
                    filterRecordsByDateRange(
                        uiState.allRecords,
                        startDateLong = startDate,
                        endDateLong = endDate
                    )
                )
            },
        )
        val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()
        if (uiState.isAdsEnabled && adView != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPaddingIfNeed(),
                contentAlignment = Alignment.BottomCenter
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = {
                        adView!!.apply { (parent as? ViewGroup)?.removeView(this) }
                    }
                )
            }
        }

    }
}

@Composable
private fun HeartRateDetailContent(
    modifier: Modifier = Modifier,
    uiState: HeartRateDetailViewModel.UiState,
    onNavigateToHistory: () -> Unit,
    onNavigateToRecordDetail: (Long) -> Unit,
    onDateRangeChanged: (Long, Long) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        DateRangeSelection(onDateRangeChanged = onDateRangeChanged)

        if (uiState.filteredRecords.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState())
            ) {
                HeartRateTrendsCard(modifier = Modifier.fillMaxHeight(0.4f), uiState = uiState)
                HeartRateStatisticsCard(uiState = uiState)
                HeartRateHistoryCard(
                    records = uiState.filteredRecords.take(5),
                    onNavigateToHistory = onNavigateToHistory,
                    onRecordItem = onNavigateToRecordDetail
                )

            }
        } else {
            EmptyHeartRateRecords(modifier = Modifier.fillMaxSize())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeSelection(modifier: Modifier = Modifier, onDateRangeChanged: (Long, Long) -> Unit) {

    val calendar = Calendar.getInstance()

    var showTimePicker by remember { mutableStateOf(false) }
    val pattern = "MMM d, yyyy"

    var startDate by remember { mutableStateOf(calendar.timeInMillis) }

    var endDate by remember { mutableStateOf(calendar.timeInMillis) }

    if (showTimePicker) {

        val dateRangePickerState = rememberDateRangePickerState()

        DatePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false

                        if (dateRangePickerState.selectedStartDateMillis != null) {
                            startDate = dateRangePickerState.selectedStartDateMillis!!
                        }

                        if (dateRangePickerState.selectedEndDateMillis != null) {
                            endDate = dateRangePickerState.selectedEndDateMillis!!
                        }

                        onDateRangeChanged(startDate, endDate)
                    },
                ) {
                    Text(text = stringResource(id = R.string.cw_ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.cw_cancel))
                }
            }
        ) {
            DateRangePicker(state = dateRangePickerState, modifier = Modifier.fillMaxHeight(0.8f))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(width = 1.dp, color = GrayScale600),
            onClick = { showTimePicker = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = "${formatDate(startDate, pattern)} - ${formatDate(endDate, pattern)}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight(400),
                        color = GrayScale900,
                    )
                )
                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = ""
                )
            }
        }
    }
}

fun filterRecordsByDateRange(
    records: List<HeartRateRecord>,
    startDateLong: Long,
    endDateLong: Long
): List<HeartRateRecord> {
    val startDate = Date(startDateLong)
    val endDate = Date(endDateLong)

    val startDateCalendar = Calendar.getInstance().apply { time = startDate }
    val endDateCalendar = Calendar.getInstance().apply { time = endDate }

    startDateCalendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    endDateCalendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return records.filter { record ->

        try {
            val recordDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(record.date)
            val recordDateCalendar = Calendar.getInstance().apply {
                if (recordDate != null) {
                    time = recordDate
                }
            }

            recordDateCalendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val isWithinRange = recordDateCalendar in startDateCalendar..endDateCalendar
            isWithinRange
        } catch (e: Exception) {
            false
        }
    }
}

fun formatDate(timestamp: Long, pattern: String): String {
    val date = Date(timestamp)
    val simpleDateFormat = SimpleDateFormat(pattern, Locale.ENGLISH)
    return simpleDateFormat.format(date)
}