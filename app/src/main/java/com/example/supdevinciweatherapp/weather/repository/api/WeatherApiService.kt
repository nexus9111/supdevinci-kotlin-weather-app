package com.example.supdevinciweatherapp.weather.repository.api

import com.example.supdevinciweatherapp.weather.models.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

public interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getWeatherForecast(
        @Query("latitude") latitude: Float,
        @Query("longitude") longitude: Float,
        //&hourly=temperature_2m,relativehumidity_2m,precipitation_probability,weathercode,windspeed_10m
        @Query("hourly") hourly: String = "temperature_2m,relativehumidity_2m,precipitation_probability,weathercode,windspeed_10m",
        @Query("current_weather") current_weather: String = "true"
    ): Response<ApiResponse>
}
