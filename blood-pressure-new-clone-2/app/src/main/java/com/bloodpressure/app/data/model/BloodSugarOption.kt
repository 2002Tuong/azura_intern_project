package com.bloodpressure.app.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.bloodpressure.app.R

enum class BloodSugarOption(
    @StringRes val titleRes: Int,
    @StringRes val contentRes: Int,
    @DrawableRes val iconRes: Int,
    val isShow: Boolean = true
) {
    ADD(R.string.add_manually, R.string.manually_add_a_bmi_weight, R.drawable.ic_blood_sugar),
//    ANALYZE(R.string.analyze, R.string.analyze_your_weight_bmi, R.drawable.ic_bmi),
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