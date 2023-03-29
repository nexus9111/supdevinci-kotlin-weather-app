package com.example.supdevinciweatherapp.weather.usecase.api


import com.example.supdevinciweatherapp.commonModels.*
import com.example.supdevinciweatherapp.viewModels.CoordinateViewModel
import com.example.supdevinciweatherapp.weather.models.ApiResponse
import com.example.supdevinciweatherapp.weather.repository.api.WeatherApiFetcher
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Date

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
        val timeParsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(apiResponse.current_weather.time)
        val dateParser = SimpleDateFormat("yyyy-MM-dd h:mm a")
        val netDate = Date(timeParsed.time)
        val beautyDate = dateParser.format(netDate)

        val currentWeather = CurrentWeather(
            beautyDate,
            apiResponse.current_weather.temperature,
            apiResponse.current_weather.weathercode,
            apiResponse.current_weather.windspeed,
            apiResponse.current_weather.winddirection
        )

        val hourlyWeather = mutableListOf<HourlyWeather>()
        for (i in 0 until apiResponse.hourly.time.size) {
            val timeParsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(apiResponse.hourly.time[i])
            val dateParser = SimpleDateFormat("yyyy-MM-dd h:mm a")
            val netDate = Date(timeParsed.time)
            val beautyDate = dateParser.format(netDate)
            hourlyWeather.add(
                HourlyWeather(
                    beautyDate,
                    timeParsed.time,
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

    fun getCurrentHourlyWeather(weatherModels: WeatherModels) : HourlyWeather {

        var lastHourlyWeather: HourlyWeather = weatherModels.hourlyWeather[0]
        val currentTs = System.currentTimeMillis()
        for (weatherModel in weatherModels.hourlyWeather) {
            val currentTime = SimpleDateFormat("yyyy-MM-dd h:mm a").parse(weatherModel.time)
            val parsedTs = currentTime.time
            if (parsedTs > currentTs) {
                break
            }
            lastHourlyWeather = weatherModel
        }

        return lastHourlyWeather
    }

    fun getCurrenDayWeather(weatherModels: WeatherModels) : List<HourlyWeather> {
        var dayWeather = mutableListOf<HourlyWeather>()
        for (i in 1 until 25) {
            dayWeather.add(weatherModels.hourlyWeather[i])
        }
        return dayWeather
    }
}