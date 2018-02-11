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

import andreabresolin.kotlincoroutinesexamples.home.di.HomeComponent

interface HomeView {
    enum class WeatherRetrievalErrorDialogResponse {
        RETRY, CANCEL
    }

    fun injectDependencies(homeComponent: HomeComponent)
    fun updateAllCities()
    fun updateCity(cityIndex: Int)
    fun displayAverageTemperature(averageTemperature: Double)
    fun displayWeatherRetrievalErrorDialog(place: String)
    suspend fun displayWeatherRetrievalErrorDialogWithRetry(place: String): WeatherRetrievalErrorDialogResponse
    fun displayWeatherRetrievalGenericError()
}