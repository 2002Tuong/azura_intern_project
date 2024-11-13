package com.bloodpressure.app.data.repository

import androidx.paging.PagingSource
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.local.NoteDao
import com.bloodpressure.app.data.local.RecordDao
import com.bloodpressure.app.data.model.AnalyticData
import com.bloodpressure.app.data.model.Note
import com.bloodpressure.app.data.model.Record
import com.bloodpressure.app.data.remote.RemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext

class BpRepository(
    private val recordDao: RecordDao,
    private val noteDao: NoteDao,
    private val remoteConfig: RemoteConfig,
    private val dataStore: AppDataStore
) {
    fun getAnalyticData(): Flow<AnalyticData> =
        recordDao.queryAnalyticData().flowOn(Dispatchers.IO).distinctUntilChanged()

    fun getChartData(): Flow<List<Record>> =
        recordDao.getChartData().flowOn(Dispatchers.IO).distinctUntilChanged()

    suspend fun insertRecord(record: Record) =
        withContext(Dispatchers.IO) { recordDao.insertRecord(record) }

    suspend fun updateRecord(record: Record) =
        withContext(Dispatchers.IO) { recordDao.updateRecord(record) }

    suspend fun deleteRecord(record: Record) =
        withContext(Dispatchers.IO) { recordDao.deleteRecord(record) }

    fun getAllRecords(): Flow<List<Record>> =
        recordDao.getAll().distinctUntilChanged().flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLatestRecord(): Flow<Record?> =
        recordDao.getAll().mapLatest { if(it.isEmpty()) null else it[0]}.distinctUntilChanged().flowOn(Dispatchers.IO)

    fun pagedListRecords(): PagingSource<Int, Record> {
        return recordDao.getAllRecordsWithPaging()
    }

    fun getRecordById(id: Long): Flow<Record?> =
        recordDao.getRecordById(id).distinctUntilChanged().flowOn(Dispatchers.IO)

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
