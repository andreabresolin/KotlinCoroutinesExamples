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

package andreabresolin.kotlincoroutinesexamples.app.components

import andreabresolin.kotlincoroutinesexamples.R
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_city_weather_view.view.*

class CityWeatherView : FrameLayout {

    companion object {
        private const val CONTENT_CHILD_INDEX = 0
        private const val LOADING_CHILD_INDEX = 1
    }

    var cityName: String?
        get() = cityNameText.text.toString()
        set(value) {
            cityNameText.text = value
        }

    var temperature: String?
        get() = temperatureText.text.toString()
        set(value) {
            temperatureText.text = value
        }

    private var _weatherIconUrl: String? = null
    var weatherIconUrl: String?
        get() = _weatherIconUrl
        set(value) {
            if (!isInEditMode && !value.isNullOrBlank()) {
                Glide.with(this).load(value).into(weatherIconImage)
            } else {
                Glide.with(this).load(R.drawable.ic_broken_image).into(weatherIconImage)
            }
        }

    var isLoading: Boolean
        get() = viewFlipper.displayedChild == LOADING_CHILD_INDEX
        set(value) {
            viewFlipper.displayedChild = if (value) LOADING_CHILD_INDEX else CONTENT_CHILD_INDEX
        }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        inflate(context, R.layout.layout_city_weather_view, this)

        val a = context.obtainStyledAttributes(
                attrs, R.styleable.CityWeatherView, defStyle, 0)

        cityName = a.getString(R.styleable.CityWeatherView_cityName) ?: "-"
        temperature = a.getString(R.styleable.CityWeatherView_temperature) ?: "-.-"
        weatherIconUrl = a.getString(R.styleable.CityWeatherView_weatherIconUrl)
        isLoading = a.getBoolean(R.styleable.CityWeatherView_isLoading, false)

        a.recycle()
    }
}
