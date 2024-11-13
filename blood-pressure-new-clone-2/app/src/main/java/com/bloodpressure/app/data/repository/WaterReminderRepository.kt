package com.bloodpressure.app.data.repository

import com.bloodpressure.app.data.local.WaterReminderDao
import com.bloodpressure.app.data.model.WaterCupRecord
import com.bloodpressure.app.utils.TextFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Calendar

class WaterReminderRepository(
    private val waterReminderDao: WaterReminderDao,
    private val textFormatter: TextFormatter
) {

    fun getAllWaterHistory(): Flow<List<WaterCupRecord>> {
        return waterReminderDao.getAllWaterHistory()
    }

    suspend fun insertWaterCupRecord(waterCupRecord: WaterCupRecord) {
        withContext(Dispatchers.IO) {
            waterReminderDao.insertWaterCupRecord(waterCupRecord)
        }
    }

    suspend fun deleteLatestRecord() {
        withContext(Dispatchers.IO) {
            val record = waterReminderDao.getLatestCupRecord()
            if(record != null) {
                waterReminderDao.delete(record)
            }
        }
    }


    fun getWaterCupRecordsToday(): Flow<List<WaterCupRecord>> {
        val cal = Calendar.getInstance()
        val today = textFormatter.formatDate(cal.timeInMillis)
        return waterReminderDao.getWaterCupToday(today).distinctUntilChanged().flowOn(Dispatchers.IO)
    }

}