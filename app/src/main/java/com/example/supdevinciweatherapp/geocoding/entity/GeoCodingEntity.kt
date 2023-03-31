package com.example.supdevinciweatherapp.geocoding.entity


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_list")
data class GeoCodingEntity(
    @PrimaryKey @ColumnInfo(name = "city") val city: String,
    @ColumnInfo(name = "lat") val lat: String,
    @ColumnInfo(name = "lon") val lon: String
)