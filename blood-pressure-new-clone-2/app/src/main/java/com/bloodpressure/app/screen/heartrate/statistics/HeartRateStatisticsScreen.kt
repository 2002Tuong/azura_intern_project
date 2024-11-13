package com.bloodpressure.app.screen.heartrate.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.screen.heartrate.detail.StatisticsPieChart
import com.bloodpressure.app.screen.heartrate.history.EmptyHeartRateRecords
import com.bloodpressure.app.screen.heartrate.trends.DateRangeSelection
import com.bloodpressure.app.screen.heartrate.trends.filterRecordsByDateRange
import com.bloodpressure.app.ui.theme.GrayScale600
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateStatisticsScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    viewModel: HeartRateStatisticsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(title = {
            Text(
                text = stringResource(R.string.heart_rate_statistics), style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight(700),
                    color = GrayScale900,
                )
            )
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        ), navigationIcon = {
            IconButton(onClick = { onNavigateUp() }) {
                Icon(
                    painter = rememberVectorPainter(image = Icons.Default.ArrowBack), contentDescription = null
                )
            }
        })

        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(GrayScale600)
        )

        HeartRateStatisticsContent(
            uiState = uiState,
            onDateRangeChanged = { startDate, endDate ->
                viewModel.setFilteredHeartRateRecords(filterRecordsByDateRange(uiState.allRecords, startDateLong = startDate, endDateLong = endDate))
            }
        )
    }
}

@Composable
fun HeartRateStatisticsContent(
    modifier: Modifier = Modifier,
    uiState: HeartRateStatisticsViewModel.UiState,
    onDateRangeChanged: (Long, Long) -> Unit
) {

    Column(
        modifier = modifier.fillMaxSize(),
    ) {

        DateRangeSelection(onDateRangeChanged = onDateRangeChanged)

        if (uiState.allRecords.isEmpty()) {
            EmptyHeartRateRecords()

        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPaddingIfNeed(), verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.White, shape = RoundedCornerShape(8.dp))
                ) {

                    if (uiState.filteredRecords.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.4f), contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No data found in this period.")
                        }
                    } else {

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.average), style = TextStyle(
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            fontWeight = FontWeight(400),
                                            color = Color(0xFF8C8E97),
                                        )
                                    )

                                    Text(
                                        text = "${uiState.averageHeartRate}", style = TextStyle(
                                            fontSize = 24.sp,
                                            lineHeight = 32.sp,
                                            fontWeight = FontWeight(700),
                                            color = GrayScale900,
                                        )
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.min), style = TextStyle(
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            fontWeight = FontWeight(400),
                                            color = Color(0xFF8C8E97),
                                        )
                                    )

                                    Text(
                                        text = "${uiState.minHeartRate}", style = TextStyle(
                                            fontSize = 24.sp,
                                            lineHeight = 32.sp,
                                            fontWeight = FontWeight(700),
                                            color = GrayScale900,
                                        )
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.max), style = TextStyle(
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            fontWeight = FontWeight(400),
                                            color = Color(0xFF8C8E97),
                                        )
                                    )

                                    Text(
                                        text = "${uiState.maxHeartRate}", style = TextStyle(
                                            fontSize = 24.sp,
                                            lineHeight = 32.sp,
                                            fontWeight = FontWeight(700),
                                            color = GrayScale900,
                                        )
                                    )
                                }
                            }

                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(GrayScale600)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(color = Color(0xFF329EFB), shape = RoundedCornerShape(size = 20.dp))
                                    )

                                    Text(
                                        modifier = Modifier.padding(start = 4.dp),
                                        text = stringResource(id = R.string.slow),
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            fontWeight = FontWeight(400),
                                            color = Color(0xFF329EFB),
                                        )
                                    )
                                }

                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(color = Color(0xFF62A970), shape = RoundedCornerShape(size = 20.dp))
                                    )

                                    Text(
                                        modifier = Modifier.padding(start = 4.dp),
                                        text = stringResource(id = R.string.normal),
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            fontWeight = FontWeight(400),
                                            color = Color(0xFF62A970),
                                        )
                                    )
                                }

                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(color = Color(0xFFAE2F05), shape = RoundedCornerShape(size = 20.dp))
                                    )

                                    Text(
                                        modifier = Modifier.padding(start = 4.dp),
                                        text = stringResource(id = R.string.fast),
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            fontWeight = FontWeight(400),
                                            color = Color(0xFFAE2F05),
                                        )
                                    )
                                }
                            }

                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                StatisticsPieChart(
                                    modifier = Modifier
                                        .fillMaxWidth(0.75f)
                                        .aspectRatio(1f),
                                    slowRecordsCount = uiState.slowRecordsCount,
                                    normalRecordsCount = uiState.normalRecordsCount,
                                    fastRecordsCount = uiState.fastRecordsCount,
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = stringResource(R.string.total),
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            fontWeight = FontWeight(400),
                                            color = Color(0xFF8C8E97),
                                        )
                                    )

                                    Text(
                                        text = "${uiState.filteredRecords.size}",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp,
                                            fontWeight = FontWeight(700),
                                            color = GrayScale900,
                                            textAlign = TextAlign.Center,
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }

    }

}