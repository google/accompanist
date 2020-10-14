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

@file:JvmName("GlideImage")
@file:JvmMultifileClass

package dev.chrisbanes.accompanist.glide

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageAsset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ViewAmbient
import androidx.compose.ui.unit.IntSize
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dev.chrisbanes.accompanist.imageloading.DataSource
import dev.chrisbanes.accompanist.imageloading.DefaultRefetchOnSizeChangeLambda
import dev.chrisbanes.accompanist.imageloading.EmptyRequestCompleteLambda
import dev.chrisbanes.accompanist.imageloading.ImageLoad
import dev.chrisbanes.accompanist.imageloading.ImageLoadState
import dev.chrisbanes.accompanist.imageloading.MaterialLoadingImage
import dev.chrisbanes.accompanist.imageloading.toPainter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.IllegalArgumentException

/**
 * Creates a composable that will attempt to load the given [data] using [Glide], and provides
 * complete content of how the current state is displayed:
 *
 * ```
 * GlideImage(
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
 * @param data The data to load.
 * @param modifier [Modifier] used to adjust the layout algorithm or draw decoration content.
 * @param requestBuilder Optional builder for the [RequestBuilder].
 * @param requestManager The [RequestManager] to use when requesting the image. Defaults to `Glide.with(view)`
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 * @param content Content to be displayed for the given state.
 */
@Composable
fun GlideImage(
    data: Any,
    modifier: Modifier = Modifier,
    requestBuilder: (RequestBuilder<Drawable>.(size: IntSize) -> RequestBuilder<Drawable>)? = null,
    requestManager: RequestManager = Glide.with(ViewAmbient.current),
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = DefaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (ImageLoadState) -> Unit = EmptyRequestCompleteLambda,
    content: @Composable (imageLoadState: ImageLoadState) -> Unit
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    ImageLoad(
        request = requestManager.load(data),
        executeRequest = { (r, size) ->
            suspendCancellableCoroutine { cont ->
                val target = object : CustomTarget<Drawable>(size.width, size.height) {
                    override fun onResourceReady(
                        drawable: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        val result = ImageLoadState.Success(
                            painter = drawable.toPainter(),
                            // TODO: Work out how Glide reports this
                            source = DataSource.NETWORK
                        )

                        cont.resume(result) {
                            // TODO
                        }
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        val result = ImageLoadState.Error(
                            painter = errorDrawable?.toPainter(),
                            throwable = IllegalArgumentException("Error while loading $data")
                        )

                        cont.resume(result) {
                            // TODO
                        }
                    }

                    override fun onLoadCleared(drawable: Drawable?) {
                        // TODO
                    }
                }

                // Start the image request into the target
                r.into(target)

                // If we're cancelled, clear the request fron Glide
                cont.invokeOnCancellation {
                    requestManager.clear(target)
                }
            }
        },
        transformRequestForSize = { r, size ->
            Pair(requestBuilder?.invoke(r, size) ?: r, size)
        },
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        onRequestCompleted = onRequestCompleted,
        modifier = modifier,
        content = content
    )
}

/**
 * Creates a composable that will attempt to load the given [data] using [Glide], and then
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
 * GlideImage(
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
 * @param data The data to load.
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
 * @param requestBuilder Optional builder for the [RequestBuilder].
 * @param requestManager The [RequestManager] to use when requesting the image. Defaults to `Glide.with(view)`
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 */
@Composable
fun GlideImage(
    data: Any,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    fadeIn: Boolean = false,
    requestBuilder: (RequestBuilder<Drawable>.(size: IntSize) -> RequestBuilder<Drawable>)? = null,
    requestManager: RequestManager = Glide.with(ViewAmbient.current),
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = DefaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (ImageLoadState) -> Unit = EmptyRequestCompleteLambda,
    error: @Composable ((ImageLoadState.Error) -> Unit)? = null,
    loading: @Composable (() -> Unit)? = null,
) {
    GlideImage(
        data = data,
        modifier = modifier,
        requestBuilder = requestBuilder,
        requestManager = requestManager,
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

// private fun ImageResult.toResult(): ImageLoadState = when (this) {
//    is coil.request.SuccessResult -> {
//        ImageLoadState.Success(
//            painter = drawable.toPainter(),
//            source = metadata.dataSource.toDataSource()
//        )
//    }
//    is coil.request.ErrorResult -> {
//        ImageLoadState.Error(
//            painter = drawable?.toPainter(),
//            throwable = throwable
//        )
//    }
// }
//
// private fun coil.decode.DataSource.toDataSource(): DataSource = when (this) {
//    coil.decode.DataSource.NETWORK -> DataSource.NETWORK
//    coil.decode.DataSource.MEMORY -> DataSource.MEMORY
//    coil.decode.DataSource.MEMORY_CACHE -> DataSource.MEMORY
//    coil.decode.DataSource.DISK -> DataSource.DISK
// }
