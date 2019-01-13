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
import andreabresolin.kotlincoroutinesexamples.app.model.City
import andreabresolin.kotlincoroutinesexamples.app.model.CityWeather
import andreabresolin.kotlincoroutinesexamples.app.model.LoadedCityWeather
import andreabresolin.kotlincoroutinesexamples.app.network.model.CurrentWeather
import andreabresolin.kotlincoroutinesexamples.app.repository.WeatherRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.delay
import java.util.*
import javax.inject.Inject

class GetWeatherUseCase
@Inject constructor(asyncTasksManager: AsyncTasksManager, private val weatherRepository: WeatherRepository) : BaseUseCase(asyncTasksManager) {

    class GetWeatherException constructor(val cityAndCountry: String) : RuntimeException()

    suspend fun execute(city: City): CityWeather {
        val weather: CurrentWeather? = asyncAwait {
            simulateSlowNetwork()
            weatherRepository.getCurrentWeather(city.cityAndCountry)
        }

        return mapCurrentWeatherToCityWeather(weather, city)
    }

    suspend fun execute(cities: List<City>): List<CityWeather> {
        return cities
                .map { getCityWeather(it.cityAndCountry) }
                .mapIndexed { index, deferred ->
                    mapCurrentWeatherToCityWeather(deferred.await(), cities[index])
                }
    }

    private suspend fun getCityWeather(cityAndCountry: String): Deferred<CurrentWeather?> = async {
        simulateSlowNetwork()
        weatherRepository.getCurrentWeather(cityAndCountry)
    }

    private suspend fun simulateSlowNetwork() {
        // Random delay used to simulate a slow network connection
        delay(1000 + Random().nextInt(4000).toLong())
    }

    private fun mapCurrentWeatherToCityWeather(weather: CurrentWeather?, city: City): CityWeather {
        return LoadedCityWeather(
                city,
                weather?.weather?.get(0)?.description ?: throw GetWeatherException(city.cityAndCountry),
                weather.main?.temp ?: throw GetWeatherException(city.cityAndCountry),
                weather.weather[0].icon)
    }
}