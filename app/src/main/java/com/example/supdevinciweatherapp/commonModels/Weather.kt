package com.example.supdevinciweatherapp.commonModels

data class WeatherModels(
    val latitude: Double,
    val longitude: Double,
    val currentWeather: CurrentWeather,
    val hourlyWeather: List<HourlyWeather>,
)

data class CurrentWeather(
    val time: String,
    val temperature: Double,
    val weatherCode: Int,
    val windSpeed: Double,
    val windDirection: Double,
)

data class HourlyWeather(
    val time: String,
    val timeStamp: Long,
    val windSpeed: Double,
    val temperature: Double,
    val humidity: Int,
    val weatherCode: Int
)