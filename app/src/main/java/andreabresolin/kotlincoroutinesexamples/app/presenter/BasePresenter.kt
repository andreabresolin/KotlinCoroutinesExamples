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
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import kotlin.coroutines.Continuation

interface BasePresenter<View> : CoroutinesManager, LifecycleObserver {
    fun injectDependencies(dependenciesInjectionBlock: () -> Unit)
    suspend fun view(): View
    fun attachView(view: View, viewLifecycle: Lifecycle)
    fun onViewAttached(view: View)
    suspend fun <ReturnType> executeStickyContinuation(stickyContinuationBlock: (Continuation<ReturnType>) -> Unit): ReturnType
    fun addStickyContinuation(continuation: StickyContinuation<*>, block: View.(StickyContinuation<*>) -> Unit)
    fun removeStickyContinuation(continuation: StickyContinuation<*>): Boolean
    fun cleanup()

    /**
     * Executes the given block on the view. The block is executed again
     * every time the view instance changes and the new view is resumed.
     * This, for example, is useful for dialogs that need to be persisted
     * across orientation changes.
     *
     * @param block code that has to be executed on the view
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <ReturnType> View.stickySuspension(
            block: View.(StickyContinuation<ReturnType>) -> Unit): ReturnType {
        return this@BasePresenter.executeStickyContinuation { continuation ->
            val stickyContinuation: StickyContinuation<ReturnType> = StickyContinuation(continuation, this@BasePresenter)
            addStickyContinuation(stickyContinuation, block as View.(StickyContinuation<*>) -> Unit)
            block(stickyContinuation)
        }
    }
}