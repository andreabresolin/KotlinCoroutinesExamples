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

package andreabresolin.kotlincoroutinesexamples.home.view

import andreabresolin.kotlincoroutinesexamples.R
import andreabresolin.kotlincoroutinesexamples.app.presenter.StickyContinuation
import andreabresolin.kotlincoroutinesexamples.home.di.HomeComponent
import andreabresolin.kotlincoroutinesexamples.home.presenter.HomePresenter
import andreabresolin.kotlincoroutinesexamples.home.presenter.HomePresenterImpl
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView.ErrorDialogResponse
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity(), HomeView {

    private lateinit var presenter: HomePresenter<HomeView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupPresenter()
        setupListeners()
        setupCitiesWeatherList()
    }

    override fun injectDependencies(homeComponent: HomeComponent) {
        homeComponent.inject(this)
    }

    private fun setupPresenter() {
        presenter = ViewModelProviders.of(this).get(HomePresenterImpl::class.java)
        presenter.attachView(this, lifecycle)
        lifecycle.addObserver(presenter)
    }

    private fun setupListeners() {
        getCurrentWeatherSequentialButton.setOnClickListener { onGetCurrentWeatherSequentialButtonClick() }
        getCurrentWeatherParallelButton.setOnClickListener { onGetCurrentWeatherParallelButtonClick() }
        getAverageTemperatureButton.setOnClickListener { onGetAverageTemperatureButtonClick() }
        getCurrentWeatherWithRetryButton.setOnClickListener { onGetCurrentWeatherWithRetryButtonClick() }
    }

    private fun setupCitiesWeatherList() {
        citiesWeatherList.adapter = CitiesWeatherListAdapter(this, presenter.getCitiesWeather())
    }

    private fun onGetCurrentWeatherSequentialButtonClick() {
        presenter.getWeatherSequential()
    }

    private fun onGetCurrentWeatherParallelButtonClick() {
        presenter.getWeatherParallel()
    }

    private fun onGetAverageTemperatureButtonClick() {
        presenter.getAverageTemperatureInCities()
    }

    private fun onGetCurrentWeatherWithRetryButtonClick() {
        presenter.getWeatherWithRetry()
    }

    override fun updateAllCities() {
        citiesWeatherList.adapter.notifyDataSetChanged()
    }

    override fun updateCity(cityIndex: Int) {
        citiesWeatherList.adapter.notifyItemChanged(cityIndex)
    }

    override fun displayAverageTemperature(temperature: Double) {
        AlertDialog.Builder(this)
                .setTitle(R.string.average_temperature_dialog_title)
                .setMessage(getString(R.string.average_temperature_dialog_message, temperature))
                .setPositiveButton(R.string.ok_dialog_button, {
                    dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                })
                .create()
                .show()
    }

    override fun displayGetWeatherError(place: String) {
        AlertDialog.Builder(this)
                .setTitle(R.string.retrieval_error_dialog_title)
                .setMessage(getString(R.string.retrieval_error_dialog_message_with_place, place))
                .setPositiveButton(R.string.ok_dialog_button, {
                    dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                })
                .create()
                .show()
    }

    override fun displayGetWeatherErrorWithRetry(
            continuation: StickyContinuation<ErrorDialogResponse>,
            place: String) {
        AlertDialog.Builder(this)
                .setTitle(R.string.retrieval_error_dialog_title)
                .setMessage(getString(R.string.retrieval_error_dialog_message_with_retry, place))
                .setPositiveButton(R.string.retry_dialog_button, { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    continuation.resume(ErrorDialogResponse.RETRY)
                })
                .setNegativeButton(R.string.cancel_dialog_button, { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    continuation.resume(ErrorDialogResponse.CANCEL)
                })
                .setOnCancelListener {
                    continuation.resume(ErrorDialogResponse.CANCEL)
                }
                .create()
                .show()
    }

    override fun displayWeatherRetrievalGenericError() {
        AlertDialog.Builder(this)
                .setTitle(R.string.retrieval_error_dialog_title)
                .setMessage(R.string.retrieval_error_dialog_message)
                .setPositiveButton(R.string.ok_dialog_button, {
                    dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                })
                .create()
                .show()
    }
}
