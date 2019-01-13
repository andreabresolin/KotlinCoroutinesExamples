/*
 *  Copyright 2018-2019 Andrea Bresolin
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

package andreabresolin.kotlincoroutinesexamples.forecast.view

import andreabresolin.kotlincoroutinesexamples.R
import andreabresolin.kotlincoroutinesexamples.app.components.WeatherView
import andreabresolin.kotlincoroutinesexamples.app.model.DayForecast
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams

class DaysForecastListAdapter
constructor(private val context: Context,
            private val forecasts: List<DayForecast>)
    : RecyclerView.Adapter<DaysForecastListAdapter.ForecastsListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastsListViewHolder {
        val view = WeatherView(context)
        view.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        return ForecastsListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return forecasts.size
    }

    override fun onBindViewHolder(holder: ForecastsListViewHolder, position: Int) {
        holder.bind(forecasts[position])
    }

    class ForecastsListViewHolder constructor(private val view: WeatherView) : RecyclerView.ViewHolder(view) {
        fun bind(forecast: DayForecast) {
            view.title = forecast.dayName

            if (forecast.minTemperature != null) {
                view.temperature = view.context.getString(R.string.temperature_string, forecast.minTemperature)
            } else {
                view.temperature = "-.-"
            }

            if (forecast.maxTemperature != null) {
                view.maxTemperature = view.context.getString(R.string.temperature_string, forecast.maxTemperature)
            } else {
                view.maxTemperature = "-.-"
            }

            if (forecast.icon != null) {
                view.weatherIconUrl = "https://openweathermap.org/img/w/${forecast.icon}.png"
            } else {
                view.weatherIconUrl = null
            }
        }
    }
}