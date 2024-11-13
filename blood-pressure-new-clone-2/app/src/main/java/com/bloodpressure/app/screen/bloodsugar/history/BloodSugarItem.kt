package com.bloodpressure.app.screen.bloodsugar.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.bloodpressure.app.data.model.BloodSugarRecord
import com.bloodpressure.app.screen.bloodsugar.convertToMg
import com.bloodpressure.app.screen.bloodsugar.convertToMole
import com.bloodpressure.app.screen.bloodsugar.type.getStringAnnotation
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.BloodSugarUnit
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalTextFormatter

@Composable
fun BloodSugarRecordItem(
    modifier: Modifier = Modifier,
    record: BloodSugarRecord,
    onClick: (Long) -> Unit,
    bloodSugarUnit: BloodSugarUnit
) {
    val textFormatter = LocalTextFormatter.current
    val adsManager = LocalAdsManager.current
    Column(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Text(
            text = textFormatter.formatDateTime(record.date, record.time),
            style = TextStyle(
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight(400),
                color = GrayScale900,
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    adsManager.showClickHistoryItemAd {
                        onClick(record.rowId)
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
                        .height(48.dp)
                        .background(
                            color = record.bloodSugarRateType.color,
                            shape = RoundedCornerShape(8.dp)
                        )
                )

                Column(
                    modifier = Modifier.padding(horizontal = 11.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (record.bloodSugarUnit == bloodSugarUnit) {
                            "${record.bloodSugar}"
                        } else if (bloodSugarUnit == BloodSugarUnit.MILLIMOLES_PER_LITRE) {
                            "${record.bloodSugar.convertToMole()}"
                        } else {
                            "${record.bloodSugar.convertToMg()}"
                        },
                        style = TextStyle(
                            fontSize = 24.sp,
                            lineHeight = 32.sp,
                            fontWeight = FontWeight(700),
                            color = GrayScale900,
                            textAlign = TextAlign.Center,
                        )
                    )

                    Text(
                        text = bloodSugarUnit.value,
                        style = TextStyle(
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
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(id = record.bloodSugarRateType.nameRes),
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(700),
                            color = GrayScale900,
                        )
                    )

                    Text(
                        text = record.bloodSugarStateType.getStringAnnotation(),
                        style = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight(400),
                            color = GrayScale900,
                        )
                    )

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