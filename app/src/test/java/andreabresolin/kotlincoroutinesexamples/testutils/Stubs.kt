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

package andreabresolin.kotlincoroutinesexamples.testutils

import andreabresolin.kotlincoroutinesexamples.app.network.model.*

interface Stubs {
    companion object {
        val STUB_WEATHER_NEW_YORK = CurrentWeather(
                Coord(-74.01, 40.71),
                listOf(Weather(800, "Clear", "clear sky", "01d")),
                "stations",
                Main(7.0, 1030, 39, 6.0, 8.0),
                Wind(1.02, 203.501),
                Clouds(1),
                Rain(1),
                1510521300,
                Sys(1, 1969, 0.0045, "US", 1510486839, 1510522774),
                5128581,
                "New York",
                200
        )
    }
}