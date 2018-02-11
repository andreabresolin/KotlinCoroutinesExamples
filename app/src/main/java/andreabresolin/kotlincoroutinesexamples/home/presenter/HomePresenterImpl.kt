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
import andreabresolin.kotlincoroutinesexamples.home.domain.GetAverageTemperatureInCitiesUseCase
import andreabresolin.kotlincoroutinesexamples.home.domain.GetCurrentWeatherUseCase
import andreabresolin.kotlincoroutinesexamples.home.domain.GetCurrentWeatherUseCase.GetCurrentWeatherException
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView.WeatherRetrievalErrorDialogResponse.CANCEL
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView.WeatherRetrievalErrorDialogResponse.RETRY
import android.util.Log
import javax.inject.Inject

class HomePresenterImpl : BasePresenterImpl<HomeView>(), HomePresenter<HomeView> {

    companion object {
        private val CITIES: Array<City> = arrayOf(
                City("London", "uk"),
                City("Venice", "it"),
                City("New York", "us"))
    }

    private val citiesWeather: MutableList<CityWeather> = mutableListOf()

    @Inject
    internal lateinit var getCurrentWeatherUseCase: GetCurrentWeatherUseCase
    @Inject
    internal lateinit var getAverageTemperatureInCitiesUseCase: GetAverageTemperatureInCitiesUseCase

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
            launchAsync {
                view().injectDependencies(it)
            }
        }
    }

    override fun cleanup() {
        getCurrentWeatherUseCase.cleanup()
        getAverageTemperatureInCitiesUseCase.cleanup()
        super.cleanup()
    }

    override fun onCleared() {
        Log.d("HomePresenterImpl", "onCleared()")
        cleanup()
        super.onCleared()
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

    private suspend fun getCurrentWeatherForCity(index: Int) {
        updateCityWeather(index, LoadingCityWeather)
        updateCityWeather(index, getCurrentWeatherUseCase.execute(CITIES[index].cityAndCountry))
    }

    override fun getCurrentWeatherSequential() {
        launchAsyncTryCatch({
            clearAllCitiesWeather()

            for (i in CITIES.indices) {
                getCurrentWeatherForCity(i)
            }
        }, {
            when (it) {
                is GetCurrentWeatherException -> view().displayWeatherRetrievalErrorDialog(it.cityAndCountry)
                else -> view().displayWeatherRetrievalGenericError()
            }
        })
    }

    override fun getCurrentWeatherParallel() {
        launchAsync {
            clearAllCitiesWeather()
        }

        for (i in CITIES.indices) {
            launchAsyncTryCatch({
                getCurrentWeatherForCity(i)
            }, {
                when (it) {
                    is GetCurrentWeatherException -> view().displayWeatherRetrievalErrorDialog(it.cityAndCountry)
                    else -> view().displayWeatherRetrievalGenericError()
                }
            })
        }
    }

    override fun getCurrentWeatherForCityWithRetry() {
        launchAsync {
            view().updateAllCities()
            getCurrentWeatherForCityWithRetry(City("VeniceWrong", "it"))
        }
    }

    private fun getCurrentWeatherForCityWithRetry(city: City) {
        launchAsyncTryCatch ({
            updateCityWeather(1, LoadingCityWeather)
            updateCityWeather(1, getCurrentWeatherUseCase.execute(city.cityAndCountry))
        }, {
            when (it) {
                is GetCurrentWeatherException -> {
                    updateCityWeather(1, UnknownCityWeather)

                    when (view().displayWeatherRetrievalErrorDialogWithRetry(it.cityAndCountry)) {
                        RETRY -> getCurrentWeatherForCityWithRetry(CITIES[1])
                        CANCEL -> {
                            updateCityWeather(1, UnknownCityWeather)
                        }
                    }
                }
                else -> view().displayWeatherRetrievalGenericError()
            }
        })
    }

    override fun getAverageTemperatureInCities() {
        launchAsync {
            val citiesAndCountries: List<String> = CITIES.map { it.cityAndCountry }
            val averageTemperature: Double = getAverageTemperatureInCitiesUseCase.execute(citiesAndCountries)
            view().displayAverageTemperature(averageTemperature)
        }
    }
}