package com.bloodpressure.app.screen.home.tracker

import com.bloodpressure.app.R

enum class TrackerType(val titleRes: Int, val iconRes: Int) {
    BLOOD_PRESSURE(titleRes = R.string.blood_pressure, iconRes = R.drawable.ic_blood_pressure),
    HEART_RATE(titleRes = R.string.heart_rate, iconRes = R.drawable.ic_heart_rate),
    BLOOD_SUGAR(titleRes = R.string.blood_sugar, iconRes = R.drawable.ic_blood_sugar),
    WEIGHT_BMI(titleRes = R.string.weight_bmi, iconRes = R.drawable.ic_bmi),
    CHOLESTEROL(titleRes = R.string.cholesterol, iconRes = R.drawable.ic_cholesterol),
    STEP_COUNTER(titleRes = R.string.step_counter, iconRes = R.drawable.ic_step),
    WATER_REMINDER(titleRes = R.string.water_reminder, iconRes = R.drawable.ic_water_reminder),
    STRESS_LEVEL(titleRes = R.string.stress_level, iconRes = R.drawable.ic_stress_level),
    AI_CHATGPT(titleRes = R.string.ai_chatgpt, iconRes = R.drawable.ic_chatgpt)
}