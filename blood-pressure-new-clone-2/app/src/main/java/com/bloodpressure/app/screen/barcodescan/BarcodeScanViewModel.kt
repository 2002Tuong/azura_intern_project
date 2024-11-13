package com.bloodpressure.app.screen.barcodescan

import android.graphics.SurfaceTexture
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BarcodeScanViewModel(
    private val scanner: ScannerSupport
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            scanner.detectResult.collectLatest {res ->
                if(res == null) {
                    //Log.d("ScanCode", "res is null")
                    _uiState.update { it.copy(detectState = DetectResult.UNDETECT) }
                } else {
                    Log.d("ScanCode", res.resultMetadata.toString())
                    if( res.text.isNullOrEmpty()) {
                        _uiState.update { it.copy(detectState = DetectResult.FAIL) }
                    } else {
                        _uiState.update { it.copy(detectState = DetectResult.SUCCESS, detectResult = res.text) }
                    }
                    stopDetectCode()
                }
            }
        }

        viewModelScope.launch {
            uiState.collectLatest {state ->
                if(state.detectState != DetectResult.UNDETECT) {
                    delay(2000L)
                    reset()
                }
            }
        }
    }

    fun setSurfaceSize(width: Int, height: Int) {
        scanner.setSizeSurfaceTexture(width, height)
    }

    fun addPreviewSurface(surfaceTexture: SurfaceTexture) {
        scanner.addPreviewSurface(surfaceTexture)
    }

    fun startDetectCode() {
        scanner.startScanCode()
    }

    fun stopDetectCode() {
        scanner.stopScanCode()
    }
    fun startDetectScreen() {
        _uiState.update { it.copy(screenState = ScreenState.BEGIN) }
    }

    fun reset() {
        scanner.resetResult()
        startDetectCode()
    }

    override fun onCleared() {
        super.onCleared()
        scanner.stopScanCode()
    }



    data class UiState(
        val permissionGranted: Boolean = false,
        val detectState: DetectResult = DetectResult.UNDETECT,
        val detectResult: String = "",
        val screenState: ScreenState = ScreenState.INTRO,
    )
}

public enum class DetectResult {
    UNDETECT, FAIL, SUCCESS
}

enum class ScreenState {
    BEGIN, INTRO
}