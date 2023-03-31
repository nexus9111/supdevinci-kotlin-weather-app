package com.example.supdevinciweatherapp.geocoding.usecase

import com.example.supdevinciweatherapp.commonModels.CounrtyCoordonates
import com.example.supdevinciweatherapp.geocoding.repository.api.GeoCodingApiFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GeoCodingApiUsecase {
    suspend fun getWeatherForecast(country: String): CounrtyCoordonates? {
        val geoCodingApiFetcher = GeoCodingApiFetcher()
        var finalCounrtyCoordonates: CounrtyCoordonates? = null

        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            val counrtyCoordonates = geoCodingApiFetcher.fetchCityCoordonate(country)
            if (counrtyCoordonates != null) {
                finalCounrtyCoordonates = counrtyCoordonates
            }
        }.join()

        if (finalCounrtyCoordonates == null) {
            return null
        }

        return finalCounrtyCoordonates
    }
}