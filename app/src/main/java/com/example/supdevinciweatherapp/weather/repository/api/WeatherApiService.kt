package com.example.supdevinciweatherapp.weather.repository.api

import com.example.supdevinciweatherapp.weather.models.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

const val GET_WEATHER_FORECAST_ENDPOINT = "v1/forecast"

const val HOURLY_PARAMETERS = "temperature_2m,relativehumidity_2m,precipitation_probability,weathercode,windspeed_10m"
const val CURRENT_WEATHER = "true"
const val TIME_ZONE = "Europe/Paris"

interface WeatherApiService {
    @GET(GET_WEATHER_FORECAST_ENDPOINT)
    suspend fun getWeatherForecast(
        @Query("latitude") latitude: Float,
        @Query("longitude") longitude: Float,
        @Query("hourly") hourly: String = HOURLY_PARAMETERS,
        @Query("current_weather") current_weather: String = CURRENT_WEATHER,
        @Query("timezone") timezone: String = TIME_ZONE
    ): Response<ApiResponse>
}
