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

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize

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
    content: @Composable BoxScope.(imageLoadState: ImageLoadState) -> Unit
) {
    val updatedOnRequestCompleted by rememberUpdatedState(onRequestCompleted)
    val updatedTransformRequestForSize by rememberUpdatedState(transformRequestForSize)
    val updatedExecuteRequest by rememberUpdatedState(executeRequest)

    var requestSize by remember(requestKey) { mutableStateOf<IntSize?>(null) }

    val loadState by produceState<ImageLoadState>(
        initialValue = ImageLoadState.Loading,
        key1 = requestKey,
        key2 = requestSize,
        key3 = updatedTransformRequestForSize,
    ) {
        value = requestSize?.let { updatedTransformRequestForSize(request, it) }
            ?.let { transformedRequest ->
                try {
                    updatedExecuteRequest(transformedRequest)
                } catch (throwable: Throwable) {
                    ImageLoadState.Error(painter = null, throwable = throwable)
                }.also(updatedOnRequestCompleted)
            } ?: ImageLoadState.Loading
    }

    BoxWithConstraints(
        modifier = modifier,
        propagateMinConstraints = true,
    ) {
        val size = IntSize(
            width = if (constraints.hasBoundedWidth) constraints.maxWidth else -1,
            height = if (constraints.hasBoundedHeight) constraints.maxHeight else -1
        )

        val lastSize = requestSize
        if (lastSize == null || (lastSize != size && shouldRefetchOnSizeChange(loadState, size))) {
            requestSize = size
        }

        content(loadState)
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
