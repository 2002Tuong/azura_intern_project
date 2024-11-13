package com.bloodpressure.app.data.model

import com.bloodpressure.app.R

enum class HeartRateOptionType(val nameRes: Int, val titleRes: Int, val iconRes: Int) {
    MEASURE_NOW(
        R.string.measure_now,
        R.string.measure_your_heart_rate_simply_by_using_your_finger,
        R.drawable.ic_heart_rate
    ),
    ADD_MANUALLY(
        R.string.add_manually,
        R.string.manually_add_a_heart_rate,
        R.drawable.ic_heart_rate_add_manual
    ),
    TRENDS(R.string.trends, R.string.get_detailed_analysis, R.drawable.ic_trend),

    HISTORY(
        R.string.history,
        R.string.check_the_record_of_your_measurements,
        R.drawable.ic_feature_history
    )
}
