package com.bloodpressure.app.screen.record.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.model.Note
import com.bloodpressure.app.data.repository.BpRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: BpRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    init {
        getAllNotes()
    }

    fun delete(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun createNote(note: String) {
        viewModelScope.launch {
            val position = (uiState.value.notes.maxOfOrNull { it.position } ?: 0) + 1
            repository.insertNote(Note(note, position))
        }
    }

    private fun getAllNotes() {
        viewModelScope.launch {
            repository.populateNotesIfNeeded()
            repository.getAllNotes().collectLatest { notes ->
                _uiState.update { it.copy(notes = notes) }
            }
        }
    }

    data class UiState(val notes: List<Note> = emptyList())
}
