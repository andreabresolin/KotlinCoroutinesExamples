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

package andreabresolin.kotlincoroutinesexamples.home.di

import andreabresolin.kotlincoroutinesexamples.app.di.scopes.PerPresenter
import andreabresolin.kotlincoroutinesexamples.app.repository.WeatherRepository
import andreabresolin.kotlincoroutinesexamples.home.domain.GetAverageTemperatureInCitiesUseCase
import andreabresolin.kotlincoroutinesexamples.home.domain.GetCurrentWeatherUseCase
import dagger.Module
import dagger.Provides

@Module
class HomeModule constructor() {

    @Provides
    @PerPresenter
    internal fun provideGetCurrentWeatherUseCase(weatherRepository: WeatherRepository) : GetCurrentWeatherUseCase {
        return GetCurrentWeatherUseCase(weatherRepository)
    }

    @Provides
    @PerPresenter
    internal fun provideGetAverageTemperatureInCitiesUseCase(weatherRepository: WeatherRepository) : GetAverageTemperatureInCitiesUseCase {
        return GetAverageTemperatureInCitiesUseCase(weatherRepository)
    }
}