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
import andreabresolin.kotlincoroutinesexamples.home.domain.GetAverageTemperatureUseCase
import andreabresolin.kotlincoroutinesexamples.home.domain.GetWeatherUseCase
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView.ErrorDialogResponse.CANCEL
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
    private lateinit var mockGetWeatherUseCase: GetWeatherUseCase
    @Mock
    private lateinit var mockGetAverageTemperatureUseCase: GetAverageTemperatureUseCase;

    @InjectMocks
    private lateinit var subject: HomePresenterImpl

    @Test
    fun getAverageTemperatureInCities_displaysAverageTemperature() {
        runBlocking {
            // Given
            val givenAverageTemperature = 21.5
            whenever(mockGetAverageTemperatureUseCase.execute(anyList())).thenReturn(givenAverageTemperature)

            // When
            subject.getAverageTemperatureInCities()

            // Then
            verify(mockView).updateAllCities()
            verify(mockView).displayAverageTemperature(givenAverageTemperature)
        }
    }

    @Test
    fun getCurrentWeatherForCityWithRetry_displaysWeatherForCity() {
        runBlocking {
            // Given
            val givenCity = City("VeneziaWrong", "it")
            val givenWeather = CityWeather("sunny", 22.5)
            whenever(mockGetWeatherUseCase.execute(givenCity.cityAndCountry)).thenReturn(givenWeather)

            // When
            subject.getWeatherWithRetry()

            // Then
            verify(mockView).updateAllCities()
            verify(mockView).updateCity(anyInt())
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
            val givenException = GetWeatherUseCase.GetWeatherException(givenCity.cityAndCountry)
            whenever(mockGetWeatherUseCase.execute(givenCity.cityAndCountry)).thenThrow(givenException)
            whenever(mockView.displayGetWeatherErrorWithRetry(givenException.cityAndCountry)).thenReturn(CANCEL)

            // When
            subject.getWeatherWithRetry()

            // Then
            verify(mockView).updateAllCities()
            verify(mockView).updateCity(anyInt())
            verify(mockView).displayGetWeatherErrorWithRetry(givenException.cityAndCountry)
            verify(mockView).displayCanceledForCity(ArgumentMatchers.anyInt())
        }
    }
}