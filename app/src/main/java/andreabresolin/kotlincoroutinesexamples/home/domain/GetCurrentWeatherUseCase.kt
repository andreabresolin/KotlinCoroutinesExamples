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

package andreabresolin.kotlincoroutinesexamples.home.domain

import andreabresolin.kotlincoroutinesexamples.app.domain.BaseUseCase
import andreabresolin.kotlincoroutinesexamples.app.model.CityWeather
import andreabresolin.kotlincoroutinesexamples.app.model.LoadedCityWeather
import andreabresolin.kotlincoroutinesexamples.app.network.model.CurrentWeather
import andreabresolin.kotlincoroutinesexamples.app.repository.WeatherRepository
import kotlinx.coroutines.experimental.delay
import java.util.*

class GetCurrentWeatherUseCase constructor(
        private val weatherRepository: WeatherRepository) : BaseUseCase() {

    class GetCurrentWeatherException constructor(val cityAndCountry: String) : RuntimeException()

    suspend fun execute(cityAndCountry: String): CityWeather {
        val weather: CurrentWeather? = asyncAwait {
            delay(1000 + Random().nextInt(3000).toLong()) // Random delay used to simulate a slow network connection
            weatherRepository.getCurrentWeather(cityAndCountry)
        }

        return LoadedCityWeather(
                weather?.name ?: throw GetCurrentWeatherException(cityAndCountry),
                weather.weather?.get(0)?.description ?: throw GetCurrentWeatherException(cityAndCountry),
                weather.main?.temp ?: throw GetCurrentWeatherException(cityAndCountry),
                weather.weather.get(0)?.icon)
    }
}