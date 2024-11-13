package com.bloodpressure.app.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.bloodpressure.app.R

enum class BMIOptionType(@StringRes val titleRes: Int, @StringRes val contentRes: Int, @DrawableRes val iconRes: Int, val isShow: Boolean = true) {
    ANALYZE(R.string.analyze, R.string.analyze_your_weight_bmi, R.drawable.ic_bmi),
    TRENDS(R.string.trends, R.string.get_detailed_analysis, R.drawable.ic_trend),
    HISTORY(R.string.history, R.string.check_the_record_of_your_measurements, R.drawable.ic_feature_history),
}