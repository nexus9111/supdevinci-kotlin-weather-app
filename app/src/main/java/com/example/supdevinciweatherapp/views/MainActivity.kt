package com.example.supdevinciweatherapp.views

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.supdevinciweatherapp.commonModels.City
import com.example.supdevinciweatherapp.commonModels.WeatherModels
import com.example.supdevinciweatherapp.commonModels.cities
import com.example.supdevinciweatherapp.databinding.ActivityMainBinding
import com.example.supdevinciweatherapp.utils.utils
import com.example.supdevinciweatherapp.viewModels.CoordinateViewModel
import com.example.supdevinciweatherapp.views.adapter.WeatherHourListItemsAdapter
import com.example.supdevinciweatherapp.weather.entity.WeatherCityEntity
import com.example.supdevinciweatherapp.weather.repository.CityRoomDatabase
import com.example.supdevinciweatherapp.weather.repository.database.WeatherDatabaseRepository
import com.example.supdevinciweatherapp.weather.usecase.WeatherCode
import com.example.supdevinciweatherapp.weather.usecase.api.WeatherApiUsecase
import kotlinx.coroutines.*

var citySelected = "Talence"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var coordinateViewModel: CoordinateViewModel
    private lateinit var dbRepository: WeatherDatabaseRepository
    private var weatherUsecase = WeatherApiUsecase()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbRepository = initDbAndUpdateSelectedCity()
        coordinateViewModel = getCityCoordinates(citySelected)

        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            val weatherForecast = weatherUsecase.getWeatherForecast(coordinateViewModel)
            if (weatherForecast != null) {
                updateView(weatherForecast)
            } else {
                utils().showToast("Weather forecast is null", this@MainActivity)
            }
        }
    }

    private fun getCityCoordinates(city: String): CoordinateViewModel {
        coordinateViewModel = CoordinateViewModel()
        val defaultCityIndex = cities.indexOfFirst { it.town == citySelected }
        val selectedCity = cities[defaultCityIndex]
        coordinateViewModel.updateCoordinate(selectedCity.longitude, selectedCity.latitude)
        return coordinateViewModel
    }

    private fun onCitySelected(city: City) {
        coordinateViewModel = CoordinateViewModel()

        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            coordinateViewModel.updateCoordinate(city.longitude, city.latitude)
            val weatherForecast = weatherUsecase.getWeatherForecast(coordinateViewModel)
            if (weatherForecast != null) {
                if (city.town != citySelected) {
                    citySelected = city.town
                    updateView(weatherForecast)
                    updateCity(citySelected)
                }
            } else {
                utils().showToast("Weather forecast is null", this@MainActivity)
            }
        }
    }

    private fun updateView(weatherForecast: WeatherModels) {
        val citySpinner = findViewById<Spinner>(binding.citySpinner.id)
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, cities.map { it.town })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        citySpinner.adapter = adapter

        val defaultCityIndex = cities.indexOfFirst { it.town == citySelected }
        citySpinner.setSelection(defaultCityIndex)

        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCity = cities[position]
                onCitySelected(selectedCity)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // ----------------------- STRING BULDERS -----------------------
        val currentTemperatureStr = "${weatherForecast.currentWeather.temperature}°C"
        val currentWindSpeedStr = "${weatherForecast.currentWeather.windSpeed}km/h"

        val currentHourly = weatherUsecase.getCurrentHourlyWeather(weatherForecast)
        val currentHumidityStr = "${currentHourly.humidity}%"

        val weatherImageName =
            WeatherCode().getWeatherImage(weatherForecast.currentWeather.weatherCode)
        val weatherImage = resources.getIdentifier(weatherImageName, "drawable", packageName)
        var currentTime = "${citySelected}, ${weatherForecast.currentWeather.time}"

        // ----------------------- UPDATE VIEW -----------------------
        binding.currentTime.text = currentTime
        binding.currentTemperature.text = currentTemperatureStr
        binding.currentWindSpeed.text = currentWindSpeedStr
        binding.currentWeather.text =
            WeatherCode().getWeatherDescription(weatherForecast.currentWeather.weatherCode)
        binding.currentWeatherBigIcon.setImageResource(weatherImage)
        binding.currentHumidity.text = currentHumidityStr

        val hourlyAdapter =
            WeatherHourListItemsAdapter(weatherUsecase.getCurrentDayWeather(weatherForecast))
        binding.hourlyItems.adapter = hourlyAdapter
    }

    private fun initDbAndUpdateSelectedCity(): WeatherDatabaseRepository {
        val applicationScope = CoroutineScope(SupervisorJob())
        val database = CityRoomDatabase.getDatabase(this, applicationScope)
        val repository = WeatherDatabaseRepository(database.cityDao())
        applicationScope.launch(Dispatchers.IO) {
            repository.allCities.collect {
                if (it.isNotEmpty()) {
                    citySelected = it[0].city
                } else {
                    repository.insert(WeatherCityEntity(citySelected))
                }
            }
        }
        return repository
    }

    private fun updateCity(city: String) {
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            dbRepository.deleteAll()
            dbRepository.insert(WeatherCityEntity(city))
        }
    }
}
