package com.example.supdevinciweatherapp.weather.usecase.api

import com.example.supdevinciweatherapp.commonModels.CurrentWeather
import com.example.supdevinciweatherapp.commonModels.HourlyWeather
import com.example.supdevinciweatherapp.commonModels.WeatherModels
import com.example.supdevinciweatherapp.viewModels.CoordinateViewModel
import com.example.supdevinciweatherapp.weather.models.ApiResponse
import com.example.supdevinciweatherapp.weather.repository.api.WeatherApiFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

const val INPUT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm"
const val OUTPUT_DATE_FORMAT = "yyyy-MM-dd h:mm a"

class WeatherApiUsecase {

    suspend fun getWeatherForecast(coordinate: CoordinateViewModel): WeatherModels? {
        val weatherApiFetcher = WeatherApiFetcher()
        var weatherForecastResponse: ApiResponse? = null

        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
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

    fun apiResponseConvertisor(apiResponse: ApiResponse): WeatherModels {
        val finalDate = convertDate(apiResponse.current_weather.time)

        val currentWeather = CurrentWeather(
            finalDate,
            apiResponse.current_weather.temperature,
            apiResponse.current_weather.weathercode,
            apiResponse.current_weather.windspeed,
            apiResponse.current_weather.winddirection
        )

        val hourlyWeather = mutableListOf<HourlyWeather>()
        for (i in 0 until apiResponse.hourly.time.size) {
            val hourlyRawTimeParsed = SimpleDateFormat(INPUT_DATE_FORMAT).parse(apiResponse.hourly.time[i])
            val beautyDate = convertDate(apiResponse.hourly.time[i])
            hourlyWeather.add(
                HourlyWeather(
                    beautyDate,
                    hourlyRawTimeParsed.time,
                    apiResponse.hourly.windspeed_10m[i],
                    apiResponse.hourly.temperature_2m[i],
                    apiResponse.hourly.relativehumidity_2m[i],
                    apiResponse.hourly.weathercode[i]
                )
            )
        }

        val weatherModels = WeatherModels(
            apiResponse.latitude,
            apiResponse.longitude,
            currentWeather,
            hourlyWeather
        )

        return weatherModels
    }

    fun getCurrentHourlyWeather(weatherModels: WeatherModels): HourlyWeather {
        var lastHourlyWeather: HourlyWeather = weatherModels.hourlyWeather[0]

        val currentTs = System.currentTimeMillis()
        for (weatherModel in weatherModels.hourlyWeather) {
            val currentTime = SimpleDateFormat(OUTPUT_DATE_FORMAT).parse(weatherModel.time)
            if (currentTime.time > currentTs) {
                break
            }
            lastHourlyWeather = weatherModel
        }

        return lastHourlyWeather
    }

    fun getCurrentDayWeather(weatherModels: WeatherModels): List<HourlyWeather> {
        var dayWeather = mutableListOf<HourlyWeather>()
        for (i in 1 until 25) {
            dayWeather.add(weatherModels.hourlyWeather[i])
        }
        return dayWeather
    }

    private fun convertDate(date: String): String {
        val rawTimeParsed = SimpleDateFormat(INPUT_DATE_FORMAT).parse(date)
        val rawTimeToDate = Date(rawTimeParsed.time)

        val dateParser = SimpleDateFormat(OUTPUT_DATE_FORMAT)
        return dateParser.format(rawTimeToDate)
    }
}