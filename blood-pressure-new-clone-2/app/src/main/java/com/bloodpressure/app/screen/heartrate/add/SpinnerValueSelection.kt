package com.bloodpressure.app.screen.heartrate.add

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bloodpressure.app.ui.component.Picker2

@Composable
fun SpinnerValueSelection(
    modifier: Modifier = Modifier,
    value: Int,
    onValueChanged: (Int) -> Unit,
    range: IntRange,
    selectedTextColor: Color
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFECEDEF), shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Picker2(
                currentValue = value,
                onValueChanged = onValueChanged,
                items = range.toList(),
                selectedTextColor = selectedTextColor,
                itemText = { it.toString() },
                itemKey = { it.toString() }
            )
        }
    }
}
