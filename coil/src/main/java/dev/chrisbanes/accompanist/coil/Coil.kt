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
import androidx.compose.launchInComposition
import androidx.compose.remember
import androidx.compose.setValue
import androidx.compose.state
import androidx.core.graphics.drawable.toBitmap
import androidx.ui.core.Alignment
import androidx.ui.core.ContentScale
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.Image
import androidx.ui.graphics.ColorFilter
import androidx.ui.graphics.ImageAsset
import androidx.ui.graphics.asImageAsset
import androidx.ui.graphics.painter.ImagePainter
import androidx.ui.graphics.painter.Painter
import androidx.ui.unit.IntPxSize
import coil.Coil
import coil.decode.DataSource
import coil.request.GetRequest
import coil.request.GetRequestBuilder

/**
 * Creates a composable that will attempt to load the given [data] using [Coil], and then
 * display the result in an [Image].
 *
 * @param data The data to load. See [GetRequestBuilder.data] for the types allowed.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
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
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 */
@Composable
fun CoilImage(
    data: Any,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    getSuccessPainter: @Composable ((SuccessResult) -> Painter)? = null,
    getFailurePainter: @Composable ((ErrorResult) -> Painter?)? = null,
    loading: @Composable (() -> Unit)? = null,
    onRequestCompleted: (RequestResult) -> Unit = emptySuccessLambda
) {
    CoilImage(
        request = when (data) {
            // If the developer is accidentally using the wrong function (data vs request), just
            // pass the request through
            is GetRequest -> data
            // Otherwise we construct a GetRequest using the data parameter
            else -> remember(data) {
                GetRequest.Builder(ContextAmbient.current).data(data).build()
            }
        },
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
 * Creates a composable that will attempt to load the given [request] using [Coil], and then
 * display the result in an [Image].
 *
 * @param request The request to execute. If the request does not have a [GetRequest.sizeResolver]
 * set, one will be set on the request using the layout constraints.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
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
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 */
@Composable
fun CoilImage(
    request: GetRequest,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    getSuccessPainter: @Composable ((SuccessResult) -> Painter)? = null,
    getFailurePainter: @Composable ((ErrorResult) -> Painter?)? = null,
    loading: @Composable (() -> Unit)? = null,
    onRequestCompleted: (RequestResult) -> Unit = emptySuccessLambda
) {
    var result by state<RequestResult?> { null }

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
    val callback = state { onRequestCompleted }
    callback.value = onRequestCompleted

    // GetRequest does not support object equality (as of Coil v0.10.1) so we can not key the
    // remember() using the request itself. For now we just use the [data] field, but
    // ideally this should use [request] to track changes in size, transformations, etc too.
    // See: https://github.com/coil-kt/coil/issues/405
    val requestActor = remember(request.data) { CoilRequestActor(request) }

    launchInComposition(requestActor) {
        // Launch the Actor
        requestActor.run { _, actorResult ->
            // Store the result
            result = actorResult

            if (actorResult != null) {
                // Execute the onRequestCompleted callback if we have a new result
                callback.value(actorResult)
            }
        }
    }

    val painter = when (val r = result) {
        is SuccessResult -> {
            if (getSuccessPainter != null) {
                getSuccessPainter(r)
            } else {
                defaultSuccessPainterGetter(r)
            }
        }
        is ErrorResult -> {
            if (getFailurePainter != null) {
                getFailurePainter(r)
            } else {
                defaultFailurePainterGetter(r)
            }
        }
        else -> null
    }

    val mod = modifier.onSizeChanged { size ->
        // When the size changes, send it to the request actor
        requestActor.send(size)
    }

    if (painter == null) {
        // If we don't have a result painter, we add a Box with our modifier
        Box(mod) {
            // If we don't have a result yet, we can show the loading content
            // (if not null)
            if (result == null && loading != null) {
                loading()
            }
        }
    } else {
        Image(
            painter = painter,
            contentScale = contentScale,
            alignment = alignment,
            colorFilter = colorFilter,
            modifier = mod
        )
    }
}

private fun CoilRequestActor(
    request: GetRequest
) = RequestActor<IntPxSize, RequestResult?> { size ->
    when {
        request.sizeResolver != null -> {
            // If the request has a sizeResolver set, we just execute the request as-is
            request
        }
        size != IntPxSize.Zero -> {
            // If we have a non-zero size, we can modify the request to include the size
            request.newBuilder()
                .size(size.width.value, size.height.value)
                .build()
        }
        else -> {
            // Otherwise we have a zero size, so no point executing a request
            null
        }
    }?.let { transformedRequest ->
        // Now execute the request in Coil...
        Coil.imageLoader(transformedRequest.context)
            .execute(transformedRequest)
            .toResult()
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

private fun coil.request.RequestResult.toResult(): RequestResult {
    return when (this) {
        is coil.request.SuccessResult -> SuccessResult(this)
        is coil.request.ErrorResult -> ErrorResult(this)
    }
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
