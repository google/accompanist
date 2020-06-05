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

import android.graphics.drawable.Drawable
import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.onCommit
import androidx.compose.remember
import androidx.compose.setValue
import androidx.compose.stateFor
import androidx.core.graphics.drawable.toBitmap
import androidx.ui.core.Alignment
import androidx.ui.core.Constraints
import androidx.ui.core.ContentScale
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.WithConstraints
import androidx.ui.core.hasBoundedHeight
import androidx.ui.core.hasBoundedWidth
import androidx.ui.core.hasFixedHeight
import androidx.ui.core.hasFixedWidth
import androidx.ui.foundation.Box
import androidx.ui.foundation.Image
import androidx.ui.graphics.ColorFilter
import androidx.ui.graphics.ImageAsset
import androidx.ui.graphics.asImageAsset
import androidx.ui.graphics.painter.ImagePainter
import androidx.ui.graphics.painter.Painter
import coil.Coil
import coil.decode.DataSource
import coil.request.GetRequest
import coil.request.GetRequestBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val Constraints.requestWidth
    get() = if (hasFixedWidth || hasBoundedWidth) maxWidth else minWidth

private val Constraints.requestHeight
    get() = if (hasFixedHeight || hasBoundedHeight) maxHeight else minHeight

/**
 * Creates a composable that will attempt to load the given [data] using [Coil], and then
 * display the result in an [Image].
 *
 * @param data The data to load. See [GetRequestBuilder.data] for the types allowed.
 * @param alignment Optional alignment parameter used to place the loaded [ImageAsset] in the
 * given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 * used if the bounds are a different size from the intrinsic size of the loaded [ImageAsset].
 * @param colorFilter Optional colorFilter to apply for the [Painter] when it is rendered onscreen.
 * @param getSuccessPainter Optional builder for the [Painter] to be used to draw the successful
 * loading result. Passing in `null` will result in falling back to the default [Painter].
 * @param getFailurePainter Optional builder for the [Painter] to be used to draw the failure
 * loading result. Passing in `null` will result in falling back to the default [Painter].
 * @param loading Content to be displayed when the request is in progress.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content (ex.
 * background)
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 */
@Composable
fun CoilImage(
    data: Any,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    getSuccessPainter: @Composable ((SuccessResult) -> Painter)? = null,
    getFailurePainter: @Composable ((ErrorResult) -> Painter?)? = null,
    loading: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onRequestCompleted: (RequestResult) -> Unit = emptySuccessLambda
) {
    CoilImage(
        request = GetRequest.Builder(ContextAmbient.current).data(data).build(),
        alignment = alignment,
        contentScale = contentScale,
        colorFilter = colorFilter,
        onRequestCompleted = onRequestCompleted,
        getSuccessPainter = getSuccessPainter,
        getFailurePainter = getFailurePainter,
        loading = loading,
        modifier = modifier
    )
}

/**
 * Creates a composable that will attempt to load the given [data] using [Coil], and then
 * display the result in an [Image].
 *
 * @param request The request to execute. If the request does not have a [GetRequest.sizeResolver]
 * set, one will be set on the request using the layout constraints.
 * @param alignment Optional alignment parameter used to place the loaded [ImageAsset] in the
 * given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 * used if the bounds are a different size from the intrinsic size of the loaded [ImageAsset].
 * @param colorFilter Optional colorFilter to apply for the [Painter] when it is rendered onscreen.
 * @param getSuccessPainter Optional builder for the [Painter] to be used to draw the successful
 * loading result. Passing in `null` will result in falling back to the default [Painter].
 * @param getFailurePainter Optional builder for the [Painter] to be used to draw the failure
 * loading result. Passing in `null` will result in falling back to the default [Painter].
 * @param loading Content to be displayed when the request is in progress.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content (ex.
 * background)
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 */
@Composable
fun CoilImage(
    request: GetRequest,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    getSuccessPainter: @Composable ((SuccessResult) -> Painter)? = null,
    getFailurePainter: @Composable ((ErrorResult) -> Painter?)? = null,
    loading: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onRequestCompleted: (RequestResult) -> Unit = emptySuccessLambda
) {
    WithConstraints(modifier) {
        val requestWidth = constraints.requestWidth.value
        val requestHeight = constraints.requestHeight.value

        // Execute the request using executeAsComposable(), which guards the actual execution
        // so that the request is only run if the request changes.
        val result = when {
            request.sizeResolver != null -> {
                // If the request has a sizeResolver set, we just execute the request as-is
                request.executeAsComposable()
            }
            requestWidth > 0 && requestHeight > 0 -> {
                // If we have a non-zero size, we can modify the request to include the size
                request.newBuilder()
                    .size(requestWidth, requestHeight)
                    .build()
                    .executeAsComposable()
            }
            else -> {
                // Otherwise we have a zero size, so no point executing a request
                null
            }
        }

        onCommit(result) {
            if (result != null) {
                onRequestCompleted(result)
            }
        }

        val painter = when (result) {
            is SuccessResult -> {
                if (getSuccessPainter != null) {
                    getSuccessPainter(result)
                } else {
                    defaultSuccessPainterGetter(result)
                }
            }
            is ErrorResult -> {
                if (getFailurePainter != null) {
                    getFailurePainter(result)
                } else {
                    defaultFailurePainterGetter(result)
                }
            }
            else -> null
        }

        if (result == null && loading != null) {
            Box(modifier, children = loading)
        } else if (painter != null) {
            Image(
                painter = painter,
                contentScale = contentScale,
                alignment = alignment,
                colorFilter = colorFilter,
                modifier = modifier
            )
        }
    }
}

/**
 * Represents the result of an image request.
 */
sealed class RequestResult {
    abstract val image: ImageAsset?
}

/**
 * Indicates that the request completed successfully.
 *
 * @param image The result image.
 * @param source The data source that the image was loaded from.
 */
data class SuccessResult(
    override val image: ImageAsset,
    val source: DataSource
) : RequestResult() {
    internal constructor(result: coil.request.SuccessResult) : this(
        image = result.drawable.toImageAsset(),
        source = result.source
    )
}

/**
 * Indicates that an error occurred while executing the request.
 *
 * @param image The error image.
 * @param throwable The error that failed the request.
 */
data class ErrorResult(
    override val image: ImageAsset?,
    val throwable: Throwable
) : RequestResult() {
    internal constructor(result: coil.request.ErrorResult) : this(
        image = result.drawable?.toImageAsset(),
        throwable = result.throwable
    )
}

/**
 * This will execute the [GetRequest] within a composable, ensuring that the request is only
 * execute once and storing the result, and cancelling requests as required.
 *
 * @return the result from the request execution, or `null` if the request has not finished yet.
 */
@Composable
fun GetRequest.executeAsComposable(): RequestResult? {
    // GetRequest does not support object equality (as of v0.10.1) so we can not key off the
    // request itself. For now we can just use the `data` parameter, but ideally this should use
    // `this` to track changes in size, transformations, etc too.
    // See https://github.com/coil-kt/coil/issues/405
    val key = data

    var result by stateFor<RequestResult?>(key) { null }

    // Launch and execute a new request when it changes
    onCommit(key) {
        val job = CoroutineScope(Dispatchers.Main).launch {
            // Start loading the image and await the result
            result = Coil.imageLoader(context).execute(this@executeAsComposable).let {
                // We map to our internal result entities
                when (it) {
                    is coil.request.SuccessResult -> SuccessResult(it)
                    is coil.request.ErrorResult -> ErrorResult(it)
                }
            }
        }

        // Cancel the request if the input to onCommit changes or
        // the Composition is removed from the composition tree.
        onDispose { job.cancel() }
    }

    return result
}

@Composable
internal fun defaultFailurePainterGetter(error: ErrorResult): Painter? {
    return error.image?.let { image ->
        remember(image) { ImagePainter(image) }
    }
}

@Composable
internal fun defaultSuccessPainterGetter(result: SuccessResult): Painter {
    return remember(result.image) { ImagePainter(result.image) }
}

internal val emptySuccessLambda: (RequestResult) -> Unit = {}

internal fun Drawable.toImageAsset(): ImageAsset {
    return toBitmap().asImageAsset()
}
