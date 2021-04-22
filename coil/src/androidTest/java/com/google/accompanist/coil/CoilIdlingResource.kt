/*
 * Copyright 2021 The Android Open Source Project
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

package com.google.accompanist.coil

import androidx.compose.ui.test.IdlingResource
import coil.EventListener
import coil.annotation.ExperimentalCoilApi
import coil.request.ImageRequest
import coil.request.ImageResult

/**
 * An [IdlingResource] implemented as a Coil [EventListener].
 */
@OptIn(ExperimentalCoilApi::class)
class CoilIdlingResource : EventListener, IdlingResource {
    private val ongoingRequests = mutableSetOf<ImageRequest>()

    var finishedRequests = 0

    override val isIdleNow: Boolean
        get() = ongoingRequests.isEmpty()

    override fun onStart(request: ImageRequest) {
        ongoingRequests.add(request)
    }

    override fun onCancel(request: ImageRequest) {
        ongoingRequests.remove(request)
    }

    override fun onError(request: ImageRequest, throwable: Throwable) {
        ongoingRequests.remove(request)
        finishedRequests++
    }

    override fun onSuccess(request: ImageRequest, metadata: ImageResult.Metadata) {
        ongoingRequests.remove(request)
        finishedRequests++
    }
}
