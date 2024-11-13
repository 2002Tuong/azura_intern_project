package com.bloodpressure.app.screen.alarm

import com.bloodpressure.app.R

enum class AlarmType(val titleRes: Int, val iconRes: Int) {
    BLOOD_PRESSURE(
        titleRes = R.string.blood_pressure,
        iconRes = R.drawable.ic_blood_pressure,
    ),
    HEART_RATE(
        titleRes = R.string.heart_rate,
        iconRes = R.drawable.ic_heart_rate,
    ),
    BLOOD_SUGAR(
        titleRes = R.string.blood_sugar,
        iconRes = R.drawable.ic_blood_sugar,
    ),
    WEIGHT_BMI(
        titleRes = R.string.weight_bmi,
        iconRes = R.drawable.ic_bmi,
    ),
//    CHOLESTEROL(
//        titleRes = R.string.cholesterol,
//        iconRes = R.drawable.ic_cholesterol,
//    ),
    WATER_REMINDER(
        titleRes = R.string.water_reminder,
        iconRes = R.drawable.ic_water_reminder,
    ),
}
