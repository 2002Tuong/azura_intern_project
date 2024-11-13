package com.bloodpressure.app.screen.bloodpressure

import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.data.local.NoteConverters
import com.bloodpressure.app.data.model.Record
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.screen.alarm.SetAlarmDialog
import com.bloodpressure.app.screen.bloodpressure.statistics.BloodPressureStatisticsCard
import com.bloodpressure.app.screen.heartrate.detail.DateRangeSelection
import com.bloodpressure.app.screen.home.tracker.BottomAxisValueFormatter
import com.bloodpressure.app.screen.home.tracker.Entry
import com.bloodpressure.app.screen.home.tracker.chartColors
import com.bloodpressure.app.screen.home.tracker.rememberChartStyle
import com.bloodpressure.app.screen.home.tracker.rememberMarker
import com.bloodpressure.app.ui.component.TopAppBarAction
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalTextFormatter
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.scroll.InitialScroll
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodPressureScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onItemClick: (Long) -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: BloodPressureViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()
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
                Text(
                    text = stringResource(id = R.string.blood_pressure),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight(800),
                        color = GrayScale900,
                    )
                )
            },
            actions = {
                TopAppBarAction(
                    isPurchased = uiState.isPurchased,
                    onSetAlarmClick = { viewModel.showSetReminder(true) },
                    onNavigateToPremium = onNavigateToPremium
                )
            },
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        Surface(modifier = Modifier
            .fillMaxSize()
            .weight(1f)) {
            BloodPressureContent(
                modifier = Modifier.fillMaxSize(),
                onItemClick = { id ->
                    onItemClick(id)
                },
                onPreviousClick = { viewModel.getPreviousRecordType() },
                onNextClick = { viewModel.getNextRecordType() },
                onSelectedDate = { startDate, endDate ->
                    viewModel.setFilteredRecords(
                        startDate, endDate
                    )
                },
                onNavigateToHistory = onNavigateToHistory,
                uiState = uiState
            )
        }

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
fun BloodPressureContent(
    modifier: Modifier = Modifier,
    onItemClick: (Long) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSelectedDate: (Long, Long) -> Unit,
    onNavigateToHistory: () -> Unit,
    uiState: BloodPressureViewModel.UiState
) {

    Column(modifier = modifier.fillMaxSize()) {
        if (
            uiState.filteredRecords.isEmpty() &&
            uiState.filterChartData.isEmpty()
        ) {
            BloodPressureEmpty(
                onAddRecordClick = { },
                modifier = Modifier.padding(16.dp)
            )
        } else if (uiState.records != null || uiState.chartData != null) {
            RecordList(
                records = uiState.filteredRecords,
                onItemClick = onItemClick,
                onPreviousClick = onPreviousClick,
                onNextClick = onNextClick,
                recordType = uiState.currentRecordType,
                recordTypeData = uiState.currentRecordTypeData,
                chartData = uiState.filterChartData,
                onSelectedDate = onSelectedDate,
                onNavigateToHistory = onNavigateToHistory,
                sampleRecordId = uiState.sampleRecordId
            )
        }
    }
}

@Composable
fun BloodPressureEmpty(modifier: Modifier = Modifier, onAddRecordClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = R.drawable.ic_empty), contentDescription = null)
            Text(
                text = stringResource(id = R.string.tracker_empty_message),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordList(
    modifier: Modifier = Modifier,
    onItemClick: (Long) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    records: Map<String, List<Record>>,
    recordType: BloodPressureViewModel.RecordType,
    recordTypeData: BloodPressureViewModel.RecordTypeData?,
    onSelectedDate: (Long, Long) -> Unit,
    chartData: List<Record>,
    sampleRecordId: Long,
    onNavigateToHistory: () -> Unit
) {
    DateRangeSelection(
        onDateRangeChanged = onSelectedDate
    )
    Spacer(
        modifier = Modifier
            .height(1.dp)
            .fillMaxWidth()
            .background(Color(0xFFECEDEF))
    )
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp)
    ) {
        item {
            TrackerChart(
                onPreviousClick = onPreviousClick,
                onNextClick = onNextClick,
                recordType = recordType,
                recordTypeData = recordTypeData,
                chartData = chartData
            )
            Spacer(modifier = Modifier.height(16.dp))

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

        item {
            BloodPressureStatisticsCard(
                records = chartData,
            )
            Spacer(modifier = Modifier.height(16.dp))

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

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.history),
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                    TextButton(onClick = onNavigateToHistory) {
                        Text(
                            text = stringResource(R.string.view_all),
                            style = TextStyle(
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight(400),
                                color = Color(0xFF1892FA),
                                textAlign = TextAlign.Right,
                                textDecoration = TextDecoration.Underline,
                            )
                        )
                    }
                }
            }
            val recordList = records.flatMap { it.value }.take(5).groupBy { it.date }
            recordList.forEach { entry ->
                DateItem(date = entry.key)
                entry.value.forEach { record ->
                    RecordItem(
                        record = record,
                        onClick = onItemClick,
                        isSampleRecord = record.createdAt == sampleRecordId
                    )
                }
            }
        }
    }
}

@Composable
fun TrackerChart(
    modifier: Modifier = Modifier,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    recordType: BloodPressureViewModel.RecordType,
    recordTypeData: BloodPressureViewModel.RecordTypeData?,
    chartData: List<Record>
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousClick) {
                Icon(
                    painter = painterResource(id = R.drawable.img_btn_previous),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }

            Text(
                text = getRecordTypeTitle(recordType = recordType),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textAlign = TextAlign.Center
            )

            IconButton(onClick = onNextClick) {
                Icon(
                    painter = painterResource(id = R.drawable.img_btn_next),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 12.dp), color = Color(0xFFF4F4F5))

        RecordData(
            data = recordTypeData,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
        )

        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color = Color(0xFFF95721), shape = CircleShape)
            )

            Text(text = "SYS", color = Color(0xFFF95721), modifier = Modifier.padding(start = 2.dp))

            Box(
                modifier = Modifier
                    .padding(start = 24.dp)
                    .size(10.dp)
                    .background(color = Color(0xFF329EFB), shape = CircleShape)
            )

            Text(
                text = "DIA",
                color = Color(0xFF329EFB),
                modifier = Modifier.padding(start = 2.dp, end = 4.dp)
            )
        }

        RecordsLineChart(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
            chartData = chartData
        )
    }
}

@Composable
fun RecordData(modifier: Modifier = Modifier, data: BloodPressureViewModel.RecordTypeData?) {
    Row(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(text = stringResource(id = R.string.cw_systolic), color = Color(0xFF8C8E97))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = data?.systolic?.toString() ?: "0",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFF95721)
                )

                Text(
                    text = "mmHg",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8C8E97),
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(text = stringResource(id = R.string.cw_diastolic), color = Color(0xFF8C8E97))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = data?.diastolic?.toString() ?: "0",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF1892FA)
                )

                Text(
                    text = "mmHg",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8C8E97),
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(text = stringResource(id = R.string.cw_pulse), color = Color(0xFF8C8E97))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = data?.pulse?.toString() ?: "0",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF53B69F)
                )

                Text(
                    text = "bpm",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8C8E97),
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }
}

@Composable
fun RecordsLineChart(modifier: Modifier = Modifier, chartData: List<Record>) {
    val textFormatter = LocalTextFormatter.current
    val chartEntryModelProducer = remember(chartData) {
        val systolics = chartData.map { record -> record.date to record.systolic }
            .sortedBy { it.first }
            .mapIndexed { index, (dateString, y) ->
                Entry(textFormatter.parse(dateString), index.toFloat(), y.toFloat())
            }

        val diastolics = chartData.map { record -> record.date to record.diastolic }
            .sortedBy { it.first }
            .mapIndexed { index, (dateString, y) ->
                Entry(textFormatter.parse(dateString), index.toFloat(), y.toFloat())
            }
            .sortedByDescending { it.date }
        ChartEntryModelProducer().apply {
            setEntries(listOf(systolics, diastolics))
        }
    }
    val bottomAxisValueFormatter = remember(textFormatter) {
        BottomAxisValueFormatter(textFormatter)
    }
    val startAxisValueFormatter = remember {
        DecimalFormatAxisValueFormatter<AxisPosition.Vertical.Start>("##")
    }
    val bottomTickPosition = remember(chartData) {
        if (chartData.size <= 5) {
            HorizontalAxis.TickPosition.Center(0, 1)
        } else {
            HorizontalAxis.TickPosition.Center(0, 2)
        }
    }
    val marker = rememberMarker()
    ProvideChartStyle(rememberChartStyle(chartColors)) {
        Box(modifier = modifier) {
            Chart(
                chart = lineChart(
                    persistentMarkers = if (chartData.size == 1) {
                        remember(marker) { mapOf(0f to marker) }
                    } else {
                        null
                    }
                ),
                chartModelProducer = chartEntryModelProducer,
                startAxis = startAxis(
                    valueFormatter = startAxisValueFormatter,
                    maxLabelCount = 5
                ),
                bottomAxis = bottomAxis(
                    label = axisLabelComponent(horizontalMargin = 2.dp),
                    guideline = null,
                    valueFormatter = bottomAxisValueFormatter,
                    tickLength = 0.dp,
                    tickPosition = bottomTickPosition
                ),
                marker = rememberMarker(),
                chartScrollSpec = rememberChartScrollSpec(initialScroll = InitialScroll.End)
            )
        }
    }
}

@Composable
fun DateItem(
    modifier: Modifier = Modifier,
    date: String
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White)
    ) {
        Text(
            text = date,
            color = Color(0xFF8C8E97),
            modifier = Modifier.padding(top = 10.dp, start = 16.dp)
        )
    }
}

@Composable
fun RecordItem(
    modifier: Modifier = Modifier,
    record: Record,
    onClick: (Long) -> Unit,
    isSampleRecord: Boolean
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White)
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .fillMaxWidth()
                .clickable { onClick(record.createdAt) },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(width = 1.dp, color = Color(0xFFF4F4F5))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(28.dp)
                        .background(color = record.type.color, shape = RoundedCornerShape(8.dp))
                )

                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = if (isSampleRecord) {
                            stringResource(id = R.string.sample)
                        } else {
                            stringResource(id = record.type.nameRes)
                        },
                        style = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight(400),
                            color = GrayScale900,
                        )
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = record.systolic.toString(),
                            style = TextStyle(
                                fontSize = 24.sp,
                                lineHeight = 32.sp,
                                fontWeight = FontWeight(700),
                                color = GrayScale900,
                                textAlign = TextAlign.Center,
                            )
                        )

                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp, top = 4.dp)
                                .width(1.dp)
                                .height(16.dp)
                                .background(color = Color(0xFFECEDEF))
                        )

                        Text(
                            text = record.diastolic.toString(),
                            style = TextStyle(
                                fontSize = 24.sp,
                                lineHeight = 32.sp,
                                fontWeight = FontWeight(700),
                                color = GrayScale900,
                                textAlign = TextAlign.Center,
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .width(1.dp)
                                .height(16.dp)
                                .background(color = Color(0xFFECEDEF))
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.ic_heart_gray),
                            contentDescription = null,
                            modifier = Modifier.padding(start = 8.dp),
                            tint = Color.Unspecified
                        )
                        Text(
                            text = "${record.pulse} bpm",
                            style = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight(400),
                                color = Color(0xFF8C8E97),
                                textAlign = TextAlign.Center,
                            ),
                            color = Color(0xFF8C8E97),
                            modifier = Modifier.padding(start = 2.dp),
                        )
                    }

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

                Text(text = record.time, color = Color(0xFF8C8E97))

                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 16.dp, start = 24.dp),
                    tint = Color(0xFF8C8E97)
                )
            }
        }
    }
}

@Composable
private fun getRecordTypeTitle(recordType: BloodPressureViewModel.RecordType): String {
    return when (recordType) {
        BloodPressureViewModel.RecordType.MAX -> stringResource(id = R.string.record_type_max)
        BloodPressureViewModel.RecordType.MIN -> stringResource(id = R.string.record_type_min)
        BloodPressureViewModel.RecordType.AVERAGE -> stringResource(id = R.string.record_type_average)
        BloodPressureViewModel.RecordType.LATEST -> stringResource(id = R.string.record_type_latest)
    }
}
