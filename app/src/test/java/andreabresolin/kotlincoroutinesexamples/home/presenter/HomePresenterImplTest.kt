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

import andreabresolin.kotlincoroutinesexamples.app.model.City
import andreabresolin.kotlincoroutinesexamples.app.model.CityWeather
import andreabresolin.kotlincoroutinesexamples.home.domain.GetAverageTemperatureInCitiesUseCase
import andreabresolin.kotlincoroutinesexamples.home.domain.GetCurrentWeatherUseCase
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView.WeatherRetrievalErrorDialogResponse.CANCEL
import andreabresolin.kotlincoroutinesexamples.testutils.KotlinTestsUtils.Companion.eqString
import andreabresolin.kotlincoroutinesexamples.testutils.KotlinTestsUtils.Companion.whenever
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class HomePresenterImplTest {

    @Mock
    private lateinit var mockView: HomeView
    @Mock
    private lateinit var mockGetCurrentWeatherUseCase: GetCurrentWeatherUseCase
    @Mock
    private lateinit var mockGetAverageTemperatureInCitiesUseCase: GetAverageTemperatureInCitiesUseCase;

    @InjectMocks
    private lateinit var subject: HomePresenterImpl

    @Test
    fun getAverageTemperatureInCities_displaysAverageTemperature() {
        runBlocking {
            // Given
            val givenAverageTemperature = 21.5
            whenever(mockGetAverageTemperatureInCitiesUseCase.execute(anyList())).thenReturn(givenAverageTemperature)

            // When
            subject.getAverageTemperatureInCities()

            // Then
            verify(mockView).clearAllCities()
            verify(mockView).displayAverageTemperature(givenAverageTemperature)
        }
    }

    @Test
    fun getCurrentWeatherForCityWithRetry_displaysWeatherForCity() {
        runBlocking {
            // Given
            val givenCity = City("VeneziaWrong", "it")
            val givenWeather = CityWeather("sunny", 22.5)
            whenever(mockGetCurrentWeatherUseCase.execute(givenCity.cityAndCountry)).thenReturn(givenWeather)

            // When
            subject.getCurrentWeatherForCityWithRetry()

            // Then
            verify(mockView).clearAllCities()
            verify(mockView).displayInProgressForCity(anyInt())
            verify(mockView).displayWeatherForCity(
                    anyInt(),
                    eqString(givenCity.cityName),
                    eqString(givenWeather.description),
                    eq(givenWeather.temperature))
        }
    }

    @Test
    fun getCurrentWeatherForCityWithRetry_andGetCurrentWeatherException_andCancel_displaysCanceledForCity() {
        runBlocking {
            // Given
            val givenCity = City("VeneziaWrong", "it")
            val givenException = GetCurrentWeatherUseCase.GetCurrentWeatherException(givenCity.cityAndCountry)
            whenever(mockGetCurrentWeatherUseCase.execute(givenCity.cityAndCountry)).thenThrow(givenException)
            whenever(mockView.displayWeatherRetrievalErrorDialogWithRetry(givenException.cityAndCountry)).thenReturn(CANCEL)

            // When
            subject.getCurrentWeatherForCityWithRetry()

            // Then
            verify(mockView).clearAllCities()
            verify(mockView).displayInProgressForCity(anyInt())
            verify(mockView).displayWeatherRetrievalErrorDialogWithRetry(givenException.cityAndCountry)
            verify(mockView).displayCanceledForCity(ArgumentMatchers.anyInt())
        }
    }
}