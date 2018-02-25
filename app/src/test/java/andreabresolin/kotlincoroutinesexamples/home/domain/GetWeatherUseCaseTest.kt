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

package andreabresolin.kotlincoroutinesexamples.home.domain

import andreabresolin.kotlincoroutinesexamples.app.model.City
import andreabresolin.kotlincoroutinesexamples.app.model.LoadedCityWeather
import andreabresolin.kotlincoroutinesexamples.app.repository.WeatherRepository
import andreabresolin.kotlincoroutinesexamples.testutils.KotlinTestsUtils.Companion.whenever
import andreabresolin.kotlincoroutinesexamples.testutils.Stubs
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GetWeatherUseCaseTest {

    @Mock
    private lateinit var mockWeatherRepository: WeatherRepository

    private lateinit var subject: GetWeatherUseCase

    @Before
    fun before() {
        subject = GetWeatherUseCase(mockWeatherRepository)
    }

    @Test
    fun execute_returnsWeatherForCity() {
        runBlocking {
            // Given
            val givenCity = City("New York", "us")

            whenever(mockWeatherRepository.getCurrentWeather(givenCity.cityAndCountry)).thenReturn(Stubs.STUB_WEATHER_NEW_YORK)

            // When
            val givenResult: LoadedCityWeather = subject.execute(givenCity.cityAndCountry) as LoadedCityWeather

            // Then
            verify(mockWeatherRepository).getCurrentWeather(givenCity.cityAndCountry)
            assertThat(givenResult.cityName).isEqualTo(Stubs.STUB_WEATHER_NEW_YORK.name)
            assertThat(givenResult.description).isEqualTo(Stubs.STUB_WEATHER_NEW_YORK.weather?.get(0)?.description)
            assertThat(givenResult.temperature).isEqualTo(Stubs.STUB_WEATHER_NEW_YORK.main?.temp)
            assertThat(givenResult.icon).isEqualTo(Stubs.STUB_WEATHER_NEW_YORK.weather?.get(0)?.icon)
        }
    }
}