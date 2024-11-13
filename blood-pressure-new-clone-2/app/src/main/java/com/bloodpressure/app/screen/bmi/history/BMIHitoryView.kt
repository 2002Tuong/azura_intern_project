package com.bloodpressure.app.screen.bmi.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodpressure.app.R
import com.bloodpressure.app.data.local.NoteConverters
import com.bloodpressure.app.data.model.BMIRecord
import com.bloodpressure.app.screen.bloodpressure.DateItem
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.ConvertUnit
import com.bloodpressure.app.utils.HeightUnit
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.WeightUnit

@Composable
fun BMIRecordsList(
    modifier: Modifier,
    records: Map<String, List<BMIRecord>>,
    onItemClick: (Long) -> Unit,
    weightUnit: WeightUnit = WeightUnit.KG,
    heightUnit: HeightUnit = HeightUnit.CM,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
    ) {
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

        records.forEach { entry ->
            item {
                DateItem(date = entry.key)
            }

            items(entry.value) { item ->
                BMIItem(
                    bmiRecord = item,
                    onItemClick = onItemClick,
                    weightUnit = weightUnit,
                    heightUnit = heightUnit,
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
fun BMIItem(
    modifier: Modifier = Modifier,
    bmiRecord: BMIRecord,
    onItemClick: (Long) -> Unit,
    weightUnit: WeightUnit,
    heightUnit: HeightUnit,
) {
    val adsManager = LocalAdsManager.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White)
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .clickable {
                    adsManager.showClickHistoryItemAd {
                        onItemClick(bmiRecord.createdAt)
                    }
                },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(width = 1.dp, color = Color(0xFFF4F4F5))
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
                        .background(color = bmiRecord.type.color, shape = RoundedCornerShape(8.dp))
                )

                Column(
                    modifier = Modifier.padding(horizontal = 11.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format("%.2f", bmiRecord.bmi), style = TextStyle(
                            fontSize = 24.sp,
                            lineHeight = 32.sp,
                            fontWeight = FontWeight(700),
                            color = GrayScale900,
                            textAlign = TextAlign.Center,
                        )
                    )

                    Text(
                        text = stringResource(R.string.bmi), style = TextStyle(
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
                        .weight(1f),
                ) {
                    Text(
                        text = stringResource(id = bmiRecord.type.nameRes),
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(700),
                            color = GrayScale900,
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when(weightUnit) {
                                WeightUnit.KG -> "${String.format("%.2f", bmiRecord.weight)} ${weightUnit.value}"
                                WeightUnit.LBS ->  {
                                    val convertValue = ConvertUnit.convertKgToLbs(bmiRecord.weight)
                                    "${String.format("%.2f", convertValue)} ${weightUnit.value}"}
                            },
                            style =TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight(700),
                                color = Color(0xFF1892FA),
                            ),
                            color = Color(0xFF1892FA)
                        )

                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp, top = 4.dp)
                                .width(1.dp)
                                .height(16.dp)
                                .background(color = Color(0xFFECEDEF))
                        )

                        Text(
                            text =  when(heightUnit) {
                                HeightUnit.CM -> "${String.format("%.1f", bmiRecord.height)} ${heightUnit.value}"
                                HeightUnit.FT_IN -> {val convertValue = ConvertUnit.convertCmToFtIn(bmiRecord.height)
                                    String.format("%d'%.1f''", convertValue.ft.toInt(), convertValue.inches).replace(",", ".")
                                }
                            } ,
                            style =TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight(700),
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    val noteText = remember(bmiRecord) {
                        if (bmiRecord.notes.isNotEmpty()) {
                            "#${bmiRecord.notes.joinToString(separator = NoteConverters.JOIN_CHAR)}"
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
}
