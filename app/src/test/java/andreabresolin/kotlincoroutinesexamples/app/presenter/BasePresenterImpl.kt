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

import andreabresolin.kotlincoroutinesexamples.app.coroutines.CoroutinesManager
import andreabresolin.kotlincoroutinesexamples.testutils.KotlinTestUtils.Companion.mockContinuation
import android.arch.lifecycle.Lifecycle

abstract class BasePresenterImpl<View>
constructor(coroutinesManager: CoroutinesManager): CoroutinesManager by coroutinesManager, BasePresenter<View> {

    private var mockView: View? = null

    protected fun injectDependencies() {
        // Nothing to do
    }

    open protected fun onInjectDependencies() {
        // Nothing to do
    }

    protected suspend fun view(): View {
        return mockView!!
    }

    override fun attachView(view: View, viewLifecycle: Lifecycle) {
        mockView = view
    }

    open protected fun onViewAttached(view: View) {
        // Nothing to do
    }

    override fun addStickyContinuation(continuation: StickyContinuation<*>,
                                       block: View.(StickyContinuation<*>) -> Unit) {
        // Nothing to do
    }

    override fun removeStickyContinuation(continuation: StickyContinuation<*>): Boolean {
        // Nothing to do
        return true
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun <ReturnType> View.stickySuspension(
            block: View.(StickyContinuation<ReturnType>) -> Unit): ReturnType {
        val stickyContinuation: StickyContinuation<ReturnType> =
                StickyContinuation(mockContinuation(), this@BasePresenterImpl)

        block(stickyContinuation)

        if (stickyContinuation.resumeException != null) {
            throw stickyContinuation.resumeException as Throwable
        }

        return stickyContinuation.resumeValue as ReturnType
    }

    open fun cleanup() {
        cancelAllCoroutines()
    }
}