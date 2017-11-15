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

import andreabresolin.kotlincoroutinesexamples.BuildConfig
import andreabresolin.kotlincoroutinesexamples.app.di.scopes.PerApplication
import andreabresolin.kotlincoroutinesexamples.app.network.api.WeatherApi
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class NetworkModule {

    @Provides
    @PerApplication
    internal fun provideHttpClientBuilder(): OkHttpClient.Builder = OkHttpClient.Builder()

    @Provides
    @PerApplication
    internal fun provideHttpClient(builder: OkHttpClient.Builder): OkHttpClient = builder.build()

    @Provides
    @PerApplication
    internal fun provideConverter(): Gson = Gson()

    @Provides
    @PerApplication
    internal fun provideRestAdapterBuilder(client: OkHttpClient, gson: Gson): Retrofit.Builder {
        return Retrofit.Builder()
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
    }

    @Provides
    @PerApplication
    internal fun provideWeatherApi(builder: Retrofit.Builder): WeatherApi {
        return builder
                .baseUrl(BuildConfig.WEATHER_API_ENDPOINT)
                .build()
                .create(WeatherApi::class.java)
    }
}