package com.example.supdevinciweatherapp.weather.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_table")
data class WeatherCityEntity(@PrimaryKey @ColumnInfo(name = "city") val city: String)