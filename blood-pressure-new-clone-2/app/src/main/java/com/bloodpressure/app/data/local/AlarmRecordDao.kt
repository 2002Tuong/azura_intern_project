package com.bloodpressure.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bloodpressure.app.data.model.AlarmRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmRecordDao {

    @Query("SELECT * FROM alarm_record ORDER BY created_at DESC")
    fun getAll(): Flow<List<AlarmRecord>>


    @Query("SELECT * FROM alarm_record WHERE created_at = :id")
    fun getRecordById(id: Long): Flow<AlarmRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AlarmRecord)

    @Delete
    suspend fun deleteRecord(record: AlarmRecord)

    @Update
    suspend fun updateRecord(record: AlarmRecord)
}
