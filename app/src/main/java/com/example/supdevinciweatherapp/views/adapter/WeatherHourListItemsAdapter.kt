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
        val time = view.findViewById<TextView>(R.id.hourTime)
        val temperature = view.findViewById<TextView>(R.id.hourTemperature)
        val windSpeed = view.findViewById<TextView>(R.id.hourWindSpeed)
        val weatherIcon = view.findViewById<ImageView>(R.id.hourWeatherIcon)

        val timeParsed = SimpleDateFormat("yyyy-MM-dd h:mm a").parse(arrayList[position].time)
        val dateParser = SimpleDateFormat("hh:mm")
        val netDate = Date(timeParsed.time)
        val beautyDate = dateParser.format(netDate)
        val iconName = WeatherCode().getWeatherImage(arrayList[position].weathercode)

        time.text = beautyDate
        temperature.text = "${arrayList[position].temperature_2m}°C"
        windSpeed.text = "${arrayList[position].windspeed_10m}km/h"

        val weatherImage = view.context.resources.getIdentifier(iconName + "_icon", "drawable", view.context.packageName)
        weatherIcon.setImageResource(weatherImage)

        return view
    }
}