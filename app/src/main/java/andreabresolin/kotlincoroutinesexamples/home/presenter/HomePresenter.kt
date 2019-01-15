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

package andreabresolin.kotlincoroutinesexamples.home.presenter

import andreabresolin.kotlincoroutinesexamples.app.model.CityWeather
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver

interface HomePresenter<ViewInterface> {
    val weather: List<CityWeather>

    fun attachView(view: ViewInterface, viewLifecycle: Lifecycle)
    fun getLifecycleObserver(): LifecycleObserver
    fun isWeatherLoaded(): Boolean
    fun getWeatherSequential()
    fun getWeatherParallel()
    fun getWeatherIndependent()
    fun getWeatherWithRetry()
    fun getAverageTemperature()
}