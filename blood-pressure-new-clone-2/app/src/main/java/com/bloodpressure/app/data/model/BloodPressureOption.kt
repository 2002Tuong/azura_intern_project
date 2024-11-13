package com.bloodpressure.app.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.bloodpressure.app.R

enum class BloodPressureOption(
    @StringRes val titleRes: Int,
    @StringRes val contentRes: Int,
    @DrawableRes val iconRes: Int,
    val isShow: Boolean = true
) {
//    DEFAULT(R.string.blood_pressure, R.string.manually_add_a_bmi_weight, R.drawable.ic_blood_pressure),

    ADD(R.string.add_manually, R.string.manually_add_a_bmi_weight, R.drawable.ic_blood_pressure),

    TRENDS(R.string.trends, R.string.get_detailed_analysis, R.drawable.ic_trend),

    HISTORY(
        R.string.history,
        R.string.check_the_record_of_your_measurements,
        R.drawable.ic_feature_history
    ),
//    SET_ALARM(
//        R.string.set_alarms,
//        R.string.schedule_smart_alarms_for_health,
//        R.drawable.ic_alarm_small
//    )
//    STATISTICS(R.string.statistics, R.string.view_the_detailed, R.drawable.ic_statistics),
}