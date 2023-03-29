package com.example.supdevinciweatherapp.weather.repository.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.supdevinciweatherapp.weather.entity.WeatherCityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDatabaseDao {
    @Query("SELECT * FROM city_table")
    fun getWeatherCities(): Flow<List<WeatherCityEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(city: WeatherCityEntity)

    @Query("DELETE FROM city_table")
    suspend fun deleteAll()
}