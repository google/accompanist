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

@file:JvmName("CoilImage")
@file:JvmMultifileClass

package dev.chrisbanes.accompanist.coil

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.launchInComposition
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.stateFor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.WithConstraints
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageAsset
import androidx.compose.ui.graphics.asImageAsset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.request.ImageRequest
import coil.request.ImageResult

/**
 * Creates a composable that will attempt to load the given [data] using [Coil], and provides
 * complete content of how the current state is displayed:
 *
 * ```
 * CoilImage(
 *   data = "https://www.image.url",
 * ) { imageState ->
 *   when (imageState) {
 *     is CoilImageState.Success -> // TODO
 *     is CoilImageState.Error -> // TODO
 *     CoilImageState.Loading -> // TODO
 *     CoilImageState.Empty -> // TODO
 *   }
 * }
 * ```
 *
 * @param data The data to load. See [ImageRequest.Builder.data] for the types allowed.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param imageLoader The [ImageLoader] to use when requesting the image. Defaults to [Coil]'s
 * default image loader.
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 * @param content Content to be displayed for the given state.
 */
@Composable
fun CoilImage(
    data: Any,
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader = Coil.imageLoader(ContextAmbient.current),
    shouldRefetchOnSizeChange: (currentResult: CoilImageState, size: IntSize) -> Boolean = defaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (CoilImageState) -> Unit = emptySuccessLambda,
    content: @Composable (imageState: CoilImageState) -> Unit
) {
    CoilImage(
        request = data.toImageRequest(),
        modifier = modifier,
        imageLoader = imageLoader,
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        onRequestCompleted = onRequestCompleted,
        content = content
    )
}

/**
 * Creates a composable that will attempt to load the given [request] using [Coil], and provides
 * complete content of how the current state is displayed:
 *
 * ```
 * CoilImage(
 *   request = ImageRequest.Builder(context).data(...).build(),
 * ) { imageState ->
 *   when (imageState) {
 *     is CoilImageState.Success -> // TODO
 *     is CoilImageState.Error -> // TODO
 *     CoilImageState.Loading -> // TODO
 *     CoilImageState.Empty -> // TODO
 *   }
 * }
 * ```
 *
 * @param request The request to execute. If the request does not have a [ImageRequest.sizeResolver]
 * set, one will be set on the request using the layout constraints.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param imageLoader The [ImageLoader] to use when requesting the image. Defaults to [Coil]'s
 * default image loader.
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 * @param content Content to be displayed for the given state.
 */
@Composable
fun CoilImage(
    request: ImageRequest,
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader = Coil.imageLoader(ContextAmbient.current),
    shouldRefetchOnSizeChange: (currentResult: CoilImageState, size: IntSize) -> Boolean = defaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (CoilImageState) -> Unit = emptySuccessLambda,
    content: @Composable (imageState: CoilImageState) -> Unit
) {
    var state by stateFor<CoilImageState>(request) { CoilImageState.Empty }

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

    val requestActor = remember(imageLoader, request) {
        CoilRequestActor(imageLoader, request)
    }

    launchInComposition(requestActor) {
        // Launch the Actor
        requestActor.run { _, newState ->
            // Update the result state
            state = newState

            if (newState is CoilImageState.Success || newState is CoilImageState.Error) {
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
            width = if (constraints.hasBoundedWidth) constraints.maxWidth else UNSPECIFIED,
            height = if (constraints.hasBoundedHeight) constraints.maxHeight else UNSPECIFIED
        )

        val lastSize = lastRequestedSize.value
        if (lastSize == null ||
            (lastSize != requestSize && shouldRefetchOnSizeChange(state, requestSize))
        ) {
            requestActor.send(requestSize)
            lastRequestedSize.value = requestSize
        }

        content(state)
    }
}

/**
 * Creates a composable that will attempt to load the given [data] using [Coil], and then
 * display the result in an [Image].
 *
 * This version of the function is more opinionated, providing:
 *
 * - Support for displaying alternative content while the request is 'loading'.
 *   See the [loading] parameter.
 * - Support for displaying alternative content if the request was unsuccessful.
 *   See the [error] parameter.
 * - Support for automatically fading-in the image once loaded. See the [fadeIn] parameter.
 *
 * ```
 * CoilImage(
 *   data = "https://www.image.url",
 *   fadeIn = true,
 *   loading = {
 *     Stack(Modifier.fillMaxSize()) {
 *       CircularProgressIndicator(Modifier.align(Alignment.Center))
 *     }
 *   }
 * )
 * ```
 *
 * @param data The data to load. See [ImageRequest.Builder.data] for the types allowed.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param alignment Optional alignment parameter used to place the loaded [ImageAsset] in the
 * given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 * used if the bounds are a different size from the intrinsic size of the loaded [ImageAsset].
 * @param colorFilter Optional colorFilter to apply for the [Painter] when it is rendered onscreen.
 * @param error Content to be displayed when the request failed.
 * @param loading Content to be displayed when the request is in progress.
 * @param fadeIn Whether to run a fade-in animation when images are successfully loaded.
 * Default: `false`.
 * @param imageLoader The [ImageLoader] to use when requesting the image. Defaults to [Coil]'s
 * default image loader.
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 */
@Composable
fun CoilImage(
    data: Any,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    fadeIn: Boolean = false,
    imageLoader: ImageLoader = Coil.imageLoader(ContextAmbient.current),
    shouldRefetchOnSizeChange: (currentResult: CoilImageState, size: IntSize) -> Boolean = defaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (CoilImageState) -> Unit = emptySuccessLambda,
    error: @Composable ((CoilImageState.Error) -> Unit)? = null,
    loading: @Composable (() -> Unit)? = null,
) {
    CoilImage(
        request = data.toImageRequest(),
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        colorFilter = colorFilter,
        fadeIn = fadeIn,
        imageLoader = imageLoader,
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        onRequestCompleted = onRequestCompleted,
        error = error,
        loading = loading
    )
}

/**
 * Creates a composable that will attempt to load the given [request] using [Coil], and then
 * display the result in an [Image].
 *
 * This version of the function is more opinionated, providing:
 *
 * - Support for displaying alternative content while the request is 'loading'.
 *   See the [loading] parameter.
 * - Support for displaying alternative content if the request was unsuccessful.
 *   See the [error] parameter.
 * - Support for automatically fading-in the image once loaded. See the [fadeIn] parameter.
 *
 * ```
 * CoilImage(
 *   request = ImageRequest.Builder(context).data(...).build(),
 *   fadeIn = true,
 *   loading = {
 *     Stack(Modifier.fillMaxSize()) {
 *       CircularProgressIndicator(Modifier.align(Alignment.Center))
 *     }
 *   }
 * )
 * ```
 *
 * @param request The request to execute. If the request does not have a [ImageRequest.sizeResolver]
 * set, one will be set on the request using the layout constraints.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param alignment Optional alignment parameter used to place the loaded [ImageAsset] in the
 * given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 * used if the bounds are a different size from the intrinsic size of the loaded [ImageAsset].
 * @param colorFilter Optional colorFilter to apply for the [Painter] when it is rendered onscreen.
 * @param error Content to be displayed when the request failed.
 * @param loading Content to be displayed when the request is in progress.
 * @param fadeIn Whether to run a fade-in animation when images are successfully loaded. Default: `false`.
 * @param imageLoader The [ImageLoader] to use when requesting the image. Defaults to [Coil]'s
 * default image loader.
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 */
@Composable
fun CoilImage(
    request: ImageRequest,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    fadeIn: Boolean = false,
    imageLoader: ImageLoader = Coil.imageLoader(ContextAmbient.current),
    shouldRefetchOnSizeChange: (currentResult: CoilImageState, size: IntSize) -> Boolean = defaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (CoilImageState) -> Unit = emptySuccessLambda,
    error: @Composable ((CoilImageState.Error) -> Unit)? = null,
    loading: @Composable (() -> Unit)? = null,
) {
    CoilImage(
        request = request,
        modifier = modifier,
        imageLoader = imageLoader,
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        onRequestCompleted = onRequestCompleted,
    ) { imageState ->
        when (imageState) {
            is CoilImageState.Success -> {
                MaterialLoadingImage(
                    result = imageState,
                    fadeInEnabled = fadeIn,
                    alignment = alignment,
                    contentScale = contentScale,
                    colorFilter = colorFilter
                )
            }
            is CoilImageState.Error -> if (error != null) error(imageState)
            CoilImageState.Loading -> if (loading != null) loading()
            CoilImageState.Empty -> Unit
        }
    }
}

/**
 * Value for a [IntSize] dimension, where the dimension is not specified or is unknown.
 */
private const val UNSPECIFIED = -1

@Stable
private data class MutableRef<T>(var value: T)

private fun CoilRequestActor(
    imageLoader: ImageLoader,
    request: ImageRequest
) = RequestActor<IntSize, CoilImageState> { size, onResult ->
    // First, send the loading state
    onResult(CoilImageState.Loading)

    val transformedRequest = when {
        // If the request has a size resolver set we just execute the request as-is
        request.defined.sizeResolver != null -> request
        // If the size contains an unspecified dimension, we don't specify a size
        // in the Coil request
        size.width == UNSPECIFIED || size.height == UNSPECIFIED -> request
        // If we have a non-zero size, we can modify the request to include the size
        size != IntSize.Zero -> request.newBuilder().size(size.width, size.height).build()
        // Otherwise we have a zero size, so no point executing a request
        else -> null
    }

    if (transformedRequest != null) {
        // Now execute the request in Coil...
        imageLoader
            .execute(transformedRequest)
            .toResult(size)
            .also { imageState ->
                // Tell RenderThread to pre-upload this bitmap. Saves the GPU upload cost on the
                // first draw. See https://github.com/square/picasso/issues/1620 for a explanation
                // from @ChrisCraik
                when (imageState) {
                    is CoilImageState.Success -> imageState.image.prepareToDraw()
                    is CoilImageState.Error -> imageState.image?.prepareToDraw()
                }
            }
            .also { state ->
                // Send the result
                onResult(state)
            }
    } else {
        // If we don't have a request to execute, send empty
        onResult(CoilImageState.Empty)
    }
}

@Deprecated(
    message = "Use RequestState",
    replaceWith = ReplaceWith("RequestState", "dev.chrisbanes.accompanist.coil.LoadState")
)
@Suppress("unused")
typealias RequestResult = CoilImageState

@Deprecated(
    message = "Use CoilImageState.Success",
    replaceWith = ReplaceWith(
        "LoadState.Success",
        "dev.chrisbanes.accompanist.coil.CoilImageState.Success"
    )
)
@Suppress("unused")
typealias SuccessResult = CoilImageState.Success

@Deprecated(
    message = "Use CoilImageState.Error",
    replaceWith = ReplaceWith(
        "LoadState.Error",
        "dev.chrisbanes.accompanist.coil.CoilImageState.Error"
    )
)
@Suppress("unused")
typealias ErrorResult = CoilImageState.Error

/**
 * Represents the state of a [CoilImage]
 */
sealed class CoilImageState {
    /**
     * Indicates that a request is not in progress.
     */
    object Empty : CoilImageState()

    /**
     * Indicates that the request is currently in progress.
     */
    object Loading : CoilImageState()

    /**
     * Indicates that the request completed successfully.
     *
     * @param image The result image.
     * @param source The data source that the image was loaded from.
     */
    data class Success(
        val image: ImageAsset,
        val source: DataSource
    ) : CoilImageState() {
        internal constructor(result: coil.request.SuccessResult, fallbackSize: IntSize) : this(
            image = result.drawable.toImageAsset(fallbackSize),
            source = result.metadata.dataSource
        )
    }

    /**
     * Indicates that an error occurred while executing the request.
     *
     * @param image The error image.
     * @param throwable The error that failed the request.
     */
    data class Error(
        val image: ImageAsset?,
        val throwable: Throwable
    ) : CoilImageState() {
        internal constructor(result: coil.request.ErrorResult, fallbackSize: IntSize) : this(
            image = result.drawable?.toImageAsset(fallbackSize),
            throwable = result.throwable
        )
    }
}

private fun ImageResult.toResult(
    fallbackSize: IntSize = IntSize.Zero
): CoilImageState = when (this) {
    is coil.request.SuccessResult -> CoilImageState.Success(this, fallbackSize)
    is coil.request.ErrorResult -> CoilImageState.Error(this, fallbackSize)
}

@Composable
internal fun Any.toImageRequest(): ImageRequest {
    // If the developer is accidentally using the wrong function (data vs request), just
    // pass the request through
    return if (this is ImageRequest) this else {
        // Otherwise we construct a GetRequest using the data parameter
        val context = ContextAmbient.current
        remember(this) { ImageRequest.Builder(context).data(this).build() }
    }
}

internal val emptySuccessLambda: (CoilImageState) -> Unit = {}

internal val defaultRefetchOnSizeChangeLambda: (CoilImageState, IntSize) -> Boolean = { _, _ -> false }

internal fun Drawable.toImageAsset(fallbackSize: IntSize): ImageAsset {
    return toBitmap(
        width = if (intrinsicWidth > 0) intrinsicWidth else fallbackSize.width,
        height = if (intrinsicHeight > 0) intrinsicHeight else fallbackSize.height
    ).asImageAsset()
}
