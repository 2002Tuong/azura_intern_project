package com.bloodpressure.app.utils

import kotlin.math.roundToInt

object ConvertUnit {
    fun convertKgToLbs(kg: Float) = kg * COEFFICIENT_WEIGHT

    fun convertLbsToKg(lbs: Float) = lbs / COEFFICIENT_WEIGHT

    fun convertMeterToCm(meter: Float) = meter * COEFFICIENT_M_CM

    fun convertCmToMeter(cm: Float) = cm / COEFFICIENT_M_CM

    fun convertCmToFtIn(cm: Float): FeetInches {
        val ft = (cm * COEFFICIENT_CM_FT).toInt()
        val inches = (cm - ft / COEFFICIENT_CM_FT) * COEFFICIENT_CM_INCHES
        return FeetInches(
            ft = ft.toFloat(),
            inches = inches
        )
    }

    fun convertFtInToCm(ft: Float, inches: Float): Float
        = ft / COEFFICIENT_CM_FT + inches / COEFFICIENT_CM_INCHES

    fun convertFtInToMeter(ft: Float, inches: Float): Float
        = convertCmToMeter(convertFtInToCm(ft, inches))


    data class FeetInches(val ft: Float, val inches: Float)
    private const val COEFFICIENT_WEIGHT = 2.2046f
    private const val COEFFICIENT_CM_FT = 0.0328F
    private const val COEFFICIENT_M_CM = 100f
    private const val COEFFICIENT_CM_INCHES = 0.3937f
    const val COEFFICIENT_DL_L = 0.0555f

}

enum class HeightUnit(val value: String) {
    CM("cm"),
    FT_IN("ft-in")
}

enum class WeightUnit(val value: String) {
    KG("kg"),
    LBS("lbs")
}

enum class BloodSugarUnit(val value: String) {
    MILLIMOLES_PER_LITRE("mmol/l"),
    MILLIGRAMS_PER_DECILITER("mg/dL");

    fun getValueByMole(value: Float): Float {
        return if (this == MILLIMOLES_PER_LITRE) value else (value * ConvertUnit.COEFFICIENT_DL_L * 10).roundToInt() / 10f
    }
}
