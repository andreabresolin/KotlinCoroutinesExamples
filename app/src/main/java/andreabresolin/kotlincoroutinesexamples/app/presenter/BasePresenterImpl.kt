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
import android.util.Log
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

abstract class BasePresenterImpl<ViewInterface>: ViewModel(), BasePresenter<ViewInterface> {

    private val asyncJobs: MutableList<Job> = mutableListOf()

    private var viewInstance: ViewInterface? = null
    private var viewLifecycle: Lifecycle? = null
    private var viewContinuations: MutableList<Continuation<ViewInterface>> = mutableListOf()

    @Synchronized
    protected suspend fun view(): ViewInterface {
        Log.d("BasePresenterImpl", "view(): start")
        viewInstance?.let {
            Log.d("BasePresenterImpl", "view(): checking viewLifecycle")
            if (viewLifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == true) {
                Log.d("BasePresenterImpl", "view(): returning viewInstance")
                return it
            }
        }

        Log.d("BasePresenterImpl", "view(): waiting for the view to be ready...")
        // Wait until the view is ready to be used again
        return suspendCoroutine { continuation -> viewContinuations.add(continuation) }
    }

    @Synchronized
    override fun attachView(view: ViewInterface, viewLifecycle: Lifecycle) {
        viewInstance = view
        this.viewLifecycle = viewLifecycle

        onViewAttached(view)
    }

    open protected fun onViewAttached(view: ViewInterface) {
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
                Log.d("BasePresenterImpl", "onViewStarted(): resuming viewContinuation")
                viewContinuationsIterator.remove()
                continuation.resume(view)
            }
        }
    }

    @Synchronized
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onViewDestroyed() {
        Log.d("BasePresenterImpl", "onViewDestroyed")
        viewInstance = null
        viewLifecycle = null
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
        asyncJobs.forEach {
            it.cancel()
        }
    }

    @CallSuper
    open fun cleanup() {
        cancelAllAsync()
    }
}