package com.example.supdevinciweatherapp.geocoding.repository.api

import com.example.supdevinciweatherapp.commonModels.CounrtyCoordonates
import io.github.cdimascio.dotenv.dotenv
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val API_ENDPOINT = "https://api.api-ninjas.com/"

class GeoCodingApiFetcher {
    private val geoCodingApiService: GeoCodingApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(API_ENDPOINT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        geoCodingApiService = retrofit.create(GeoCodingApiService::class.java)
    }

    suspend fun fetchCityCoordonate(country: String): CounrtyCoordonates? {
        val dotenv = dotenv {
            directory = "/assets"
            filename = "env"
        }
        if (dotenv["GEO_NINJA_API"] == null) {
            throw Exception("GEO_NINJA_API is null")
        }

        val apiKey = dotenv["GEO_NINJA_API"]
        val response = geoCodingApiService.getCountryCoordonates(country, apiKey!!)
        return if (response.isSuccessful) {
            response.body()!![0]
        } else {
            null
        }
    }
}
