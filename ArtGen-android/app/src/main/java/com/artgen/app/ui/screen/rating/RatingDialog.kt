package com.artgen.app.ui.screen.rating

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.artgen.app.R
import com.artgen.app.utils.LocalRatingManager
import kotlinx.coroutines.launch

@Composable
fun RatingDialog(
    onDismissRequest: () -> Unit,
    onRateClick: (Int, String) -> Unit
) {
    val ratingManager = LocalRatingManager.current
    val scope = rememberCoroutineScope()
    ratingManager.requestReviewFlow()
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 30.dp)
                .fillMaxWidth()
                .background(color = Color(0xFF343D65), shape = RoundedCornerShape(6.dp))
        ) {
            var rate by remember { mutableStateOf(0) }

            val icon = when (rate) {
                1 -> R.drawable.ic_loudly_crying_face
                2 -> R.drawable.ic_2_star_rate
                3 -> R.drawable.ic_3_star_rate
                4 -> R.drawable.ic_4_star_rate
                else -> R.drawable.ic_5_star_rate
            }
            if (rate != 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                ) {
                    Image(
                        modifier = Modifier
                            .align(Alignment.Center),
                        painter = painterResource(id = icon),
                        contentDescription = "",
                        alignment = Alignment.Center
                    )
                }
            }

            Text(
                // TODO: change when build
                text = "Are you happy with ArtGen?",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Text(
                // TODO: Change hardcode
                text = "Your feedback help us improve the quality of the app and provides a better experience",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                textAlign = TextAlign.Center
            )

            RatingBar(
                rate = rate,
                onRateChange = { rate = it },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {

                Text(
                    modifier = Modifier
                        .align(Alignment.Bottom)
                        .padding(end = 4.dp),
                    text = "The best we can get",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.5.sp,
                        color = Color(0xFFFF2E6C),
                    ),
                )
                Image(
                    modifier = Modifier
                        .padding(end = 16.dp, bottom = 4.dp),
                    painter = painterResource(id = R.drawable.ic_arrow_move_up_right),
                    contentDescription = ""
                )
            }

            val (feedBack, onFeedbackChange) = remember { mutableStateOf("") }
            if (rate != 0) {
                OutlinedTextField(
                    value = feedBack,
                    onValueChange = onFeedbackChange,
                    placeholder = {
                        Text(text = "Please leave feedback to help us improve product")
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        ratingManager.updateShowRatedStatus()
                    }
                    onRateClick(rate, feedBack)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                // TODO: Change hardcode
                Text(text = "Rate us")
            }

            TextButton(
                onClick = {
                    onDismissRequest()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                // TODO: Change hardcode
                Text(
                    text = "Cancel",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.5.sp,
                        color = Color(0xFF7884BA),
                    )
                )
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
            isLastItem = true
        )
    }
}

@Composable
fun RatingStar(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onClick: () -> Unit,
    isLastItem: Boolean = false
) {

    val starIcon = if (isLastItem) {
        if (isSelected) R.drawable.ic_last_star_highlight
        else R.drawable.ic_star_last_item
    } else {
        if (isSelected) R.drawable.ic_star_highlight
        else R.drawable.ic_star_normal
    }
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(id = starIcon),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = Color.Unspecified
        )
    }
}
