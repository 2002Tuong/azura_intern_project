package com.bloodpressure.app.screen.heartrate.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.screen.heartrate.history.HeartRateRecordItem
import com.bloodpressure.app.ui.theme.GrayScale900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateHistoryCard(
    modifier: Modifier = Modifier,
    records: List<HeartRateRecord>,
    onNavigateToHistory: () -> Unit,
    onRecordItem: (Long) -> Unit
) {

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White, shape = RoundedCornerShape(8.dp))
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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

                    records.map { record ->
                        HeartRateRecordItem(
                            record = record,
                            onClick = { onRecordItem(record.createdAt) }
                        )
                    }
                }
            }

        }
    }

}