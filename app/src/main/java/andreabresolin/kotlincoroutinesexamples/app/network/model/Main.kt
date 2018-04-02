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

data class Main(
        @SerializedName("temp") val temp: Double?,
        @SerializedName("temp_min") val tempMin: Double?,
        @SerializedName("temp_max") val tempMax: Double?,
        @SerializedName("pressure") val pressure: Double?,
        @SerializedName("sea_level") val seaLevel: Double?,
        @SerializedName("grnd_level") val grndLevel: Double?,
        @SerializedName("humidity") val humidity: Int?,
        @SerializedName("temp_kf") val tempKf: Double?)