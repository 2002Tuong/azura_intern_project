package com.bloodpressure.app.screen.heartrate.measure

import android.graphics.SurfaceTexture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.camera.FingerListener
import com.bloodpressure.app.camera.HeartRate
import com.bloodpressure.app.camera.PulseListener
import com.bloodpressure.app.camera.TimerListener
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.remote.RemoteConfig
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.screen.heartrate.add.GenderType
import com.bloodpressure.app.screen.heartrate.add.HeartRateType
import com.bloodpressure.app.utils.AlarmingManager
import com.bloodpressure.app.utils.SoundManager
import com.bloodpressure.app.utils.TextFormatter
import com.bloodpressure.app.utils.VibrationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class HeartRateMeasureViewModel(
    private val heartRate: HeartRate,
    private val vibrationManager: VibrationManager,
    private val dataStore: AppDataStore,
    private val soundManager: SoundManager,
    private val textFormatter: TextFormatter,
    private val remoteConfig: RemoteConfig,
    private val alarmingManager: AlarmingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())

    val uiState: StateFlow<UiState> get() = _uiState
    private val timeLimit: Long = 15000
    var isMeasuring: Boolean = false
    var finishMeasure = false
    var cancelMeasure = false


    init {
        observePurchases()
        viewModelScope.launch {
            dataStore.isMeasureFlashEnabledFlow
                .distinctUntilChanged()
                .collectLatest { isEnabled ->
                    _uiState.update { it.copy(isMeasureFlashEnabled = isEnabled) }
                    heartRate.enableFlash(isEnabled)
                }
        }

        viewModelScope.launch {
            dataStore.isSoundOn
                .distinctUntilChanged()
                .collectLatest { isOn ->
                    _uiState.update { it.copy(isSoundOn = isOn) }
                }
        }

        viewModelScope.launch {
            dataStore.age
                .distinctUntilChanged()
                .collectLatest { age ->
                    if (age >= 2) {
                        heartRate.setAge(age)
                        _uiState.update { it.copy(age = age) }
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

    private fun observePurchases() {
        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update {
                        it.copy(
                            isPurchased = isPurchased,
                            isAdsEnabled = !isPurchased && !remoteConfig.offAllAds(),
                        )
                    }
                }
        }
    }

    fun startFingerDetection() {
        heartRate.heartSupport.startPulseCheck()
        heartRate.addOnResultListener(object : PulseListener {
            override fun onPulseResult(pulse: String) {
                _uiState.update { it.copy(heartRate = pulse.toInt()) }
            }
        }).addOnTimerListener(object : TimerListener {
            override fun onTimerStarted() {
                isMeasuring = true
                finishMeasure = false
                cancelMeasure = false
                _uiState.update { it.copy(progress = 0) }
            }

            override fun onTimerRunning(progress: Int) {
                _uiState.update { it.copy(progress = progress) }
                isMeasuring = true
            }

            override fun onTimerStopped() {
                isMeasuring = false
                _uiState.update {
                    it.copy(
                        progress = 100,
                        finishMeasure = !cancelMeasure,
                        quickHeartRateRecord = createHeartRateRecordNow(uiState.value.heartRate)
                    )
                }
                finishMeasure = true
                vibrationManager.stopVibration()
                soundManager.stopPlaying()
            }
        }).addOnFingerListener(object : FingerListener {
            override fun onFingerDetected(success: Int) {

                if (success > 3) {
                    _uiState.update { it.copy(isFingerDetected = true) }

                    if (!isMeasuring && !finishMeasure) {
                        heartRate.heartSupport.startPulseCheck(timeLimit)
                        isMeasuring = true

                        vibrationManager.vibrateHeartbeatPattern()

                        if (_uiState.value.isSoundOn) {
                            soundManager.startPlaying()
                        }
                    }
                }
            }

            override fun onFingerDetectFailed(failed: Int, pixelAverage: Int) {

                if (failed > 10) {
                    _uiState.update {
                        it.copy(
                            isFingerDetected = false,
                            progress = 0,
                            heartRate = 0
                        )
                    }
                    isMeasuring = false
                    heartRate.cancelPulseCheck()
                    vibrationManager.stopVibration()
                    soundManager.stopPlaying()
                }
            }
        })
    }

    private fun createHeartRateRecordNow(heartRate: Int): HeartRateRecord? {

        if (uiState.value.age == null || uiState.value.genderType == null || heartRate !in 40..220) {
            return null
        }

        val cal = Calendar.getInstance()
        val formattedTime = textFormatter.formatTime(cal.time)
        val formattedDate = textFormatter.formatDate(cal.timeInMillis)

        val heartRateType = getSelectedHeartRateType(heartRate)

        return HeartRateRecord(
            heartRate = uiState.value.heartRate,
            time = formattedTime,
            date = formattedDate,
            type = heartRateType,
            typeName = textFormatter.getBpTypeName(heartRateType.nameRes),
            notes = emptySet(),
            age = uiState.value.age!!,
            genderType = uiState.value.genderType!!,
            createdAt = System.currentTimeMillis()
        )
    }

    private fun getSelectedHeartRateType(heartRate: Int): HeartRateType {
        HeartRateType.values().forEach {
            if (it.isValid(heartRate)) {
                return it
            }
        }
        return HeartRateType.NORMAL
    }

    fun addPreviewSurface(surfaceTexture: SurfaceTexture) {
        heartRate.addPreviewSurface(surfaceTexture)
    }

    fun startMeasureScreen() {
        _uiState.update { it.copy(screenState = ScreenState.BEGIN) }
    }

    fun returnToIntroScreen() {
        cancelMeasure = true
        _uiState.update {
            UiState(showNoticeDialog = false)
        }
        isMeasuring = false
        heartRate.stopPulseCheck()
        vibrationManager.stopVibration()
        soundManager.stopPlaying()
        finishMeasure = false
    }

    fun dismissNoticeDialog() {
        _uiState.update { it.copy(showNoticeDialog = false) }
    }

    fun showIntroDialog(shown: Boolean) {
        _uiState.update { it.copy(showIntroDialog = shown) }
    }

    fun showErrorDialog(shown: Boolean) {
        _uiState.update { it.copy(showErrorDialog = shown) }
    }

    fun toggleFlash() {

        viewModelScope.launch {
            heartRate.toggleFlash()
            dataStore.setMeasureFlashEnabled(!_uiState.value.isMeasureFlashEnabled)
        }
    }

    fun turnFlashOn(isOn: Boolean) {
        viewModelScope.launch {
            heartRate.turnFlashOn(isOn)
        }
    }

    fun toggleSound() {
        viewModelScope.launch {
            soundManager.toggleSound()
            dataStore.setMeasureSoundOn(!_uiState.value.isSoundOn)
        }
    }

    override fun onCleared() {
        super.onCleared()

        heartRate.stopPulseCheck()
        vibrationManager.stopVibration()
        soundManager.stopPlaying()
    }

    fun onAdsShown() {
        _uiState.update { it.copy(showAds = false) }
    }

    fun showSetReminder(shown: Boolean) {
        _uiState.update { it.copy(showSetAlarmDialog = shown) }
    }

    fun insertRecord(alarmRecord: AlarmRecord) {
        viewModelScope.launch {
            alarmingManager.insertRecord(alarmRecord)
        }
    }

    data class UiState(
        val screenState: ScreenState = ScreenState.INTRO,
        val isFingerDetected: Boolean = false,
        val progress: Int = 0,
        val heartRate: Int = 0,
        val finishMeasure: Boolean = false,
        val showNoticeDialog: Boolean = true,
        val showIntroDialog: Boolean = false,
        val showErrorDialog: Boolean = false,
        val isMeasureFlashEnabled: Boolean = false,
        val isSoundOn: Boolean = false,
        val age: Int? = null,
        val genderType: GenderType? = null,
        val quickHeartRateRecord: HeartRateRecord? = null,
        val showAds: Boolean = true,
        val isAdsEnabled: Boolean = false,
        val showSetAlarmDialog: Boolean = false,
        val isPurchased: Boolean = false,
    )

    enum class ScreenState {
        INTRO, BEGIN
    }
}