package com.bloodpressure.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bloodpressure.app.data.model.WaterCupRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterCupRecord(waterCupRecord: WaterCupRecord)

    @Query("SELECT * FROM water_cup_record ORDER BY date DESC, time DESC, created_at DESC")
    fun getAllWaterHistory(): Flow<List<WaterCupRecord>>

    @Query("SELECT * FROM water_cup_record WHERE `date` = :date ORDER BY created_at DESC")
    fun getWaterCupToday(date: String): Flow<List<WaterCupRecord>>

    @Query("SELECT * FROM water_cup_record ORDER BY created_at DESC LIMIT 1")
    fun getLatestCupRecord(): WaterCupRecord?

    @Delete
    fun delete(record: WaterCupRecord?)

}