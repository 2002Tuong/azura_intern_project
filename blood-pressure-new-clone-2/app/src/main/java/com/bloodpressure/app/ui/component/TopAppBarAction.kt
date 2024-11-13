package com.bloodpressure.app.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bloodpressure.app.R

@Composable
fun RowScope.TopAppBarAction(
    isPurchased: Boolean,
    additionalAction: @Composable RowScope.() -> Unit = {},
    onSetAlarmClick: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    additionalAction.invoke(this)
    IconButton(onClick = onSetAlarmClick) {
        Image(
            painter = painterResource(id = R.drawable.ic_set_alarm),
            contentDescription = null,
            modifier = Modifier.size(21.dp),
            contentScale = ContentScale.Inside
        )
    }
    if (!isPurchased) {
        IconButton(onClick = onNavigateToPremium) {
            Icon(
                painter = painterResource(id = R.drawable.ic_premium_title),
                contentDescription = null,
                tint = Color.Unspecified
            )
        }
    }
}