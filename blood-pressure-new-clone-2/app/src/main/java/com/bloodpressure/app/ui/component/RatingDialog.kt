package com.bloodpressure.app.ui.component

import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.bloodpressure.app.R

@Composable
fun RatingDialog(
    onDismissRequest: () -> Unit,
    onRateClick: (Int) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White, shape = RoundedCornerShape(6.dp))
        ) {
            IconButton(onClick = { onDismissRequest() }, modifier = Modifier.align(Alignment.End)) {
                Icon(imageVector = Icons.Outlined.Close, contentDescription = null)
            }

            Text(
                text = stringResource(id = R.string.rating_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(id = R.string.rating_des),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                textAlign = TextAlign.Center
            )

            var rate by remember { mutableStateOf(0) }
            RatingBar(
                rate = rate,
                onRateChange = { rate = it },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
            )

            Button(
                onClick = {
                    onDismissRequest()
                    onRateClick(rate)
                },
                enabled = rate != 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(text = stringResource(id = R.string.setting_rate_us))
            }
        }
    }
}

@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rate: Int,
    onRateChange: (Int) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        RatingStar(
            isSelected = rate >= 1,
            modifier = Modifier.weight(1f),
            onClick = { onRateChange(1) }
        )

        RatingStar(
            isSelected = rate >= 2,
            modifier = Modifier.weight(1f),
            onClick = { onRateChange(2) }
        )

        RatingStar(
            isSelected = rate >= 3,
            modifier = Modifier.weight(1f),
            onClick = { onRateChange(3) }
        )

        RatingStar(
            isSelected = rate >= 4,
            modifier = Modifier.weight(1f),
            onClick = { onRateChange(4) }
        )

        RatingStar(
            isSelected = rate == 5,
            modifier = Modifier.weight(1f),
            onClick = { onRateChange(5) },
            rotation = true
        )
    }
}

@Composable
fun RatingStar(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onClick: () -> Unit,
    rotation: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition()
    val angle by if (rotation && !isSelected) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000),
                initialStartOffset = StartOffset(offsetMillis = 100)
            )
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    val starIcon = if (isSelected) R.drawable.ic_star_highlight else R.drawable.ic_star_normal
    IconButton(onClick = onClick, modifier = modifier.graphicsLayer { rotationZ = angle }) {
        Icon(
            painter = painterResource(id = starIcon),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = Color.Unspecified
        )
    }
}

@Preview
@Composable
fun RatingDialogPreview() {
    RatingDialog({}, {})
}
