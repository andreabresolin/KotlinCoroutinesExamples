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

package andreabresolin.kotlincoroutinesexamples.app.presenter

import andreabresolin.kotlincoroutinesexamples.app.coroutines.CoroutinesManager
import andreabresolin.kotlincoroutinesexamples.testutils.KotlinTestUtils.Companion.mockContinuation
import android.arch.lifecycle.Lifecycle
import kotlin.coroutines.Continuation

class TestBasePresenter<View>
constructor(coroutinesManager: CoroutinesManager): CoroutinesManager by coroutinesManager, BasePresenter<View> {

    private var mockView: View? = null
    private lateinit var currentStickyContinuation: StickyContinuation<*>

    override fun injectDependencies(dependenciesInjectionBlock: () -> Unit) {
        // Nothing to do
    }

    override suspend fun view(): View {
        return mockView!!
    }

    override fun attachView(view: View, viewLifecycle: Lifecycle) {
        mockView = view
    }

    override fun onViewAttached(view: View) {
        // Nothing to do
    }

    override suspend fun <ReturnType> executeStickyContinuation(stickyContinuationBlock: (Continuation<ReturnType>) -> Unit): ReturnType {
        stickyContinuationBlock(mockContinuation())

        if (currentStickyContinuation.resumeException != null) {
            throw currentStickyContinuation.resumeException as Throwable
        }

        @Suppress("UNCHECKED_CAST")
        return currentStickyContinuation.resumeValue as ReturnType
    }

    override fun addStickyContinuation(continuation: StickyContinuation<*>,
                                       block: View.(StickyContinuation<*>) -> Unit) {
        currentStickyContinuation = continuation
    }

    override fun removeStickyContinuation(continuation: StickyContinuation<*>): Boolean {
        // Nothing to do
        return true
    }

    override fun cleanup() {
        cancelAllCoroutines()
    }
}