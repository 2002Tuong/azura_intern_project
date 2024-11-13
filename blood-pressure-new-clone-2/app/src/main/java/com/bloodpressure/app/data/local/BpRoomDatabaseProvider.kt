package com.bloodpressure.app.data.local

import android.content.Context
import android.os.Debug
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


class BpRoomDatabaseProvider(private val context: Context) {

    private var roomDatabase: BpRoomDatabase? = null

    fun provide(): BpRoomDatabase {
        if (this.roomDatabase != null) {
            return this.roomDatabase!!
        }

        val builder = Room.databaseBuilder(context, BpRoomDatabase::class.java, "BloodPressure.db")
            .fallbackToDestructiveMigration()
        if (Debug.isDebuggerConnected()) {
            builder.allowMainThreadQueries()
        }
        val roomDatabase = builder.build()
        this.roomDatabase = roomDatabase
        return roomDatabase
    }

    val MIGRATION_1_3: Migration = object : Migration(1, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // create comic bookmark table
            database.execSQL("CREATE TABLE IF NOT EXISTS `heart_rate_record` (`date` TEXT  NOT NULL,`note` TEXT NOT NULL,`gender_type` TEXT NOT NULL,`type_name` TEXT NOT NULL,`heart_rate` INTEGER NOT NULL,`created_at` INTEGER NOT NULL, `time` TEXT  NOT NULL,  `type` INTEGER NOT NULL, `age` INTEGER NOT NULL , PRIMARY KEY(`created_at`))")
        }
    }

}
