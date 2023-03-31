package com.example.supdevinciweatherapp.geocoding.repository.database

import androidx.annotation.WorkerThread
import com.example.supdevinciweatherapp.geocoding.entity.GeoCodingEntity
import kotlinx.coroutines.flow.Flow

class GeoCodingDatabaseRepository (private val wordDao: GeoCodingDatabaseDao) {
    val allCities: Flow<List<GeoCodingEntity>> = wordDao.getCities()

    @WorkerThread
    suspend fun insert(city: GeoCodingEntity) {
        wordDao.insert(city)
    }

    @WorkerThread
    suspend fun deleteAll() {
        wordDao.deleteAll()
    }
}
