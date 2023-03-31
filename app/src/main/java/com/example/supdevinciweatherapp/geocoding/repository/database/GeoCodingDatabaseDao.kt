package com.example.supdevinciweatherapp.geocoding.repository.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.supdevinciweatherapp.geocoding.entity.GeoCodingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeoCodingDatabaseDao {
    @Query("SELECT * FROM city_list")
    fun getCities(): Flow<List<GeoCodingEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(city: GeoCodingEntity)

    @Query("DELETE FROM city_list")
    suspend fun deleteAll()
}