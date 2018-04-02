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

package andreabresolin.kotlincoroutinesexamples.forecast.domain

import andreabresolin.kotlincoroutinesexamples.app.domain.BaseUseCase
import andreabresolin.kotlincoroutinesexamples.app.model.DayForecast
import andreabresolin.kotlincoroutinesexamples.app.network.model.ThreeHoursWeatherForecast
import andreabresolin.kotlincoroutinesexamples.app.network.model.WeatherForecast
import andreabresolin.kotlincoroutinesexamples.app.repository.WeatherRepository
import kotlinx.coroutines.experimental.delay
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class GetForecastUseCase
@Inject constructor(private val weatherRepository: WeatherRepository) : BaseUseCase() {

    class GetForecastException constructor(val cityAndCountry: String) : RuntimeException()

    private val dayIdFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
    private lateinit var dayNameFormat: SimpleDateFormat

    init {
        dayIdFormat.timeZone = TimeZone.getTimeZone("UTC")
    }

    suspend fun execute(cityAndCountry: String): List<DayForecast> {
        // We need to initialize the day name format for each execution because
        // the default locale and timezone might change at any time in the device.
        initDayNameFormat()

        val forecast: WeatherForecast? = asyncAwait {
            delay(1500) // Simulate a delay in the response
            weatherRepository.getWeatherForecast(cityAndCountry)
        }

        return mapWeatherForecastToDayForecastList(forecast, cityAndCountry)
    }

    private fun initDayNameFormat() {
        dayNameFormat = SimpleDateFormat("EEEE d", Locale.getDefault())
        dayNameFormat.timeZone = TimeZone.getDefault()
    }

    private suspend fun mapWeatherForecastToDayForecastList(
            forecast: WeatherForecast?,
            cityAndCountry: String): List<DayForecast> = asyncAwait {

        val result: MutableList<DayForecast> = mutableListOf()

        if (forecast?.list != null && forecast.list.isNotEmpty()) {
            // Group the forecasts by day
            val forecastsGroupedByDay = getForecastsGroupedByDay(forecast.list)

            forecastsGroupedByDay.forEach { _, dayForecasts ->
                if (dayForecasts.isNotEmpty()) {
                    var minTemperature = 0.0
                    var maxTemperature = 0.0
                    var isTemperatureFound = false

                    // Find the minimum and maximum temperatures for each day
                    dayForecasts.forEach {
                        it.main?.temp?.let {
                            if (!isTemperatureFound) {
                                isTemperatureFound = true
                                minTemperature = it
                                maxTemperature = it
                            } else {
                                minTemperature = min(minTemperature, it)
                                maxTemperature = max(maxTemperature, it)
                            }
                        }
                    }

                    val firstForecast: ThreeHoursWeatherForecast = dayForecasts[0]

                    // The day name can be extracted by any of the forecasts for the same day
                    val dayName = if (firstForecast.dt != null) getDayName(firstForecast.dt * 1000) else throw GetForecastException(cityAndCountry)

                    // Use the third weather condition as representative for the whole day
                    // and if not available, then use the first one.
                    val dayWeatherForecast: ThreeHoursWeatherForecast = if (dayForecasts.size > 2) dayForecasts[2] else firstForecast
                    val description = dayWeatherForecast.weather?.get(0)?.description ?: throw GetForecastException(cityAndCountry)
                    val icon = dayWeatherForecast.weather.get(0).icon ?: throw GetForecastException(cityAndCountry)

                    result.add(
                            DayForecast(
                                    dayName,
                                    description,
                                    if (isTemperatureFound) minTemperature else null,
                                    if (isTemperatureFound) maxTemperature else null,
                                    icon))
                }
            }
        }

        return@asyncAwait result
    }

    private fun getForecastsGroupedByDay(forecasts: List<ThreeHoursWeatherForecast>): Map<String, List<ThreeHoursWeatherForecast>> {
        return forecasts
                .filter { it.dt != null }
                .groupBy { getDayId(it.dt!! * 1000) }
    }

    private fun getDayId(utcTimeMillis: Long): String {
        return dayIdFormat.format(Date(utcTimeMillis))
    }

    private fun getDayName(utcTimeMillis: Long): String {
        return dayNameFormat.format(Date(utcTimeMillis))
    }
}