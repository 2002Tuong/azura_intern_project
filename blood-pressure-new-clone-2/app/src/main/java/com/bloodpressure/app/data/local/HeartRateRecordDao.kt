package com.bloodpressure.app.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bloodpressure.app.data.model.HeartRateRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HeartRateRecordDao {

    @Query("SELECT * FROM heart_rate_record ORDER BY date DESC, time DESC, created_at DESC")
    fun getAll(): Flow<List<HeartRateRecord>>

    @Query("SELECT * FROM heart_rate_record ORDER BY date DESC, time DESC, created_at DESC")
    fun getAllRecordsWithPaging(): PagingSource<Int, HeartRateRecord>

    @Query("SELECT * FROM heart_rate_record ORDER BY date DESC, time DESC, created_at DESC LIMIT 100")
    fun getChartData(): Flow<List<HeartRateRecord>>

    @Query("SELECT * FROM heart_rate_record WHERE created_at = :id")
    fun getRecordById(id: Long): Flow<HeartRateRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HeartRateRecord)

    @Delete
    suspend fun deleteRecord(record: HeartRateRecord)

    @Update
    suspend fun updateRecord(record: HeartRateRecord)
}
