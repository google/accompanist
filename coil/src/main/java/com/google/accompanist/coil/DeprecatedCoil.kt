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

@file:JvmName("CoilImage")
@file:JvmMultifileClass

@file:Suppress("DEPRECATION")

package com.google.accompanist.coil

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.imageloading.ImageSuchDeprecated
import com.google.accompanist.imageloading.isFinalState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter

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
 * [CoilImageStateDefaults.defaultImageLoader].
 * @param previewPlaceholder Drawable resource ID which will be displayed when this function is
 * ran in preview mode.
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 * @param content Content to be displayed for the given state.
 */
@Deprecated(
    message = "Replaced with Image() and rememberCoilImageState()",
    ReplaceWith(
        expression = """ImageLoad(
            request = rememberCoilImageLoadRequest(
                data = data,
                imageLoader = imageLoader,
                requestBuilder = requestBuilder,
            ),
            contentDescription = contentDescription,
            modifier = modifier,
            previewPlaceholder = previewPlaceholder,
            shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        )""",
        "com.google.accompanist.coil.rememberCoilImageLoadRequest",
        "com.google.accompanist.imageloading.ImageLoad"
    )
)
@Composable
fun CoilImage(
    data: Any,
    modifier: Modifier = Modifier,
    requestBuilder: (ImageRequest.Builder.(size: IntSize) -> ImageRequest.Builder)? = null,
    imageLoader: ImageLoader = CoilImageStateDefaults.defaultImageLoader(),
    @DrawableRes previewPlaceholder: Int = 0,
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = { _, _ -> false },
    onRequestCompleted: (ImageLoadState) -> Unit = {},
    content: @Composable BoxScope.(imageLoadState: ImageLoadState) -> Unit
) {
    val painter = rememberCoilPainter(
        data = data,
        requestBuilder = requestBuilder,
        imageLoader = imageLoader,
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        previewPlaceholder = previewPlaceholder,
    )

    LaunchedEffect(painter) {
        snapshotFlow { painter.loadState }
            .filter { it.isFinalState() }
            .collect { onRequestCompleted(it) }
    }

    @Suppress("DEPRECATION")
    ImageSuchDeprecated(
        loadPainter = painter,
        contentDescription = null,
        modifier = modifier,
        content = content
    )
}

/**
 * Creates a composable that will attempt to load the given [request] using [Coil], and provides
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
 * @param request The request to execute. If the request does not have a [ImageRequest.sizeResolver]
 * set, one will be set on the request using the layout constraints.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param requestBuilder Optional builder for the [ImageRequest].
 * @param imageLoader The [ImageLoader] to use when requesting the image. Defaults to
 * [CoilImageStateDefaults.defaultImageLoader].
 * @param previewPlaceholder Drawable resource ID which will be displayed when this function is
 * ran in preview mode.
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 * @param content Content to be displayed for the given state.
 */
@Deprecated(
    message = "Replaced with Image() and rememberCoilImageState()",
    ReplaceWith(
        expression = """ImageLoad(
            request = rememberCoilImageLoadRequest(
                data = request,
                imageLoader = imageLoader,
                requestBuilder = requestBuilder,
            ),
            contentDescription = contentDescription,
            modifier = modifier,
            previewPlaceholder = previewPlaceholder,
            shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        )""",
        "com.google.accompanist.coil.rememberCoilImageLoadRequest",
        "com.google.accompanist.imageloading.ImageLoad"
    )
)
@Composable
fun CoilImage(
    request: ImageRequest,
    modifier: Modifier = Modifier,
    requestBuilder: (ImageRequest.Builder.(size: IntSize) -> ImageRequest.Builder)? = null,
    imageLoader: ImageLoader = CoilImageStateDefaults.defaultImageLoader(),
    @DrawableRes previewPlaceholder: Int = 0,
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = { _, _ -> false },
    onRequestCompleted: (ImageLoadState) -> Unit = {},
    content: @Composable BoxScope.(imageLoadState: ImageLoadState) -> Unit
) {
    CoilImage(
        data = request,
        modifier = modifier,
        requestBuilder = requestBuilder,
        imageLoader = imageLoader,
        previewPlaceholder = previewPlaceholder,
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        onRequestCompleted = onRequestCompleted,
        content = content
    )
}

/**
 * Creates a composable that will attempt to load the given [data] using [coil.Coil], and then
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
 * [CoilImageStateDefaults.defaultImageLoader].
 * @param previewPlaceholder Drawable resource ID which will be displayed when this function is
 * ran in preview mode.
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 */
@Deprecated(
    "Replaced with Image() and rememberCoilImageState()",
    ReplaceWith(
        expression = """ImageLoad(
            request = rememberCoilImageLoadRequest(
                data = data,
                imageLoader = imageLoader,
                requestBuilder = requestBuilder,
            ),
            contentDescription = contentDescription,
            modifier = modifier,
            alignment = alignment,
            contentScale = contentScale,
            colorFilter = colorFilter,
            fadeIn = fadeIn,
            previewPlaceholder = previewPlaceholder,
            shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        )""",
        "com.google.accompanist.imageloading.ImageLoad",
        "com.google.accompanist.coil.rememberCoilImageLoadRequest",
    )
)
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
    imageLoader: ImageLoader = CoilImageStateDefaults.defaultImageLoader(),
    @DrawableRes previewPlaceholder: Int = 0,
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = { _, _ -> false },
    onRequestCompleted: (ImageLoadState) -> Unit = {},
    error: @Composable (BoxScope.(ImageLoadState.Error) -> Unit)? = null,
    loading: @Composable (BoxScope.() -> Unit)? = null,
) {
    val painter = rememberCoilPainter(
        data = data,
        requestBuilder = requestBuilder,
        imageLoader = imageLoader,
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        fadeIn = fadeIn,
        previewPlaceholder = previewPlaceholder,
    )

    LaunchedEffect(painter) {
        snapshotFlow { painter.loadState }
            .filter { it.isFinalState() }
            .collect { onRequestCompleted(it) }
    }

    @Suppress("DEPRECATION")
    ImageSuchDeprecated(
        loadPainter = painter,
        contentDescription = contentDescription,
        alignment = alignment,
        contentScale = contentScale,
        colorFilter = colorFilter,
        modifier = modifier,
    ) { imageState ->
        when (imageState) {
            is ImageLoadState.Error -> if (error != null) error(imageState)
            ImageLoadState.Loading -> if (loading != null) loading()
            else -> Unit
        }
    }
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
 * @param fadeIn Whether to run a fade-in animation when images are successfully loaded.
 * Default: `false`.
 * @param requestBuilder Optional builder for the [ImageRequest].
 * @param imageLoader The [ImageLoader] to use when requesting the image. Defaults to
 * [CoilImageStateDefaults.defaultImageLoader].
 * @param previewPlaceholder Drawable resource ID which will be displayed when this function is
 * ran in preview mode.
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 */
@Deprecated(
    "Replaced with Image() and rememberCoilImageState()",
    ReplaceWith(
        expression = """ImageLoad(
            request = rememberCoilImageLoadRequest(
                data = request,
                imageLoader = imageLoader,
                requestBuilder = requestBuilder,
            ),
            contentDescription = contentDescription,
            modifier = modifier,
            alignment = alignment,
            contentScale = contentScale,
            colorFilter = colorFilter,
            fadeIn = fadeIn,
            previewPlaceholder = previewPlaceholder,
            shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        )""",
        "com.google.accompanist.imageloading.ImageLoad",
        "com.google.accompanist.coil.rememberCoilImageLoadRequest",
    )
)
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
    imageLoader: ImageLoader = CoilImageStateDefaults.defaultImageLoader(),
    @DrawableRes previewPlaceholder: Int = 0,
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = { _, _ -> false },
    onRequestCompleted: (ImageLoadState) -> Unit = {},
    error: @Composable (BoxScope.(ImageLoadState.Error) -> Unit)? = null,
    loading: @Composable (BoxScope.() -> Unit)? = null,
) {
    CoilImage(
        data = request,
        contentDescription = contentDescription,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        colorFilter = colorFilter,
        fadeIn = fadeIn,
        requestBuilder = requestBuilder,
        imageLoader = imageLoader,
        previewPlaceholder = previewPlaceholder,
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        onRequestCompleted = onRequestCompleted,
        error = error,
        loading = loading,
    )
}
