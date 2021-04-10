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

package com.google.accompanist.imageloading

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.cancellation.CancellationException

/**
 * A state base class that can be hoisted to control image loads for [rememberLoadPainter].
 *
 * @param shouldRefetchOnSizeChange Initial value for [shouldRefetchOnSizeChange].
 */
@Stable
abstract class ImageState<R : Any>(
    shouldRefetchOnSizeChange: (currentState: ImageLoadState, size: IntSize) -> Boolean,
) {
    /**
     * The current request object.
     *
     * Extending classes should return the current request object, which should be backed by
     * a [androidx.compose.runtime.State] instance.
     */
    protected abstract val request: R?

    /**
     * The current [ImageLoadState].
     */
    var loadState by mutableStateOf<ImageLoadState>(ImageLoadState.Empty)
        private set

    /**
     * Lambda which will be invoked when the size changes, allowing
     * optional re-fetching of the image.
     */
    var shouldRefetchOnSizeChange by mutableStateOf(shouldRefetchOnSizeChange)

    /**
     * The function which executes the requests, and update [loadState] as appropriate with the
     * result.
     */
    internal suspend fun execute(request: R?, size: IntSize) {
        if (request == null) {
            // If we don't have a request, set our state to Empty and return
            loadState = ImageLoadState.Empty
            return
        }

        if (loadState != ImageLoadState.Empty &&
            request == loadState.request &&
            !shouldRefetchOnSizeChange(loadState, size)
        ) {
            // If we're not empty, the request is the same and shouldRefetchOnSizeChange()
            // returns false, return now to skip this request
            return
        }

        // Otherwise we're about to start a request, so set us to 'Loading'
        loadState = ImageLoadState.Loading

        loadState = try {
            executeRequest(request, size)
        } catch (ce: CancellationException) {
            // We specifically don't do anything for the request coroutine being
            // cancelled: https://github.com/google/accompanist/issues/217
            throw ce
        } catch (e: Error) {
            // Re-throw all Errors
            throw e
        } catch (e: IllegalStateException) {
            // Re-throw all IllegalStateExceptions
            throw e
        } catch (e: IllegalArgumentException) {
            // Re-throw all IllegalArgumentExceptions
            throw e
        } catch (t: Throwable) {
            // Anything else, we wrap in a Error state instance
            ImageLoadState.Error(result = null, throwable = t, request = request)
        }
    }

    /**
     * Extending classes should implement this function to execute the [request] with the given
     * [size] constraints.
     */
    protected abstract suspend fun executeRequest(request: R, size: IntSize): ImageLoadState

    internal val internalRequestFlow: Flow<R?> get() = snapshotFlow { request }
    internal val internalRequest get() = request
}
