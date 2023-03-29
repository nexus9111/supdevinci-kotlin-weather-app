package com.example.supdevinciweatherapp.weather.usecase

class WeatherCode {
    fun getWeatherDescription(weatherCode: Int): String {
        return when (weatherCode) {
            0 -> "Clear sky"
            1, 2, 3 -> "Mainly clear, partly cloudy, and overcast"
            45, 48 -> "Fog and depositing rime fog"
            51, 53, 55 -> "Drizzle: Light, moderate, and dense intensity"
            56, 57 -> "Freezing Drizzle: Light and dense intensity"
            61, 63, 65 -> "Rain: Slight, moderate and heavy intensity"
            66, 67 -> "Freezing Rain: Light and heavy intensity"
            71, 73, 75 -> "Snow fall: Slight, moderate, and heavy intensity"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers: Slight, moderate, and violent"
            85, 86 -> "Snow showers slight and heavy"
            95 -> "Thunderstorm: Slight or moderate"
            96, 99 -> "Thunderstorm with slight and heavy hail"
            else -> "Unknown weather code"
        }
    }

    fun getWeatherImage(weatherCode: Int): String {
        return when (weatherCode) {
            0 -> "full_sun"
            1, 2, 3 -> "sun_cloud"
            45, 48 -> "fog"
            51, 53, 55 -> "rain"
            56, 57 -> "rain"
            61, 63, 65 -> "rain"
            66, 67 -> "rain"
            71, 73, 75 -> "snow"
            77 -> "snow"
            80, 81, 82 -> "rain"
            85, 86 -> "snow"
            95 -> "thunder"
            96, 99 -> "thunder"
            else -> "unknown"
        }
    }
}