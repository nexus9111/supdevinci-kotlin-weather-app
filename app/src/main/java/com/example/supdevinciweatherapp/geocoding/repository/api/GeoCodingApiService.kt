package com.example.supdevinciweatherapp.geocoding.repository.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import com.example.supdevinciweatherapp.commonModels.CounrtyCoordonates

const val GET_CITY_COORD_ENDPOINT = "v1/geocoding"

const val COUNTRY = "France"
const val CURRENT_WEATHER = "true"
const val TIME_ZONE = "Europe/Paris"

interface GeoCodingApiService {
    @GET(GET_CITY_COORD_ENDPOINT)
    suspend fun getCountryCoordonates(
        @Query("city") city: String,
        @Header("X-Api-Key") apiKey: String,
        @Query("country") country: String = COUNTRY,
    ): Response<List<CounrtyCoordonates>>
}
