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

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.size.Precision
import com.google.accompanist.imageloading.DataSource
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.imageloading.ImageState

/**
 * Composition local containing the preferred [ImageLoader] to be used by
 * [rememberCoilImageState].
 */
val LocalImageLoader = staticCompositionLocalOf<ImageLoader?> { null }

/**
 * Contains some default values used by [rememberCoilImageState].
 */
object CoilImageStateDefaults {
    /**
     * Returns the default [ImageLoader] value for the `imageLoader` parameter in [CoilImage].
     */
    @Composable
    fun defaultImageLoader(): ImageLoader {
        return LocalImageLoader.current ?: LocalContext.current.imageLoader
    }
}

/**
 * Creates a [CoilImageState] that is remembered across compositions.
 *
 * Changes to the provided values for [imageLoader] and [context] will **not** result
 * in the state being recreated or changed in any way if it has already been created.
 * Changes to [data], [shouldRefetchOnSizeChange] & [requestBuilder] will result in
 * the [CoilImageState] being updated.
 *
 * @param data the value for [CoilImageState.data]
 * @param imageLoader the value for [CoilImageState.imageLoader]
 * @param context the initial value for [CoilImageState.context]
 * @param shouldRefetchOnSizeChange the value for [CoilImageState.shouldRefetchOnSizeChange]
 * @param requestBuilder the value for [CoilImageState.requestBuilder]
 */
@Composable
fun rememberCoilImageState(
    data: Any?,
    imageLoader: ImageLoader = CoilImageStateDefaults.defaultImageLoader(),
    context: Context = LocalContext.current,
    shouldRefetchOnSizeChange: (currentState: ImageLoadState, size: IntSize) -> Boolean = { _, _ -> false },
    requestBuilder: (ImageRequest.Builder.(size: IntSize) -> ImageRequest.Builder)? = null,
): CoilImageState = remember(imageLoader, context) {
    CoilImageState(
        imageLoader = imageLoader,
        context = context,
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
    )
}.apply {
    this.data = data
    this.requestBuilder = requestBuilder
    this.shouldRefetchOnSizeChange = shouldRefetchOnSizeChange
}

/**
 * A state object that can be hoisted for [com.google.accompanist.imageloading.Image]
 * to load images using [coil.Coil].
 *
 * In most cases, this will be created via [rememberCoilImageState].
 *
 * @param imageLoader The [ImageLoader] to use when requesting the image. Defaults to
 * [CoilImageStateDefaults.defaultImageLoader].
 * @param context The Android [Context] to use when creating [ImageRequest]s.
 * @param shouldRefetchOnSizeChange the value for [CoilImageState.shouldRefetchOnSizeChange].
 */
@Stable
class CoilImageState(
    private val imageLoader: ImageLoader,
    private val context: Context,
    shouldRefetchOnSizeChange: (currentState: ImageLoadState, size: IntSize) -> Boolean = { _, _ -> false },
) : ImageState<Any>(shouldRefetchOnSizeChange) {
    private var currentData by mutableStateOf<Any?>(null)

    override val request: Any?
        get() = currentData

    /**
     * Holds an optional builder for every created [ImageRequest].
     */
    var requestBuilder by mutableStateOf<(ImageRequest.Builder.(size: IntSize) -> ImageRequest.Builder)?>(null)

    /**
     * The data to load. See [ImageRequest.Builder.data] for the types supported.
     */
    var data: Any?
        get() = currentData
        set(value) {
            currentData = checkData(value)
        }

    override suspend fun executeRequest(request: Any, size: IntSize): ImageLoadState {
        val baseRequest = when (request) {
            // If we've been given an ImageRequest instance, use it...
            is ImageRequest -> request.newBuilder()
            // Otherwise we construct a request from the data
            else -> {
                ImageRequest.Builder(context)
                    .data(request)
                    // We force in-exact precision as AUTOMATIC only works when used from views.
                    // INEXACT is correct as we can scale the result appropriately.
                    .precision(Precision.INEXACT)
            }
        }.apply {
            // Apply the request builder
            requestBuilder?.invoke(this, size)
        }.build()

        val sizedRequest = when {
            // If the request has a size resolver set we just execute the request as-is
            baseRequest.defined.sizeResolver != null -> baseRequest
            // If the size contains an unspecified sized dimension, we don't specify a size
            // in the Coil request
            size.width < 0 || size.height < 0 -> baseRequest
            // If we have a non-zero size, we can modify the request to include the size
            size.width > 0 && size.height > 0 -> {
                baseRequest.newBuilder().size(size.width, size.height).build()
            }
            // Otherwise we have a zero size, so no point executing a request so return empty now
            else -> return ImageLoadState.Empty
        }

        return imageLoader.execute(sizedRequest).toResult(request)
    }
}

private fun ImageResult.toResult(request: Any): ImageLoadState = when (this) {
    is coil.request.SuccessResult -> {
        ImageLoadState.Success(
            result = drawable,
            request = request,
            source = metadata.dataSource.toDataSource()
        )
    }
    is coil.request.ErrorResult -> {
        ImageLoadState.Error(
            result = drawable,
            request = request,
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

private fun checkData(data: Any?): Any? {
    when (data) {
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
    }
    return data
}
