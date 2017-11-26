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

package andreabresolin.kotlincoroutinesexamples.app.network.model

import com.google.gson.annotations.SerializedName

data class CurrentWeather(
        @SerializedName("coord") val coord: Coord?,
        @SerializedName("weather") val weather: List<Weather?>?,
        @SerializedName("base") val base: String?,
        @SerializedName("main") val main: Main?,
        @SerializedName("wind") val wind: Wind?,
        @SerializedName("clouds") val clouds: Clouds?,
        @SerializedName("rain") val rain: Rain?,
        @SerializedName("dt") val dt: Int?,
        @SerializedName("sys") val sys: Sys?,
        @SerializedName("id") val id: Int?,
        @SerializedName("name") val name: String?,
        @SerializedName("cod") val cod: Int?
)

data class Weather(
        @SerializedName("id") val id: Int?,
        @SerializedName("main") val main: String?,
        @SerializedName("description") val description: String?,
        @SerializedName("icon") val icon: String?
)

data class Rain(
        @SerializedName("3h") val h: Double?
)

data class Wind(
        @SerializedName("speed") val speed: Double?,
        @SerializedName("deg") val deg: Double?
)

data class Coord(
        @SerializedName("lon") val lon: Double?,
        @SerializedName("lat") val lat: Double?
)

data class Sys(
        @SerializedName("type") val type: Int?,
        @SerializedName("id") val id: Int?,
        @SerializedName("message") val message: Double?,
        @SerializedName("country") val country: String?,
        @SerializedName("sunrise") val sunrise: Int?,
        @SerializedName("sunset") val sunset: Int?
)

data class Main(
        @SerializedName("temp") val temp: Double?,
        @SerializedName("pressure") val pressure: Int?,
        @SerializedName("humidity") val humidity: Int?,
        @SerializedName("temp_min") val tempMin: Double?,
        @SerializedName("temp_max") val tempMax: Double?
)

data class Clouds(
        @SerializedName("all") val all: Int?
)