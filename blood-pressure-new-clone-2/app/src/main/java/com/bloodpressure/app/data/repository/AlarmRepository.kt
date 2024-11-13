package com.bloodpressure.app.data.repository

import com.bloodpressure.app.data.local.AlarmRecordDao
import com.bloodpressure.app.data.model.AlarmRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class AlarmRepository(private val alarmRecordDao: AlarmRecordDao) {
    suspend fun insertRecord(record: AlarmRecord) =
        withContext(Dispatchers.IO) { alarmRecordDao.insertRecord(record) }

    suspend fun updateRecord(record: AlarmRecord) =
        withContext(Dispatchers.IO) { alarmRecordDao.updateRecord(record) }

    suspend fun deleteRecord(record: AlarmRecord) =
        withContext(Dispatchers.IO) { alarmRecordDao.deleteRecord(record) }

    fun getAllRecords(): Flow<List<AlarmRecord>> =
        alarmRecordDao.getAll().distinctUntilChanged().flowOn(Dispatchers.IO)

    fun getRecordById(id: Long): Flow<AlarmRecord?> =
        alarmRecordDao.getRecordById(id).distinctUntilChanged().flowOn(Dispatchers.IO)
}
