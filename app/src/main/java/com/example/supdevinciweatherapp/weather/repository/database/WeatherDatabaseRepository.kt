package com.example.supdevinciweatherapp.weather.repository.database

import androidx.annotation.WorkerThread
import com.example.supdevinciweatherapp.weather.entity.WeatherCityEntity
import kotlinx.coroutines.flow.Flow

class WeatherDatabaseRepository (private val wordDao: WeatherDatabaseDao) {
    val allCities: Flow<List<WeatherCityEntity>> = wordDao.getWeatherCities()

    @WorkerThread
    suspend fun insert(city: WeatherCityEntity) {
        wordDao.insert(city)
    }

    @WorkerThread
    suspend fun deleteAll() {
        wordDao.deleteAll()
    }
}
