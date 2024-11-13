package com.bloodpressure.app.screen.bloodsugar.trends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodpressure.app.R
import com.bloodpressure.app.screen.bloodsugar.history.BloodSugarStatisticViewModel
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.BloodSugarUnit

@Composable
fun BloodSugarTrendsCard(
    modifier: Modifier = Modifier,
    uiState: BloodSugarStatisticViewModel.UiState,
    bloodSugarUnit: BloodSugarUnit
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White, shape = RoundedCornerShape(8.dp))
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.trends), style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                    )
                )

                BloodSugarTrendsBarChart(
                    modifier = Modifier.height(200.dp),
                    records = uiState.filteredRecords,
                    onRecordSelected = { },
                    bloodSugarUnit = bloodSugarUnit
                )
            }

        }
    }

}