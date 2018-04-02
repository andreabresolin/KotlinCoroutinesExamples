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

package andreabresolin.kotlincoroutinesexamples.app.network.api

import andreabresolin.kotlincoroutinesexamples.app.network.model.CurrentWeather
import andreabresolin.kotlincoroutinesexamples.app.network.model.WeatherForecast
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    // Docs: https://openweathermap.org/current
    // Example: http://api.openweathermap.org/data/2.5/weather?q=London,uk&units=metric&appid=f04391f2a7b156421675d08ac24dc908
    @GET("data/2.5/weather")
    fun getCurrentWeather(@Query("q") cityName: String,
                          @Query("units") units: String,
                          @Query("appid") appId: String): Call<CurrentWeather>

    // Docs: https://openweathermap.org/forecast5
    // Example: http://api.openweathermap.org/data/2.5/forecast?q=London,uk&units=metric&appid=f04391f2a7b156421675d08ac24dc908
    @GET("data/2.5/forecast")
    fun getWeatherForecast(@Query("q") cityName: String,
                           @Query("units") units: String,
                           @Query("appid") appId: String): Call<WeatherForecast>
}