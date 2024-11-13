package com.bloodpressure.app.screen.home.info

import androidx.compose.ui.graphics.Color
import com.bloodpressure.app.R

class InfoFactory {

    private var infos: List<InfoItemData> = buildList {
        add(
            InfoItemData(
                id = InfoItemId.AM_I_AT_NORMAL_BP,
                titleRes = R.string.bp_info_title_at_a_normal_bp_range,
                titleColor = Color(0xFF57A368),
                imageRes = R.drawable.ic_info_am_at_normal_bp,
                bgRes = R.drawable.img_info_am_at_normal_bp,
                contentRes = R.string.bp_info_content_at_a_normal_bp_range
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.WHAT_IS_BP,
                titleRes = R.string.bp_info_title_what_is_bp,
                titleColor = Color(0xFF7C1D2C),
                imageRes = R.drawable.ic_what_is_bp,
                bgRes = R.drawable.img_what_is_bp,
                contentRes = R.string.bp_info_content_what_is_bp
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.HOW_TO_MEASURE_BP,
                titleRes = R.string.bp_info_title_how_to_measure_bp,
                titleColor = Color(0xFF850099),
                imageRes = R.drawable.ic_how_to_measure_bp,
                bgRes = R.drawable.img_how_to_measure_bp,
                contentRes = R.string.bp_info_content_how_to_measure_bp
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.TYPE_OF_BP,
                titleRes = R.string.bp_info_title_type_of_bp,
                titleColor = Color(0xFFCC0400),
                imageRes = R.drawable.ic_type_of_bp,
                bgRes = R.drawable.img_type_of_bp,
                contentRes = R.string.bp_info_content_type_of_bp
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.LIFESTYLE,
                titleRes = R.string.bp_info_title_lifestyle_for_hypertension,
                titleColor = Color(0xFF45A3FE),
                imageRes = R.drawable.ic_lifestyle_for_hypertension,
                bgRes = R.drawable.img_lifestyle_for_hypertension,
                contentRes = R.string.bp_info_content_lifestyle_for_hypertension
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.HYPERTENSION,
                titleRes = R.string.bp_info_title_hypertension,
                titleColor = Color(0xFF57A368),
                imageRes = R.drawable.ic_info_hypertension,
                bgRes = R.drawable.img_info_hypertension,
                contentRes = R.string.bp_info_content_hypertension
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.WHY_IS_IT_A_RISK_FACTORY,
                titleRes = R.string.bp_info_title_why_is_it_a_risk_factory,
                titleColor = Color(0xFF45A3FE),
                imageRes = R.drawable.ic_info_why_is_it_a_risk_factory,
                bgRes = R.drawable.img_info_why_is_it_a_risk_factory,
                contentRes = R.string.bp_info_content_why_is_it_a_risk_factory
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.HOW_DOES_HYPER_AFFECT_BRAIN,
                titleRes = R.string.bp_info_title_how_does_hypertension_affect_brain,
                titleColor = Color(0xFF21C5A0),
                imageRes = R.drawable.ic_info_how_does_hypertension_affect_brain,
                bgRes = R.drawable.img_info_how_does_hypertension_affect_brain,
                contentRes = R.string.bp_info_content_how_does_hypertension_affect_brain
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.HOW_DOES_BP_AFFECT_THE_KIDNEYS,
                titleRes = R.string.bp_info_title_how_bp_affect_the_kidneys,
                titleColor = Color(0xFF7F621A),
                imageRes = R.drawable.ic_info_how_bp_affect_the_kidneys,
                bgRes = R.drawable.img_info_how_bp_affect_the_kidneys,
                contentRes = R.string.bp_info_content_how_bp_affect_the_kidneys
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.HOW_DOES_BP_AFFECT_OTHER_ORGANS,
                titleRes = R.string.bp_info_title_how_does_bp_affect_other_organs,
                titleColor = Color(0xFF997A00),
                imageRes = R.drawable.ic_info_how_does_bp_affect_other_organs,
                bgRes = R.drawable.img_info_how_does_bp_affect_other_organs,
                contentRes = R.string.bp_info_content_how_does_bp_affect_other_organs
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.DIAGNOSING_HYPERTENSION,
                titleRes = R.string.bp_info_title_dianosing_hypertension,
                titleColor = Color(0xFFCC0400),
                imageRes = R.drawable.ic_info_dianosing_hypertension,
                bgRes = R.drawable.img_info_dianosing_hypertension,
                contentRes = R.string.bp_info_content_dianosing_hypertension
            )
        )
    }
    private var heartRateInfos = listOf(
        InfoItemData(
            id = InfoItemId.HR_HEART_RATE,
            titleRes = R.string.hr_info_title_info_hr_heart_rate,
            titleColor = Color(0xFF57A368),
            imageRes = R.drawable.ic_info_hr_heart_rate,
            bgRes = R.drawable.img_info_hr_heart_rate,
            contentRes = R.string.info_hr_heart_rate
        ),
        InfoItemData(
            id = InfoItemId.HR_RESTING_HEART_RATE,
            titleRes = R.string.hr_info_title_info_hr_resting_heart_rate,
            titleColor = Color(0xFF7C1D2C),
            imageRes = R.drawable.ic_info_hr_resting,
            bgRes = R.drawable.img_info_hr_resting_heart_rate,
            contentRes = R.string.info_hr_resting_heart_rate
        ),
        InfoItemData(
            id = InfoItemId.HR_MEASURING_HEART_RATE,
            titleRes = R.string.hr_info_title_info_hr_measure_resting_heart_rate,
            titleColor = Color(0xFF370099),
            imageRes = R.drawable.ic_info_hr_measure,
            bgRes = R.drawable.img_info_hr_measuring_resting_heart_rate,
            contentRes = R.string.info_hr_measure_resting_heart_rate
        ),
        InfoItemData(
            id = InfoItemId.HR_WHEN_CHECK_RESTING_HEART_RATE,
            titleRes = R.string.hr_info_title_info_hr_when_to_check_resting_heart_rate,
            titleColor = Color(0xFF3C2210),
            imageRes = R.drawable.ic_info_hr_when_check_resting,
            bgRes = R.drawable.img_info_hr_when_check_resting_heart_rate,
            contentRes = R.string.info_hr_when_to_check_resting_heart_rate
        ),
        InfoItemData(
            id = InfoItemId.HR_TARGET_HEART_RATE,
            titleRes = R.string.hr_info_title_info_hr_target_heart_rate,
            titleColor = Color(0xFF176382),
            imageRes = R.drawable.ic_info_hr_target_heart_rate,
            bgRes = R.drawable.img_info_hr_target_heart_rate,
            contentRes = R.string.info_hr_target_heart_rate
        ),
        InfoItemData(
            id = InfoItemId.HR_TACHYCARDIA,
            titleRes = R.string.hr_info_title_info_hr_tachycardia,
            titleColor = Color(0xFF910811),
            imageRes = R.drawable.ic_info_hr_tachycardia,
            bgRes = R.drawable.img_info_hr_tachycardia,
            contentRes = R.string.info_hr_tachycardia
        ),
    )

    private var bloodSugarInfos = buildList {
        add(
            InfoItemData(
                id = InfoItemId.BS_NORMAL_BLOOD_SUGAR_LEVEL,
                titleRes = R.string.bs_info_title_normal_blood_sugar_level,
                titleColor = Color(0xFF57A368),
                imageRes = R.drawable.ic_info_bs_normal_blood_sugar_level,
                bgRes = R.drawable.img_info_bs_normal_blood_sugar_level,
                contentRes = R.string.info_bs_normal_blood_sugar_level
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.BS_DIABETES,
                titleRes = R.string.bs_info_title_diabetes,
                titleColor = Color(0xFF176382),
                imageRes = R.drawable.ic_info_bs_diabetes,
                bgRes = R.drawable.img_info_bs_diabetes,
                contentRes = R.string.info_bs_diabetes
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.BS_HOW_COMMON_IS_DIABETES,
                titleRes = R.string.bs_info_title_how_common_is_diabetes,
                titleColor = Color(0xFF370099),
                imageRes = R.drawable.ic_info_bs_how_common_is_diabetes,
                bgRes = R.drawable.img_info_bs_how_common_is_diabetes,
                contentRes = R.string.info_bs_how_common_is_diabetes
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.BS_DIABETES_SYMPTOMS,
                titleRes = R.string.bs_info_title_diabetes_symptoms,
                titleColor = Color(0xFF3C2210),
                imageRes = R.drawable.ic_info_bs_diabetes_symptoms,
                bgRes = R.drawable.img_info_bs_diabetes_symptoms,
                contentRes = R.string.info_bs_diabetes_symptoms
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.BS_DIABETES_TYPES,
                titleRes = R.string.bs_info_title_diabetes_types,
                titleColor = Color(0xFF910811),
                imageRes = R.drawable.ic_info_bs_diabetes_types,
                bgRes = R.drawable.img_info_bs_diabetes_types,
                contentRes = R.string.info_bs_diabetes_types
            )
        )
    }

    private var bmiInfos = buildList {
        add(
            InfoItemData(
                id = InfoItemId.BMI_HEALTHY_WEIGHT,
                titleRes = R.string.bmi_info_title_healthy_body_weight,
                titleColor = Color(0xFF176382),
                imageRes = R.drawable.ic_info_bmi_healthy_weight,
                bgRes = R.drawable.img_info_bmi_healthy_weight,
                contentRes = R.string.info_bmi_healthy_body_weight
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.BMI_OBESITY,
                titleRes = R.string.bmi_info_title_obesity,
                titleColor = Color(0xFF3C2210),
                imageRes = R.drawable.ic_info_bmi_obesity,
                bgRes = R.drawable.img_info_bmi_obesity,
                contentRes = R.string.info_bmi_obesity
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.BMI_OBESITY_CAUSES,
                titleRes = R.string.bmi_info_title_obesity_causes,
                titleColor = Color(0xFF57A368),
                imageRes = R.drawable.ic_info_bmi_obesity_causes,
                bgRes = R.drawable.img_info_bmi_obesity_causes,
                contentRes = R.string.info_bmi_obesity_causes
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.BMI_OBESITY_DIAGNOSIS,
                titleRes = R.string.bmi_info_title_obesity_diagnosis,
                titleColor = Color(0xFF370099),
                imageRes = R.drawable.ic_info_bmi_obesity_diagnosis,
                bgRes = R.drawable.img_info_bmi_obesity_diagnosis,
                contentRes = R.string.info_bmi_obesity_diagnosis
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.BMI_LIFESTYLE_FOR_OBESITY,
                titleRes = R.string.bmi_info_title_lifestyle_for_obesity,
                titleColor = Color(0xFF910811),
                imageRes = R.drawable.ic_info_bmi_lifestyle_for_obesity,
                bgRes = R.drawable.img_info_bmi_lifestyle_for_obesity,
                contentRes = R.string.info_bmi_lifestyle_for_obesity
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.BMI_WEIGHT_LOSS_TIP,
                titleRes = R.string.bmi_info_title_healthy_body_weight,
                titleColor = Color(0xFF913608),
                imageRes = R.drawable.ic_info_bmi_weight_loss_tip,
                bgRes = R.drawable.img_info_bmi_weight_loss_tip,
                contentRes = R.string.info_bmi_weight_loss_tips
            )
        )
    }

    private var waterReminderInfos = buildList {
        add(
            InfoItemData(
                id = InfoItemId.WATER_DRINKING_MISTAKE,
                titleRes = R.string.water_info_title_avoid_drink_mistake,
                titleColor = Color(0xFF3C2210),
                imageRes = R.drawable.ic_info_water_drink_mistakes,
                bgRes = R.drawable.ic_info_water_bg_drink_mistakes,
                contentRes = R.string.water_info_avoid_drink_mistake
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.WATER_BEST_TIME_TO_DRINK,
                titleRes = R.string.water_info_title_best_time_to_drink,
                titleColor = Color(0xFF512C6D),
                imageRes = R.drawable.ic_info_water_drink_time,
                bgRes = R.drawable.ic_info_water_bg_best_times,
                contentRes = R.string.water_info_best_time_to_drink
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.WATER_REPLACE_WITH_WATER,
                titleRes = R.string.water_info_title_replace_with_water,
                titleColor = Color(0xFF176382),
                imageRes = R.drawable.ic_info_water_replace_beverage,
                bgRes = R.drawable.ic_info_water_bg_replace_beverage,
                contentRes = R.string.water_info_replace_with_water
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.WATER_DRINK_ON_EMPTY_STOMACH,
                titleRes = R.string.water_info_title_drink_on_empty_stomach,
                titleColor = Color(0xFF3C5D52),
                imageRes = R.drawable.ic_info_water_empty_stomach,
                bgRes = R.drawable.ic_info_water_bg_empty_stomach,
                contentRes = R.string.water_info_drink_on_empty_stomatch
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.WATER_BENEFIT_LEMON_WATER,
                titleRes = R.string.water_info_title_benefit_lemon_water,
                titleColor = Color(0xFF2D6C3C),
                imageRes = R.drawable.ic_info_water_lemon_water,
                bgRes = R.drawable.ic_info_water_bg_lemon_water,
                contentRes = R.string.water_info_benefit_lemon_water
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.WATER_IS_OK_DRINK_DURING_MEAL,
                titleRes = R.string.water_info_title_is_ok_drink_during_meal,
                titleColor = Color(0xFF186A81),
                imageRes = R.drawable.ic_info_water_drink_during_meal,
                bgRes = R.drawable.ic_info_water_bg_during_meal,
                contentRes = R.string.water_info_is_ok_drink_during_meal
            )
        )

        add(
            InfoItemData(
                id = InfoItemId.WATER_BENEFIT_FOR_HEALTH,
                titleRes = R.string.water_info_title_benefit_for_health,
                titleColor = Color(0xFF7C731D),
                imageRes = R.drawable.ic_info_water_benefit_for_health,
                bgRes = R.drawable.ic_info_water_bg_benefit_health,
                contentRes = R.string.water_info_benefit_for_health
            )
        )
        add(
            InfoItemData(
                id = InfoItemId.WATER_DANGER_OF_OVER_HYDRATION,
                titleRes = R.string.water_info_title_danger_of_overhydration,
                titleColor = Color(0xFF232D30),
                imageRes = R.drawable.ic_info_water_overhydration,
                bgRes = R.drawable.ic_info_water_bg_overhydration,
                contentRes = R.string.water_info_danger_of_overhydration
            )
        )
        add(
            InfoItemData(
                id = InfoItemId.WATER_WHY_SHOULD_GIVE_BABY_WATER,
                titleRes = R.string.water_info_title_why_should_give_baby_water,
                titleColor = Color(0xFF8D800C),
                imageRes = R.drawable.ic_info_water_babies,
                bgRes = R.drawable.ic_info_water_bg_babies,
                contentRes = R.string.water_info_why_should_give_baby_water
            )
        )
    }

    fun createItem(id: String, type: InfoItemType): InfoItemData {
        return when (type) {
            InfoItemType.BLOOD_PRESSURE -> infos.first { it.id == id }
            InfoItemType.HEART_RATE -> heartRateInfos.first { it.id == id }
            InfoItemType.BLOOD_SUGAR -> bloodSugarInfos.first { it.id == id }
            InfoItemType.WEIGHT_BMI -> bmiInfos.first{it.id == id}
            InfoItemType.WATER_REMINDER -> waterReminderInfos.first{it.id == id}
            else -> throw IllegalArgumentException("Unsupported info item ${type}")
        }
    }

    fun getInfos(type: InfoItemType) = when (type) {
        InfoItemType.BLOOD_PRESSURE -> infos
        InfoItemType.HEART_RATE -> heartRateInfos
        InfoItemType.BLOOD_SUGAR -> bloodSugarInfos
        InfoItemType.WEIGHT_BMI -> bmiInfos
        InfoItemType.WATER_REMINDER -> waterReminderInfos
        else -> emptyList()
    }
}
