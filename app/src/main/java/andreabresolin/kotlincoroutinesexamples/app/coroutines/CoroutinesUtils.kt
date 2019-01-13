/*
 *  Copyright 2017-2019 Andrea Bresolin
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

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope

interface CoroutinesUtils {
    companion object {
        suspend fun CoroutineScope.tryCatch(
                tryBlock: suspend CoroutineScope.() -> Unit,
                catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
                handleCancellationExceptionManually: Boolean = false) {
            try {
                tryBlock()
            } catch (e: Throwable) {
                if (e !is CancellationException || handleCancellationExceptionManually) {
                    catchBlock(e)
                } else {
                    throw e
                }
            }
        }

        suspend fun CoroutineScope.tryCatchFinally(
                tryBlock: suspend CoroutineScope.() -> Unit,
                catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
                finallyBlock: suspend CoroutineScope.() -> Unit,
                handleCancellationExceptionManually: Boolean = false) {

            var caughtThrowable: Throwable? = null

            try {
                tryBlock()
            } catch (e: Throwable) {
                if (e !is CancellationException || handleCancellationExceptionManually) {
                    catchBlock(e)
                } else {
                    caughtThrowable = e
                }
            } finally {
                if (caughtThrowable is CancellationException && !handleCancellationExceptionManually) {
                    throw caughtThrowable
                } else {
                    finallyBlock()
                }
            }
        }

        suspend fun CoroutineScope.tryFinally(
                tryBlock: suspend CoroutineScope.() -> Unit,
                finallyBlock: suspend CoroutineScope.() -> Unit,
                suppressCancellationException: Boolean = false) {

            var caughtThrowable: Throwable? = null

            try {
                tryBlock()
            } catch (e: CancellationException) {
                if (!suppressCancellationException) {
                    caughtThrowable = e
                }
            } finally {
                if (caughtThrowable is CancellationException && !suppressCancellationException) {
                    throw caughtThrowable
                } else {
                    finallyBlock()
                }
            }
        }
    }
}