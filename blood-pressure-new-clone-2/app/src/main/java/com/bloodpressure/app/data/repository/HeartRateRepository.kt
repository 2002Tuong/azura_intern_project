package com.bloodpressure.app.data.repository

import androidx.paging.PagingSource
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.local.HeartRateRecordDao
import com.bloodpressure.app.data.local.NoteDao
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.data.model.Note
import com.bloodpressure.app.data.remote.RemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext

class HeartRateRepository(
    private val heartRateRecordDao: HeartRateRecordDao,
    private val noteDao: NoteDao,
    private val remoteConfig: RemoteConfig,
    private val dataStore: AppDataStore
) {
    suspend fun insertRecord(record: HeartRateRecord) =
        withContext(Dispatchers.IO) { heartRateRecordDao.insertRecord(record) }

    suspend fun updateRecord(record: HeartRateRecord) =
        withContext(Dispatchers.IO) { heartRateRecordDao.updateRecord(record) }

    suspend fun deleteRecord(record: HeartRateRecord) =
        withContext(Dispatchers.IO) { heartRateRecordDao.deleteRecord(record) }

    fun getAllRecords(): Flow<List<HeartRateRecord>> =
        heartRateRecordDao.getAll().distinctUntilChanged().flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLatestRecord(): Flow<HeartRateRecord?> =
        heartRateRecordDao.getAll().distinctUntilChanged()
            .mapLatest { if (it.isEmpty()) null else it[0] }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)


    fun pagedListRecords(): PagingSource<Int, HeartRateRecord> {
        return heartRateRecordDao.getAllRecordsWithPaging()
    }

    fun getRecordById(id: Long): Flow<HeartRateRecord?> =
        heartRateRecordDao.getRecordById(id).distinctUntilChanged().flowOn(Dispatchers.IO)

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

    suspend fun getNoteById(id: String): Note? = withContext(Dispatchers.IO) {
        noteDao.getNoteById(id)
    }
}
