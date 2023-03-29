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
    val weathercode: Int,
    val windspeed: Double,
    val winddirection: Double,
)

data class HourlyWeather(
    val time: String,
    val timeStamp: Long,
    val windspeed_10m: Double,
    val temperature_2m: Double,
    val relativehumidity_2m: Int,
    val weathercode: Int
)