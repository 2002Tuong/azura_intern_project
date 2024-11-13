package com.bloodpressure.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.math.MathUtils.clamp
import com.bloodpressure.app.ui.theme.Blue80
import com.bloodpressure.app.ui.theme.GrayScale600
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Math.abs
import java.lang.Math.min
import kotlin.math.absoluteValue


@OptIn(ExperimentalTextApi::class)
@Composable
fun WaterRulerView(
    currentValue: Int = 200,
    onValueChange: (Int) -> Unit,
    isUseMlUnit: Boolean = true,
    containerPadding: Dp = 0.dp
) {
    Box(modifier = Modifier.height(86.dp)) {
        var offsetX by remember(key1 = currentValue) { mutableStateOf(0f) }
        val textMeasurer = rememberTextMeasurer()
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        offsetX += delta
                    }
                )
        ) {
            val maxItemCount = if (isUseMlUnit) 20 else 10
            val longLine = 61.dp.toPx()
            val shortLine = 34.dp.toPx()
            val horizontalPadding = 10.dp.toPx() + containerPadding.toPx()

            val initialPos = (size.width) / 2
            val lineSpacing = (size.width - horizontalPadding * 2) / (maxItemCount * 2 + 1)

            val maxDrawingPosition = initialPos + (maxItemCount - 1) * lineSpacing
            val minDrawingPosition = initialPos - (maxItemCount) * lineSpacing

            val initValue = if (isUseMlUnit) 200 else 10
            val ratio = if (isUseMlUnit) 10 else 5
            val valueDiff = initValue - currentValue.coerceAtMost(if (isUseMlUnit) 400 else 20)
            val roundingDp = when {
                valueDiff < 0 -> -1.5.dp.toPx()
                valueDiff > 0 -> 1.5.dp.toPx()
                else -> 0f
            }
            val indicatorPos =
                clamp(
                    initialPos + roundingDp + offsetX + valueDiff / (if (isUseMlUnit) 10f else 1f) * lineSpacing,
                    minDrawingPosition,
                    maxDrawingPosition
                )

            drawLine(
                color = GrayScale600,
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round,
                start = Offset(indicatorPos, 0f),
                end = Offset(indicatorPos, longLine)
            )

            drawText(
                textMeasurer = textMeasurer,
                text = "$initValue",
                style = TextStyle(
                    fontSize = 15.sp,
                    color = Color(0xff8C8E97),
                ),
                topLeft = Offset(
                    x = indicatorPos - measureText("$initValue", textMeasurer) / 2,
                    y = longLine + 3.dp.toPx()
                ),
                overflow = TextOverflow.Clip,
                maxLines = 1
            )

            var nextPos = indicatorPos + lineSpacing
            var positionCount = 1

            while (positionCount <= maxItemCount) {
                drawContent(
                    nextPos,
                    positionCount,
                    longLine,
                    shortLine,
                    maxDrawingPosition,
                    minDrawingPosition,
                    textMeasurer,
                    true,
                    isUseMlUnit,
                    ratio
                )

                nextPos += lineSpacing
                positionCount++
            }

            nextPos = indicatorPos - lineSpacing
            positionCount = 1
            while (positionCount <= maxItemCount) {
                drawContent(
                    nextPos,
                    positionCount,
                    longLine,
                    shortLine,
                    maxDrawingPosition,
                    minDrawingPosition,
                    textMeasurer,
                    false,
                    isUseMlUnit,
                    ratio
                )

                nextPos -= lineSpacing
                positionCount++
            }

            drawLine(
                color = Blue80,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
                start = Offset(initialPos, 0f),
                end = Offset(initialPos, longLine)
            )

            val newValue =
                initValue + ((initialPos - indicatorPos) / lineSpacing).toInt() * if (isUseMlUnit) 10f else 1f
            onValueChange(newValue.toInt())
        }

    }

}

@OptIn(ExperimentalTextApi::class)
fun DrawScope.drawContent(
    nextPos: Float,
    positionCount: Int,
    longLine: Float,
    shortLine: Float,
    maxDrawingPosition: Float,
    minDrawingPosition: Float,
    textMeasurer: TextMeasurer,
    isDrawRight: Boolean,
    isUseMlUnit: Boolean,
    ratio: Int,
) {
    drawLine(
        color = GrayScale600,
        strokeWidth = 1.dp.toPx(),
        cap = StrokeCap.Round,
        start = Offset(nextPos, 0f),
        end = Offset(nextPos, if (positionCount % ratio == 0) longLine else shortLine)
    )

    if (positionCount % ratio == 0
        && if (isDrawRight) (nextPos < maxDrawingPosition + 20.dp.toPx())
        else (nextPos > minDrawingPosition - 20.dp.toPx())
    ) {
        var firstPositionDigit = (positionCount / ratio).toString()
            .let { it.dropLastWhile { char -> char == '0' }.ifBlank { "0" }.toInt() }
        val number = "${
            if (isUseMlUnit) (200 + 100 * (firstPositionDigit % 4) * if (isDrawRight) 1 else -1)
            else (10 + 5 * (firstPositionDigit % 4) * if (isDrawRight) 1 else -1)
        }"
        drawText(
            textMeasurer = textMeasurer,
            text = number,
            style = TextStyle(
                fontSize = 15.sp,
                color = Color(0xff8C8E97),
            ),
            topLeft = Offset(
                x = nextPos - if (number == "0") 5.dp.toPx().toInt()
                else (measureText(number, textMeasurer)) / 2,
                y = longLine + 3.dp.toPx(),
            ),
            softWrap = false
        )
    }
}

@OptIn(ExperimentalTextApi::class)
fun measureText(s: String, textMeasurer: TextMeasurer): Int {
    val textLayoutResult: TextLayoutResult =
        textMeasurer.measure(
            text = AnnotatedString("200"),
            style = TextStyle(fontSize = 15.sp)
        )
    return textLayoutResult.size.width
}

@Preview(showBackground = true)
@Composable
fun TestWaterRuler() {
    var number by remember {
        mutableStateOf(200)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$number")
        WaterRulerView(
            7, {
                number = it
            },
            false
        )
    }

}