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

@file:JvmName("PicassoImage")
@file:JvmMultifileClass

package dev.chrisbanes.accompanist.picasso

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageAsset
import androidx.compose.ui.graphics.asImageAsset
import androidx.compose.ui.graphics.painter.ImagePainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import dev.chrisbanes.accompanist.imageloading.DataSource
import dev.chrisbanes.accompanist.imageloading.ImageLoad
import dev.chrisbanes.accompanist.imageloading.ImageLoadState
import dev.chrisbanes.accompanist.imageloading.MaterialLoadingImage
import dev.chrisbanes.accompanist.imageloading.toPainter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.HttpUrl
import java.io.File

/**
 * Creates a composable that will attempt to load the given [data] using [Picasso], and provides
 * complete content of how the current state is displayed:
 *
 * ```
 * PicassoImage(
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
 * @param data The data to load. See [RequestCreator.data] for the types allowed.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param requestBuilder Optional builder for the [RequestCreator].
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 * @param content Content to be displayed for the given state.
 */
@Composable
fun PicassoImage(
    data: Any,
    modifier: Modifier = Modifier,
    picasso: Picasso = Picasso.get(),
    requestBuilder: (RequestCreator.(size: IntSize) -> RequestCreator)? = null,
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = defaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (ImageLoadState) -> Unit = emptySuccessLambda,
    content: @Composable (imageLoadState: ImageLoadState) -> Unit
) {
    ImageLoad(
        request = data.toRequestCreator(picasso),
        requestKey = data, // Picasso RequestCreator doesn't support equality so we use the data
        executeRequest = { r ->
            @OptIn(ExperimentalCoroutinesApi::class)
            suspendCancellableCoroutine { cont ->
                val target = object : com.squareup.picasso.Target {
                    override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                        val state = ImageLoadState.Success(
                            painter = ImagePainter(bitmap.asImageAsset()),
                            source = from.toDataSource()
                        )
                        cont.resume(state) {
                            // Not much we can do here. Ignore this
                        }
                    }

                    override fun onBitmapFailed(exception: Exception, errorDrawable: Drawable?) {
                        val state = ImageLoadState.Error(
                            throwable = exception,
                            painter = errorDrawable?.toPainter(),
                        )
                        cont.resume(state) {
                            // Not much we can do here. Ignore this
                        }
                    }

                    override fun onPrepareLoad(placeholder: Drawable?) = Unit
                }

                cont.invokeOnCancellation {
                    // If the coroutine is cancelled, cancel the request
                    picasso.cancelRequest(target)
                }

                // Now kick off the image load into our target
                r.into(target)
            }
        },
        transformRequestForSize = { r, size ->
            val sizedRequest = when {
                // If the size contains an unspecified sized dimension, we don't specify a size
                // in the Coil request
                size.width < 0 || size.height < 0 -> r
                // If we have a non-zero size, we can modify the request to include the size
                size != IntSize.Zero -> r.resize(size.width, size.height).onlyScaleDown()
                // Otherwise we have a zero size, so no point executing a request
                else -> null
            }

            if (sizedRequest != null && requestBuilder != null) {
                // If we have a transformed request and builder, let it run
                requestBuilder(sizedRequest, size)
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
 * Creates a composable that will attempt to load the given [data] using [Picasso], and then
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
 * PicassoImage(
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
 * @param data The data to load. See [RequestCreator.data] for the types allowed.
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
 * @param requestBuilder Optional builder for the [RequestCreator].
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 */
@Composable
fun PicassoImage(
    data: Any,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    fadeIn: Boolean = false,
    picasso: Picasso = Picasso.get(),
    requestBuilder: (RequestCreator.(size: IntSize) -> RequestCreator)? = null,
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = defaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (ImageLoadState) -> Unit = emptySuccessLambda,
    error: @Composable ((ImageLoadState.Error) -> Unit)? = null,
    loading: @Composable (() -> Unit)? = null,
) {
    PicassoImage(
        data = data,
        modifier = modifier,
        requestBuilder = requestBuilder,
        picasso = picasso,
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        onRequestCompleted = onRequestCompleted,
    ) { imageState ->
        when (imageState) {
            is ImageLoadState.Success -> {
                MaterialLoadingImage(
                    result = imageState,
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

private fun Picasso.LoadedFrom.toDataSource(): DataSource = when (this) {
    Picasso.LoadedFrom.MEMORY -> DataSource.MEMORY
    Picasso.LoadedFrom.DISK -> DataSource.DISK
    Picasso.LoadedFrom.NETWORK -> DataSource.NETWORK
}

@Composable
internal fun Any.toRequestCreator(picasso: Picasso): RequestCreator = when (this) {
    is String -> picasso.load(this)
    is Uri -> picasso.load(this)
    is File -> picasso.load(this)
    is Int -> picasso.load(this)
    is HttpUrl -> picasso.load(Uri.parse(toString()))
    else -> throw IllegalArgumentException("Data is not of a type which Picasso supports: ${this::class.java}")
}

internal val emptySuccessLambda: (ImageLoadState) -> Unit = {}

internal val defaultRefetchOnSizeChangeLambda: (ImageLoadState, IntSize) -> Boolean = { _, _ -> false }
