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
import android.arch.lifecycle.OnLifecycleEvent
import android.support.annotation.CallSuper
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DefaultBasePresenter<View>
constructor(coroutinesManager: CoroutinesManager) : CoroutinesManager by coroutinesManager, BasePresenter<View> {

    private var viewInstance: View? = null
    private var viewLifecycle: Lifecycle? = null
    private val isViewResumed = AtomicBoolean(false)
    private val viewContinuations: MutableList<Continuation<View>> = mutableListOf()
    private val stickyContinuations: MutableMap<StickyContinuation<*>, View.(StickyContinuation<*>) -> Unit> = mutableMapOf()
    private var mustRestoreStickyContinuations: Boolean = false

    override fun injectDependencies(dependenciesInjectionBlock: () -> Unit) {
        dependenciesInjectionBlock()
    }

    @Synchronized
    override suspend fun view(): View {
        if (isViewResumed.get()) {
            viewInstance?.let { return it }
        }

        // Wait until the view is ready to be used again
        return suspendCoroutine { continuation -> viewContinuations.add(continuation) }
    }

    @Synchronized
    override fun attachView(view: View, viewLifecycle: Lifecycle) {
        viewInstance = view
        this.viewLifecycle = viewLifecycle

        onViewAttached(view)
    }

    override fun onViewAttached(view: View) {
        // Nothing to do here. This is an event handled by the specific presenters.
    }

    @Synchronized
    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    private fun onViewStateChanged() {
        isViewResumed.set(viewLifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) ?: false)
    }

    @Synchronized
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onViewReadyForContinuations() {
        val view = viewInstance

        if (view != null) {
            val viewContinuationsIterator = viewContinuations.listIterator()

            while (viewContinuationsIterator.hasNext()) {
                val continuation = viewContinuationsIterator.next()

                // The view was not ready when the presenter needed it earlier,
                // but now it's ready again so the presenter can continue
                // interacting with it.
                viewContinuationsIterator.remove()
                continuation.resume(view)
            }
        }
    }

    @Synchronized
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onViewReadyForStickyContinuations() {
        val view = viewInstance

        if (mustRestoreStickyContinuations && view != null) {
            mustRestoreStickyContinuations = false

            val stickyContinuationsIterator = stickyContinuations.iterator()

            while (stickyContinuationsIterator.hasNext()) {
                val stickyContinuationBlockMap = stickyContinuationsIterator.next()
                val stickyContinuation = stickyContinuationBlockMap.key
                val stickyContinuationBlock = stickyContinuationBlockMap.value
                view.stickyContinuationBlock(stickyContinuation)
            }
        }
    }

    @Synchronized
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onViewDestroyed() {
        viewInstance = null
        viewLifecycle = null
        mustRestoreStickyContinuations = true
    }

    override suspend fun <ReturnType> executeStickyContinuation(stickyContinuationBlock: (Continuation<ReturnType>) -> Unit): ReturnType {
        return suspendCoroutine { continuation -> stickyContinuationBlock(continuation) }
    }

    @Synchronized
    override fun addStickyContinuation(continuation: StickyContinuation<*>,
                                       block: View.(StickyContinuation<*>) -> Unit) {
        stickyContinuations[continuation] = block
    }

    @Synchronized
    override fun removeStickyContinuation(continuation: StickyContinuation<*>): Boolean {
        return stickyContinuations.remove(continuation) != null
    }

    @CallSuper
    @Synchronized
    override fun cleanup() {
        cancelAllCoroutines()
    }
}