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

package com.google.accompanist.coil

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import coil.Coil
import coil.ImageLoader
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.ImageResult
import com.google.accompanist.imageloading.DataSource
import com.google.accompanist.imageloading.DefaultRefetchOnSizeChangeLambda
import com.google.accompanist.imageloading.EmptyRequestCompleteLambda
import com.google.accompanist.imageloading.ImageLoad
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.imageloading.MaterialLoadingImage
import com.google.accompanist.imageloading.toPainter

/**
 * Composition local containing the preferred [ImageLoader] to use in [CoilImage].
 */
val LocalImageLoader = staticCompositionLocalOf<ImageLoader?> { null }

/**
 * Contains some default values used for [CoilImage].
 */
object CoilImageDefaults {
    /**
     * Returns the default [ImageLoader] value for the `imageLoader` parameter in [CoilImage].
     */
    @Composable
    fun defaultImageLoader(): ImageLoader {
        return LocalImageLoader.current ?: LocalContext.current.imageLoader
    }
}

/**
 * Creates a composable that will attempt to load the given [data] using [Coil], and provides
 * complete content of how the current state is displayed:
 *
 * ```
 * CoilImage(
 *   data = "https://www.image.url",
 * ) { imageState ->
 *   when (imageState) {
 *     is ImageLoadState.Success -> // TODO
 *     is ImageLoadState.Error -> // TODO
 *     ImageLoadState.Loading -> // TODO
 *     ImageLoadState.Empty -> // TODO
 *   }
 * }
 * ```
 *
 * @param data The data to load. See [ImageRequest.Builder.data] for the types allowed.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param requestBuilder Optional builder for the [ImageRequest].
 * @param imageLoader The [ImageLoader] to use when requesting the image. Defaults to
 * [CoilImageDefaults.defaultImageLoader].
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 * @param content Content to be displayed for the given state.
 */
@Composable
fun CoilImage(
    data: Any,
    modifier: Modifier = Modifier,
    requestBuilder: (ImageRequest.Builder.(size: IntSize) -> ImageRequest.Builder)? = null,
    imageLoader: ImageLoader = CoilImageDefaults.defaultImageLoader(),
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = DefaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (ImageLoadState) -> Unit = EmptyRequestCompleteLambda,
    content: @Composable BoxScope.(imageLoadState: ImageLoadState) -> Unit
) {
    CoilImage(
        request = data.toImageRequest(),
        modifier = modifier,
        requestBuilder = requestBuilder,
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
 *     is ImageLoadState.Success -> // TODO
 *     is ImageLoadState.Error -> // TODO
 *     ImageLoadState.Loading -> // TODO
 *     ImageLoadState.Empty -> // TODO
 *   }
 * }
 * ```
 *
 * @param request The request to execute. If the request does not have a [ImageRequest.sizeResolver]
 * set, one will be set on the request using the layout constraints.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param requestBuilder Optional builder for the [ImageRequest].
 * @param imageLoader The [ImageLoader] to use when requesting the image. Defaults to
 * [CoilImageDefaults.defaultImageLoader].
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 * @param content Content to be displayed for the given state.
 */
@Composable
fun CoilImage(
    request: ImageRequest,
    modifier: Modifier = Modifier,
    requestBuilder: (ImageRequest.Builder.(size: IntSize) -> ImageRequest.Builder)? = null,
    imageLoader: ImageLoader = CoilImageDefaults.defaultImageLoader(),
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = DefaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (ImageLoadState) -> Unit = EmptyRequestCompleteLambda,
    content: @Composable BoxScope.(imageLoadState: ImageLoadState) -> Unit
) {
    ImageLoad(
        request = request,
        executeRequest = { imageLoader.execute(it).toResult() },
        transformRequestForSize = { r, size ->
            val sizedRequest = when {
                // If the request has a size resolver set we just execute the request as-is
                r.defined.sizeResolver != null -> r
                // If the size contains an unspecified sized dimension, we don't specify a size
                // in the Coil request
                size.width < 0 || size.height < 0 -> r
                // If we have a non-zero size in both dimensions, we can modify the request to include the size
                size.width > 0 || size.height > 0 -> r.newBuilder().size(size.width, size.height).build()
                // Otherwise we have a zero size, so no point executing a request
                else -> null
            }

            if (sizedRequest != null && requestBuilder != null) {
                // If we have a transformed request and builder, let it run
                requestBuilder(sizedRequest.newBuilder(), size).build()
            } else {
                // Otherwise we just return the sizedRequest
                sizedRequest
            }
        },
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        onRequestCompleted = onRequestCompleted,
        modifier = modifier,
        content = content
    )
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
 * @param contentDescription text used by accessibility services to describe what this image
 * represents. This should always be provided unless this image is used for decorative purposes,
 * and does not represent a meaningful action that a user can take. This text should be
 * localized, such as by using [androidx.compose.ui.res.stringResource] or similar.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param alignment Optional alignment parameter used to place the loaded [ImageBitmap] in the
 * given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 * used if the bounds are a different size from the intrinsic size of the loaded [ImageBitmap].
 * @param colorFilter Optional colorFilter to apply for the [Painter] when it is rendered onscreen.
 * @param error Content to be displayed when the request failed.
 * @param loading Content to be displayed when the request is in progress.
 * @param fadeIn Whether to run a fade-in animation when images are successfully loaded.
 * Default: `false`.
 * @param requestBuilder Optional builder for the [ImageRequest].
 * @param imageLoader The [ImageLoader] to use when requesting the image. Defaults to
 * [CoilImageDefaults.defaultImageLoader].
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 */
@Composable
fun CoilImage(
    data: Any,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    fadeIn: Boolean = false,
    requestBuilder: (ImageRequest.Builder.(size: IntSize) -> ImageRequest.Builder)? = null,
    imageLoader: ImageLoader = CoilImageDefaults.defaultImageLoader(),
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = DefaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (ImageLoadState) -> Unit = EmptyRequestCompleteLambda,
    error: @Composable (BoxScope.(ImageLoadState.Error) -> Unit)? = null,
    loading: @Composable (BoxScope.() -> Unit)? = null,
) {
    CoilImage(
        request = data.toImageRequest(),
        modifier = modifier,
        contentDescription = contentDescription,
        alignment = alignment,
        contentScale = contentScale,
        colorFilter = colorFilter,
        fadeIn = fadeIn,
        requestBuilder = requestBuilder,
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
 * @param contentDescription text used by accessibility services to describe what this image
 * represents. This should always be provided unless this image is used for decorative purposes,
 * and does not represent a meaningful action that a user can take. This text should be
 * localized, such as by using [androidx.compose.ui.res.stringResource] or similar.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param alignment Optional alignment parameter used to place the loaded [ImageBitmap] in the
 * given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 * used if the bounds are a different size from the intrinsic size of the loaded [ImageBitmap].
 * @param colorFilter Optional colorFilter to apply for the [Painter] when it is rendered onscreen.
 * @param error Content to be displayed when the request failed.
 * @param loading Content to be displayed when the request is in progress.
 * @param fadeIn Whether to run a fade-in animation when images are successfully loaded. Default: `false`.
 * @param requestBuilder Optional builder for the [ImageRequest].
 * @param imageLoader The [ImageLoader] to use when requesting the image. Defaults to
 * [CoilImageDefaults.defaultImageLoader].
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 */
@Composable
fun CoilImage(
    request: ImageRequest,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    fadeIn: Boolean = false,
    requestBuilder: (ImageRequest.Builder.(size: IntSize) -> ImageRequest.Builder)? = null,
    imageLoader: ImageLoader = CoilImageDefaults.defaultImageLoader(),
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = DefaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (ImageLoadState) -> Unit = EmptyRequestCompleteLambda,
    error: @Composable (BoxScope.(ImageLoadState.Error) -> Unit)? = null,
    loading: @Composable (BoxScope.() -> Unit)? = null,
) {
    CoilImage(
        request = request,
        modifier = modifier,
        requestBuilder = requestBuilder,
        imageLoader = imageLoader,
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        onRequestCompleted = onRequestCompleted,
    ) { imageState ->
        when (imageState) {
            is ImageLoadState.Success -> {
                MaterialLoadingImage(
                    result = imageState,
                    contentDescription = contentDescription,
                    fadeInEnabled = fadeIn,
                    alignment = alignment,
                    contentScale = contentScale,
                    colorFilter = colorFilter
                )
            }
            is ImageLoadState.Error -> if (error != null) error(imageState)
            ImageLoadState.Loading -> if (loading != null) loading()
            ImageLoadState.Empty -> Unit
        }
    }
}

private fun ImageResult.toResult(): ImageLoadState = when (this) {
    is coil.request.SuccessResult -> {
        ImageLoadState.Success(
            painter = drawable.toPainter(),
            source = metadata.dataSource.toDataSource()
        )
    }
    is coil.request.ErrorResult -> {
        ImageLoadState.Error(
            painter = drawable?.toPainter(),
            throwable = throwable
        )
    }
}

private fun coil.decode.DataSource.toDataSource(): DataSource = when (this) {
    coil.decode.DataSource.NETWORK -> DataSource.NETWORK
    coil.decode.DataSource.MEMORY -> DataSource.MEMORY
    coil.decode.DataSource.MEMORY_CACHE -> DataSource.MEMORY
    coil.decode.DataSource.DISK -> DataSource.DISK
}

@Composable
internal fun Any.toImageRequest(): ImageRequest {
    when (this) {
        is android.graphics.drawable.Drawable -> {
            throw IllegalArgumentException(
                "Unsupported type: Drawable." +
                    " If you wish to load a drawable, pass in the resource ID."
            )
        }
        is ImageBitmap -> {
            throw IllegalArgumentException(
                "Unsupported type: ImageBitmap." +
                    " If you wish to display this ImageBitmap, use androidx.compose.foundation.Image()"
            )
        }
        is ImageVector -> {
            throw IllegalArgumentException(
                "Unsupported type: ImageVector." +
                    " If you wish to display this ImageVector, use androidx.compose.foundation.Image()"
            )
        }
        is Painter -> {
            throw IllegalArgumentException(
                "Unsupported type: Painter." +
                    " If you wish to draw this Painter, use androidx.compose.foundation.Image()"
            )
        }
        // If the developer is accidentally using the wrong function (data vs request), just
        // pass the request through
        is ImageRequest -> return this
        else -> {
            // Otherwise we construct a GetRequest using the data parameter
            val context = LocalContext.current
            return remember(this) { ImageRequest.Builder(context).data(this).build() }
        }
    }
}
