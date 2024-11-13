package com.bloodpressure.app.screen.bmi.historyandstatistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bloodpressure.app.R
import com.bloodpressure.app.screen.bmi.statistics.StatisticsData
import com.bloodpressure.app.screen.bmi.statistics.StatisticsPieChart
import com.bloodpressure.app.utils.WeightUnit

@Composable
fun BMIStatisticsCard(
    modifier: Modifier = Modifier,
    uiState: BMIHistoryAndStatisticsViewModel.UiState,
    onWeightUnitChange: (WeightUnit, Float) -> Float
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
        ) {
            StatisticsData(
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 16.dp),
                minValue = String.format("%.2f", onWeightUnitChange(uiState.weightUnit, uiState.weightMin)).replace(",", "."),
                maxValue = String.format("%.2f", onWeightUnitChange(uiState.weightUnit, uiState.weightMax)).replace(",", "."),
                title = "${stringResource(id = R.string.weight)}  (${uiState.weightUnit.value})")

            Spacer(modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(Color(0xFFECEDEF))
            )

            StatisticsData(
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 16.dp),
                minValue = String.format("%.2f", uiState.bmiMin).replace(",", "."),
                maxValue = String.format("%.2f", uiState.bmiMax).replace(",", "."),
                title = stringResource(id = R.string.bmi)
            )

            Spacer(modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(Color(0xFFECEDEF))
            )

            StatisticsPieChart(
                modifier = Modifier.padding(16.dp),
                listData = uiState.filteredRecordForStatistics!!,
                totalRecords = uiState.filteredRecordForTrends!!.size
            )
        }
    }
}