# Kotlin Coroutines Examples

This is the example code for the Medium post [Playing with Kotlin in Android: coroutines and how to get rid of the callback hell](https://medium.com/@andrea.bresolin/playing-with-kotlin-in-android-coroutines-and-how-to-get-rid-of-the-callback-hell-a96e817c108b).

This app is provided as a proof of concept of some ideas about [Kotlin coroutines](https://kotlinlang.org/docs/reference/coroutines.html) in Android.

## Note

To show examples of remote REST service calls, I’ve used the weather API at [OpenWeatherMap](https://openweathermap.org/api) and my app ID is bundled in the code to make it easier for you to test it immediately, but mine is a free account, so make sure you create your own account at [OpenWeatherMap](https://openweathermap.org/api) and replace **WEATHER_API_APP_ID** inside **build.gradle** with your own ID to avoid blocking the account because of too many requests to the API.

## License

```
Copyright 2017 Andrea Bresolin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```