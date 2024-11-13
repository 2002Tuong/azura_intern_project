package com.bloodpressure.app.data.repository.bloodsugar

import com.bloodpressure.app.data.local.BloodSugarRecordDao
import com.bloodpressure.app.data.local.NoteDao
import com.bloodpressure.app.data.model.BloodSugarRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class BloodSugarRepository(
    private val bloodSugarRecordDao: BloodSugarRecordDao,
    private val noteDao: NoteDao,
): IBloodSugarRepository {
    override suspend fun insertRecord(record: BloodSugarRecord) =
        withContext(Dispatchers.IO) { bloodSugarRecordDao.insertRecord(record) }

    override suspend fun updateRecord(record: BloodSugarRecord) =
        withContext(Dispatchers.IO) { bloodSugarRecordDao.updateRecord(record) }

    override fun getRecordById(id: Long): Flow<BloodSugarRecord?> =
        bloodSugarRecordDao.getRecordById(id).distinctUntilChanged().flowOn(Dispatchers.IO)

    override fun getAllRecords(): Flow<List<BloodSugarRecord>> =
        bloodSugarRecordDao.getAll().distinctUntilChanged().flowOn(Dispatchers.IO)

    override suspend fun deleteRecord(record: BloodSugarRecord) =
        withContext(Dispatchers.IO) { bloodSugarRecordDao.deleteRecord(record) }

}