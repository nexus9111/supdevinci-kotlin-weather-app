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

// Note pour Maxime:
// L'ensemble des fonctionnalités de l'application sont implémentées.
// Il n'y a que la BDD qui fonctionne mal.
// Les données ne sont jamais récupérées au début de l'app.

var citySelected = "Talence"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var coordinateViewModel: CoordinateViewModel
    private lateinit var cityNameDbRepository: WeatherDatabaseRepository
    private lateinit var cityCoordDbRepository: GeoCodingDatabaseRepository
    private var weatherUsecase = WeatherApiUsecase()
    private var geoCodingApiUsecase = GeoCodingApiUsecase()
    private lateinit var citySpinner: Spinner
    private lateinit var allCities: List<City>

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        citySpinner = findViewById<Spinner>(binding.citySpinner.id)

        setupSearchButton()

        initializeData()
    }

    private fun setupSearchButton() {
        val searchCityButton = findViewById<Button>(R.id.search_city_button)
        searchCityButton.setOnClickListener {
            searchCustomCity()
        }
    }

    private fun initializeData() {
        val dbScope = CoroutineScope(Job() + Dispatchers.Main)
        dbScope.launch {
            val cityNameDbInit = initDbAndUpdateSelectedCity()
            cityNameDbRepository = cityNameDbInit

            val dbInit = initCityCoordinateDB()
            cityCoordDbRepository = dbInit

            coordinateViewModel = updateSelectedCoordonate()

            val scope = CoroutineScope(Job() + Dispatchers.Main)
            scope.launch {
                val weatherForecast = weatherUsecase.getWeatherForecast(coordinateViewModel)
                if (weatherForecast != null) {
                    updateView(weatherForecast)
                } else {
                    utils().showToast("Weather forecast is null", this@MainActivity)
                }
            }

            allCities = getAllCitiesFromDb()
            setupCitySpinner()
        }
    }

    private fun setupCitySpinner() {
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, allCities.map { it.town })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        citySpinner.adapter = adapter

        val defaultCityIndex = allCities.indexOfFirst { it.town == citySelected }
        citySpinner.setSelection(defaultCityIndex)

        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val selectedCity = allCities[position]
                if (selectedCity.town != citySelected) {
                    onCitySelected(selectedCity)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
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
            val cityCoordinates = geoCodingApiUsecase.getCityLocation(cityName)
            if (cityCoordinates != null) {
                println("cityCoordinates: $cityCoordinates")
                if (cityCoordinates.name != citySelected) {
                    citySelected = cityCoordinates.name
                    coordinateViewModel.updateCoordinate(
                        cityCoordinates.latitude, cityCoordinates.longitude
                    )
                    val weatherForecast = weatherUsecase.getWeatherForecast(coordinateViewModel)
                    if (weatherForecast != null) {
                        updateCityCoord(cityCoordinates)
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

    private suspend fun updateSelectedCoordonate(): CoordinateViewModel {
        coordinateViewModel = CoordinateViewModel()
        val selectedCity = getCityCoordFromDb()
        coordinateViewModel.updateCoordinate(selectedCity.latitude, selectedCity.longitude)
        return coordinateViewModel
    }

    private fun onCitySelected(city: City) {
        println("CALLED")
        coordinateViewModel = CoordinateViewModel()

        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            coordinateViewModel.updateCoordinate(city.longitude, city.latitude)
            val weatherForecast = weatherUsecase.getWeatherForecast(coordinateViewModel)
            if (weatherForecast != null) {
                citySelected = city.town
                updateCity()
                getCityCoordFromDb()
                updateView(weatherForecast)
            } else {
                utils().showToast("Weather forecast is null", this@MainActivity)
            }
        }
    }

    // initDbAndUpdateSelectedCity is called when the app starts
    // it init the city name db to set the selected city
    private fun initDbAndUpdateSelectedCity(): WeatherDatabaseRepository {
        val applicationScope = CoroutineScope(SupervisorJob())
        val database = CityRoomDatabase.getDatabase(this, applicationScope)
        val repository = WeatherDatabaseRepository(database.cityDao())
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
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

    // initCoordDb is called when the app starts
    // it inserts the cities in the database if the database is empty
    private suspend fun initCityCoordinateDB(): GeoCodingDatabaseRepository {
        val applicationScope = CoroutineScope(SupervisorJob())
        val database = GeoCityRoomDatabase.getDatabase(this, applicationScope)
        val repository = GeoCodingDatabaseRepository(database.cityDao())
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            val cityList = repository.allCities.first()
            if (cityList.isEmpty()) {
                for (city in cities) {
                    repository.insert(
                        GeoCodingEntity(
                            city.town, city.latitude.toString(), city.longitude.toString()
                        )
                    )
                }
            }
        }.join()
        return repository
    }

    // updateCity is called when the user selects a city from the spinner
    // it updates the city name in the database
    private fun updateCity() {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            cityNameDbRepository.deleteAll()
            cityNameDbRepository.insert(WeatherCityEntity(citySelected))
        }
    }

    // updateCityCoord is called when the user selects a city from the spinner
    // it updates the city coordinates in the database
    // (it inserts a new row if the city is not in the database)
    private suspend fun updateCityCoord(city: CounrtyCoordonates) {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            var allCities = getAllCitiesFromDb()
            var cityFound = false
            for (cityDb in allCities) {
                if (cityDb.town == city.name) {
                    cityFound = true
                    break
                }
            }
            if (!cityFound) {
                var mutableAllCities = allCities.toMutableList()
                mutableAllCities.add(City(city.name, city.latitude, city.longitude))
                cityCoordDbRepository.deleteAll()
                for (city in mutableAllCities) {
                    cityCoordDbRepository.insert(
                        GeoCodingEntity(
                            city.town, city.latitude.toString(), city.longitude.toString()
                        )
                    )
                }
            }
        }.join()
        return
    }

    // getCityCoordFromDb is called when the user selects a city from the spinner
    // it gets the selected city coordinates from the database and updates the view
    private suspend fun getCityCoordFromDb(): CounrtyCoordonates {
        var coordonates = CounrtyCoordonates("", 0f, 0f, "", "")
        val applicationScope = CoroutineScope(SupervisorJob())
        val database = GeoCityRoomDatabase.getDatabase(this, applicationScope)
        val repository = GeoCodingDatabaseRepository(database.cityDao())
        applicationScope.launch(Dispatchers.IO) {
            val allCities = repository.allCities.first()
            if (allCities.isNotEmpty()) {
                for (city in allCities) {
                    if (city.city == citySelected) {
                        coordonates = CounrtyCoordonates(
                            city.city, city.lat.toFloat(), city.lon.toFloat(), "France", "FR"
                        )
                        coordinateViewModel.updateCoordinate(
                            coordonates.longitude, coordonates.latitude
                        )
                    }
                }
            } else {
                throw Exception("No city found, there is a problem with the database")
            }
        }.join()
        return coordonates
    }

    // getAllCitiesFromDb is used to get all the cities from the database
    // it should never be empty, because it is initialized in the initCoordDb function
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

    private suspend fun updateView(weatherForecast: WeatherModels) {
        citySpinner = findViewById<Spinner>(binding.citySpinner.id)
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
}
