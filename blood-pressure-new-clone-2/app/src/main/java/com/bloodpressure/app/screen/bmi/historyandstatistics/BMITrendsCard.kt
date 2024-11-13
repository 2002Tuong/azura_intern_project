package com.bloodpressure.app.screen.bmi.historyandstatistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.BMIRecord
import com.bloodpressure.app.screen.home.tracker.chartColors
import com.bloodpressure.app.utils.WeightUnit

@Composable
fun BMITrendsCard(
    modifier: Modifier = Modifier,
    chartData: List<BMIRecord>,
    weightUnit: WeightUnit,
    onWeightUnitChange:(WeightUnit, Float) -> Float
) {
    Column(
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
        )

        TrackerChart(
            title = "${stringResource(id = R.string.weight)} (${weightUnit.value})",
            chartColor = chartColors.reversed(),
            chartData = chartData.map { record -> record.date to onWeightUnitChange(weightUnit, record.weight) }
        )

        TrackerChart(
            title = stringResource(id = R.string.bmi),
            chartColor = chartColors,
            chartData = chartData.map { record -> record.date to record.bmi }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                )
        )
    }
}