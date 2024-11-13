package com.bloodpressure.app.screen.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.WeekDays
import java.time.DayOfWeek

@Composable
fun DayOfWeekPicker(
    modifier: Modifier = Modifier,
    selectedDays: List<WeekDays>,
    onDaysSelected: (List<WeekDays>) -> Unit
) {
    val daysOfWeek = WeekDays.values()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (day in daysOfWeek) {
            DayOfWeekItem(
                modifier = Modifier.weight(1f),
                day = day,
                isSelected = selectedDays.contains(day),
                onDaySelected = { selectedDay ->
                    val updatedSelectedDays = if (selectedDays.contains(selectedDay)) {
                        selectedDays - selectedDay
                    } else {
                        selectedDays + selectedDay
                    }
                    onDaysSelected(updatedSelectedDays)
                }
            )
        }
    }
}

@Composable
fun DayOfWeekItem(
    modifier: Modifier = Modifier,
    day: WeekDays,
    isSelected: Boolean,
    onDaySelected: (WeekDays) -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF1892FA) else Color(0xFFECEDEF)
    val textColor = if (isSelected) Color.White else GrayScale900

    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .background(color = backgroundColor, shape = shape)
            .clip(shape)
            .clickable { onDaySelected(day) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.name.take(1),
            color = textColor
        )
    }
}