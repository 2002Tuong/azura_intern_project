package com.bloodpressure.app.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.bloodpressure.app.data.model.AlarmRecord
import com.bloodpressure.app.data.model.BMIRecord
import com.bloodpressure.app.data.model.BloodSugarRecord
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.data.model.Note
import com.bloodpressure.app.data.model.Record
import com.bloodpressure.app.data.model.WaterCupRecord

@Database(
    entities = [
        Record::class,
        Note::class,
        HeartRateRecord::class,
        AlarmRecord::class,
        BMIRecord::class,
        BloodSugarRecord::class,
        WaterCupRecord::class
    ],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 1, to = 4),
    ],
    exportSchema = true

)
@TypeConverters(
    BpTypeConverters::class,
    NoteConverters::class,
    HeartRateTypeConverters::class,
    BMITypeConverters::class,
    BloodSugarStateTypeConverters::class,
    TargetRangesConverters::class,
    BloodSugarRateTypeConverters::class,
    BloodSugarUnitTypeConverters::class
)
abstract class BpRoomDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao

    abstract fun noteDao(): NoteDao

    abstract fun heartRateDao(): HeartRateRecordDao

    abstract fun alarmDao(): AlarmRecordDao

    abstract fun bmiRecordDao(): BMIRecordDao

    abstract fun bloodSugarRecordDao(): BloodSugarRecordDao

    abstract fun waterReminderDao(): WaterReminderDao
}
