package com.example.supdevinciweatherapp.views

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.supdevinciweatherapp.R
import com.example.supdevinciweatherapp.commonModels.City
import com.example.supdevinciweatherapp.commonModels.CounrtyCoordonates
import com.example.supdevinciweatherapp.commonModels.WeatherModels
import com.example.supdevinciweatherapp.commonModels.cities
import com.example.supdevinciweatherapp.databinding.ActivityMainBinding
import com.example.supdevinciweatherapp.geocoding.entity.GeoCodingEntity
import com.example.supdevinciweatherapp.geocoding.repository.database.GeoCityRoomDatabase
import com.example.supdevinciweatherapp.geocoding.repository.database.GeoCodingDatabaseRepository
import com.example.supdevinciweatherapp.geocoding.usecase.GeoCodingApiUsecase
import com.example.supdevinciweatherapp.utils.utils
import com.example.supdevinciweatherapp.viewModels.CoordinateViewModel
import com.example.supdevinciweatherapp.views.adapter.WeatherHourListItemsAdapter
import com.example.supdevinciweatherapp.weather.entity.WeatherCityEntity
import com.example.supdevinciweatherapp.weather.repository.database.CityRoomDatabase
import com.example.supdevinciweatherapp.weather.repository.database.WeatherDatabaseRepository
import com.example.supdevinciweatherapp.weather.usecase.WeatherCode
import com.example.supdevinciweatherapp.weather.usecase.api.WeatherApiUsecase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

var citySelected = "Talence"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var coordinateViewModel: CoordinateViewModel
    private lateinit var cityNameDbRepository: WeatherDatabaseRepository
    private lateinit var cityCoordDbRepository: GeoCodingDatabaseRepository
    private var weatherUsecase = WeatherApiUsecase()
    private var geoCodingApiUsecase = GeoCodingApiUsecase()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cityNameDbRepository = initDbAndUpdateSelectedCity()
        cityCoordDbRepository = initCoordDb()
        coordinateViewModel = getCityCoordinates(citySelected)


        val searchCityButton = findViewById<Button>(R.id.search_city_button)
        searchCityButton.setOnClickListener {
            searchCustomCity()
        }

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

    private fun searchCustomCity() {

        val cityInput = findViewById<EditText>(R.id.city_input)
        val cityName = cityInput.text.toString()
        if (cityName.isEmpty()) {
            return
        }

        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            val cityCoordonates = geoCodingApiUsecase.getCityLocation(cityName)
            if (cityCoordonates != null) {
                println("cityCoordonates: $cityCoordonates")
                if (cityCoordonates.name != citySelected) {
                    citySelected = cityCoordonates.name
                    coordinateViewModel.updateCoordinate(cityCoordonates.longitude, cityCoordonates.latitude)
                    val weatherForecast = weatherUsecase.getWeatherForecast(coordinateViewModel)
                    if (weatherForecast != null) {
                        println("INSERTING DATA")
                        updateCityCoord(cityCoordonates)
                        println("DATA INSERTED")
                        updateView(weatherForecast)
                    } else {
                        utils().showToast("Weather forecast is null", this@MainActivity)
                    }
                }

            } else {
                utils().showToast("cityCoordonates is null", this@MainActivity)
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
                    updateCity(citySelected)
                    getCityCoordFromDb()
                    updateView(weatherForecast)
                }
            } else {
                utils().showToast("Weather forecast is null", this@MainActivity)
            }
        }
    }

    private suspend fun updateView(weatherForecast: WeatherModels) {
        val citySpinner = findViewById<Spinner>(binding.citySpinner.id)
        val allCities = getAllCitiesFromDb()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, allCities.map { it.town })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        citySpinner.adapter = adapter

        val defaultCityIndex = allCities.indexOfFirst { it.town == citySelected }
        citySpinner.setSelection(defaultCityIndex)

        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCity = allCities[position]
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

    private fun initCoordDb(): GeoCodingDatabaseRepository {
        val applicationScope = CoroutineScope(SupervisorJob())
        val database = GeoCityRoomDatabase.getDatabase(this, applicationScope)
        val repository = GeoCodingDatabaseRepository(database.cityDao())
        applicationScope.launch(Dispatchers.IO) {
            repository.allCities.collect {
                if (it.isEmpty()) {
                    for (city in cities) {
                        repository.insert(
                            GeoCodingEntity(
                                city.town,
                                city.latitude.toString(),
                                city.longitude.toString()
                            )
                        )
                    }
                }
            }
        }
        return repository
    }

    private fun updateCity(city: String) {
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            cityNameDbRepository.deleteAll()
            cityNameDbRepository.insert(WeatherCityEntity(city))
        }
    }

    private suspend fun updateCityCoord(city: CounrtyCoordonates) {
        val applicationScope = CoroutineScope(SupervisorJob())
        val database = GeoCityRoomDatabase.getDatabase(this, applicationScope)
        val repository = GeoCodingDatabaseRepository(database.cityDao())

        applicationScope.launch(Dispatchers.IO) {
            repository.insert(
                GeoCodingEntity(
                    city.name,
                    city.latitude.toString(),
                    city.longitude.toString()
                )
            )
        }
        return
    }

    private fun getCityCoordFromDb(): CounrtyCoordonates {
        var coordonates: CounrtyCoordonates = CounrtyCoordonates("", 0f, 0f, "", "")
        val applicationScope = CoroutineScope(SupervisorJob())
        val database = GeoCityRoomDatabase.getDatabase(this, applicationScope)
        val repository = GeoCodingDatabaseRepository(database.cityDao())
        applicationScope.launch(Dispatchers.IO) {
            repository.allCities.collect {
                if (it.isNotEmpty()) {
                    for (city in it) {
                        if (city.city == citySelected) {
                            coordonates = CounrtyCoordonates(
                                city.city,
                                city.lat.toFloat(),
                                city.lon.toFloat(),
                                "France",
                                "FR"
                            )
                            coordinateViewModel.updateCoordinate(
                                coordonates.longitude,
                                coordonates.latitude
                            )
                        }
                    }
                } else {
                    throw Exception("No city found, there is a problem with the database")
                }
            }
        }
        return coordonates
    }

    private suspend fun getAllCitiesFromDb(): List<City> {
        var cities: MutableList<City> = mutableListOf()

        val applicationScope = CoroutineScope(SupervisorJob())
        val database = GeoCityRoomDatabase.getDatabase(this, applicationScope)
        val repository = GeoCodingDatabaseRepository(database.cityDao())

        applicationScope.launch(Dispatchers.IO) {
            val cityList = repository.allCities.first()
            if (cityList.isNotEmpty()) {
                for (city in cityList) {
                    cities.add(City(city.city, city.lat.toFloat(), city.lon.toFloat()))
                }
            } else {
                throw Exception("No city found, there is a problem with the database")
            }
        }.join()

        cities.sortBy { it.town }
        return cities
    }
}
