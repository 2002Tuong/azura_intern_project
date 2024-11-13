package com.bloodpressure.app.screen.home.info

import androidx.annotation.StringDef
import androidx.compose.ui.graphics.Color
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.AM_I_AT_NORMAL_BP
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.BMI_HEALTHY_WEIGHT
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.BMI_LIFESTYLE_FOR_OBESITY
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.BMI_OBESITY
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.BMI_OBESITY_CAUSES
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.BMI_OBESITY_DIAGNOSIS
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.BMI_WEIGHT_LOSS_TIP
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.BS_DIABETES
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.BS_DIABETES_SYMPTOMS
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.BS_DIABETES_TYPES
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.BS_HOW_COMMON_IS_DIABETES
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.BS_NORMAL_BLOOD_SUGAR_LEVEL
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.DIAGNOSING_HYPERTENSION
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.HOW_DOES_BP_AFFECT_OTHER_ORGANS
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.HOW_DOES_BP_AFFECT_THE_KIDNEYS
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.HOW_DOES_HYPER_AFFECT_BRAIN
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.HOW_TO_MEASURE_BP
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.HR_HEART_RATE
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.HR_MEASURING_HEART_RATE
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.HR_RESTING_HEART_RATE
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.HR_TACHYCARDIA
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.HR_TARGET_HEART_RATE
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.HR_WHEN_CHECK_RESTING_HEART_RATE
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.HYPERTENSION
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.LIFESTYLE
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.TYPE_OF_BP
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.WATER_BENEFIT_FOR_HEALTH
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.WATER_BENEFIT_LEMON_WATER
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.WATER_BEST_TIME_TO_DRINK
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.WATER_DANGER_OF_OVER_HYDRATION
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.WATER_DRINKING_MISTAKE
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.WATER_DRINK_ON_EMPTY_STOMACH
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.WATER_IS_OK_DRINK_DURING_MEAL
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.WATER_REPLACE_WITH_WATER
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.WATER_WHY_SHOULD_GIVE_BABY_WATER
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.WHAT_IS_BP
import com.bloodpressure.app.screen.home.info.InfoItemId.Companion.WHY_IS_IT_A_RISK_FACTORY

data class InfoItemData(
    val id: String,
    val titleRes: Int,
    val titleColor: Color,
    val imageRes: Int,
    val bgRes: Int,
    val contentRes: Int
)

@StringDef(
    AM_I_AT_NORMAL_BP,
    WHAT_IS_BP,
    HOW_TO_MEASURE_BP,
    TYPE_OF_BP,
    LIFESTYLE,
    HYPERTENSION,
    WHY_IS_IT_A_RISK_FACTORY,
    HOW_DOES_HYPER_AFFECT_BRAIN,
    HOW_DOES_BP_AFFECT_THE_KIDNEYS,
    HOW_DOES_BP_AFFECT_OTHER_ORGANS,
    DIAGNOSING_HYPERTENSION,
    HR_HEART_RATE,
    HR_RESTING_HEART_RATE,
    HR_MEASURING_HEART_RATE,
    HR_WHEN_CHECK_RESTING_HEART_RATE,
    HR_TARGET_HEART_RATE,
    HR_TACHYCARDIA,

    BS_NORMAL_BLOOD_SUGAR_LEVEL,
    BS_DIABETES,
    BS_HOW_COMMON_IS_DIABETES,
    BS_DIABETES_SYMPTOMS,
    BS_DIABETES_TYPES,

    BMI_HEALTHY_WEIGHT,
    BMI_OBESITY,
    BMI_OBESITY_CAUSES,
    BMI_OBESITY_DIAGNOSIS,
    BMI_LIFESTYLE_FOR_OBESITY,
    BMI_WEIGHT_LOSS_TIP,
    WATER_DRINKING_MISTAKE,
    WATER_BEST_TIME_TO_DRINK,
    WATER_REPLACE_WITH_WATER,
    WATER_DRINK_ON_EMPTY_STOMACH,
    WATER_BENEFIT_LEMON_WATER,
    WATER_IS_OK_DRINK_DURING_MEAL,
    WATER_BENEFIT_FOR_HEALTH,
    WATER_DANGER_OF_OVER_HYDRATION,
    WATER_WHY_SHOULD_GIVE_BABY_WATER
)
@Retention(value = AnnotationRetention.SOURCE)
annotation class InfoItemId {
    companion object {
        const val AM_I_AT_NORMAL_BP = "AM_I_AT_NORMAL_BP"
        const val WHAT_IS_BP = "WHAT_IS_BP"
        const val HOW_TO_MEASURE_BP = "HOW_TO_MEASURE_BP"
        const val TYPE_OF_BP = "TYPE_OF_BP"
        const val LIFESTYLE = "LIFESTYLE"
        const val HYPERTENSION = "HYPERTENSION"
        const val WHY_IS_IT_A_RISK_FACTORY = "WHY_IS_IT_A_RISK_FACTORY"
        const val HOW_DOES_HYPER_AFFECT_BRAIN = "HOW_DOES_HYPER_AFFECT_BRAIN"
        const val HOW_DOES_BP_AFFECT_THE_KIDNEYS = "HOW_DOES_BP_AFFECT_THE_KIDNEYS"
        const val HOW_DOES_BP_AFFECT_OTHER_ORGANS = "HOW_DOES_BP_AFFECT_OTHER_ORGANS"
        const val DIAGNOSING_HYPERTENSION = "DIAGNOSING_HYPERTENSION"


        const val HR_HEART_RATE = "HR_HEART_RATE"
        const val HR_RESTING_HEART_RATE = "HR_RESTING_HEART_RATE"
        const val HR_MEASURING_HEART_RATE = "HR_MEASURING_HEART_RATE"
        const val HR_WHEN_CHECK_RESTING_HEART_RATE = "HR_WHEN_CHECK_RESTING_HEART_RATE"
        const val HR_TARGET_HEART_RATE = "HR_TARGET_HEART_RATE"
        const val HR_TACHYCARDIA = "HR_TACHYCARDIA"

        const val BS_NORMAL_BLOOD_SUGAR_LEVEL = "BS_NORMAL_BLOOD_SUGAR_LEVEL"
        const val BS_DIABETES = "BS_DIABETES"
        const val BS_HOW_COMMON_IS_DIABETES= "BS_HOW_COMMON_IS_DIABETES"
        const val BS_DIABETES_SYMPTOMS = "BS_DIABETES_SYMPTOMS"
        const val BS_DIABETES_TYPES = "BS_DIABETES_TYPES"

        const val BMI_HEALTHY_WEIGHT = "BMI_HEALTHY_WEIGHT"
        const val BMI_OBESITY = "BMI_OBESITY"
        const val BMI_OBESITY_CAUSES = "BMI_OBESITY_CAUSES"
        const val BMI_OBESITY_DIAGNOSIS = "BMI_OBESITY_DIAGNOSIS"
        const val BMI_LIFESTYLE_FOR_OBESITY = "BMI_LIFESTYLE_FOR_OBESITY"
        const val BMI_WEIGHT_LOSS_TIP = "BMI_WEIGHT_LOSS_TIP"

        const val WATER_DRINKING_MISTAKE = "WATER_DRINKING_MISTAKE"
        const val WATER_BEST_TIME_TO_DRINK = "WATER_BEST_TIME_TO_DRINK"
        const val WATER_REPLACE_WITH_WATER = "WATER_REPLACE_WITH_WATER"
        const val WATER_DRINK_ON_EMPTY_STOMACH = "WATER_DRINK_ON_EMPTY_STOMACH"
        const val WATER_BENEFIT_LEMON_WATER = "WATER_BENEFIT_LEMON_WATER"
        const val WATER_IS_OK_DRINK_DURING_MEAL = "WATER_IS_OK_DRINK_DURING_MEAL"
        const val WATER_BENEFIT_FOR_HEALTH = "WATER_BENEFIT_FOR_HEALTH"
        const val WATER_DANGER_OF_OVER_HYDRATION = "WATER_DANGER_OF_OVER_HYDRATION"
        const val WATER_WHY_SHOULD_GIVE_BABY_WATER = "WATER_WHY_SHOULD_GIVE_BABY_WATER"
    }
}

enum class InfoItemType {
    BLOOD_PRESSURE, BLOOD_SUGAR, HEART_RATE, WEIGHT_BMI, STRESS, CHROLESTERON, CALORINE, HOT_NEWS, WATER_REMINDER
}