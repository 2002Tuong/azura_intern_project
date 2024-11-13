package com.bloodpressure.app.screen.bloodpressure.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.Record
import com.bloodpressure.app.screen.record.BpType
import com.bloodpressure.app.ui.theme.GrayScale600
import com.bloodpressure.app.ui.theme.GrayScale900

@Composable
fun BloodPressureStatisticsCard(
    modifier: Modifier = Modifier,
    records: List<Record>,
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White, shape = RoundedCornerShape(8.dp))
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        text = stringResource(id = R.string.statistics),
                        style = TextStyle(
                            fontSize = 18.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight(700),
                            color = GrayScale900,
                        )
                    )

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(GrayScale600)
                    )

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        BloodPressureStatisticsPieChart(
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .aspectRatio(1f),
                            records = records
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
                                text = "${records.size}",
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

                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            color = BpType.HYPOTENSION.color,
                                            shape = RoundedCornerShape(size = 20.dp)
                                        )
                                )

                                Text(
                                    modifier = Modifier.padding(start = 4.dp),
                                    text = stringResource(id = BpType.HYPOTENSION.nameRes),
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        fontWeight = FontWeight(400),
                                        color = Color.Black,
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            color = BpType.NORMAL.color,
                                            shape = RoundedCornerShape(size = 20.dp)
                                        )
                                )

                                Text(
                                    modifier = Modifier.padding(start = 4.dp),
                                    text = stringResource(id = BpType.NORMAL.nameRes),
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        fontWeight = FontWeight(400),
                                        color = Color.Black,
                                    )
                                )
                            }

                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            color = BpType.ELEVATED.color,
                                            shape = RoundedCornerShape(size = 20.dp)
                                        )
                                )

                                Text(
                                    modifier = Modifier.padding(start = 4.dp),
                                    text = stringResource(id = BpType.ELEVATED.nameRes),
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        fontWeight = FontWeight(400),
                                        color = Color.Black,
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            color = BpType.HYPERTENSION_STAGE_1.color,
                                            shape = RoundedCornerShape(size = 20.dp)
                                        )
                                )

                                Text(
                                    modifier = Modifier.padding(start = 4.dp),
                                    text = stringResource(id = BpType.HYPERTENSION_STAGE_1.nameRes),
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        fontWeight = FontWeight(400),
                                        color = Color.Black,
                                    )
                                )
                            }

                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            color = BpType.HYPERTENSION_STAGE_2.color,
                                            shape = RoundedCornerShape(size = 20.dp)
                                        )
                                )

                                Text(
                                    modifier = Modifier.padding(start = 4.dp),
                                    text = stringResource(id = BpType.HYPERTENSION_STAGE_2.nameRes),
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        fontWeight = FontWeight(400),
                                        color = Color.Black,
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            color = BpType.HYPERTENSIVE.color,
                                            shape = RoundedCornerShape(size = 20.dp)
                                        )
                                )

                                Text(
                                    modifier = Modifier.padding(start = 4.dp),
                                    text = stringResource(id = BpType.HYPERTENSIVE.nameRes),
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        fontWeight = FontWeight(400),
                                        color = Color.Black,
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