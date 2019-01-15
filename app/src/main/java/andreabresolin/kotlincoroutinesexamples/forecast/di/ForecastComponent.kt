/*
 *  Copyright 2018-2019 Andrea Bresolin
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

package andreabresolin.kotlincoroutinesexamples.forecast.di

import andreabresolin.kotlincoroutinesexamples.app.di.scopes.PerScreen
import andreabresolin.kotlincoroutinesexamples.forecast.presenter.ForecastPresenter
import andreabresolin.kotlincoroutinesexamples.forecast.view.ForecastActivity
import andreabresolin.kotlincoroutinesexamples.forecast.view.ForecastView
import dagger.Subcomponent

@PerScreen
@Subcomponent(modules = [(ForecastModule::class)])
interface ForecastComponent {
    fun inject(forecastPresenter: ForecastPresenter<ForecastView>)
    fun inject(forecastActivity: ForecastActivity)
}