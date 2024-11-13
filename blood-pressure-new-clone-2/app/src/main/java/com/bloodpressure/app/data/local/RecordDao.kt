package com.bloodpressure.app.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bloodpressure.app.data.model.AnalyticData
import com.bloodpressure.app.data.model.Record
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    @Query("SELECT * FROM record ORDER BY date DESC, time DESC, created_at DESC")
    fun getAll(): Flow<List<Record>>

    @Query("SELECT * FROM record ORDER BY date DESC, time DESC, created_at DESC")
    fun getAllRecordsWithPaging(): PagingSource<Int, Record>

    @Query("SELECT * FROM record ORDER BY date DESC, time DESC, created_at DESC LIMIT 100")
    fun getChartData(): Flow<List<Record>>

    @Query("SELECT * FROM record WHERE created_at = :id")
    fun getRecordById(id: Long): Flow<Record?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: Record)

    @Delete
    suspend fun deleteRecord(record: Record)

    @Update
    suspend fun updateRecord(record: Record)

    @Query(
        "SELECT MAX(systolic) as max_systolic, MIN(systolic) as min_systolic, " +
                "AVG(systolic) as average_systolic, (SELECT systolic FROM record ORDER BY created_at DESC LIMIT 1) as latest_systolic, " +
                "MAX(diastolic) as max_diastolic, MIN(diastolic) as min_diastolic, " +
                "AVG(diastolic) as average_diastolic, (SELECT diastolic FROM record ORDER BY created_at DESC LIMIT 1) as latest_diastolic, " +
                "MAX(pulse) as max_pulse, MIN(pulse) as min_pulse, " +
                "AVG(pulse) as average_pulse, (SELECT pulse FROM record ORDER BY created_at DESC LIMIT 1) as latest_pulse " +
                "FROM record"
    )
    fun queryAnalyticData(): Flow<AnalyticData>
}
