/*
 *  Copyright 2018 Andrea Bresolin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package andreabresolin.kotlincoroutinesexamples.home.view

import andreabresolin.kotlincoroutinesexamples.R
import andreabresolin.kotlincoroutinesexamples.app.components.CityWeatherView
import andreabresolin.kotlincoroutinesexamples.app.model.CityWeather
import andreabresolin.kotlincoroutinesexamples.app.model.LoadedCityWeather
import andreabresolin.kotlincoroutinesexamples.app.model.LoadingCityWeather
import andreabresolin.kotlincoroutinesexamples.app.model.UnknownCityWeather
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams

class CitiesWeatherListAdapter
constructor(private val context: Context,
            private val citiesWeather: List<CityWeather>)
    : RecyclerView.Adapter<CitiesWeatherListAdapter.WeatherListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherListViewHolder {
        val view = CityWeatherView(context)
        view.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        return WeatherListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return citiesWeather.size
    }

    override fun onBindViewHolder(holder: WeatherListViewHolder, position: Int) {
        holder.bind(citiesWeather[position])
    }

    class WeatherListViewHolder constructor(private val view: CityWeatherView) : RecyclerView.ViewHolder(view) {
        fun bind(cityWeather: CityWeather) {
            when (cityWeather) {
                is LoadedCityWeather -> {
                    view.cityName = cityWeather.cityName
                    view.temperature = view.context.getString(R.string.temperature_string, cityWeather.temperature)

                    if (cityWeather.icon != null) {
                        view.weatherIconUrl = "http://openweathermap.org/img/w/${cityWeather.icon}.png"
                    } else {
                        view.weatherIconUrl = null
                    }

                    view.isLoading = false
                }
                is LoadingCityWeather -> view.isLoading = true
                is UnknownCityWeather -> {
                    view.cityName = "-"
                    view.temperature = "-.-"
                    view.weatherIconUrl = null
                    view.isLoading = false
                }
            }
        }
    }
}