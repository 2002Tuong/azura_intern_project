package com.bloodpressure.app.ui.component

import androidx.compose.animation.core.Ease
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun WaterWaveView(
    modifier: Modifier = Modifier,
    progress: Float
) = BoxWithConstraints {

    val density = LocalDensity.current
    val width = with(density) {
        maxWidth.toPx() }

    var movePositionX by remember { mutableStateOf(0f) }
    var movePositionY by remember { mutableStateOf(0f) }
    var movePositionXReverse by remember { mutableStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val ticker by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = width,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = EaseOut
            ),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(modifier = modifier) {

        LaunchedEffect(ticker) {
            movePositionX = ticker
            movePositionY = 100.dp.value * sin(150f).roundToInt()
            movePositionXReverse =  width - ticker

        }
        Canvas(modifier = Modifier.fillMaxSize()) {

            val progressValue = size.height * progress / 100
            val currentHeight = size.height - progressValue + 1.dp.toPx()

            val reversePath = Path().apply {
                moveTo(0f, currentHeight)

                cubicTo(
                    0f,
                    currentHeight,
                    movePositionXReverse,
                    currentHeight + movePositionY,
                    size.width,
                    currentHeight
                )
                close()
            }

            drawPath(
                reversePath,
                color = Color(0xffE8F4FE)
            )

            val path = Path().apply {
                moveTo(0f, currentHeight)

                cubicTo(
                    0f,
                    currentHeight,
                    movePositionX,
                    currentHeight + movePositionY,
                    size.width,
                    currentHeight
                )
                close()
            }

            drawPath(
                path,
                color = Color(0xff8BC9FD)
            )

            drawRect(
                color = Color(0xff8BC9FD),
                topLeft = Offset(
                    0f, size.height - progressValue
                ),
                size = Size(size.width, progressValue)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun testWaveView() {
    WaterWaveView(
        progress = 10f,
        modifier = Modifier.size(194.dp)
    )
}