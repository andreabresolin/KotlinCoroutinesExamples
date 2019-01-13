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

package andreabresolin.kotlincoroutinesexamples.home.domain

import andreabresolin.kotlincoroutinesexamples.app.coroutines.AsyncTasksManager
import andreabresolin.kotlincoroutinesexamples.app.domain.BaseUseCase
import andreabresolin.kotlincoroutinesexamples.app.network.model.CurrentWeather
import andreabresolin.kotlincoroutinesexamples.app.repository.WeatherRepository
import kotlinx.coroutines.Deferred
import javax.inject.Inject

class GetAverageTemperatureUseCase
@Inject constructor(asyncTasksManager: AsyncTasksManager, private val weatherRepository: WeatherRepository) : BaseUseCase(asyncTasksManager) {

    suspend fun execute(citiesAndCountries: List<String>): Double {
        return citiesAndCountries
                .map { getCityWeather(it) }
                .map { getCityTemperature(it.await()) }
                .average()
    }

    private suspend fun getCityWeather(cityAndCountry: String): Deferred<CurrentWeather?> = async {
        weatherRepository.getCurrentWeather(cityAndCountry)
    }

    private fun getCityTemperature(weather: CurrentWeather?): Double {
        return weather?.main?.temp ?: 0.0
    }
}