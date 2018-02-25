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

package andreabresolin.kotlincoroutinesexamples.app.presenter

import kotlin.coroutines.experimental.Continuation

class StickyContinuation<ReturnType>
constructor(
        private val continuation: Continuation<ReturnType>,
        private val presenter: BasePresenter<*>) : Continuation<ReturnType> by continuation {

    private var _resumeValue: ReturnType? = null
    val resumeValue: ReturnType?
        get() = _resumeValue

    private var _resumeException: Throwable? = null
    val resumeException: Throwable?
        get() = _resumeException

    override fun resume(value: ReturnType) {
        _resumeValue = value
        presenter.removeStickyContinuation(this)
        continuation.resume(value)
    }

    override fun resumeWithException(exception: Throwable) {
        _resumeException = exception
        presenter.removeStickyContinuation(this)
        continuation.resumeWithException(exception)
    }
}