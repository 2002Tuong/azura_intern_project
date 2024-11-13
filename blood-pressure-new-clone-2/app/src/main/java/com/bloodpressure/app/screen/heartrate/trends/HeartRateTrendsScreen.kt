package com.bloodpressure.app.screen.heartrate.trends

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.data.local.NoteConverters
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.screen.heartrate.detail.TrendsBarChart
import com.bloodpressure.app.screen.heartrate.history.EmptyHeartRateRecords
import com.bloodpressure.app.ui.theme.GrayScale600
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateTrendsScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onNavigateToRecordDetail: (Long) -> Unit,
    viewModel: HeartRateTrendsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.heart_rate_trends),
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
                IconButton(onClick = { onNavigateUp() }) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.ArrowBack),
                        contentDescription = null
                    )
                }
            }
        )

        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(GrayScale600)
        )

        uiState.records?.let {
            HeartRateTrendsContent(
                uiState = uiState,
                records = it,
                onItemClick = onNavigateToRecordDetail,
                onRecordSelected = viewModel::setSelectedHeartRateRecord
            )
        }
    }
}

@Composable
fun HeartRateTrendsContent(
    modifier: Modifier = Modifier,
    uiState: HeartRateTrendsViewModel.UiState,
    records: List<HeartRateRecord>,
    onRecordSelected: (HeartRateRecord) -> Unit,
    onItemClick: (Long) -> Unit,
) {

    var filteredRecords: List<HeartRateRecord> by remember {
        mutableStateOf(records)
    }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {

        DateRangeSelection(
            onDateRangeChanged = { startDate, endDate ->
                filteredRecords = filterRecordsByDateRange(records, startDateLong = startDate, endDateLong = endDate)
            }
        )

        if (records.isEmpty()) {
            EmptyHeartRateRecords()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPaddingIfNeed(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.White, shape = RoundedCornerShape(8.dp))
                ) {
                    if (filteredRecords.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.4f), contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No records found in this period.")
                        }
                    } else {

                        TrendsBarChart(
                            modifier = Modifier
                                .fillMaxHeight(0.4f)
                                .padding(16.dp),
                            records = filteredRecords,
                            onRecordSelected = onRecordSelected
                        )
                    }
                }

                if (uiState.selectedHeartRateRecord != null && filteredRecords.isNotEmpty()) {
                    HeartRateRecordView(record = uiState.selectedHeartRateRecord, onClick = onItemClick)
                }
            }
        }
    }

}

@Composable
fun HeartRateRecordView(
    modifier: Modifier = Modifier,
    record: HeartRateRecord,
    onClick: (Long) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(shape = RoundedCornerShape(8.dp), color = Color.White)
            .clickable { onClick(record.createdAt) },

        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(28.dp)
                    .background(color = record.type.color, shape = RoundedCornerShape(8.dp))
            )

            Column(modifier = Modifier.padding(horizontal = 11.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${record.heartRate}",
                    style = TextStyle(
                        fontSize = 24.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                        textAlign = TextAlign.Center,
                    )
                )

                Text(
                    text = "BPM",
                    style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight(400),
                        color = Color(0xFF8C8E97),
                        textAlign = TextAlign.Center,
                    )
                )
            }

            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = stringResource(id = record.type.nameRes),
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                    )
                )

                val noteText = remember(record) {
                    if (record.notes.isNotEmpty()) {
                        "#${record.notes.joinToString(separator = NoteConverters.JOIN_CHAR)}"
                    } else {
                        ""
                    }
                }
                if (noteText.isNotEmpty()) {
                    Text(
                        text = noteText,
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8C8E97)
                    )
                }
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp, start = 24.dp),
                tint = Color(0xFF8C8E97)
            )
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
        modifier = modifier
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

                Icon(painter = painterResource(id = R.drawable.ic_calendar), contentDescription = "")
            }
        }
    }
}

fun filterRecordsByDateRange(records: List<HeartRateRecord>, startDateLong: Long, endDateLong: Long): List<HeartRateRecord> {
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