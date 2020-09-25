/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.chrisbanes.accompanist.imageloading

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * RequestActor is a wrapper around a [Channel] to implement the actor pattern.
 *
 * You pass input parameters to the actor via [send], which will be processed. Then in a coroutine
 * you should start the actor by calling [run], passing in a lambda to be notified of any results.
 *
 * When creating a [RequestActor] you just need to pass in a [execute] lambda which is
 * executed on every distinct parameter passed in.
 *
 * As an example for use in Compose, you would typically do this:
 *
 * ```
 * var text by state { "" }
 *
 * val requestActor = remember {
 *     RequestActor<Int, String> { input ->
 *         generateStringForInt(input)
 *     }
 * }
 *
 * launchInComposition(requestActor) {
 *     requestActor.run { input, result ->
 *         text = result
 *     }
 * }
 *
 * // Send the actor something to act on
 * requestActor.send(439)
 *
 * // do something with text
 * ```
 */
internal class RequestActor<Param, Result>(
    private val execute: (Param) -> Flow<Result>
) {
    private val channel = Channel<Param>(Channel.CONFLATED)

    /**
     * Start and run this [RequestActor].
     *
     * This will invoke [execute] on any input values passed in via [send], and then callback
     * with the result to [onResult].
     *
     * @param onResult A lambda which will be called with each input and the processed result.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun run(onResult: (Param, Result) -> Unit) {
        channel.receiveAsFlow()
            .distinctUntilChanged()
            .flatMapLatest { param ->
                execute(param).map { param to it }
            }
            .collect { (param, result) ->
                onResult(param, result)
            }
    }

    /**
     * Send an input for processing by the [RequestActor].
     */
    fun send(input: Param) {
        channel.offer(input)
    }
}
