package com.bloodpressure.app.screen.barcodescan

import android.os.Debug
import android.util.Log
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.applovin.impl.sdk.c.b.by
import kotlinx.serialization.json.Json.Default.configuration

@Composable
fun ScanningView(
    modifier: Modifier = Modifier,
    stopScanning: Boolean,
    width: Dp,
    height: Dp
) {

    val density = LocalDensity.current
    val animation = rememberInfiniteTransition(label = "")
    val scanner by animation.animateFloat(
        initialValue = 0f,
        targetValue = with(density) {
            height.toPx()
        } + 20,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                delayMillis = 400,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )
    var currentAnimationValue by remember { mutableStateOf(0f) }

    LaunchedEffect(scanner) {
        if(!stopScanning) {
            currentAnimationValue = scanner
        }
    }
    Canvas(
        modifier = modifier.size(width, height),
        onDraw = {
            fun drawScanningLine(scanner: Float) {
                if(scanner < 50) {
                    drawRect(
                        brush = Brush.linearGradient(
                            listOf(Color.Transparent, Color.Green),
                            start = Offset(0f, 0f),
                            end =  Offset(0f, scanner)
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size(this.size.width,scanner)
                    )
                } else if(scanner >= 50 && scanner <= size.height) {
                    drawRect(
                        brush = Brush.linearGradient(
                            listOf(Color.Transparent, Color.Green),
                            start = Offset(0f, scanner-50),
                            end =  Offset(0f, scanner)
                        ),
                        topLeft = Offset(0f, scanner-50),
                        size = Size(this.size.width - 8f,50f)
                    )
                }
            }

            if(stopScanning) {
                drawScanningLine(currentAnimationValue)
            } else {
                drawScanningLine(scanner)
            }
            //Draw detect region part

            // Define the path for the curve
            val path1 = Path().apply {
                moveTo(0f, size.height / 5) // Starting point
                cubicTo(
                    0f, 0f, // Control point
                    0f, 0f,// Control point
                    size.width/5, 0f  // End point
                )
            }

            fun draw(path: Path) {
                drawPath(path, Color.White, style = Stroke(width = 6.dp.toPx()))
            }
            draw(path1)

            withTransform({
                scale(-1f, 1f)
            }) {
                draw(path1)
            }

            withTransform({
                scale(1f, -1f)
            }) {
                draw(path1)
            }

            withTransform({
                scale(-1f, -1f)
            }) {
                draw(path1)
            }
        }
    )
}
@Preview
@Composable
private fun DetectRegionPreview() {
    Surface {
//        ScanningView(
//            width = 300.dp,
//            height = 300.dp * 2 / 3
//        )
    }

}