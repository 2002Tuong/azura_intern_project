package com.wifi.wificharger.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wifi.wificharger.data.model.Wifi
import kotlinx.coroutines.flow.Flow

@Dao
interface WifiDao {

    /**
     * Counts the number of cheeses in the table.
     *
     * @return The number of cheeses.
     */
    @Query("SELECT COUNT(*) FROM wifi")
    fun count(): Int

    @Query("SELECT * FROM wifi ORDER BY wifi.id")
    fun getWifiList(): Flow<List<Wifi>>

    @Query("SELECT * FROM wifi WHERE wifi.name = :name ")
    fun getWifi(name: String): Flow<Wifi?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWifi(wifi: Wifi)

    @Update
    fun updateWifi(wifi: Wifi)
}