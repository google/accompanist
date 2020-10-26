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
import androidx.compose.runtime.staticAmbientOf
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
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import dev.chrisbanes.accompanist.imageloading.DataSource
import dev.chrisbanes.accompanist.imageloading.DefaultRefetchOnSizeChangeLambda
import dev.chrisbanes.accompanist.imageloading.EmptyRequestCompleteLambda
import dev.chrisbanes.accompanist.imageloading.ImageLoad
import dev.chrisbanes.accompanist.imageloading.ImageLoadState
import dev.chrisbanes.accompanist.imageloading.MaterialLoadingImage
import dev.chrisbanes.accompanist.imageloading.toPainter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Ambient containing the preferred [RequestManager] to use in [GlideImage].
 */
val AmbientRequestManager = staticAmbientOf<RequestManager?> { null }

object GlideImageConstants {
    /**
     * Returns the default [RequestManager] value for the `requestManager` parameter
     * in [GlideImage].
     */
    @Composable
    fun defaultRequestManager(): RequestManager {
        return AmbientRequestManager.current ?: Glide.with(ViewAmbient.current)
    }
}

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
 * @param requestManager The [RequestManager] to use when requesting the image. Defaults to the
 * current value of [AmbientRequestManager].
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
    requestManager: RequestManager = GlideImageConstants.defaultRequestManager(),
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = DefaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (ImageLoadState) -> Unit = EmptyRequestCompleteLambda,
    content: @Composable (imageLoadState: ImageLoadState) -> Unit
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    ImageLoad(
        request = requestManager.load(data),
        requestKey = data, // Glide RequestBuilder doesn't support equality so we use the data
        executeRequest = { (r, size) -> requestManager.execute(r, size) },
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
 * @param requestManager The [RequestManager] to use when requesting the image. Defaults to the
 * current value of [AmbientRequestManager].
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
    requestManager: RequestManager = GlideImageConstants.defaultRequestManager(),
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

/**
 * A Coroutines wrapper around [Glide]
 */
@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun RequestManager.execute(
    request: RequestBuilder<Drawable>,
    size: IntSize
): ImageLoadState = suspendCancellableCoroutine { cont ->
    var failException: Throwable? = null

    val target = object : EmptyCustomTarget(size.width, size.height) {
        override fun onLoadFailed(errorDrawable: Drawable?) {
            val result = ImageLoadState.Error(
                painter = errorDrawable?.toPainter(),
                throwable = failException
                    ?: IllegalArgumentException("Error while loading $request")
            )

            cont.resume(result) {
                // Clear any resources from the target if cancelled
                clear(this)
            }
        }
    }

    val listener = object : RequestListener<Drawable> {
        override fun onResourceReady(
            drawable: Drawable,
            model: Any,
            target: Target<Drawable>,
            dataSource: com.bumptech.glide.load.DataSource,
            isFirstResource: Boolean
        ): Boolean {
            val result = ImageLoadState.Success(
                painter = drawable.toPainter(),
                source = dataSource.toDataSource()
            )

            cont.resume(result) {
                // Clear any resources from the target if cancelled
                clear(target)
            }

            // Return true so that the target doesn't receive the drawable
            return true
        }

        override fun onLoadFailed(
            e: GlideException?,
            model: Any,
            target: Target<Drawable>,
            isFirstResource: Boolean
        ): Boolean {
            // Glide only passes the exception to the listener, so we store it
            // for the target to use
            failException = e
            // Return false, allowing the target to receive it's onLoadFailed.
            // This is needed so we can use any errorDrawable
            return false
        }
    }

    // Start the image request into the target
    request.addListener(listener).into(target)

    // If we're cancelled, clear the request from Glide
    cont.invokeOnCancellation {
        clear(target)
    }
}

private fun com.bumptech.glide.load.DataSource.toDataSource(): DataSource = when (this) {
    com.bumptech.glide.load.DataSource.LOCAL -> DataSource.DISK
    com.bumptech.glide.load.DataSource.REMOTE -> DataSource.NETWORK
    com.bumptech.glide.load.DataSource.DATA_DISK_CACHE -> DataSource.DISK
    com.bumptech.glide.load.DataSource.RESOURCE_DISK_CACHE -> DataSource.DISK
    com.bumptech.glide.load.DataSource.MEMORY_CACHE -> DataSource.MEMORY
}
