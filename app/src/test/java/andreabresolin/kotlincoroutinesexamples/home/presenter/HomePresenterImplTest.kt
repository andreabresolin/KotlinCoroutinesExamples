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

import andreabresolin.kotlincoroutinesexamples.app.model.City
import andreabresolin.kotlincoroutinesexamples.app.model.LoadedCityWeather
import andreabresolin.kotlincoroutinesexamples.app.model.UnknownCityWeather
import andreabresolin.kotlincoroutinesexamples.app.presenter.StickyContinuation
import andreabresolin.kotlincoroutinesexamples.home.domain.GetAverageTemperatureUseCase
import andreabresolin.kotlincoroutinesexamples.home.domain.GetWeatherUseCase
import andreabresolin.kotlincoroutinesexamples.home.domain.GetWeatherUseCase.GetWeatherException
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView.ErrorDialogResponse
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView.ErrorDialogResponse.CANCEL
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView.ErrorDialogResponse.RETRY
import andreabresolin.kotlincoroutinesexamples.testutils.KotlinTestUtils.Companion.eqString
import andreabresolin.kotlincoroutinesexamples.testutils.KotlinTestUtils.Companion.mockContinuation
import andreabresolin.kotlincoroutinesexamples.testutils.KotlinTestUtils.Companion.stubStickyContinuation
import andreabresolin.kotlincoroutinesexamples.testutils.KotlinTestUtils.Companion.whenever
import android.arch.lifecycle.Lifecycle
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
    private lateinit var mockGetAverageTemperatureUseCase: GetAverageTemperatureUseCase

    @InjectMocks
    private lateinit var subject: HomePresenterImpl

    @Before
    fun before() {
        subject.attachView(mockView, mock(Lifecycle::class.java))
    }

    @Test
    fun getAverageTemperature_displaysAverageTemperature() = runBlocking {
        // Given
        val givenAverageTemperature = 21.5

        whenever(mockGetAverageTemperatureUseCase.execute(anyList())).thenReturn(givenAverageTemperature)

        // When
        subject.getAverageTemperature()

        // Then
        verify(mockView).displayAverageTemperature(givenAverageTemperature)
    }

    @Test
    fun getWeatherWithRetry_andRetry_displaysWeatherForCity() = runBlocking {
        // Given
        val givenCityIndex = 1
        val givenWrongCity = City("VeniceWrong", "it")
        val givenRightCity = City("Venice", "it")
        val givenLoadedCityWeather = LoadedCityWeather(givenRightCity, "sunny", 22.5, "icon")
        val givenException = GetWeatherException(givenWrongCity.cityAndCountry)
        val givenStickyContinuation = StickyContinuation<ErrorDialogResponse>(mockContinuation(), subject)
        givenStickyContinuation.resume(RETRY)

        whenever(mockGetWeatherUseCase.execute(givenWrongCity)).thenThrow(givenException)
        whenever(mockGetWeatherUseCase.execute(givenRightCity)).thenReturn(givenLoadedCityWeather)
        whenever(mockView.displayGetWeatherErrorWithRetry(
                stubStickyContinuation(givenStickyContinuation),
                eqString(givenException.cityAndCountry)))

        // When
        subject.getWeatherWithRetry()

        // Then
        val updatedCityWeather = subject.weather[givenCityIndex]
        assertThat(updatedCityWeather is LoadedCityWeather)

        val loadedCityWeather = updatedCityWeather as LoadedCityWeather
        assertThat(loadedCityWeather.city).isEqualTo(givenLoadedCityWeather.city)
        assertThat(loadedCityWeather.description).isEqualTo(givenLoadedCityWeather.description)
        assertThat(loadedCityWeather.temperature).isEqualTo(givenLoadedCityWeather.temperature)
        assertThat(loadedCityWeather.icon).isEqualTo(givenLoadedCityWeather.icon)

        verify(mockView, times(4)).updateCity(givenCityIndex)
    }

    @Test
    fun getWeatherWithRetry_andCancel_displaysCanceledForCity() = runBlocking {
        // Given
        val givenCityIndex = 1
        val givenWrongCity = City("VeniceWrong", "it")
        val givenException = GetWeatherException(givenWrongCity.cityAndCountry)
        val givenStickyContinuation = StickyContinuation<ErrorDialogResponse>(mockContinuation(), subject)
        givenStickyContinuation.resume(CANCEL)

        whenever(mockGetWeatherUseCase.execute(givenWrongCity)).thenThrow(givenException)
        whenever(mockView.displayGetWeatherErrorWithRetry(
                stubStickyContinuation(givenStickyContinuation),
                eqString(givenException.cityAndCountry)))

        // When
        subject.getWeatherWithRetry()

        // Then
        val updatedCityWeather = subject.weather[givenCityIndex]
        assertThat(updatedCityWeather).isEqualTo(UnknownCityWeather)
        verify(mockView, times(3)).updateCity(givenCityIndex)
    }
}