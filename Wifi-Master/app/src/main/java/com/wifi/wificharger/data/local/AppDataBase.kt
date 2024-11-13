package com.wifi.wificharger.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.wifi.wificharger.data.dao.WifiDao
import com.wifi.wificharger.data.model.Wifi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Wifi::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wifiDao(): WifiDao

    companion object {
        val DATABASE_NAME = "DATABASE_NAME"
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME
            ).allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build().also {
                    it.populateInitialData()
                    INSTANCE = it
                }
        }
    }

    val dummyList = listOf(
        Wifi(
            id = 0,
            name = "HomeNetwork",
            password = "HomePass123",
            securityType = "",
            rewardedEarn = false
        ),
        Wifi(
            id = 0,
            name = "CafeWireless",
            password = "CoffeeCafe456",
            securityType = "",
            rewardedEarn = false
        ),
        Wifi(
            id = 0,
            name = "LibraryWifi",
            password = "BooksAndWifi",
            securityType = "",
            rewardedEarn = false
        ),
        Wifi(
            id = 0,
            name = "GuestHouseNet",
            password = "Vacation2022",
            securityType = "",
            rewardedEarn = false
        ),
        Wifi(
            id = 0,
            name = "AirportFreeWifi",
            password = "Travel123",
            securityType = "",
            rewardedEarn = false
        ),
        Wifi(
            id = 0,
            name = "CityParkNetwork",
            password = "CityParkWifi",
            securityType = "",
            rewardedEarn = false
        )
    )
    private fun populateInitialData() {
        if (wifiDao().count() == 0) {
            runInTransaction {
                CoroutineScope(Dispatchers.IO).launch {
                    dummyList.forEach {
                        wifiDao().insertWifi(it)
                    }
                }
            }
        }
    }
}