package com.bloodpressure.app.data.repository.bloodsugar

import com.bloodpressure.app.data.model.BloodSugarRecord
import kotlinx.coroutines.flow.Flow

interface IBloodSugarRepository {

    suspend fun insertRecord(record: BloodSugarRecord)

    suspend fun updateRecord(record: BloodSugarRecord)

    fun getRecordById(id: Long): Flow<BloodSugarRecord?>

    fun getAllRecords(): Flow<List<BloodSugarRecord>>

    suspend fun deleteRecord(record: BloodSugarRecord)
}