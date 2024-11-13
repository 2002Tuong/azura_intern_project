package com.bloodpressure.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.WaterCupRecord
import com.bloodpressure.app.ui.theme.Blue80
import com.bloodpressure.app.ui.theme.GrayScale600
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.ui.theme.Green800
import kotlin.random.Random

@OptIn(ExperimentalTextApi::class)
@Composable
fun WaterHistoryChart(
    goal: Int,
    record: List<WaterCupRecord>,
    selectedPosition: Int,
) {
    val maxColumnHeight = remember { 144.dp }
    val maxValue = goal * 5 / 3
    val textMeasurer = rememberTextMeasurer()
    val longestLabel = textMeasurer.measure(
        "$maxValue",
        style = TextStyle(
            fontSize = 12.sp
        )
    )
    val leftPadding = LocalDensity.current.run { longestLabel.size.width.toDp() + 6.dp }
    val listLabel = remember { listOf(0, goal / 3, goal * 2 / 3, goal, goal * 4 / 3).reversed() }

    Box {
        Canvas(modifier = Modifier.fillMaxSize(), onDraw = {

            drawLine(
                color = GrayScale600,
                start = Offset(
                    leftPadding.toPx(), 194.dp.toPx()
                            - 16.dp.toPx() - longestLabel.size.height + 1.5.dp.toPx()
                ),
                end = Offset(
                    size.width - leftPadding.toPx(), 194.dp.toPx()
                            - 16.dp.toPx() - longestLabel.size.height + 1.5.dp.toPx()
                ),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Square,
            )

            drawLine(
                color = GrayScale600,
                start = Offset(
                    leftPadding.toPx(), 16.dp.toPx()
                ),
                end = Offset(
                    leftPadding.toPx(), 178.dp.toPx() - longestLabel.size.height + 1.5.dp.toPx() / 2
                ),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Square,
            )

            val labelStartY = 194.dp.toPx()
            -16.dp.toPx() - longestLabel.size.height + 1.5.dp.toPx()
            for (index in listLabel.indices) {
                val label = "${listLabel[index]}"
                val textSize = textMeasurer.measure(label, style = TextStyle(fontSize = 12.sp)).size
                val yPos = labelStartY / 12 + textSize.height / 2 + (index * labelStartY / 6)
                drawText(
                    textMeasurer = textMeasurer,
                    text = "${listLabel[index]}",
                    topLeft = Offset(
                        0f + longestLabel.size.width - textSize.width, yPos

                    ),
                    style = TextStyle(
                        fontSize = 12.sp
                    )
                )
                drawLine(
                    color = if (index == 1) Green800 else GrayScale600,
                    start = Offset(
                        leftPadding.toPx() + 1.dp.toPx(), yPos + longestLabel.size.height / 2
                    ),
                    end = Offset(
                        size.width - leftPadding.toPx(), yPos + longestLabel.size.height / 2
                    ),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Square,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(3.dp.toPx(), 5.dp.toPx()),
                        0f
                    )
                )
            }
        })
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(194.dp)
                .padding(start = leftPadding),
            contentPadding = PaddingValues(start = 10.dp),
            verticalAlignment = Alignment.Bottom,
            reverseLayout = true,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            content = {
                items(
                    record.size,
                    key = { index -> record[index].createdAt },
                    itemContent = { index ->
                        val item = record[index]
                        Column(
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(
                                        if (item.actualWater >= maxValue) maxColumnHeight
                                        else maxColumnHeight * item.actualWater / maxValue
                                    )
                                    .background(
                                        if (index == selectedPosition) Blue80
                                        else Color(0xff8BC9FD),
                                        shape = if (item.actualWater >= maxValue) RectangleShape
                                        else RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                item.date,
                                fontSize = 12.sp,
                                color = GrayScale900,
                                textAlign = TextAlign.Center
                            )
                            Image(
                                painter = painterResource(
                                    id = if (item.actualWater >= goal)
                                        R.drawable.check_circle_active
                                    else R.drawable.check_circle_inactive
                                ),
                                contentDescription = ""
                            )
                        }
                    }
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TestHistoryChart() {
    WaterHistoryChart(
        2340,
        listOf(
            WaterCupRecord(
                System.currentTimeMillis() + Random.nextInt(),
                2,
                300,
                "2:00",
                "20/9",
                3000
            ),
            WaterCupRecord(
                System.currentTimeMillis() + Random.nextInt(),
                2,
                300,
                "2:00",
                "19/9",
                2100
            ),
            WaterCupRecord(
                System.currentTimeMillis() + Random.nextInt(),
                2,
                300,
                "2:00",
                "18/9",
                2300
            ),
            WaterCupRecord(
                System.currentTimeMillis() + Random.nextInt(),
                2,
                300,
                "2:00",
                "17/9",
                1800
            ),
        ),
        0
    )
}