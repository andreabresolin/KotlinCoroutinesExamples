/*
 *  Copyright 2017-2019 Andrea Bresolin
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

import andreabresolin.kotlincoroutinesexamples.app.di.scopes.PerScreen
import andreabresolin.kotlincoroutinesexamples.home.presenter.HomePresenter
import andreabresolin.kotlincoroutinesexamples.home.view.HomeActivity
import andreabresolin.kotlincoroutinesexamples.home.view.HomeView
import dagger.Subcomponent

@PerScreen
@Subcomponent(modules = [(HomeModule::class)])
interface HomeComponent {
    fun inject(homePresenter: HomePresenter<HomeView>)
    fun inject(homeActivity: HomeActivity)
}