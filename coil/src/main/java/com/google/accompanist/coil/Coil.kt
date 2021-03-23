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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import coil.ImageLoader
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.ImageResult
import com.google.accompanist.imageloading.DataSource
import com.google.accompanist.imageloading.ImageLoadRequest
import com.google.accompanist.imageloading.ImageLoadState
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
 * TODO
 */
@Composable
fun rememberCoilImageLoadRequest(
    data: Any,
    imageLoader: ImageLoader,
    requestBuilder: (ImageRequest.Builder.(size: IntSize) -> ImageRequest.Builder)?,
    onRequestCompleted: (ImageLoadState) -> Unit = {},
): ImageLoadRequest<ImageRequest> = rememberCoilImageLoadRequest(
    request = data.toImageRequest(),
    imageLoader = imageLoader,
    requestBuilder = requestBuilder,
    onRequestCompleted = onRequestCompleted
)

@Composable
fun rememberCoilImageLoadRequest(
    request: ImageRequest,
    imageLoader: ImageLoader,
    requestBuilder: (ImageRequest.Builder.(size: IntSize) -> ImageRequest.Builder)?,
    onRequestCompleted: (ImageLoadState) -> Unit = {},
): ImageLoadRequest<ImageRequest> = remember(request, imageLoader) {
    CoilImageLoadRequest(
        request = request,
        imageLoader = imageLoader,
        requestBuilder = requestBuilder,
        onRequestCompleted = onRequestCompleted
    )
}

/**
 * TODO
 */
private class CoilImageLoadRequest(
    override val request: ImageRequest,
    private val imageLoader: ImageLoader,
    private val requestBuilder: (ImageRequest.Builder.(size: IntSize) -> ImageRequest.Builder)?,
    override val onRequestCompleted: (ImageLoadState) -> Unit = {},
) : ImageLoadRequest<ImageRequest>() {
    override suspend fun doExecute(request: ImageRequest, size: IntSize): ImageLoadState {
        val sizedRequest = when {
            // If the request has a size resolver set we just execute the request as-is
            request.defined.sizeResolver != null -> request
            // If the size contains an unspecified sized dimension, we don't specify a size
            // in the Coil request
            size.width < 0 || size.height < 0 -> request
            // If we have a non-zero size, we can modify the request to include the size
            size.width > 0 && size.height > 0 -> {
                request.newBuilder().size(size.width, size.height).build()
            }
            // Otherwise we have a zero size, so no point executing a request so return empty now
            else -> return ImageLoadState.Empty
        }

        val r = requestBuilder?.invoke(sizedRequest.newBuilder(), size)?.build() ?: sizedRequest
        return imageLoader.execute(r).toResult()
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
