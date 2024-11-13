package com.bloodpressure.app.screen.waterreminder.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.WaterCupRecord
import com.bloodpressure.app.screen.waterreminder.WaterReminderViewModel.Companion.convertMlToOz
import com.bloodpressure.app.ui.theme.Blue80
import com.bloodpressure.app.ui.theme.GrayScale600
import com.bloodpressure.app.ui.theme.GrayScale800
import com.bloodpressure.app.ui.theme.GrayScale900

@Composable
fun WaterRecordList(
    modifier: Modifier = Modifier,
    isMl: Boolean,
    records: List<WaterCupRecord>,
    onViewAllHistory: (() -> Unit)? = null,
    isFull: Boolean = false
) {

    Column(
        modifier = modifier
            .background(color = Color.White)
    ) {
        if (!isFull) {
            Row(
                Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.history),
                    style = TextStyle(GrayScale900, fontWeight = FontWeight(700))
                )
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onViewAllHistory?.let {
                                if (!isFull) it.invoke()
                            }
                        },
                    textAlign = TextAlign.End,
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Blue80)) {
                            append(stringResource(id = R.string.view_all))
                        }
                    },
                    style = TextStyle(
                        GrayScale900,
                        fontWeight = FontWeight(700),
                        textDecoration = TextDecoration.Underline
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        if (isFull) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
            ) {
                records.forEach {
                    item {
                        WaterRecordViewItem(isMl, it)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        } else {
            records.take(5).forEach {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(8.dp))
                ) {
                    WaterRecordViewItem(isMl, it)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }

}

@Composable
fun WaterRecordViewItem(isMl: Boolean, item: WaterCupRecord) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .border(
                width = 1.dp,
                color = GrayScale600,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Image(
                modifier = Modifier
                    .width(44.dp)
                    .aspectRatio(1f),
                painter = painterResource(id = R.drawable.ic_water_cup_item),
                contentDescription = ""
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = if(isMl) "${item.bottleSize} ml" else "${convertMlToOz(item.bottleSize)} oz",
                    style = TextStyle(fontSize = 15.sp, color = GrayScale900)
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "${item.date}, ${item.time}",
                    style = TextStyle(fontSize = 15.sp, color = GrayScale800)
                )
            }
        }
    }


}
