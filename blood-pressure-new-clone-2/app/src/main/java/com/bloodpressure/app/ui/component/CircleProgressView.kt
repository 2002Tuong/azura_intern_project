package com.bloodpressure.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.math.MathUtils.clamp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.bloodpressure.app.R
import java.lang.Float.max
import kotlin.math.cos
import kotlin.math.sin

val defaultCircleWidth = 24.dp

@Composable
fun CircularProgressView(
    progress: Float = 0f,
    viewSize: Dp = 242.dp
) {
    Canvas(
        modifier = Modifier
            .size(viewSize)
    ) {
        val realProgress = clamp(0f, progress, 100f)
        val angle = 360f * realProgress / 100 - 5

        drawArc(
            color = Color(0xffECEDEF),
            0f,
            360f,
            false,
            style = Stroke(defaultCircleWidth.toPx(), cap = StrokeCap.Round),
            size = Size(
                size.width - defaultCircleWidth.toPx(),
                size.height - defaultCircleWidth.toPx()
            ),
            topLeft = Offset(defaultCircleWidth.toPx() / 2, defaultCircleWidth.toPx() / 2)
        )

        drawArc(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xff38ACFA),
                    Color(0xff2935DD),
                ),
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, size.height)
            ),
            -85f,
            angle,
            false,
            style = Stroke(defaultCircleWidth.toPx(), cap = StrokeCap.Round),
            size = Size(
                size.width - defaultCircleWidth.toPx(),
                size.height - defaultCircleWidth.toPx()
            ),
            topLeft = Offset(defaultCircleWidth.toPx() / 2, defaultCircleWidth.toPx() / 2)
        )

        drawArc(
            color = Color(0xff2935DD).copy(alpha = 0.4f),
            90f,
            360f * max(realProgress, 50f) / 100 - 180,
            false,
            style = Stroke(defaultCircleWidth.toPx(), cap = StrokeCap.Round),
            size = Size(
                size.width - defaultCircleWidth.toPx(),
                size.height - defaultCircleWidth.toPx()
            ),
            topLeft = Offset(defaultCircleWidth.toPx() / 2, defaultCircleWidth.toPx() / 2)
        )

        val radius = (size.height / 2 - defaultCircleWidth.toPx() / 2)
        val x = (radius * sin(Math.toRadians(angle.toDouble() + 5))).toFloat() + (size.width / 2)
        val y = -(radius * cos(Math.toRadians(angle.toDouble() + 5))).toFloat() + (size.height / 2)
        drawCircle(
            color = Color.White,
            radius = defaultCircleWidth.toPx() / 2 - 2.dp.toPx(),
            center = Offset(
                x,
                y
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TestProgress() {
    var isPlaying by remember {
        mutableStateOf(true)
    }
// for speed
    var speed by remember {
        mutableStateOf(1f)
    }
    val composition by rememberLottieComposition(
        LottieCompositionSpec
            .RawRes(R.raw.water_anim)
    )
    var clipSpec by remember {
        mutableStateOf<LottieClipSpec>(
            LottieClipSpec.Frame(
                0,
                composition?.endFrame?.toInt() ?: 0
            )
        )
    }
    val progress by animateLottieCompositionAsState(
        // pass the composition created above
        composition,
        clipSpec  = clipSpec,
        iterations = 1,
        isPlaying = isPlaying,
        speed = speed,
        restartOnPlay = false
    )


    Box(contentAlignment = Alignment.Center) {

        CircularProgressView(progress = 100f)

        LottieAnimation(
            composition = composition, progress = progress,
            modifier = Modifier
                .size(194.dp)
                .background(Color.White, RoundedCornerShape(97.dp))
                .clip(RoundedCornerShape(97.dp))
        )
    }
}