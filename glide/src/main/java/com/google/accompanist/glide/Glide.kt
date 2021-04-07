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

package com.google.accompanist.glide

import android.graphics.drawable.Drawable
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
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.accompanist.imageloading.DataSource
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.imageloading.ImageState
import com.google.accompanist.imageloading.toPainter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Composition local containing the preferred [RequestManager] to use
 * for [rememberGlideImageState].
 */
val LocalRequestManager = staticCompositionLocalOf<RequestManager?> { null }

/**
 * Contains some default values used for [GlideImageState].
 */
object GlideImageDefaults {
    /**
     * Returns the default [RequestManager] value for the `requestManager` parameter
     * in [GlideImage].
     */
    @Composable
    fun defaultRequestManager(): RequestManager {
        // By default Glide tries to install lifecycle listeners to automatically re-trigger
        // requests when resumed. We don't want that with Compose, since we rely on composition
        // for our 'lifecycle'. We can stop Glide doing this by using the application context.
        return LocalRequestManager.current ?: Glide.with(LocalContext.current.applicationContext)
    }
}

/**
 * Creates a [GlideImageState] that is remembered across compositions.
 *
 * Changes to the provided values for [requestManager] will **not** result
 * in the state being recreated or changed in any way if it has already been created.
 * Changes to [data], [shouldRefetchOnSizeChange] & [requestBuilder] will result in
 * the [GlideImageState] being updated.
 *
 * @param data the value for [GlideImageState.data]
 * @param requestManager the initial value for [GlideImageState.requestManager]
 * @param shouldRefetchOnSizeChange the value for [GlideImageState.shouldRefetchOnSizeChange]
 * @param requestBuilder the value for [GlideImageState.requestBuilder]
 */
@Composable
fun rememberGlideImageState(
    data: Any?,
    requestManager: RequestManager = GlideImageDefaults.defaultRequestManager(),
    shouldRefetchOnSizeChange: ShouldRefetchOnSizeChange = { _, _ -> false },
    requestBuilder: (RequestBuilder<Drawable>.(size: IntSize) -> RequestBuilder<Drawable>)? = null,
): GlideImageState = remember(requestManager) {
    GlideImageState(requestManager, shouldRefetchOnSizeChange)
}.apply {
    this.data = data
    this.requestBuilder = requestBuilder
    this.shouldRefetchOnSizeChange = shouldRefetchOnSizeChange
}

private typealias ImageRequestBuilder = (RequestBuilder<Drawable>.(size: IntSize) -> RequestBuilder<Drawable>)
private typealias ShouldRefetchOnSizeChange = (currentState: ImageLoadState, size: IntSize) -> Boolean

/**
 * A state object that can be hoisted for [com.google.accompanist.imageloading.Image]
 * to load images using [Glide].
 *
 * In most cases, this will be created via [rememberGlideImageState].
 *
 * @param requestManager The [RequestManager] to use when requesting the image.
 * @param shouldRefetchOnSizeChange Initial value for [GlideImageState.shouldRefetchOnSizeChange]
 */
@Stable
class GlideImageState(
    private val requestManager: RequestManager,
    shouldRefetchOnSizeChange: ShouldRefetchOnSizeChange,
) : ImageState<Any>(shouldRefetchOnSizeChange) {
    private var currentData by mutableStateOf<Any?>(null)

    override val request: Any?
        get() = currentData

    /**
     * The data to load. See [RequestManager.load] for the types supported.
     */
    var data: Any?
        get() = currentData
        set(value) {
            currentData = checkData(value)
        }

    /**
     * Holds an optional builder for every created [RequestBuilder].
     */
    var requestBuilder by mutableStateOf<ImageRequestBuilder?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun executeRequest(
        request: Any,
        size: IntSize
    ): ImageLoadState = suspendCancellableCoroutine { cont ->
        var failException: Throwable? = null

        val target = object : EmptyCustomTarget(
            if (size.width > 0) size.width else Target.SIZE_ORIGINAL,
            if (size.height > 0) size.height else Target.SIZE_ORIGINAL
        ) {
            override fun onLoadFailed(errorDrawable: Drawable?) {
                if (cont.isCompleted) {
                    // If we've already completed, ignore this
                    return
                }

                val result = ImageLoadState.Error(
                    painter = errorDrawable?.toPainter(),
                    request = request,
                    throwable = failException
                        ?: IllegalArgumentException("Error while loading $request")
                )

                cont.resume(result) {
                    // Clear any resources from the target if cancelled
                    requestManager.clear(this)
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
                if (cont.isCompleted) {
                    // If we've already completed, ignore this
                    return true
                }

                val result = ImageLoadState.Success(
                    painter = drawable.toPainter(),
                    request = request,
                    source = dataSource.toDataSource()
                )

                cont.resume(result) {
                    // Clear any resources from the target if cancelled
                    requestManager.clear(target)
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
        requestManager.load(request)
            .apply { requestBuilder?.invoke(this, size) }
            .addListener(listener)
            .into(target)

        // If we're cancelled, clear the request from Glide
        cont.invokeOnCancellation {
            requestManager.clear(target)
        }
    }
}

private fun com.bumptech.glide.load.DataSource.toDataSource(): DataSource = when (this) {
    com.bumptech.glide.load.DataSource.LOCAL -> DataSource.DISK
    com.bumptech.glide.load.DataSource.REMOTE -> DataSource.NETWORK
    com.bumptech.glide.load.DataSource.DATA_DISK_CACHE -> DataSource.DISK
    com.bumptech.glide.load.DataSource.RESOURCE_DISK_CACHE -> DataSource.DISK
    com.bumptech.glide.load.DataSource.MEMORY_CACHE -> DataSource.MEMORY
}

private fun checkData(data: Any?): Any? {
    when (data) {
        is Drawable -> {
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
