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

import andreabresolin.kotlincoroutinesexamples.app.utils.CoroutinesUtils.Companion.tryCatch
import andreabresolin.kotlincoroutinesexamples.app.utils.CoroutinesUtils.Companion.tryCatchFinally
import andreabresolin.kotlincoroutinesexamples.app.utils.CoroutinesUtils.Companion.tryFinally
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.OnLifecycleEvent
import android.arch.lifecycle.ViewModel
import android.support.annotation.CallSuper
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

abstract class BasePresenterImpl<View>: ViewModel(), BasePresenter<View> {

    private val asyncJobs: MutableList<Job> = mutableListOf()

    private var viewInstance: View? = null
    private var viewLifecycle: Lifecycle? = null
    private val viewContinuations: MutableList<Continuation<View>> = mutableListOf()
    private val stickyContinuations: MutableMap<StickyContinuation<*>, View.(StickyContinuation<*>) -> Unit> = mutableMapOf()
    private var mustRestoreStickyContinuations: Boolean = false

    @Synchronized
    protected suspend fun view(): View {
        viewInstance?.let {
            if (viewLifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == true) {
                return it
            }
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

    open protected fun onViewAttached(view: View) {
        // Nothing to do here. This is an event handled by the subclasses.
    }

    @Synchronized
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onViewStarted() {
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
    private fun onViewResumed() {
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

    override fun onCleared() {
        cleanup()
        super.onCleared()
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

    @Suppress("UNCHECKED_CAST")
    suspend fun <ReturnType> View.stickySuspension(
            block: View.(StickyContinuation<ReturnType>) -> Unit): ReturnType {
        return suspendCoroutine<ReturnType> { continuation ->
            val stickyContinuation: StickyContinuation<ReturnType> = StickyContinuation(continuation, this@BasePresenterImpl)
            addStickyContinuation(stickyContinuation, block as View.(StickyContinuation<*>) -> Unit)
            block(stickyContinuation)
        }
    }

    @CallSuper
    @Synchronized
    protected fun launchAsync(block: suspend CoroutineScope.() -> Unit) {
        val job: Job = launch(UI) { block() }
        asyncJobs.add(job)
        job.invokeOnCompletion { asyncJobs.remove(job) }
    }

    @Synchronized
    protected fun launchAsyncTryCatch(
            tryBlock: suspend CoroutineScope.() -> Unit,
            catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
            handleCancellationExceptionManually: Boolean = false) {
        launchAsync { tryCatch(tryBlock, catchBlock, handleCancellationExceptionManually) }
    }

    @Synchronized
    protected fun launchAsyncTryCatchFinally(
            tryBlock: suspend CoroutineScope.() -> Unit,
            catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
            finallyBlock: suspend CoroutineScope.() -> Unit,
            handleCancellationExceptionManually: Boolean = false) {
        launchAsync { tryCatchFinally(tryBlock, catchBlock, finallyBlock, handleCancellationExceptionManually) }
    }

    @Synchronized
    protected fun launchAsyncTryFinally(
            tryBlock: suspend CoroutineScope.() -> Unit,
            finallyBlock: suspend CoroutineScope.() -> Unit,
            suppressCancellationException: Boolean = false) {
        launchAsync { tryFinally(tryBlock, finallyBlock, suppressCancellationException) }
    }

    @CallSuper
    @Synchronized
    protected fun cancelAllAsync() {
        val asyncJobsSize = asyncJobs.size

        if (asyncJobsSize > 0) {
            for (i in asyncJobsSize - 1 downTo 0) {
                asyncJobs[i].cancel()
            }
        }
    }

    @CallSuper
    @Synchronized
    open fun cleanup() {
        cancelAllAsync()
    }
}