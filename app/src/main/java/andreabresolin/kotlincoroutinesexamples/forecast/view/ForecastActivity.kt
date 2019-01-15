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

package andreabresolin.kotlincoroutinesexamples.forecast.view

import andreabresolin.kotlincoroutinesexamples.R
import andreabresolin.kotlincoroutinesexamples.app.App
import andreabresolin.kotlincoroutinesexamples.app.model.City
import andreabresolin.kotlincoroutinesexamples.app.presenter.StickyContinuation
import andreabresolin.kotlincoroutinesexamples.forecast.di.ForecastModule
import andreabresolin.kotlincoroutinesexamples.forecast.presenter.ForecastPresenter
import andreabresolin.kotlincoroutinesexamples.forecast.presenter.ForecastPresenterFactory
import andreabresolin.kotlincoroutinesexamples.forecast.presenter.ForecastPresenterImpl
import andreabresolin.kotlincoroutinesexamples.forecast.view.ForecastView.ErrorDialogResponse
import andreabresolin.kotlincoroutinesexamples.forecast.view.ForecastView.ErrorDialogResponse.CANCEL
import andreabresolin.kotlincoroutinesexamples.forecast.view.ForecastView.ErrorDialogResponse.RETRY
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_forecast.*
import javax.inject.Inject
import kotlin.coroutines.resume

class ForecastActivity : AppCompatActivity(), ForecastView {

    companion object {
        private const val CONTENT_CHILD_INDEX = 0
        private const val LOADING_CHILD_INDEX = 1
        private const val ERROR_CHILD_INDEX = 2

        private const val CITY_EXTRA = "CITY_EXTRA"

        private const val DISPLAYED_CHILD_STATE_KEY = "DISPLAYED_CHILD_STATE_KEY"

        fun start(context: Context, city: City) {
            val intent = Intent(context, ForecastActivity::class.java)
            intent.putExtra(CITY_EXTRA, city)
            context.startActivity(intent)
        }
    }

    @Inject
    internal lateinit var presenterFactory: ForecastPresenterFactory

    private lateinit var presenter: ForecastPresenter<ForecastView>
    private val openDialogs: MutableList<AlertDialog> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)

        injectDependencies()
        setUpPresenter()
        setUpDaysForecastList()

        intent.getParcelableExtra<City>(CITY_EXTRA)?.let {
            title = getString(R.string.forecast_activity_title, it.cityName)

            if (presenter.forecasts.isEmpty() && savedInstanceState == null) {
                loadForecasts(it)
            }
        }
    }

    private fun injectDependencies() {
        App.get().getAppComponent()?.plus(ForecastModule())?.inject(this)
    }

    private fun setUpPresenter() {
        presenter = ViewModelProviders.of(this, presenterFactory).get(ForecastPresenterImpl::class.java)
        presenter.attachView(this, lifecycle)
        lifecycle.addObserver(presenter.getLifecycleObserver())
    }

    private fun setUpDaysForecastList() {
        daysForecastList.adapter = DaysForecastListAdapter(this, presenter.forecasts)
    }

    private fun loadForecasts(city: City) {
        presenter.loadForecasts(city)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt(DISPLAYED_CHILD_STATE_KEY, viewFlipper.displayedChild)

        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        viewFlipper.displayedChild = savedInstanceState?.getInt(DISPLAYED_CHILD_STATE_KEY) ?: CONTENT_CHILD_INDEX

        intent.getParcelableExtra<City>(CITY_EXTRA)?.let {
            if (presenter.forecasts.isEmpty() && viewFlipper.displayedChild == CONTENT_CHILD_INDEX) {
                loadForecasts(it)
            }
        }
    }

    override fun onDestroy() {
        openDialogs.forEach {
            it.dismiss()
        }

        super.onDestroy()
    }

    override fun displayLoadingState() {
        viewFlipper.displayedChild = LOADING_CHILD_INDEX
    }

    override fun displayContentState() {
        viewFlipper.displayedChild = CONTENT_CHILD_INDEX
    }

    override fun displayErrorState() {
        errorMessageText.text = getString(R.string.forecast_retrieval_error_message)
        viewFlipper.displayedChild = ERROR_CHILD_INDEX
    }

    override fun displayNoDataState() {
        errorMessageText.text = getString(R.string.forecast_retrieval_no_data_message)
        viewFlipper.displayedChild = ERROR_CHILD_INDEX
    }

    override fun updateAllForecasts() {
        daysForecastList.adapter?.notifyDataSetChanged()
    }

    override fun displayLoadForecastsErrorWithRetry(
            continuation: StickyContinuation<ErrorDialogResponse>,
            place: String?) {

        lateinit var dialog: AlertDialog

        dialog = AlertDialog.Builder(this)
                .setTitle(R.string.forecast_retrieval_error_dialog_title)
                .setMessage(
                        if (place != null)
                            getString(R.string.forecast_retrieval_error_dialog_message_with_retry, place)
                        else
                            getString(R.string.forecast_retrieval_error_message)
                )
                .setPositiveButton(R.string.retry_dialog_button, { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    continuation.resume(RETRY)
                })
                .setNegativeButton(R.string.cancel_dialog_button, { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    continuation.resume(CANCEL)
                })
                .setOnCancelListener {
                    continuation.resume(CANCEL)
                }
                .setOnDismissListener { openDialogs.remove(dialog) }
                .create()

        openDialogs.add(dialog)

        dialog.show()
    }
}
