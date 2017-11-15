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

package andreabresolin.kotlincoroutinesexamples.app.presenter

import andreabresolin.kotlincoroutinesexamples.app.utils.CoroutinesUtils.Companion.tryCatch
import andreabresolin.kotlincoroutinesexamples.app.utils.CoroutinesUtils.Companion.tryCatchFinally
import andreabresolin.kotlincoroutinesexamples.app.utils.CoroutinesUtils.Companion.tryFinally
import android.support.annotation.CallSuper
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

open class BasePresenter {

    private val asyncJobs: MutableList<Job> = mutableListOf()

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