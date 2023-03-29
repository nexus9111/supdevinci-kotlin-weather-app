package com.example.supdevinciweatherapp.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.supdevinciweatherapp.commonModels.WeatherModels
import com.example.supdevinciweatherapp.databinding.ActivityMainBinding
import com.example.supdevinciweatherapp.viewModels.CoordinateViewModel
import com.example.supdevinciweatherapp.weather.entity.WeatherCityEntity
import com.example.supdevinciweatherapp.weather.models.ApiResponse
import com.example.supdevinciweatherapp.weather.repository.CityRoomDatabase
import com.example.supdevinciweatherapp.weather.repository.database.WeatherDatabaseRepository
import com.example.supdevinciweatherapp.weather.usecase.api.WeatherApiUsecase
import com.example.supdevinciweatherapp.weather.usecase.WeatherCode
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var coordinateViewModel: CoordinateViewModel
    private var weatherUsecase = WeatherApiUsecase();

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        initDb()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        coordinateViewModel = CoordinateViewModel()
        coordinateViewModel.updateCoordinate(44.81f, -0.61f)

        // showToastWithCoordinates()

        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            val weatherForecast = weatherUsecase.getWeatherForecast(coordinateViewModel)
            if (weatherForecast != null) {
                updateView(weatherForecast)
            } else {
                showToast("Weather forecast is null")
            }
        }
    }


    private fun updateView(weatherForecast: WeatherModels) {
        binding.currentTime.text = weatherForecast.currentWeather.time
        binding.currentTemperature.text = "${weatherForecast.currentWeather.temperature}°C"
        binding.currentWindSpeed.text = "${weatherForecast.currentWeather.windspeed.toString()}km/h"
        binding.currentWeather.text = WeatherCode().getWeatherDescription(weatherForecast.currentWeather.weathercode)
        val weatherImageName = WeatherCode().getWeatherImage(weatherForecast.currentWeather.weathercode)
        // change image with currentWeatherBigIcon id
        val weatherImage = resources.getIdentifier(weatherImageName, "drawable", packageName)
        binding.currentWeatherBigIcon.setImageResource(weatherImage)
    }

    private fun showToastWithCoordinates() {
        val text = coordinateViewModel.getCoordinateText()
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    private fun showToastWithWeatherForecast(weatherForecast: ApiResponse) {
        val text = "Weather : ${weatherForecast.longitude} is ${weatherForecast.latitude}"
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    fun initDb() {
        val applicationScope = CoroutineScope(SupervisorJob())
        val database = CityRoomDatabase.getDatabase(this, applicationScope)
        val repository = WeatherDatabaseRepository(database.cityDao())
        applicationScope.launch (Dispatchers.IO) {
            repository.insert(WeatherCityEntity("Bordeaux"))
        }
    }
}
