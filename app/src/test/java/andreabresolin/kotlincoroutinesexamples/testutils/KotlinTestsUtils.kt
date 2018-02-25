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

package andreabresolin.kotlincoroutinesexamples.testutils

import andreabresolin.kotlincoroutinesexamples.app.presenter.StickyContinuation
import org.mockito.ArgumentMatchers.argThat
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.stubbing.OngoingStubbing
import kotlin.coroutines.experimental.Continuation

interface KotlinTestsUtils {
    companion object {
        fun eqString(string: String): String {
            eq(string)
            return string
        }

        inline fun <reified T> whenever(methodCall: T): OngoingStubbing<T> {
            if (T::class.java == Unit.javaClass) {
                return `when`(methodCall).then {  }
            } else {
                return `when`(methodCall)
            }
        }

        fun <ReturnType> stubStickyContinuation(stickyContinuation: StickyContinuation<ReturnType>): StickyContinuation<ReturnType> {
            argThat<StickyContinuation<ReturnType>>(StickyContinuationArgumentMatcher<ReturnType>(stickyContinuation))
            return stickyContinuation
        }

        @Suppress("UNCHECKED_CAST")
        fun <T> mockContinuation(): Continuation<T> {
            return Mockito.mock(Continuation::class.java) as Continuation<T>
        }
    }
}