package com.example.supdevinciweatherapp.weather.usecase.api


import com.example.supdevinciweatherapp.commonModels.Coordinate
import com.example.supdevinciweatherapp.commonModels.*
import com.example.supdevinciweatherapp.viewModels.CoordinateViewModel
import com.example.supdevinciweatherapp.weather.models.ApiResponse
import com.example.supdevinciweatherapp.weather.repository.api.WeatherApiFetcher
import kotlinx.coroutines.*

class WeatherApiUsecase {

    suspend fun getWeatherForecast(coordinate: CoordinateViewModel) : WeatherModels? {
        val scope = CoroutineScope(Job() + Dispatchers.IO)

        var weatherForecastResponse: ApiResponse? = null

        val job = scope.launch {
            val weatherApiFetcher = WeatherApiFetcher()
            val weatherForecast = weatherApiFetcher.fetchWeatherForecast(coordinate.getCoordinate())
            if (weatherForecast != null) {
                weatherForecastResponse = weatherForecast
            }
        }.join()

        if (weatherForecastResponse == null) {
            return null
        }
        return apiResponseConvertisor(weatherForecastResponse!!)
    }

    fun apiResponseConvertisor(apiResponse: ApiResponse) : WeatherModels {
        val currentWeather = CurrentWeather(
            apiResponse.current_weather.time,
            apiResponse.current_weather.temperature,
            apiResponse.current_weather.weathercode,
            apiResponse.current_weather.windspeed,
            apiResponse.current_weather.winddirection
        )

        val hourlyWeather = HourlyWeather(
            apiResponse.hourly.time,
            apiResponse.hourly.windspeed_10m,
            apiResponse.hourly.temperature_2m,
            apiResponse.hourly.relativehumidity_2m
        )

        val weatherModels = WeatherModels(
            apiResponse.latitude,
            apiResponse.longitude,
            currentWeather,
            hourlyWeather
        )

        return weatherModels
    }
}