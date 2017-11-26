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

package andreabresolin.kotlincoroutinesexamples.home.presenter

import andreabresolin.kotlincoroutinesexamples.app.App
import andreabresolin.kotlincoroutinesexamples.app.model.City
import andreabresolin.kotlincoroutinesexamples.app.model.CityWeather
import andreabresolin.kotlincoroutinesexamples.app.presenter.BasePresenter
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

class HomePresenterImpl : BasePresenter<HomeView>(), HomePresenter {
    @Inject
    internal lateinit var getCurrentWeatherUseCase: GetCurrentWeatherUseCase
    @Inject
    internal lateinit var getAverageTemperatureInCitiesUseCase: GetAverageTemperatureInCitiesUseCase

    private var homeComponent: HomeComponent? = null

    companion object {
        private val CITIES: Array<City> = arrayOf(
                City("London", "uk"),
                City("Venezia", "it"),
                City("New York", "us"))
    }

    init {
        injectDependencies()
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

    private suspend fun getCurrentWeatherForCity(index: Int, city: City) {
        Log.d("HomePresenterImpl", "getCurrentWeatherForCity($index)")

        view().displayInProgressForCity(index)
        val weather: CityWeather = getCurrentWeatherUseCase.execute(city.cityAndCountry)
        view().displayWeatherForCity(index, city.cityName, weather.description, weather.temperature)
    }

    override fun getCurrentWeatherSequential() {
        launchAsyncTryCatch({
            view().clearAllCities()

            CITIES.forEachIndexed { index, city ->
                getCurrentWeatherForCity(index, city)
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
            view().clearAllCities()
        }

        CITIES.forEachIndexed { index, city ->
            launchAsyncTryCatch({
                getCurrentWeatherForCity(index, city)
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
            view().clearAllCities()
            getCurrentWeatherForCityWithRetry(City("VeneziaWrong", "it"))
        }
    }

    private fun getCurrentWeatherForCityWithRetry(city: City) {
        launchAsyncTryCatch ({
            view().displayInProgressForCity(0)
            val weather: CityWeather = getCurrentWeatherUseCase.execute(city.cityAndCountry)
            view().displayWeatherForCity(0, city.cityName, weather.description, weather.temperature)
        }, {
            when (it) {
                is GetCurrentWeatherException -> {
                    when (view().displayWeatherRetrievalErrorDialogWithRetry(it.cityAndCountry)) {
                        RETRY -> getCurrentWeatherForCityWithRetry(City("Venezia", "it"))
                        CANCEL -> view().displayCanceledForCity(0)
                    }
                }
                else -> view().displayWeatherRetrievalGenericError()
            }
        })
    }

    override fun getAverageTemperatureInCities() {
        launchAsync {
            view().clearAllCities()
            val citiesAndCountries: List<String> = CITIES.map { it.cityAndCountry }
            val averageTemperature: Double = getAverageTemperatureInCitiesUseCase.execute(citiesAndCountries)
            view().displayAverageTemperature(averageTemperature)
        }
    }
}