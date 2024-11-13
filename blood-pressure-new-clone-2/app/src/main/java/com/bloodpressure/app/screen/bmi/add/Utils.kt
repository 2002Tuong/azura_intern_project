package com.bloodpressure.app.screen.bmi.add

import java.text.NumberFormat
import java.util.Locale

internal object MaxLengthInput {
    const val MAX_LENGTH_WEIGHT_IN_KG_INPUT = 6
    const val MAX_LENGTH_WEIGHT_IN_LB_INPUT = 6
    const val MAX_LENGTH_HEIGHT_IN_CM_INPUT = 6
    const val MAX_LENGTH_HEIGHT_IN_FT_INPUT = 1
    const val MAX_LENGTH_HEIGHT_IN_INCH_INPUT = 3
}

internal object DefaultMaxValue {
    const val DEFAULT_MAX_VALUE_WEIGHT_IN_KG = 300.0f
    const val DEFAULT_MAX_VALUE_WEIGHT_IN_LBS = 660.0f
    const val DEFAULT_MAX_VALUE_HEIGHT_IN_CM = 300.0f
    const val DEFAULT_MAX_VALUE_HEIGHT_IN_FT = 8f
    const val DEFAULT_MAX_VALUE_HEIGHT_IN_INCH = 20f
}

internal fun String.toFloatCustom(): Float {
    return if (this.isEmpty()) return 1f else this.toFloatOrNull() ?: 1f
}