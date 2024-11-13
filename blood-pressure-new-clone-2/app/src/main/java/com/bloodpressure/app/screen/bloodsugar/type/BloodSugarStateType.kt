package com.bloodpressure.app.screen.bloodsugar.type

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bloodpressure.app.R
import com.bloodpressure.app.utils.BloodSugarUnit
import kotlinx.serialization.Serializable


interface StateType {
    val titleRes: Int
}

enum class BloodSugarStateType(
    @StringRes override val titleRes: Int
): StateType {
    DEFAULT(R.string.default_range),
    DURING_FASTING(R.string.during_fasting),
    BEFORE_EATING(R.string.before_eating),
    AFTER_EATING_1H(R.string.after_eating_1h),
    AFTER_EATING_2H(R.string.after_eating_2h),
    BEFORE_BEDTIME(R.string.before_bedtime),
    BEFORE_WORKOUT(R.string.before_workout),
    AFTER_WORKOUT(R.string.after_workout);
}

val ALL_TYPE = object : StateType {
    override val titleRes: Int
        get() = R.string.all
}

@Serializable
data class TargetRange(
    val bloodSugarStateType: BloodSugarStateType,
    val isChecked: Boolean = false,
    val normalRangeMin: Float = 4.0f,
    val normalRangeMax: Float = 5.5f,
    val diabetesValue: Float = 7f,
    val bloodSugarUnit: BloodSugarUnit = BloodSugarUnit.MILLIMOLES_PER_LITRE
) {

    fun isNormalMinInputValid(value: Float): Boolean {
        val minBound = if (bloodSugarUnit == BloodSugarUnit.MILLIMOLES_PER_LITRE) 1f else 18f
        return value in minBound..normalRangeMax
    }

    fun isNormalMaxInputValid(value: Float): Boolean = value in normalRangeMin..diabetesValue

    fun isDiabetesValueInputValid(value: Float): Boolean {
        val maxBound = if (bloodSugarUnit == BloodSugarUnit.MILLIMOLES_PER_LITRE) 35f else 630f
        return value in normalRangeMax..maxBound
    }

    fun getBloodSugarRateType(
        bloodSugarRate: Float
    ): BloodSugarRateType {
        return when {
            bloodSugarRate < normalRangeMin -> BloodSugarRateType.LOW
            bloodSugarRate in normalRangeMin..normalRangeMax -> BloodSugarRateType.NORMAL
            bloodSugarRate in normalRangeMax..diabetesValue -> BloodSugarRateType.PRE_DIABETES
            else -> BloodSugarRateType.DIABETES
        }
    }
}

@Composable
fun StateType.getStringAnnotation(): String {
    return stringResource(id = this.titleRes)
}

val DEFAULT_AFTER_EATING_1H = TargetRange(
    bloodSugarStateType = BloodSugarStateType.AFTER_EATING_1H,
    normalRangeMin = 4.0f,
    normalRangeMax = 7.8f,
    diabetesValue = 8.5f,
)

val DEFAULT_AFTER_EATING_2H = TargetRange(
    bloodSugarStateType = BloodSugarStateType.AFTER_EATING_2H,
    normalRangeMin = 4.0f,
    normalRangeMax = 4.7f,
    diabetesValue = 7.0f,
)