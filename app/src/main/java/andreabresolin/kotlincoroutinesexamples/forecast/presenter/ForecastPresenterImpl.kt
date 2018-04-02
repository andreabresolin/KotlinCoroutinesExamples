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

package andreabresolin.kotlincoroutinesexamples.forecast.presenter

import andreabresolin.kotlincoroutinesexamples.app.App
import andreabresolin.kotlincoroutinesexamples.app.model.*
import andreabresolin.kotlincoroutinesexamples.app.presenter.BasePresenterImpl
import andreabresolin.kotlincoroutinesexamples.forecast.di.ForecastComponent
import andreabresolin.kotlincoroutinesexamples.forecast.di.ForecastModule
import andreabresolin.kotlincoroutinesexamples.forecast.domain.GetForecastUseCase
import andreabresolin.kotlincoroutinesexamples.forecast.domain.GetForecastUseCase.GetForecastException
import andreabresolin.kotlincoroutinesexamples.forecast.view.ForecastView
import andreabresolin.kotlincoroutinesexamples.forecast.view.ForecastView.ErrorDialogResponse
import andreabresolin.kotlincoroutinesexamples.forecast.view.ForecastView.ErrorDialogResponse.CANCEL
import andreabresolin.kotlincoroutinesexamples.forecast.view.ForecastView.ErrorDialogResponse.RETRY
import javax.inject.Inject

class ForecastPresenterImpl : BasePresenterImpl<ForecastView>(), ForecastPresenter<ForecastView> {

    @Inject
    internal lateinit var getForecastUseCase: GetForecastUseCase

    private val daysForecast: MutableList<DayForecast> = mutableListOf()
    private var forecastComponent: ForecastComponent? = null

    init {
        injectDependencies()
    }

    override fun onInjectDependencies() {
        forecastComponent = App.get()
                .getAppComponent()
                ?.plus(ForecastModule())

        forecastComponent?.inject(this)
    }

    override fun onViewAttached(view: ForecastView) {
        forecastComponent?.let { view.injectDependencies(it) }
    }

    override fun cleanup() {
        getForecastUseCase.cleanup()
        super.cleanup()
    }

    override val forecasts: List<DayForecast>
        get() = daysForecast

    override fun loadForecasts(city: City) {
        launchAsyncTryCatch ({
            view().displayLoadingState()

            val forecasts = getForecastUseCase.execute(city.cityAndCountry)

            daysForecast.clear()
            daysForecast.addAll(forecasts)

            view().updateAllForecasts()

            if (forecasts.isEmpty()) {
                view().displayNoDataState()
            } else {
                view().displayContentState()
            }
        }, { error ->
            daysForecast.clear()

            view().updateAllForecasts()
            view().displayContentState()

            val place = (error as? GetForecastException)?.cityAndCountry

            when (view().stickySuspension<ErrorDialogResponse> { displayLoadForecastsErrorWithRetry(it, place) }) {
                RETRY -> loadForecasts(city)
                CANCEL -> view().displayErrorState()
            }
        })
    }
}