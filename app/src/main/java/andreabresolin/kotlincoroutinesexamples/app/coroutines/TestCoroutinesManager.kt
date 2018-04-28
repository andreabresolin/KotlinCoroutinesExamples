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

package andreabresolin.kotlincoroutinesexamples.app.coroutines

import andreabresolin.kotlincoroutinesexamples.app.coroutines.CoroutinesUtils.Companion.tryCatch
import andreabresolin.kotlincoroutinesexamples.app.coroutines.CoroutinesUtils.Companion.tryCatchFinally
import andreabresolin.kotlincoroutinesexamples.app.coroutines.CoroutinesUtils.Companion.tryFinally
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.runBlocking

open class TestCoroutinesManager : CoroutinesManager {

    override fun launchOnUI(block: suspend CoroutineScope.() -> Unit) {
        runBlocking { block() }
    }

    override fun launchOnUITryCatch(
            tryBlock: suspend CoroutineScope.() -> Unit,
            catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
            handleCancellationExceptionManually: Boolean) {
        launchOnUI { tryCatch(tryBlock, catchBlock, handleCancellationExceptionManually) }
    }

    override fun launchOnUITryCatchFinally(
            tryBlock: suspend CoroutineScope.() -> Unit,
            catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
            finallyBlock: suspend CoroutineScope.() -> Unit,
            handleCancellationExceptionManually: Boolean) {
        launchOnUI { tryCatchFinally(tryBlock, catchBlock, finallyBlock, handleCancellationExceptionManually) }
    }

    override fun launchOnUITryFinally(
            tryBlock: suspend CoroutineScope.() -> Unit,
            finallyBlock: suspend CoroutineScope.() -> Unit,
            suppressCancellationException: Boolean) {
        launchOnUI { tryFinally(tryBlock, finallyBlock, suppressCancellationException) }
    }

    override fun cancelAllCoroutines() {
        // Nothing to do
    }
}