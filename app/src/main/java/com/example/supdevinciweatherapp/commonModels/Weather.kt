package com.example.supdevinciweatherapp.commonModels


data class WeatherModels(
    val latitude: Double,
    val longitude: Double,
    val currentWeather: CurrentWeather,
    val hourlyWeather: HourlyWeather,
)

data class CurrentWeather(
    val time: String,
    val temperature: Double,
    val weathercode: Int,
    val windspeed: Double,
    val winddirection: Double
)

data class HourlyWeather(
    val time: List<String>,
    val windspeed_10m: List<Double>,
    val temperature_2m: List<Double>,
    val relativehumidity_2m: List<Int>
)