package com.bloodpressure.app.screen.bmi.historyandstatistics

import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.screen.alarm.SetAlarmDialog
import com.bloodpressure.app.screen.bloodpressure.BloodPressureEmpty
import com.bloodpressure.app.screen.bloodpressure.DateItem
import com.bloodpressure.app.screen.bmi.history.BMIItem
import com.bloodpressure.app.screen.heartrate.detail.DateRangeSelection
import com.bloodpressure.app.screen.home.tracker.BottomAxisValueFormatter
import com.bloodpressure.app.screen.home.tracker.Entry
import com.bloodpressure.app.screen.home.tracker.rememberChartStyle
import com.bloodpressure.app.screen.home.tracker.rememberMarker
import com.bloodpressure.app.ui.component.TopAppBarAction
import com.bloodpressure.app.ui.theme.BloodPressureAndroidTheme
import com.bloodpressure.app.ui.theme.GrayScale600
import com.bloodpressure.app.ui.theme.GrayScale700
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalTextFormatter
import com.bloodpressure.app.utils.WeightUnit
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
fun BMIHistoryAndStatisticsScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onAddRecordClick:() -> Unit,
    onItemClick: (Long) -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: BMIHistoryAndStatisticsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()

    if(uiState.showWeightUnitDialog) {
        WeightUnitPickerDialog(
            weightUnit = uiState.weightUnit,
            onValueSaved = {
                viewModel.setWeightUnit(it)
                viewModel.showWeightUnitDialog(false)
            },
            onDismissRequest = { viewModel.showWeightUnitDialog(false)})
    }

    if(uiState.showHeightUnitDialog) {
        HeightUnitPickerDialog(
            heightUnit = uiState.heightUnit,
            onValueSaved = {
                viewModel.setHeightUnit(it)
                viewModel.showHeightUnitDialog(false)
            },
            onDismissRequest = { viewModel.showHeightUnitDialog(false) })
    }

    if (uiState.showSetAlarmDialog) {
        SetAlarmDialog(
            alarmType = AlarmType.WEIGHT_BMI,
            onDismissRequest = { viewModel.showSetAlarmDialog(false) },
            onSave = { alarmRecord ->
                viewModel.insertRecord(alarmRecord)
                viewModel.showSetAlarmDialog(false)
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
                    text = stringResource(id = R.string.weight_bmi_statistics), style = TextStyle(
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
                IconButton(onClick = { onNavigateUp() }) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.ArrowBack), contentDescription = null
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
                .background(Color(0xFFECEDEF))
        )

        DateRangeSelection(
            modifier = Modifier.height(40.dp),
            onDateRangeChanged = { startDate, endDate ->
                viewModel.setFilteredChartData(
                    startDate, endDate
                )
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            Card(
                modifier = Modifier
                    .height(40.dp)
                    .weight(3f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(width = 1.dp, color = GrayScale600),
                onClick = {viewModel.showWeightUnitDialog(true)}
            ) {
                Row(
                    modifier = Modifier.padding(10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.weight_unit_select_text, uiState.weightUnit.value),
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(400),
                            color = Color.Black,
                        )
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = ""
                    )
                }
            }


            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .background(color = GrayScale700)
            )

            Card(
                modifier = Modifier
                    .height(40.dp)
                    .weight(3f)
                    .wrapContentWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(width = 1.dp, color = GrayScale600),
                onClick = {viewModel.showHeightUnitDialog(true)}
            ) {
                Row(
                    modifier = Modifier.padding(10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(
                            R.string.height_unit_select_text,
                            uiState.heightUnit.value
                        ),
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(400),
                            color = Color.Black,
                        )
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = ""
                    )
                }
            }
        }


        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp)
                .weight(1f)
        ){
            HistoryAndStatisticsScreenContent(
                modifier = Modifier.fillMaxSize(),
                onAddRecordClick = onAddRecordClick,
                onItemClick = onItemClick,
                onNavigateToHistory = onNavigateToHistory,
                uiState = uiState,
                viewModel = viewModel,
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
fun HistoryAndStatisticsScreenContent(
    modifier: Modifier = Modifier,
    onAddRecordClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    viewModel: BMIHistoryAndStatisticsViewModel,
    onNavigateToHistory: () -> Unit,
    uiState: BMIHistoryAndStatisticsViewModel.UiState
) {
    Box(
        modifier = modifier
    ) {
        if ((uiState.filteredRecordForTrends != null) &&
            uiState.filteredRecordForTrends.isEmpty() &&
            (uiState.filteredRecordsForHistory != null) &&
            uiState.filteredRecordsForHistory.isEmpty() &&
            (uiState.filteredRecordForStatistics != null) &&
            uiState.filteredRecordForStatistics.isEmpty()
        ) {
            BloodPressureEmpty(
                onAddRecordClick = onAddRecordClick,
                modifier = Modifier.padding(16.dp)
            )
        } else if(uiState.filteredRecordForTrends != null &&
            uiState.filteredRecordsForHistory != null &&
            uiState.filteredRecordForStatistics != null
            ) {
            TrendsAndStatisticsAndHistoryContent(
                modifier = Modifier.padding(16.dp),
                onItemClick = onItemClick,
                uiState = uiState,
                onWeightUnitChange = viewModel::onWeightUnitChange,
                onNavigateToHistory = onNavigateToHistory
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsAndStatisticsAndHistoryContent(
    modifier: Modifier,
    onItemClick: (Long) -> Unit,
    onWeightUnitChange: (WeightUnit, Float) -> Float,
    onNavigateToHistory: () -> Unit,
    uiState: BMIHistoryAndStatisticsViewModel.UiState
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            BMITrendsCard(
                chartData = uiState.filteredRecordForTrends!!,
                weightUnit = uiState.weightUnit,
                onWeightUnitChange = onWeightUnitChange)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            BMIStatisticsCard(uiState = uiState) { weightUnit, weight ->
                onWeightUnitChange(weightUnit, weight)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

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
        }

        val flatList = uiState.filteredRecordsForHistory?.flatMap {
            it.value
        }?.take(5)?.groupBy { item -> item.date }

        flatList?.forEach { entry ->
            item {
                DateItem(date = entry.key)
            }

            items(entry.value) { item ->
                BMIItem(
                    bmiRecord = item,
                    onItemClick = { onItemClick(item.createdAt) },
                    weightUnit = uiState.weightUnit,
                    heightUnit = uiState.heightUnit
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

@Composable
fun TrackerChart(
    modifier: Modifier = Modifier,
    title: String,
    chartData: List<Pair<String, Float>>,
    chartColor: List<Color>
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            modifier = modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            color = Color(0xFF191D30),
            fontWeight = FontWeight(700),
        )

        BMIRecordChart(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
            chartData = chartData,
            chartColor = chartColor
        )

    }
}

@Composable
fun BMIRecordChart(
    modifier: Modifier,
    chartData: List<Pair<String, Float>>,
    chartColor: List<Color>
) {
    val textFormatter = LocalTextFormatter.current
    val chartEntryModelProducer = remember(chartData) {
        val bmi = chartData
            .sortedBy { it.first }
            .mapIndexed { index, (date, y) ->
                Entry(
                    date = textFormatter.parse(date),
                    x = index.toFloat(),
                    y = y)
            }
        ChartEntryModelProducer().apply {
            setEntries(bmi)
        }
    }

    val bottomAxisValueFormatter = remember(textFormatter) {
        BottomAxisValueFormatter(textFormatter)
    }
    val startAxisValueFormatter = remember {
        DecimalFormatAxisValueFormatter<AxisPosition.Vertical.Start>("##.#")
    }
    val bottomTickPosition = remember(chartData) {
        if (chartData.size <= 5) {
            HorizontalAxis.TickPosition.Center(0, 1)
        } else {
            HorizontalAxis.TickPosition.Center(0, 2)
        }
    }
    val marker = rememberMarker()
    ProvideChartStyle(rememberChartStyle(chartColors = chartColor)) {
        Box(modifier = modifier) {
            Chart(
                chart =  lineChart(
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
