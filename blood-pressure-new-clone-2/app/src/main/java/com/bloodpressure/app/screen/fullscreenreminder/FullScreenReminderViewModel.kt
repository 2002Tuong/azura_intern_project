package com.bloodpressure.app.screen.fullscreenreminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.data.model.Record
import com.bloodpressure.app.data.repository.BpRepository
import com.bloodpressure.app.data.repository.HeartRateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FullScreenReminderViewModel(
    private val repository: BpRepository,
    private val hrRepository: HeartRateRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    init {
        viewModelScope.launch {
            repository.getLatestRecord().distinctUntilChanged().collectLatest { record ->
                _uiState.update { it.copy(bpRecord = record) }
            }
        }
        viewModelScope.launch {
            hrRepository.getLatestRecord().distinctUntilChanged().collectLatest { record ->
                _uiState.update { it.copy(hrRecord = record) }
            }
        }
    }

    data class UiState(
        val bpRecord: Record? = null,
        val hrRecord: HeartRateRecord? = null
    )

}