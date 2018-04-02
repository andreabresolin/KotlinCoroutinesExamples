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

package andreabresolin.kotlincoroutinesexamples.app.network.model

import com.google.gson.annotations.SerializedName

data class CurrentWeather(
        @SerializedName("coord") val coord: Coord?,
        @SerializedName("weather") val weather: List<Weather>?,
        @SerializedName("base") val base: String?,
        @SerializedName("main") val main: Main?,
        @SerializedName("wind") val wind: Wind?,
        @SerializedName("clouds") val clouds: Clouds?,
        @SerializedName("rain") val rain: Rain?,
        @SerializedName("dt") val dt: Int?,
        @SerializedName("sys") val sys: Sys?,
        @SerializedName("id") val id: Int?,
        @SerializedName("name") val name: String?,
        @SerializedName("cod") val cod: Int?)