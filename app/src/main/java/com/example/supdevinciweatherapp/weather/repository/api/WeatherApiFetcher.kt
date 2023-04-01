package com.example.supdevinciweatherapp.weather.repository.api

import com.example.supdevinciweatherapp.commonModels.Coordinate
import com.example.supdevinciweatherapp.weather.models.ApiResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val API_ENDPOINT = "https://api.open-meteo.com/"

class WeatherApiFetcher {
    private val weatherApiService: WeatherApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(API_ENDPOINT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherApiService = retrofit.create(WeatherApiService::class.java)
    }

    suspend fun fetchWeatherForecast(coordinate: Coordinate): ApiResponse? {
        val lat = coordinate.latitude
        val lon = coordinate.longitude
        println(coordinate)
        val response = weatherApiService.getWeatherForecast(coordinate.longitude, coordinate.latitude)
        return if (response.isSuccessful) {
            println(response)
            response.body()
        } else {
            null
        }
    }
}
