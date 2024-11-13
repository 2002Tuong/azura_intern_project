package com.bloodpressure.app.screen.bmi.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.BMIRecord
import com.bloodpressure.app.screen.bloodpressure.BloodPressureEmpty
import com.bloodpressure.app.screen.bmi.add.BMIType
import com.bloodpressure.app.screen.heartrate.detail.DateRangeSelection
import com.bloodpressure.app.ui.theme.BloodPressureAndroidTheme
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMIStatisticsScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onAddClick: () -> Unit,
    viewModel: BMIStatisticsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.navigationBarsPaddingIfNeed()) {
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
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.ArrowBack), contentDescription = null
                    )
                }
            },
        )

        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(Color(0xFFECEDEF))
        )

        DateRangeSelection(
            onDateRangeChanged = { startDate, endDate ->
                viewModel.setFilteredRecords(
                    startDate, endDate
                )
            }
        )

        BMIStatisticsContent(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            uiState = uiState,
            onAddClick = onAddClick,
        )
    }
}

@Composable
fun BMIStatisticsContent(
    modifier: Modifier = Modifier,
    uiState: BMIStatisticsViewModel.UiState,
    onAddClick: () -> Unit
) {

    if( uiState.filteredRecord != null &&
        uiState.filteredRecord.isEmpty() &&
        uiState.filteredPieChartRecord != null &&
        uiState.filteredPieChartRecord.isEmpty()) {
        BloodPressureEmpty(
            modifier = modifier
                .padding(16.dp)
                .fillMaxWidth(),
            onAddRecordClick = {onAddClick()}
        )
    }else if(uiState.filteredRecord != null && uiState.filteredPieChartRecord != null) {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = modifier
                .padding(16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
            ) {
                StatisticsData(
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 16.dp),
                    minValue = String.format("%.2f", uiState.weightMin).replace(",", "."),
                    maxValue = String.format("%.2f", uiState.weightMax).replace(",", "."),
                    title = "${stringResource(id = R.string.weight)}  (${stringResource(id = R.string.kg)})")
                
                Spacer(modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(Color(0xFFECEDEF))
                )

                StatisticsData(
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 16.dp),
                    minValue = String.format("%.2f", uiState.bmiMin).replace(",", "."),
                    maxValue = String.format("%.2f", uiState.bmiMax).replace(",", "."),
                    title = stringResource(id = R.string.bmi))

                Spacer(modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(Color(0xFFECEDEF))
                )

                StatisticsPieChart(
                    modifier = Modifier.padding(16.dp),
                    listData = uiState.filteredPieChartRecord,
                    totalRecords = uiState.filteredRecord.size
                )
            }
        }
    }

}

@Composable
fun Title(
    modifier: Modifier = Modifier,
    title: String
) {
    Text(
        title,
        modifier = modifier,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight(700),
        color = Color(0xFF191D30),
    )
}

@Composable
fun Attribute(
    modifier: Modifier = Modifier,
    title: String,
    content: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            title,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight(400),
            color = Color(0xFF8C8E97),
        )

        Text(
            content,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight(700),
            color = Color(0xFF191D30),
        )
    }
}

@Composable
fun StatisticsData(
    modifier: Modifier = Modifier,
    minValue: String,
    maxValue: String,
    title: String
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Title(title = title)
        Row {
            Attribute(
                title = stringResource(id = R.string.min),
                content = minValue
            )

            Spacer(modifier = Modifier.width(50.dp))

            Attribute(
                title = stringResource(id = R.string.max),
                content = maxValue
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatisticsPieChart(
    modifier: Modifier = Modifier,
    listData: Map<BMIType, List<BMIRecord>>,
    totalRecords: Int
) {
    Column(modifier = modifier) {
        FlowRow(
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            maxItemsInEachRow = 3,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            listData.keys.sorted().forEach {type ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = type.color,
                                shape = RoundedCornerShape(size = 20.dp)
                            )
                    )

                    Text(
                        modifier = Modifier.padding(start = 4.dp),
                        text = stringResource(id = type.nameRes),
                        style = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight(400),
                            color = type.color,
                        )
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(id = R.string.total),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF8C8E97),
                )

                Text(
                    totalRecords.toString(),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight(700),
                    color = Color(0xFF191D30),
                    textAlign = TextAlign.Center,
                )
            }

            BMITypePieChart(
                data = listData,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .aspectRatio(1f),
            )
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
fun BMIStatisticsPreview() {
    BloodPressureAndroidTheme {
        //BMIStatisticsContent( uiState = BMIStatisticsViewModel.UiState())
        FlowRow(
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            maxItemsInEachRow = 3
        ) {
            BMIType.values().forEach {type ->
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = type.color,
                                shape = RoundedCornerShape(size = 20.dp)
                            )
                    )

                    Text(
                        modifier = Modifier.padding(start = 4.dp),
                        text = stringResource(id = type.nameRes),
                        style = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight(400),
                            color = type.color,
                        )
                    )
                }

            }
        }
    }
}

