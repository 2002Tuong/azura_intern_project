package com.bloodpressure.app.screen.bmi.add

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.BMIRecord
import com.bloodpressure.app.data.remote.RemoteConfig
import com.bloodpressure.app.data.repository.BMIRepository
import com.bloodpressure.app.screen.heartrate.add.GenderType
import com.bloodpressure.app.utils.ConvertUnit
import com.bloodpressure.app.utils.HeightUnit
import com.bloodpressure.app.utils.TextFormatter
import com.bloodpressure.app.utils.WeightUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.NumberFormatException
import java.util.Calendar
import kotlin.math.roundToInt

class AddBmiScreenViewModel(
    private val handle: SavedStateHandle,
    private val textFormatter: TextFormatter,
    private val repository: BMIRepository,
    private val dataStore: AppDataStore,
    private val remoteConfig: RemoteConfig,
) : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val recordId: Long?
        get() = handle.get<Long>("id")

    init {
        observePurchases()

        if (recordId != null) {
            loadCurrentRecord(recordId!!)
        } else {
            getCurrentDateTime()
        }

        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update { it.copy(isPurchased = isPurchased) }
                }
        }

        viewModelScope.launch {
            dataStore.increaseAddBmiRecordClickCounter()
        }

        viewModelScope.launch {
            dataStore.age
                .distinctUntilChanged()
                .collectLatest { age ->

                    if (age <= 0) {
                        _uiState.update { it.copy(forceShowAgeDialog = true, age = 22, isAgeInputted = false) }
                    } else {
                        _uiState.update { it.copy(age = age, isAgeInputted = true) }
                    }

                }

        }

        viewModelScope.launch {
            dataStore.gender
                .distinctUntilChanged()
                .collectLatest { gender ->
                    _uiState.update { it.copy(genderType = gender) }
                }
        }
    }

    private fun loadCurrentRecord(recordId: Long) {
        viewModelScope.launch {
            repository.getRecordById(recordId).collectLatest { record ->
                if(record != null) {
                    val weight = String.format("%.2f", record.weight).replace(",", ".")
                    val height = String.format("%.2f", record.height).replace(",", ".")
                    _uiState.update {
                        it.copy(
                            recordId = record.createdAt,
                            bmi = record.bmi,
                            weight = weight,
                            weightTextField = it.weightTextField.copy( text = weight, selection = TextRange(weight.length)),
                            height = height,
                            heightTextField = it.heightTextField.copy( text = height, selection = TextRange(height.length)),
                            selectBmiType = record.type,
                            date = record.date,
                            time = record.time,
                            notes = record.notes,
                            age = record.age,
                            genderType = record.genderType
                        )
                    }
                }
            }
        }
    }
     fun separateHeightInFtUnit(height: String): Pair<String, String> {
         return try {
             val part1 = height.substring(0, height.indexOf("'"))
             val part2 = height.substring(height.indexOf("'") + 1, height.length)
             Pair(part1, part2)
         } catch (e: Exception) {
             Pair("", "")
         }


    }
    private fun convertHeightBetweenCmAndFt(): String {
        if(_uiState.value.isHeightInputWrongFormat) {
            return ""
        }
        return if(_uiState.value.heightUnit == HeightUnit.FT_IN) {
            val (part1, part2) = separateHeightInFtUnit(_uiState.value.height)
            val convertRes = ConvertUnit.convertFtInToCm( part1.toFloatCustom(),part2.toFloatCustom())
            String.format("%.2f", convertRes).replace(",", ".")
        } else {
            val convertRes = ConvertUnit.convertCmToFtIn(_uiState.value.height.toFloatCustom())
            convertRes.ft.toInt().toString() + "'" + String.format("%.1f", convertRes.inches).replace(",", ".")
        }
    }

    fun changeHeightUnit() {
        if(_uiState.value.heightUnit == HeightUnit.CM) {
            val convertValue = convertHeightBetweenCmAndFt()
            _uiState.update {
                it.copy(
                    heightUnit = HeightUnit.FT_IN,
                    height = convertValue,
                    heightTextField = it.heightTextField.copy(text = convertValue, selection = TextRange(convertValue.length))
                )
            }
        }else {
            val convertValue = convertHeightBetweenCmAndFt()
            _uiState.update {
                it.copy(
                    heightUnit = HeightUnit.CM,
                    height = convertValue,
                    heightTextField = it.heightTextField.copy(text = convertValue, selection = TextRange(convertValue.length))
                )
            }
        }
    }

    private fun isHeightInputFormatWrong(height: String, heightUnit: HeightUnit): Boolean {
        //check height format and height limited
        return try {
            if(heightUnit == HeightUnit.FT_IN) {
                val (part1, part2) = separateHeightInFtUnit(height)
                !(part1.toFloatCustom() <= DefaultMaxValue.DEFAULT_MAX_VALUE_HEIGHT_IN_FT &&
                    part2.toFloatCustom() <= DefaultMaxValue.DEFAULT_MAX_VALUE_HEIGHT_IN_INCH)
            }else {
                height.toFloatCustom() > DefaultMaxValue.DEFAULT_MAX_VALUE_HEIGHT_IN_CM
            }
        } catch (e: NumberFormatException) {
            true
        }
    }

    private fun getHeightInMeterUnit(height: String): Float {
        return try {
            if(_uiState.value.heightUnit == HeightUnit.CM) {
                ConvertUnit.convertCmToMeter(height.toFloatCustom())
            }else {
                val (part1, part2) = separateHeightInFtUnit(height)
                ConvertUnit.convertFtInToMeter(part1.toFloatCustom(), part2.toFloatCustom())
            }
        } catch (e: NumberFormatException) {
            1f
        }
    }

    private fun getHeightInCmUnit(height: String): Float {
        val heightInMeterUnit = getHeightInMeterUnit(height)
        return ConvertUnit.convertMeterToCm(heightInMeterUnit)
    }

    fun setHeight(height: String) {
        if(isHeightInputFormatWrong(height, _uiState.value.heightUnit)) {
            return
        }
        _uiState.update {
            it.copy(
                isHeightInputWrongFormat = false,
                height = height,
                heightTextField = it.heightTextField.copy(text = height, selection = TextRange(height.length))
                )
        }
    }

    fun onHeightTextFieldClick() {
        _uiState.update {
            it.copy(
                heightTextField = it.heightTextField.copy(
                    selection = TextRange(0, it.height.length)
                )
            )
        }
    }

    private fun convertWeightBetweenKgAndLbs(): String {
        if(_uiState.value.isWeightInputWrongFormat) {
            return ""
        }
        return if(_uiState.value.weightUnit == WeightUnit.KG) {
            val convertRes = ConvertUnit.convertKgToLbs(_uiState.value.weight.toFloatCustom())
           String.format("%.2f", convertRes).replace(",", ".")
        } else {
            val convertRes = ConvertUnit.convertLbsToKg(_uiState.value.weight.toFloatCustom())
            String.format("%.2f", convertRes).replace(",", ".")
        }
    }

    fun changeWeightUnit() {
        if(_uiState.value.weightUnit == WeightUnit.KG) {
            val convertValue = convertWeightBetweenKgAndLbs()
            _uiState.update {
                it.copy(
                    weightUnit = WeightUnit.LBS,
                    weight = convertValue,
                    weightTextField = it.weightTextField.copy(text = convertValue, selection = TextRange(convertValue.length))
                )
            }
        }else {
            val convertValue = convertWeightBetweenKgAndLbs()
            _uiState.update {
                it.copy(
                    weightUnit = WeightUnit.KG,
                    weight = convertValue,
                    weightTextField = it.weightTextField.copy(text = convertValue, selection = TextRange(convertValue.length))
                )
            }
        }
    }

    private fun isWeightInputFormatWrong(weight: String, weightUnit: WeightUnit): Boolean {
        //check weight format and weight limited
        return try {
            if(weightUnit == WeightUnit.KG) {
                weight.toFloatCustom() > DefaultMaxValue.DEFAULT_MAX_VALUE_WEIGHT_IN_KG
            } else {
                weight.toFloatCustom() > DefaultMaxValue.DEFAULT_MAX_VALUE_WEIGHT_IN_LBS
            }
        } catch (e: NumberFormatException) {
            true
        }
    }

    private fun getWeightInKgUnit(weight: String): Float {
        return try {
            if(_uiState.value.weightUnit == WeightUnit.KG) {
                weight.toFloatCustom()
            }else {
                ConvertUnit.convertLbsToKg(weight.toFloatCustom())
            }
        }catch (e: NumberFormatException) {
            0f
        }

    }

    fun setWeight(weight: String) {
        if(isWeightInputFormatWrong(weight, _uiState.value.weightUnit)) {
            return 
        }
        _uiState.update {
            it.copy(
                isWeightInputWrongFormat = false,
                weight = weight,
                weightTextField = it.weightTextField.copy(text = weight, selection = TextRange(weight.length))
            )
        }
    }

    fun onWeightTextFieldCLick() {
        _uiState.update {
            it.copy(
                weightTextField = it.weightTextField.copy(
                    selection = TextRange(0, it.weight.length)
                )
            )
        }
    }

    fun setBmi() {
        val weight = getWeightInKgUnit(_uiState.value.weight)
        val height = getHeightInMeterUnit(_uiState.value.height)
        val bmi = calculateBmi(weight,height)
        val bmiType = getBmiType(bmi)

        _uiState.update {
            it.copy(
                bmi = bmi,
                selectBmiType =  bmiType
            )
        }
    }

    fun setTime(time: String) {
        _uiState.update { it.copy(time = time) }
    }

    fun setDate(date: String) {
        _uiState.update { it.copy(date = date) }
    }

    fun setNotes(notes: Set<String>) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun confirmDelete() {
        _uiState.update { it.copy(shouldShowDeleteDialog = true) }
    }

    fun clearConfirmDelete() {
        _uiState.update { it.copy(shouldShowDeleteDialog = false) }
    }

    fun deleteRecord() {
        viewModelScope.launch {
            recordId?.let { id ->
                repository.getRecordById(id).firstOrNull()?.let { record ->
                    repository.deleteBmiRecord(record)
                }
                _uiState.update { it.copy(shouldNavigateUp = true) }
            }
        }
    }

    fun saveRecord() {
        if(_uiState.value.isHeightInputWrongFormat || _uiState.value.isWeightInputWrongFormat) {
            _uiState.update {
                it.copy(isSaveSuccess = false)
            }
            return
        }

        val weight = getWeightInKgUnit(_uiState.value.weight)
        val height = getHeightInCmUnit(_uiState.value.height)
        setBmi()
        val bmiRecord = BMIRecord(
            bmi = uiState.value.bmi,
            weight = weight,
            height = height,
            time = uiState.value.time,
            date = uiState.value.date,
            type = uiState.value.selectBmiType,
            typeName = textFormatter.getBpTypeName(uiState.value.selectBmiType.nameRes),
            notes = uiState.value.notes,
            age = uiState.value.age,
            genderType = uiState.value.genderType,
            createdAt = recordId ?: System.currentTimeMillis()
        )
        viewModelScope.launch {
            if(_uiState.value.recordId != null) {
                repository.updateBmiRecord(bmiRecord)
            }else {
                repository.insertBmiRecord(bmiRecord)
            }
            _uiState.update {
                it.copy(
                    isSaveSuccess = true
                )
            }
        }

    }

    fun showAgeDialog(shown: Boolean) {
        _uiState.update { it.copy(showAgeDialog = shown) }
    }

    fun forceShowAgeDialog(shown: Boolean) {
        _uiState.update { it.copy(forceShowAgeDialog = shown) }
    }

    fun showGenderDialog(shown: Boolean) {
        _uiState.update { it.copy(showGenderDialog = shown) }
    }

    fun forceShowGenderDialog(shown: Boolean) {
        _uiState.update { it.copy(forceShowGenderDialog = shown) }
    }

    fun setAge(age: Int) {
        viewModelScope.launch {
            dataStore.setAge(age)
            _uiState.update { it.copy(age = age) }
        }
    }

    fun setGender(gender: GenderType) {
        viewModelScope.launch {
            dataStore.setGender(gender)
            _uiState.update { it.copy(genderType = gender) }
        }
    }

    private fun getBmiType(bmiValue: Float): BMIType {
        val value = (bmiValue * 10f).roundToInt() / 10f
        BMIType.values().forEach {
            if(it.isValid(value)) {
                return it
            }
        }
        return BMIType.NORMAL
    }

    private fun getCurrentDateTime() {
        val cal = Calendar.getInstance()
        val formattedTime = textFormatter.formatTime(cal.time)
        val formattedDate = textFormatter.formatDate(cal.timeInMillis)
        _uiState.update { it.copy(date = formattedDate, time = formattedTime) }
    }

    private fun observePurchases() {
        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update {
                        it.copy(
                            isAdsEnabled = !isPurchased && !remoteConfig.offAllAds(),
                        )
                    }
                }
        }
    }

    private fun calculateBmi(weight: Float, height: Float) = weight / (height * height)


    data class UiState(
        val recordId: Long? = null,
        val weight: String = "70.0",
        val height: String = "175.00",
        val weightTextField: TextFieldValue = TextFieldValue(weight, TextRange(weight.length)),
        val heightTextField: TextFieldValue = TextFieldValue(height, TextRange(height.length)),
        val heightUnit: HeightUnit = HeightUnit.CM,
        val weightUnit: WeightUnit = WeightUnit.KG,
        val isHeightInputWrongFormat: Boolean = false,
        val isWeightInputWrongFormat: Boolean = false,
        val bmi: Float = 23.5f,
        val selectBmiType: BMIType = BMIType.NORMAL,
        val date: String = "",
        val time: String = "",
        val shouldNavigateUp: Boolean = false,
        val shouldShowDeleteDialog: Boolean = false,
        val notes: Set<String> = emptySet(),
        val isPurchased: Boolean = false,
        val isAgeInputted: Boolean = false,
        val isGenderInputted: Boolean = false,
        val showAgeDialog: Boolean = false,
        val age: Int = 0,
        val showGenderDialog: Boolean = false,
        val forceShowAgeDialog: Boolean = false,
        val genderType: GenderType = GenderType.OTHERS,
        val isSaveSuccess: Boolean = false,
        val forceShowGenderDialog: Boolean = false,
        val showAds: Boolean = true,
        val isAdsEnabled: Boolean = false,
    )
}