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

@file:JvmName("ImageLoad")
@file:JvmMultifileClass

package dev.chrisbanes.accompanist.imageloading

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.stateFor
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.WithConstraints
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * A generic image loading composable, which provides hooks for image loading libraries to use.
 * Apps shouldn't generally use this function, instead preferring one of the extension libraries
 * which build upon this, such as the Coil library.
 *
 * The [executeRequest] parameters allows providing of a lambda to execute the 'image load'.
 * The [R] type and [request] parameter should be whatever primitive the library uses to
 * model a request. The [TR] type would normally be the same as [R], but allows transforming of
 * the request for execution (say to wrap with extra information).
 *
 * @param request The request to execute.
 * @param executeRequest Suspending lambda to execute an image loading request.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param requestKey The object to key this request on. If the request type supports equality then
 * the default value will work. Otherwise pass in the `data` value.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param transformRequestForSize Optionally transform [request] for the given [IntSize].
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 * @param content Content to be displayed for the given state.
 */
@Composable
fun <R : Any, TR : Any> ImageLoad(
    request: R,
    executeRequest: suspend (TR) -> ImageLoadState,
    modifier: Modifier = Modifier,
    requestKey: Any = request,
    transformRequestForSize: (R, IntSize) -> TR?,
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = DefaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (ImageLoadState) -> Unit = EmptyRequestCompleteLambda,
    content: @Composable (imageLoadState: ImageLoadState) -> Unit
) {
    var state by stateFor<ImageLoadState>(requestKey) { ImageLoadState.Empty }

    // This may look a little weird, but allows the launchInComposition callback to always
    // invoke the last provided [onRequestCompleted].
    //
    // If a composition happens *after* launchInComposition has launched, the given
    // [onRequestCompleted] might have changed. If the actor lambda below directly referenced
    // [onRequestCompleted] it would have captured access to the initial onRequestCompleted
    // value, not the latest.
    //
    // This `callback` state enables the actor lambda to only capture the remembered state
    // reference, which we can update on each composition.
    val callback = remember { mutableStateOf(onRequestCompleted, referentialEqualityPolicy()) }
    callback.value = onRequestCompleted

    val requestActor = remember(requestKey) {
        ImageLoadRequestActor(executeRequest)
    }

    LaunchedEffect(requestActor) {
        // Launch the Actor
        requestActor.run { _, newState ->
            // Update the result state
            state = newState

            if (newState is ImageLoadState.Success || newState is ImageLoadState.Error) {
                callback.value(newState)
            }
        }
    }

    WithConstraints(modifier) {
        // We remember the last size in a MutableRef (below) rather than a MutableState.
        // This is because we don't need value changes to trigger a re-composition, we are only
        // using it to store the last value.
        val lastRequestedSize = remember(requestActor) { MutableRef<IntSize?>(null) }

        val requestSize = IntSize(
            width = if (constraints.hasBoundedWidth) constraints.maxWidth else -1,
            height = if (constraints.hasBoundedHeight) constraints.maxHeight else -1
        )

        val lastSize = lastRequestedSize.value
        if (lastSize == null ||
            (lastSize != requestSize && shouldRefetchOnSizeChange(state, requestSize))
        ) {
            val transformedRequest = transformRequestForSize(request, requestSize)
            if (transformedRequest != null) {
                requestActor.send(transformedRequest)
                lastRequestedSize.value = requestSize
            } else {
                // If the transform request is null, set our state to empty
                state = ImageLoadState.Empty
            }
        }

        content(state)
    }
}

/**
 * A simple mutable reference holder. Used as a replacement for [MutableState] when you don't need
 * the recomposition triggers.
 */
@Stable
private data class MutableRef<T>(var value: T)

private fun <T> ImageLoadRequestActor(
    execute: suspend (T) -> ImageLoadState
) = RequestActor<T, ImageLoadState> { request ->
    flow {
        // First, send the loading state
        emit(ImageLoadState.Loading)
        // Now execute the request in Coil...
        emit(execute(request))
    }.catch { throwable ->
        emit(ImageLoadState.Error(painter = null, throwable = throwable))
    }
}

/**
 * Empty lamdba for use in the `onRequestCompleted` parameter.
 */
val EmptyRequestCompleteLambda: (ImageLoadState) -> Unit = {}

/**
 * Default lamdba for use in the `shouldRefetchOnSizeChange` parameter.
 */
val DefaultRefetchOnSizeChangeLambda: (ImageLoadState, IntSize) -> Boolean = { _, _ -> false }
