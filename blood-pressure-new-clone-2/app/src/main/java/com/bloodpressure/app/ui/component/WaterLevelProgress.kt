package com.bloodpressure.app.ui.component

import android.animation.ValueAnimator
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodpressure.app.screen.waterreminder.WaterReminderViewModel
import com.bloodpressure.app.ui.theme.Blue80
import com.bloodpressure.app.ui.theme.GrayScale900
import kotlinx.coroutines.delay

@Composable
fun WaterLeverProgressView(
    uiState: WaterReminderViewModel.UiState
) {

    var waterLevelStart by remember { mutableStateOf(false) }
    val waterLevel by animateIntAsState(
        if (waterLevelStart) uiState.actualWater else 0, label = "float value animation",
        animationSpec = tween(
            durationMillis = 2000,
            easing = FastOutSlowInEasing
        )
    )
    var valueAnimator: ValueAnimator? = null

    val progressData = waterLevel.toFloat() / uiState.dailyGoal.toFloat() * 100
    LaunchedEffect(key1 = waterLevel, block = {
        delay(500)
        waterLevelStart = true
        valueAnimator?.start()
    })
    Box(contentAlignment = Alignment.Center) {
        CircularProgressView(progress = progressData)
        WaterWaveView(
            modifier = Modifier.size(
                194.dp
            ).clip(CircleShape),
            progress = progressData
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Blue80)) {
                        append("${uiState.actualWaterInDisplay}")
                    }
                    withStyle(SpanStyle(color = GrayScale900)) {
                        append("/${uiState.dailyGoalInDisplay} ${uiState.unitInDisplay}")
                    }
                },
                fontSize = 18.sp,
                style = TextStyle(
                    fontWeight = FontWeight(700),
                )
            )

            Text(
                text = "(${progressData.toInt()}%)",
                fontSize = 16.sp,
            )
        }
    }
}


@Preview
@Composable
fun PreviewWaterLevelProgressView() {

    Box(modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.TopCenter) {
        WaterLeverProgressView(
            uiState = WaterReminderViewModel.UiState()
        )
    }
}