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
import org.mockito.ArgumentMatcher

class StickyContinuationArgumentMatcher<ReturnType>
constructor(private val stubStickyContinuation: StickyContinuation<ReturnType>) : ArgumentMatcher<StickyContinuation<ReturnType>> {

    @Suppress("UNCHECKED_CAST")
    override fun matches(argument: StickyContinuation<ReturnType>?): Boolean {
        if (argument is StickyContinuation<ReturnType>) {
            if (stubStickyContinuation.resumeException != null) {
                argument.resumeWithException(stubStickyContinuation.resumeException as Throwable)
            } else {
                argument.resume(stubStickyContinuation.resumeValue as ReturnType)
            }

            return true
        }

        return false
    }
}