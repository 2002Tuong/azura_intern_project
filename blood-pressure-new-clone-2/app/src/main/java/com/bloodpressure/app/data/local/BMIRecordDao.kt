package com.bloodpressure.app.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bloodpressure.app.data.model.BMIRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface BMIRecordDao {
    @Query("SELECT * FROM bmi_record ORDER BY date DESC, time DESC, created_at DESC ")
    fun getAllRecords() : Flow<List<BMIRecord>>

    @Query("SELECT * FROM bmi_record ORDER BY date DESC, time DESC, created_at DESC")
    fun getAllRecordsWithPaging(): PagingSource<Int, BMIRecord>

    @Query("SELECT * FROM bmi_record ORDER BY date DESC, time DESC, created_at DESC LIMIT 100")
    fun getChartData(): Flow<List<BMIRecord>>

    @Query("SELECT * FROM bmi_record WHERE created_at = :id")
    fun getRecordById(id: Long): Flow<BMIRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BMIRecord)

    @Delete
    suspend fun deleteRecord(record: BMIRecord)

    @Update
    suspend fun updateRecord(record: BMIRecord)
}