package com.bloodpressure.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bloodpressure.app.data.model.BloodSugarRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodSugarRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BloodSugarRecord)

    @Update
    suspend fun updateRecord(record: BloodSugarRecord)

    @Query("SELECT * FROM blood_sugar_record ORDER BY date DESC, time DESC, row_id DESC")
    fun getAll(): Flow<List<BloodSugarRecord>>

    @Query("SELECT * FROM blood_sugar_record WHERE row_id = :id")
    fun getRecordById(id: Long): Flow<BloodSugarRecord?>

    @Delete
    suspend fun deleteRecord(record: BloodSugarRecord)

}