package com.bloodpressure.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bloodpressure.app.screen.heartrate.add.GenderType
import com.bloodpressure.app.utils.BloodSugarUnit
import com.bloodpressure.app.utils.HeightUnit
import com.bloodpressure.app.utils.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class AppDataStore(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "blood_preference")

    val isPurchased: Boolean
        get() = runBlocking { context.dataStore.data.first()[IS_PURCHASED] == true }

    val isPurchasedFlow: Flow<Boolean>
        get() = context.dataStore.data.map { preferences -> preferences[IS_PURCHASED] == true }

    val isLanguageSelected: Boolean
        get() = runBlocking { context.dataStore.data.first()[IS_LANGUAGE_SELECTED] == true }

    val isSuggestionPopulated: Boolean
        get() = runBlocking { context.dataStore.data.first()[IS_SUGGESTION_NOTE_POPULATED] == true }

    val isOnboardingShownFlow: Flow<Boolean>
        get() = context.dataStore.data.map { preferences ->
            preferences[IS_ONBOARDING_SHOWN] == true
        }

    val isOnboardingShown: Boolean
        get() = runBlocking { context.dataStore.data.first()[IS_ONBOARDING_SHOWN] == true }

    val selectedLanguage: String
        get() = runBlocking { context.dataStore.data.first()[SELECTED_LANGUAGE].orEmpty() }

    val addRecordButtonClickCounter: Long
        get() = runBlocking { context.dataStore.data.first()[ADD_RECORD_CLICK_COUNTER] ?: 0 }

    val infoItemClickCounter: Long
        get() = runBlocking { context.dataStore.data.first()[INFO_ITEM_CLICK_COUNTER] ?: 0 }

    val isOnboardingPremiumShownFlow: Flow<Boolean>
        get() = context.dataStore.data.map { preferences -> preferences[IS_ONBOARDING_PREMIUM_SHOWN] == true }

    val isSampleDataCreated: Boolean
        get() = runBlocking { context.dataStore.data.first()[IS_SAMPLE_DATA_CREATED] == true }

    val sampleRecordId: Long
        get() = runBlocking { context.dataStore.data.first()[SAMPLE_RECORD_ID] ?: 0L }

    val sampleRecordIdFlow: Flow<Long>
        get() = context.dataStore.data.map { preferences -> preferences[SAMPLE_RECORD_ID] ?: 0L }

    val isMeasureFlashEnabledFlow: Flow<Boolean>
        get() = context.dataStore.data.map { preferences ->
            preferences[IS_MEASURE_FLASH_ENABLED] ?: true
        }

    val isSoundOn: Flow<Boolean>
        get() = context.dataStore.data.map { preferences ->
            preferences[IS_MEASURE_SOUND_ON] ?: true
        }

    val age: Flow<Int>
        get() = context.dataStore.data.map { preferences -> preferences[AGE] ?: 0 }

    val gender: Flow<GenderType>
        get() = context.dataStore.data.map { preferences ->
            val ordinal = preferences[GENDER] ?: GenderType.OTHERS.ordinal
            GenderType.values().getOrElse(ordinal) { GenderType.OTHERS }
        }

    val bloodSugarUnit: BloodSugarUnit
        get() = runBlocking {
            val ordinal = context.dataStore.data.first()[BLOOD_SUGAR_UNIT] ?: BloodSugarUnit.MILLIMOLES_PER_LITRE.ordinal
            BloodSugarUnit.values().getOrElse(ordinal) { BloodSugarUnit.MILLIMOLES_PER_LITRE }
        }

    val currentBottleSize: Int
        get() = runBlocking { context.dataStore.data.first()[CURRENT_BOTTLE_SIZE] ?: 200 }

    val waterDailyGoal: Int
        get() = runBlocking { context.dataStore.data.first()[WATER_DAILY_GOAL] ?: 2000 }

    val waterVolumeType: Boolean
        get() = runBlocking { context.dataStore.data.first()[WATER_VOLUME_TYPE]?: true }

    val weightUnit: Flow<WeightUnit>
        get() = context.dataStore.data.map { preferences ->
            val ordinal = preferences[WEIGHT_UNIT] ?: WeightUnit.KG.ordinal
            WeightUnit.values().getOrElse(ordinal) {WeightUnit.KG}
        }

    val heightUnit: Flow<HeightUnit>
        get() = context.dataStore.data.map { preferences ->
            val ordinal = preferences[HEIGHT_UNIT] ?: HeightUnit.CM.ordinal
            HeightUnit.values().getOrElse(ordinal) {HeightUnit.CM}
        }

    suspend fun setSelectedLanguage(language: String) {
        context.dataStore.edit { settings ->
            settings[SELECTED_LANGUAGE] = language
        }
    }

    suspend fun setLanguageSelected(isSelected: Boolean) {
        context.dataStore.edit { settings ->
            settings[IS_LANGUAGE_SELECTED] = isSelected
        }
    }

    suspend fun setOnboardingShown(isOnboardingShown: Boolean) {
        context.dataStore.edit { settings ->
            settings[IS_ONBOARDING_SHOWN] = isOnboardingShown
        }
    }

    suspend fun setOnboardingPremiumShown() {
        context.dataStore.edit { settings ->
            settings[IS_ONBOARDING_PREMIUM_SHOWN] = true
        }
    }

    suspend fun setSuggestionNotePopulated(isPopulated: Boolean) {
        context.dataStore.edit { settings ->
            settings[IS_SUGGESTION_NOTE_POPULATED] = isPopulated
        }
    }

    suspend fun setPurchase(isPurchased: Boolean) {
        context.dataStore.edit { settings ->
            settings[IS_PURCHASED] = isPurchased
        }
    }

    suspend fun increaseAddRecordClickCounter() {
        context.dataStore.edit { settings ->
            val currentValue = settings[ADD_RECORD_CLICK_COUNTER] ?: 0
            settings[ADD_RECORD_CLICK_COUNTER] = currentValue + 1
        }
    }

    suspend fun increaseInfoItemClickCounter() {
        context.dataStore.edit { settings ->
            val currentValue = settings[INFO_ITEM_CLICK_COUNTER] ?: 0
            settings[INFO_ITEM_CLICK_COUNTER] = currentValue + 1
        }
    }

    suspend fun increaseAddHeartRateRecordClickCounter() {
        context.dataStore.edit { settings ->
            val currentValue = settings[ADD_HEART_RATE_RECORD_CLICK_COUNTER] ?: 0
            settings[ADD_HEART_RATE_RECORD_CLICK_COUNTER] = currentValue + 1
        }
    }

    suspend fun increaseAddBmiRecordClickCounter() {
        context.dataStore.edit { settings ->
            val currentValue = settings[ADD_BMI_RECORD_CLICK_COUNTER] ?: 0
            settings[ADD_BMI_RECORD_CLICK_COUNTER] = currentValue + 1
        }
    }

    suspend fun setSampleDataCreated() {
        context.dataStore.edit { settings ->
            settings[IS_SAMPLE_DATA_CREATED] = true
        }
    }

    suspend fun setSampleRecordId(id: Long) {
        context.dataStore.edit { settings ->
            settings[SAMPLE_RECORD_ID] = id
        }
    }

    suspend fun setMeasureFlashEnabled(isEnabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[IS_MEASURE_FLASH_ENABLED] = isEnabled
        }
    }

    suspend fun setMeasureSoundOn(isEnabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[IS_MEASURE_SOUND_ON] = isEnabled
        }
    }

    suspend fun setAge(age: Int) {
        context.dataStore.edit { settings ->
            settings[AGE] = age
        }
    }

    suspend fun setGender(genderType: GenderType) {
        context.dataStore.edit { settings ->
            settings[GENDER] = genderType.ordinal
        }
    }

    suspend fun setCurrentBottleSize(value: Int) {
        context.dataStore.edit {settings ->
            settings[CURRENT_BOTTLE_SIZE] = value
        }
    }

    suspend fun setWaterDailyGoal(value: Int) {
        context.dataStore.edit { settings ->
            settings[WATER_DAILY_GOAL] = value
        }
    }

    suspend fun setBloodSugarUnit(bloodSugarUnit: BloodSugarUnit) {
        context.dataStore.edit { settings ->
            settings[BLOOD_SUGAR_UNIT] = bloodSugarUnit.ordinal
        }
    }

    suspend fun setVolumeType(value: Boolean) {
        context.dataStore.edit { settings ->
            settings[WATER_VOLUME_TYPE] = value
        }
    }

    suspend fun setWeightUnit(weightUnit: WeightUnit) {
        context.dataStore.edit { settings ->
            settings[WEIGHT_UNIT] = weightUnit.ordinal
        }
    }

    suspend fun setHeightUnit(heightUnit: HeightUnit) {
        context.dataStore.edit { settings ->
            settings[HEIGHT_UNIT] = heightUnit.ordinal
        }
    }

    companion object {
        private val IS_LANGUAGE_SELECTED = booleanPreferencesKey("is_language_selected")
        private val IS_SUGGESTION_NOTE_POPULATED =
            booleanPreferencesKey("is_suggestion_note_populated")
        private val IS_PURCHASED = booleanPreferencesKey("is_purchased")
        private val IS_ONBOARDING_SHOWN = booleanPreferencesKey("is_onboarding_shown")
        private val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        private val ADD_RECORD_CLICK_COUNTER = longPreferencesKey("add_record_click_counter")
        private val INFO_ITEM_CLICK_COUNTER = longPreferencesKey("info_item_click_counter")
        private val IS_ONBOARDING_PREMIUM_SHOWN =
            booleanPreferencesKey("is_onboarding_premium_shown")
        private val IS_SAMPLE_DATA_CREATED = booleanPreferencesKey("is_sample_data_created")
        private val SAMPLE_RECORD_ID = longPreferencesKey("sample_record_id")
        private val ADD_HEART_RATE_RECORD_CLICK_COUNTER = longPreferencesKey("add_heart_rate_record_click_counter")
        private val IS_MEASURE_FLASH_ENABLED = booleanPreferencesKey("is_measure_flash_enabled")
        private val IS_MEASURE_SOUND_ON = booleanPreferencesKey("is_measure_sound_on")
        private val AGE = intPreferencesKey("age")
        private val GENDER = intPreferencesKey("gender")
        private val CURRENT_BOTTLE_SIZE = intPreferencesKey("current_bottle_size")
        private val WATER_DAILY_GOAL = intPreferencesKey("water_daily_goal")
        private val WATER_VOLUME_TYPE = booleanPreferencesKey("water_volume_type")
        private val BLOOD_SUGAR_UNIT = intPreferencesKey("blood_sugar_unit")
        private val ADD_BMI_RECORD_CLICK_COUNTER = longPreferencesKey("add_bmi_record_click_counter")
        private val WEIGHT_UNIT = intPreferencesKey("weight_unit")
        private val HEIGHT_UNIT = intPreferencesKey("height_unit")
    }
}
