/*
 *  Copyright 2017 Andrea Bresolin
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
import andreabresolin.kotlincoroutinesexamples.app.network.model.CurrentWeather
import andreabresolin.kotlincoroutinesexamples.app.repository.WeatherRepository
import kotlinx.coroutines.experimental.Deferred

class GetAverageTemperatureInCitiesUseCase constructor(
        private val weatherRepository: WeatherRepository) : BaseUseCase() {

    suspend fun execute(cities: List<String>): Double {
        return cities
                .map { getCityWeather(it) }
                .map { getCityTemperature(it.await()) }
                .average()
    }

    private suspend fun getCityWeather(city: String): Deferred<CurrentWeather?> {
        return async { weatherRepository.getCurrentWeather(city) }
    }

    private fun getCityTemperature(weather: CurrentWeather?): Double {
        return weather?.main?.temp ?: 0.0
    }
}