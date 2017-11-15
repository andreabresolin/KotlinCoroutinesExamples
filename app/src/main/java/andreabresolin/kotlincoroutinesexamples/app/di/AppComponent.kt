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

package andreabresolin.kotlincoroutinesexamples.app.di

import andreabresolin.kotlincoroutinesexamples.app.App
import andreabresolin.kotlincoroutinesexamples.app.di.scopes.PerApplication
import andreabresolin.kotlincoroutinesexamples.home.di.HomeComponent
import andreabresolin.kotlincoroutinesexamples.home.di.HomeModule
import dagger.Component

@PerApplication
@Component(modules = [(AppModule::class), (NetworkModule::class)])
interface AppComponent {
    fun inject(application: App)
    fun plus(module: HomeModule): HomeComponent
}