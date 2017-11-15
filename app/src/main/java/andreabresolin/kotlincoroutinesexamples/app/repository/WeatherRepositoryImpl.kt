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

package andreabresolin.kotlincoroutinesexamples.app.repository

import andreabresolin.kotlincoroutinesexamples.BuildConfig
import andreabresolin.kotlincoroutinesexamples.app.network.api.WeatherApi
import andreabresolin.kotlincoroutinesexamples.app.network.model.CurrentWeather
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(private val weatherApi: WeatherApi) : WeatherRepository {
    override fun getCurrentWeather(cityAndCountry: String): CurrentWeather? {
        return weatherApi
                .getCurrentWeather(cityAndCountry, "metric", BuildConfig.WEATHER_API_APP_ID)
                .execute()
                .body()
    }
}