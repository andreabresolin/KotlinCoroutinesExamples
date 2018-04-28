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

package andreabresolin.kotlincoroutinesexamples.home.presenter

import andreabresolin.kotlincoroutinesexamples.app.App
import andreabresolin.kotlincoroutinesexamples.app.coroutines.CoroutinesManager
import andreabresolin.kotlincoroutinesexamples.app.model.City
import andreabresolin.kotlincoroutinesexamples.app.model.CityWeather
import andreabresolin.kotlincoroutinesexamples.app.model.LoadingCityWeather
import andreabresolin.kotlincoroutinesexamples.app.model.UnknownCityWeather
import andreabresolin.kotlincoroutinesexamples.app.presenter.BasePresenterImpl
import andreabresolin.kotlincoroutinesexamples.home.di.HomeModule
import andreabresolin.kotlincoroutinesexamples.home.domain.GetAverageTemperatureUseCase
import andreabresolin.kotlincoroutinesexamples.home.domain.GetWeatherUseCase
import andreabresolin.kotlincoroutinesexamples.home.domain.GetWeatherUseCase.GetWeatherException
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView.ErrorDialogResponse
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView.ErrorDialogResponse.CANCEL
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView.ErrorDialogResponse.RETRY
import javax.inject.Inject

class HomePresenterImpl
constructor(coroutinesManager: CoroutinesManager) : BasePresenterImpl<HomeView>(coroutinesManager), HomePresenter<HomeView> {

    companion object {
        private val CITIES: List<City> = listOf(
                City("London", "uk"),
                City("Venice", "it"),
                City("New York", "us"))
    }

    @Inject
    internal lateinit var getWeatherUseCase: GetWeatherUseCase
    @Inject
    internal lateinit var getAverageTemperatureUseCase: GetAverageTemperatureUseCase

    private val citiesWeather: MutableList<CityWeather> = mutableListOf()

    init {
        injectDependencies()
        initCitiesWeather()
    }

    override fun onInjectDependencies() {
        App.get().getAppComponent()?.plus(HomeModule())?.inject(this)
    }

    override fun cleanup() {
        getWeatherUseCase.cleanup()
        getAverageTemperatureUseCase.cleanup()
        super.cleanup()
    }

    override val weather: List<CityWeather>
        get() = citiesWeather

    private fun initCitiesWeather() {
        CITIES.forEach { citiesWeather.add(UnknownCityWeather) }
    }

    override fun isWeatherLoaded(): Boolean {
        return !citiesWeather.all { it === UnknownCityWeather }
    }

    private suspend fun updateCityWeather(cityIndex: Int, cityWeather: CityWeather) {
        citiesWeather[cityIndex] = cityWeather
        view().updateCity(cityIndex)
    }

    override fun getWeatherSequential() = launchOnUITryCatch({
        CITIES.forEachIndexed { index, city ->
            updateCityWeather(index, LoadingCityWeather)
            updateCityWeather(index, getWeatherUseCase.execute(city))
        }
    }, {
        when (it) {
            is GetWeatherException -> view().displayGetWeatherError(it.cityAndCountry)
            else -> view().displayGetWeatherError()
        }
    })

    override fun getWeatherParallel() = launchOnUITryCatch({
        CITIES.indices.forEach { updateCityWeather(it, LoadingCityWeather) }

        val citiesWeather: List<CityWeather> = getWeatherUseCase.execute(CITIES)

        citiesWeather.forEachIndexed { index, cityWeather -> updateCityWeather(index, cityWeather) }
    }, {
        when (it) {
            is GetWeatherException -> view().displayGetWeatherError(it.cityAndCountry)
            else -> view().displayGetWeatherError()
        }
    })

    override fun getWeatherIndependent() {
        launchOnUI {
            CITIES.indices.forEach { updateCityWeather(it, LoadingCityWeather) }
        }

        CITIES.forEachIndexed { index, city ->
            launchOnUITryCatch({
                updateCityWeather(index, LoadingCityWeather)
                updateCityWeather(index, getWeatherUseCase.execute(city))
            }, {
                updateCityWeather(index, UnknownCityWeather)
            })
        }
    }

    override fun getWeatherWithRetry() {
        getWeatherWithRetry(City("VeniceWrong", "it"))
    }

    private fun getWeatherWithRetry(city: City) {
        launchOnUITryCatch ({
            updateCityWeather(1, LoadingCityWeather)
            updateCityWeather(1, getWeatherUseCase.execute(city))
        }, {
            val error = it
            when (error) {
                is GetWeatherException -> {
                    updateCityWeather(1, UnknownCityWeather)

                    when (view().stickySuspension<ErrorDialogResponse> { displayGetWeatherErrorWithRetry(it, error.cityAndCountry) }) {
                        RETRY -> getWeatherWithRetry(CITIES[1])
                        CANCEL -> updateCityWeather(1, UnknownCityWeather)
                    }
                }
                else -> view().displayGetWeatherError()
            }
        })
    }

    override fun getAverageTemperature() = launchOnUI {
        val citiesAndCountries: List<String> = CITIES.map { it.cityAndCountry }
        val averageTemperature: Double = getAverageTemperatureUseCase.execute(citiesAndCountries)
        view().displayAverageTemperature(averageTemperature)
    }
}