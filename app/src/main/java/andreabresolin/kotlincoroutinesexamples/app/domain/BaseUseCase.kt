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

package andreabresolin.kotlincoroutinesexamples.app.domain

import android.support.annotation.CallSuper
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async

abstract class BaseUseCase {

    private val deferredObjects: MutableList<Deferred<*>> = mutableListOf()

    @CallSuper
    @Synchronized
    protected suspend fun <T> async(block: suspend CoroutineScope.() -> T): Deferred<T> {
        val deferred: Deferred<T> = async(CommonPool) { block() }
        deferredObjects.add(deferred)
        deferred.invokeOnCompletion { deferredObjects.remove(deferred) }
        return deferred
    }

    @CallSuper
    @Synchronized
    protected suspend fun <T> asyncAwait(block: suspend CoroutineScope.() -> T): T {
        return async(block).await()
    }

    @CallSuper
    @Synchronized
    protected fun cancelAllAsync() {
        val deferredObjectsSize = deferredObjects.size

        if (deferredObjectsSize > 0) {
            for (i in deferredObjectsSize - 1 downTo 0) {
                deferredObjects[i].cancel()
            }
        }
    }

    @CallSuper
    @Synchronized
    open fun cleanup() {
        cancelAllAsync()
    }
}