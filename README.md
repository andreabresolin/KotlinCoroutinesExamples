# Kotlin Coroutines Examples

This app is provided as a proof of concept of some ideas about [Kotlin coroutines](https://kotlinlang.org/docs/reference/coroutines.html) in Android.

**This project is experimental and is not intended as production-ready code**. It's currently just a way to show what can be achieved with Kotlin coroutines and how much powerful this tool can be.

An introduction to some of the ideas presented in this code can be found in the Medium post [Playing with Kotlin in Android: coroutines and how to get rid of the callback hell](https://medium.com/@andrea.bresolin/playing-with-kotlin-in-android-coroutines-and-how-to-get-rid-of-the-callback-hell-a96e817c108b).

## Note

To show examples of remote REST service calls, I’ve used the weather API at [OpenWeatherMap](https://openweathermap.org/api) and my app ID is bundled in the code to make it easier for you to test it immediately, but mine is a free account, so make sure you create your own account at [OpenWeatherMap](https://openweathermap.org/api) and replace **WEATHER_API_APP_ID** inside **build.gradle** with your own ID to avoid blocking the account because of too many requests to the API.

## Purpose

The main purpose of this project is showing how Kotlin coroutines can be used creatively to overcome the limits of the traditional asynchronous programming with callbacks. By applying coroutines to Android, we can go further and get rid of the limits of MVP and MVVM architectures to get the best of both.

The architecture of this app is _MVP Clean_ where the presenter can also be used as a _ViewModel_. The presenter is in fact a subclass of [_ViewModel_](https://developer.android.com/topic/libraries/architecture/viewmodel.html) so its lifecycle matches the one of the _ViewModel_, but can interact directly with the view without leaking it and allowing it to be replaced transparently.

This is a work in progress that can surely be improved, but will hopefully give you some new ideas to work with Kotlin coroutines.

## Pros and cons of traditional architectures and asynchronous operations management

Here are some examples of pros and cons that we have, in my opinion, with the typical MVP and MVVM architectures in Android. In many Android projects we typically use also _RxJava_ to handle asynchronous operations, so I'll briefly list the pros and cons also for that.

### MVP in Android

#### Pros

- straightforward to manage (the presenter clearly shows all the steps that change the state of the view and their order)
- interacting with the view interface allows to pass any necessary parameters directly to the view methods
- any flow in the view, even the most complex, can be managed in a relatively easy way given that we can interact with the view directly

#### Cons

- the presenter keeps a reference to the view so its lifecycle is strictly bound to the one of the view
- on configuration changes (like screen rotation), we can't easily preserve the state in the presenter and we typically stop all the asynchronous operations to restart them again when the new instance of the presenter is created unless we use the additional complexity of managing a service

### MVVM in Android

#### Pros

- no direct reference to the view so the state of the _ViewModel_ can be preserved across configuration changes (like orientation changes)
- the asynchronous operations can easily survive the view's configuration changes allowing for a more efficient use of resources
- we can store in the _ViewModel_ all the state information that we want to keep on configuration changes without needing to restore it when the view instance changes

#### Cons

- to update the view, we set the state in [_LiveData_](https://developer.android.com/topic/libraries/architecture/livedata.html) objects, and this means that we still need to make a call to a method to update the state as in a presenter, but now we have the additional work to subscribe to the _LiveData_ in the view and handle a callback to perform the real state change on the view
- given that _LiveData_ returns a single type and we might need additional information to update the state of the view as a _LiveData_ object changes, we might end up creating multiple custom types that hold all the information to be returned by the _LiveData_ or we need to coordinate multiple _LiveData_ objects, a problem that we never had in MVP with a presenter
- automatically restoring the state of a new view instance with _LiveData_ poses new challenges in coordinating between the state that is restored from the traditional _Bundle_ and what is actually available in _LiveData_
- complex UI flows can become challenging to achieve as opposed to MVP
- to understand the flow in the code when updating the UI, we need to check where a _LiveData_ is set in the _ViewModel_, then jump to the observer method for that _LiveData_ inside the view, then jump to all that is called by that observer method, instead of a straightforward call to the view that we have in MVP (in case we use data-binding with MVVM, then we also need to jump to the view layouts)

### RxJava in Android

#### Pros

- great way to manage asynchronous operations on streams handling also backpressure problems
- well known tool even outside of Android

#### Cons

- for every asynchronous flow, we typically end up with two callbacks when we subscribe to it, one for the success case and the other one for the error case (this leads to a large amount of callbacks in our presenter/_ViewModel_)
- given that we have many callbacks, it takes more time to understand and change the flow when we have new requirements
- multiple callbacks make it hard to always have all the information that we need available in all of them and we might end up passing parameters just to pass additional information or storing temporary state in the presenter/_ViewModel_
- we usually don't need to handle streams in a typical Android app because we just read some information from a remote API call and handle the full result in one go (we handle collections instead)
- in Kotlin, we have, out of the box, the same familiar operators provided by _RxJava_, but also for collections, without the need for an external library
- if we try to reduce the callbacks by using _RxJava_ in all the layers of our app combining its operators before subscribing, we will heavily depend on a single external library and the code becomes quite hard to manage (we'll still have some callbacks at some point because we need to subscribe to the success and error cases)
- the fluent API becomes challenging when, in an operator, we need some information that is not directly returned by the previous one (nested operators chains)

## What can be achieved with Kotlin coroutines

The main benefits that we can gain from using coroutines in Kotlin are the following:

- totally remove any callback (and consequently no more need to pass around state information across multiple methods or storing temporary state in the presenter/_ViewModel_)
- remove the dependency on an external library, RxJava, or use it only when strictly necessary, while still keeping all its familiar operators because they are provided out of the box by Kotlin on collections
- all the asynchronous code can look totally synchronous with a great benefit in understanding and changing the flow as we need (a synchronous sequence of steps is much easier to manage as opposed to asynchronous code)
- much more concise code
- there are still no problems in testing the code with unit tests as in other architectures
- switching some parts of the code from synchronous to asynchronous because of changed requirements doesn't create the need to change the unit tests in most of the code because we don't have additional callbacks generated by the asynchronous code (what is asynchronous looks synchronous in fact)

## What's shown in this example project

This project can be seen as an experiment in using Kotlin coroutines to achieve the following results:

- avoiding the cons of both MVP and MVVM while keeping the pros (no need to decide beforehand which architecture to use because we can have the benefits of both and use them as needed)
- direct interaction with the view from the presenter (_LiveData_ not necessary, but can be used if more convenient in some cases)
- presenter state preserved on configuration changes because its lifecycle is not connected to the view (it's the same lifecycle as a _ViewModel_ instead)
- replacement of the view instance totally transparent for the presenter on configuration changes (the presenter suspends while the view is not ready to be used and resumes automatically as soon as the view is ready again)
- no callbacks for asynchronous operations
- no callbacks while waiting from the presenter for some user interaction on the view (e.g. waiting for the user to select an option in a dialog)
- automatic cleanup of the running asynchronous operations when the presenter is destroyed (taking advantage of the _ViewModel_ class lifecycle and Kotlin coroutines)
- the code can be unit tested

## What can be improved in this example project

Many things, as usual. For example, the base and utility classes/interfaces could be extracted in a library to be reusable across multiple projects.

## License

```
Copyright 2018-2019 Andrea Bresolin

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