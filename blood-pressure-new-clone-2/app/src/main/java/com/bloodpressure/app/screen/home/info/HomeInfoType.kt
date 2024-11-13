package com.bloodpressure.app.screen.home.info

import androidx.compose.ui.graphics.Color
import com.bloodpressure.app.R

enum class HomeInfoType(
    val titleRes: Int,
    val iconRes: Int,
    val colors: List<Color> = listOf(Color.White, Color.White)
) {
    BLOOD_PRESSURE(
        titleRes = R.string.blood_pressure, iconRes = R.drawable.ic_blood_pressure,
        colors = listOf(
            Color(0xFFBA94FF),
            Color(0xFF7630F8),
            Color(0xFF4A0EBA)
        )
    ),
    HEART_RATE(
        titleRes = R.string.heart_rate,
        iconRes = R.drawable.ic_heart_rate,
        colors = listOf(
            Color(0xFF38ACFA),
            Color(0xFF2935DD)
        )
    ),
    BLOOD_SUGAR(
        titleRes = R.string.blood_sugar,
        iconRes = R.drawable.ic_blood_sugar,
        colors = listOf(
            Color(0xFFE8DBFF),
            Color(0xFFB185FD)
        )
    ),
    WEIGHT_BMI(
        titleRes = R.string.weight_bmi,
        iconRes = R.drawable.ic_bmi,
        listOf(
            Color(0xFFF7970F),
            Color(0xFFFF4F00)
        )
    ),
    CHOLESTEROL(
        titleRes = R.string.cholesterol,
        iconRes = R.drawable.ic_cholesterol,
        colors = listOf(
            Color(0xFF38ACFA),
            Color(0xFF2935DD)
        )
    ),
    WATER_REMINDER(
        titleRes = R.string.water_reminder,
        iconRes = R.drawable.ic_water_reminder,
        colors = listOf(Color(0xFF53B69F), Color(0xFF53B69F))
    ),
}