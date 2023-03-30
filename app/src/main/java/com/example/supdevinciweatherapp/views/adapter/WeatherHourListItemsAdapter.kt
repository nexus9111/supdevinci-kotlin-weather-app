package com.example.supdevinciweatherapp.views.adapter

import android.view.View
import android.view.ViewGroup
import com.example.supdevinciweatherapp.commonModels.HourlyWeather
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.supdevinciweatherapp.R
import com.example.supdevinciweatherapp.weather.usecase.WeatherCode
import java.text.SimpleDateFormat
import java.util.*

const val RECEIVED_DATE_FORMAT = "yyyy-MM-dd h:mm a"
const val RECEIVED_DATE_TIME_EXTRACT_FORMAT = "hh:mm a"

class WeatherHourListItemsAdapter(private val arrayList: List<HourlyWeather>) : BaseAdapter() {
    override fun getCount(): Int {
        return arrayList.size
    }

    override fun getItem(position: Int): Any {
        return arrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(parent?.context, R.layout.list_item_hourly_weather, null)


        val timeParsed = SimpleDateFormat(RECEIVED_DATE_FORMAT).parse(arrayList[position].time)
        val dateParser = SimpleDateFormat(RECEIVED_DATE_TIME_EXTRACT_FORMAT)
        val netDate = Date(timeParsed?.time ?: 0)
        val beautyDate = dateParser.format(netDate)

        val iconName = WeatherCode().getWeatherImage(arrayList[position].weatherCode)
        val weatherImage = view.context.resources.getIdentifier(iconName + "_icon", "drawable", view.context.packageName)

        // ----------------------- VIEW GETTER -----------------------
        val time = view.findViewById<TextView>(R.id.hourTime)
        val temperature = view.findViewById<TextView>(R.id.hourTemperature)
        val windSpeed = view.findViewById<TextView>(R.id.hourWindSpeed)
        val weatherIcon = view.findViewById<ImageView>(R.id.hourWeatherIcon)

        // ----------------------- STRING BUILDER -----------------------
        val temperatureToString = "${arrayList[position].temperature}°C"
        val windSpeedToString = "${arrayList[position].windSpeed}km/h"

        // ----------------------- VIEW BUILDER -----------------------
        time.text = beautyDate
        temperature.text = temperatureToString
        windSpeed.text = windSpeedToString
        weatherIcon.setImageResource(weatherImage)

        return view
    }
}