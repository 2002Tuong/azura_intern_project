package com.bloodpressure.app.screen.bloodsugar.type

import androidx.compose.ui.graphics.Color
import com.bloodpressure.app.R

enum class BloodSugarRateType(
    val nameRes: Int,
    val color: Color,
) {

    LOW(
        nameRes = R.string.low,
        color = Color(0xFF1892FA),
    ),
    NORMAL(
        nameRes = R.string.normal,
        color = Color(0xFF62A970),
    ),
    PRE_DIABETES(
        nameRes = R.string.pre_diabetes,
        color = Color(0xFFF4763C),
    ),
    DIABETES(
        nameRes = R.string.diabetes,
        color = Color(0xFFAE2F05),
    );

}