package com.example.supdevinciweatherapp.viewModels

import androidx.lifecycle.ViewModel
import com.example.supdevinciweatherapp.commonModels.Coordinate

class CoordinateViewModel : ViewModel() {

    private val text = "Vos Coordonnées GPS sont : "

    private var coordinate = Coordinate(0f, 0f)

    fun updateCoordinate(longitude: Float, latitude: Float) {
        coordinate = Coordinate(longitude, latitude)
    }

    fun getCoordinate(): Coordinate {
        return coordinate
    }

    fun getCoordinateText(): String {
        return "$text Longitude: ${coordinate.longitude}, Latitude: ${coordinate.latitude}"
    }
}