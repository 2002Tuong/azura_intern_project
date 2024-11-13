package com.bloodpressure.app.data.repository

import androidx.paging.PagingSource
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.local.BMIRecordDao
import com.bloodpressure.app.data.local.NoteDao
import com.bloodpressure.app.data.model.BMIRecord
import com.bloodpressure.app.data.model.Note
import com.bloodpressure.app.data.remote.RemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class BMIRepository(
    private val bmiRecordDao: BMIRecordDao,
    private val noteDao: NoteDao,
    private val remoteConfig: RemoteConfig,
    private val dataStore: AppDataStore
) {
    fun getChartData(): Flow<List<BMIRecord>>
        = bmiRecordDao.getChartData().distinctUntilChanged().flowOn(Dispatchers.IO)

    fun getAllRecords(): Flow<List<BMIRecord>>
        = bmiRecordDao.getAllRecords().distinctUntilChanged().flowOn(Dispatchers.IO)

    suspend fun insertBmiRecord(record: BMIRecord)
        = withContext(Dispatchers.IO) {bmiRecordDao.insertRecord(record)}

    suspend fun deleteBmiRecord(record: BMIRecord)
        = withContext(Dispatchers.IO) {bmiRecordDao.deleteRecord(record)}

    suspend fun updateBmiRecord(record: BMIRecord)
        = withContext(Dispatchers.IO) {bmiRecordDao.updateRecord(record)}

    fun getRecordById(id: Long): Flow<BMIRecord?> =
        bmiRecordDao.getRecordById(id).distinctUntilChanged().flowOn(Dispatchers.IO)

    fun pagedListRecords(): PagingSource<Int, BMIRecord> {
        return bmiRecordDao.getAllRecordsWithPaging()
    }

    suspend fun insertNote(note: Note) = withContext(Dispatchers.IO) { noteDao.insert(note) }

    suspend fun updateNote(note: Note) =
        withContext(Dispatchers.IO) { noteDao.update(note) }

    suspend fun deleteNote(note: Note) =
        withContext(Dispatchers.IO) { noteDao.delete(note) }

    fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().distinctUntilChanged().flowOn(Dispatchers.IO)
    }

    suspend fun populateNotesIfNeeded() {
        if (!dataStore.isSuggestionPopulated) {
            val notes = remoteConfig.getNotes()
            if (notes.isNotEmpty()) {
                noteDao.insert(*notes.toTypedArray())
                dataStore.setSuggestionNotePopulated(true)
            }
        }
    }
}