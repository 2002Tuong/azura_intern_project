package com.bloodpressure.app.screen.heartrate.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalTextFormatter

@Composable
fun HistoryHeartRateRecordList(
    modifier: Modifier = Modifier,
    records: List<HeartRateRecord>,
    onItemClick: (Long) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(id = R.string.history),
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight(700),
                    color = GrayScale900,
                )
            )
        }

        items(records) { record ->
            HeartRateRecordItem(
                record = record,
                onClick = onItemClick,
            )
        }
    }
}

@Composable
fun HeartRateRecordItem(
    modifier: Modifier = Modifier,
    record: HeartRateRecord,
    onClick: (Long) -> Unit,
) {
    val textFormatter = LocalTextFormatter.current
    val adsManager = LocalAdsManager.current
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .clickable {
                adsManager.showClickHistoryItemAd {
                    onClick(record.createdAt)
                }
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(width = 1.dp, color = Color(0xFFF4F4F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 1.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(28.dp)
                    .background(color = record.type.color, shape = RoundedCornerShape(8.dp))
            )

            Column(modifier = Modifier.padding(horizontal = 11.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${record.heartRate}", style = TextStyle(
                        fontSize = 24.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                        textAlign = TextAlign.Center,
                    )
                )

                Text(
                    text = "BPM", style = TextStyle(
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
                    text = stringResource(id = record.type.nameRes), style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight(700),
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

                Text(
                    text = textFormatter.formatDateTime(record.date, record.time),
                    style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight(400),
                        color = Color(0xFF8C8E97),
                    )
                )
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

@Composable
fun EmptyHeartRateRecords(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White), contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Image(
                modifier = Modifier
                    .width(168.dp)
                    .height(103.dp), painter = painterResource(id = R.drawable.no_data_img), contentDescription = ""
            )

            Text(
                text = "No data",
                style = TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF8C8E97),
                )
            )
        }
    }
}