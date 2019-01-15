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

import andreabresolin.kotlincoroutinesexamples.app.coroutines.CoroutinesManager
import andreabresolin.kotlincoroutinesexamples.app.di.scopes.PerScreen
import andreabresolin.kotlincoroutinesexamples.app.presenter.BasePresenter
import andreabresolin.kotlincoroutinesexamples.app.presenter.DefaultBasePresenter
import andreabresolin.kotlincoroutinesexamples.forecast.view.ForecastView
import dagger.Module
import dagger.Provides

@Module
class ForecastModule {

    @Provides
    @PerScreen
    internal fun provideBasePresenter(coroutinesManager: CoroutinesManager): BasePresenter<ForecastView> {
        return DefaultBasePresenter(coroutinesManager)
    }
}