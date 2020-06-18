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

package dev.chrisbanes.accompanist.coil

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow

internal class RequestActor<Param, Result>(
    private val execute: suspend (Param) -> Result
) {
    private val channel = Channel<Param>(Channel.CONFLATED)

    suspend fun run(onResult: (Result) -> Unit) {
        channel.receiveAsFlow()
            .distinctUntilChanged()
            .collect { param -> onResult(execute(param)) }
    }

    fun send(param: Param) {
        channel.offer(param)
    }

    fun close() {
        channel.close()
    }
}
