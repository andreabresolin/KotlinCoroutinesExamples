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
import andreabresolin.kotlincoroutinesexamples.app.model.City
import andreabresolin.kotlincoroutinesexamples.app.model.CityWeather
import andreabresolin.kotlincoroutinesexamples.app.model.LoadingCityWeather
import andreabresolin.kotlincoroutinesexamples.app.model.UnknownCityWeather
import andreabresolin.kotlincoroutinesexamples.app.presenter.BasePresenterImpl
import andreabresolin.kotlincoroutinesexamples.home.di.HomeComponent
import andreabresolin.kotlincoroutinesexamples.home.di.HomeModule
import andreabresolin.kotlincoroutinesexamples.home.domain.GetAverageTemperatureUseCase
import andreabresolin.kotlincoroutinesexamples.home.domain.GetWeatherUseCase
import andreabresolin.kotlincoroutinesexamples.home.domain.GetWeatherUseCase.GetWeatherException
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView.ErrorDialogResponse
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView.ErrorDialogResponse.CANCEL
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView.ErrorDialogResponse.RETRY
import javax.inject.Inject

class HomePresenterImpl : BasePresenterImpl<HomeView>(), HomePresenter<HomeView> {

    companion object {
        private val CITIES: Array<City> = arrayOf(
                City("London", "uk"),
                City("Venice", "it"),
                City("New York", "us"))
    }

    @Inject
    internal lateinit var getWeatherUseCase: GetWeatherUseCase
    @Inject
    internal lateinit var getAverageTemperatureUseCase: GetAverageTemperatureUseCase

    private val citiesWeather: MutableList<CityWeather> = mutableListOf()
    private var homeComponent: HomeComponent? = null

    init {
        injectDependencies()
        initCitiesWeather()
    }

    private fun injectDependencies() {
        homeComponent = App.get()
                .getAppComponent()
                ?.plus(HomeModule())

        homeComponent?.inject(this)
    }

    override fun onViewAttached(view: HomeView) {
        homeComponent?.let {
            launchAsync { view().injectDependencies(it) }
        }
    }

    override fun cleanup() {
        getWeatherUseCase.cleanup()
        getAverageTemperatureUseCase.cleanup()
        super.cleanup()
    }

    private fun initCitiesWeather() {
        CITIES.forEach {
            citiesWeather.add(UnknownCityWeather)
        }
    }

    override fun getCitiesWeather(): MutableList<CityWeather> {
        return citiesWeather
    }

    private suspend fun clearAllCitiesWeather() {
        for (i in CITIES.indices) {
            citiesWeather[i] = UnknownCityWeather
        }

        view().updateAllCities()
    }

    private suspend fun updateCityWeather(cityIndex: Int, cityWeather: CityWeather) {
        citiesWeather[cityIndex] = cityWeather
        view().updateCity(cityIndex)
    }

    private suspend fun getWeatherForCity(index: Int) {
        updateCityWeather(index, LoadingCityWeather)
        updateCityWeather(index, getWeatherUseCase.execute(CITIES[index].cityAndCountry))
    }

    override fun getWeatherSequential() {
        launchAsyncTryCatch({
            clearAllCitiesWeather()

            for (i in CITIES.indices) {
                getWeatherForCity(i)
            }
        }, {
            when (it) {
                is GetWeatherException -> view().displayGetWeatherError(it.cityAndCountry)
                else -> view().displayGetWeatherError()
            }
        })
    }

    override fun getWeatherParallel() {
        launchAsync {
            clearAllCitiesWeather()
        }

        for (i in CITIES.indices) {
            launchAsyncTryCatch({
                getWeatherForCity(i)
            }, {
                when (it) {
                    is GetWeatherException -> view().displayGetWeatherError(it.cityAndCountry)
                    else -> view().displayGetWeatherError()
                }
            })
        }
    }

    override fun getWeatherWithRetry() {
        launchAsync {
            view().updateAllCities()
            getCurrentWeatherForCityWithRetry(City("VeniceWrong", "it"))
        }
    }

    private fun getCurrentWeatherForCityWithRetry(city: City) {
        launchAsyncTryCatch ({
            updateCityWeather(1, LoadingCityWeather)
            updateCityWeather(1, getWeatherUseCase.execute(city.cityAndCountry))
        }, {
            val error = it
            when (error) {
                is GetWeatherException -> {
                    updateCityWeather(1, UnknownCityWeather)

                    when (view().stickySuspension<ErrorDialogResponse> { displayGetWeatherErrorWithRetry(it, error.cityAndCountry) }) {
                        RETRY -> getCurrentWeatherForCityWithRetry(CITIES[1])
                        CANCEL -> {
                            updateCityWeather(1, UnknownCityWeather)
                        }
                    }
                }
                else -> view().displayGetWeatherError()
            }
        })
    }

    override fun getAverageTemperatureInCities() {
        launchAsync {
            val citiesAndCountries: List<String> = CITIES.map { it.cityAndCountry }
            val averageTemperature: Double = getAverageTemperatureUseCase.execute(citiesAndCountries)
            view().displayAverageTemperature(averageTemperature)
        }
    }
}