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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntSize
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import dev.chrisbanes.accompanist.imageloading.DataSource
import dev.chrisbanes.accompanist.imageloading.ImageLoadRequest
import dev.chrisbanes.accompanist.imageloading.ImageLoadState
import dev.chrisbanes.accompanist.imageloading.toPainter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.HttpUrl
import java.io.File

/**
 * Composition local containing the preferred [Picasso] to use in [PicassoImage]. Defaults to [Picasso.get].
 */
val LocalPicasso = staticCompositionLocalOf { Picasso.get() }

@Composable
fun rememberPicassoImageLoadRequest(
    request: Any,
    picasso: Picasso,
    requestBuilder: (RequestCreator.(size: IntSize) -> RequestCreator)?,
    onRequestCompleted: (ImageLoadState) -> Unit,
): ImageLoadRequest<Any> = remember(request, picasso) {
    PicassoImageLoadRequest(
        request = request,
        picasso = picasso,
        requestBuilder = requestBuilder,
        onRequestCompleted = onRequestCompleted
    )
}

private class PicassoImageLoadRequest(
    override val request: Any,
    private val picasso: Picasso,
    private val requestBuilder: (RequestCreator.(size: IntSize) -> RequestCreator)?,
    override val onRequestCompleted: (ImageLoadState) -> Unit,
) : ImageLoadRequest<Any>() {
    override suspend fun doExecute(request: Any, size: IntSize): ImageLoadState {
        return createRequest(size)?.let { executePicasso(it) }
            ?: ImageLoadState.Empty
    }

    private fun createRequest(size: IntSize): RequestCreator? {
        val r = request.toRequestCreator(picasso)

        val sizedRequest = when {
            // If the size contains an unspecified sized dimension, we don't specify a size
            // in the Coil request
            size.width < 0 || size.height < 0 -> r
            // If we have a non-zero size, we can modify the request to include the size
            size != IntSize.Zero -> {
                // We use centerInside() here, otherwise Picasso will resize the image ignoring
                // aspect ratio. centerInside() isn't great, since it means that the image
                // could be loaded smaller than the composable, only to be scaled up again by
                // the chosen ContentScale. Unfortunately there's not much else we can do.
                // See https://github.com/chrisbanes/accompanist/issues/118
                r.resize(size.width, size.height)
                    .centerInside()
                    .onlyScaleDown()
            }
            // Otherwise we have a zero size, so no point executing a request
            else -> return null
        }

        return if (sizedRequest != null && requestBuilder != null) {
            // If we have a transformed request and builder, let it run
            requestBuilder.invoke(sizedRequest, size)
        } else {
            // Otherwise we just return the sizedRequest
            sizedRequest
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun executePicasso(r: RequestCreator): ImageLoadState {
        return suspendCancellableCoroutine { cont ->
            val target = object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                    val state = ImageLoadState.Success(
                        painter = BitmapPainter(bitmap.asImageBitmap()),
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
    }
}

private fun Picasso.LoadedFrom.toDataSource(): DataSource = when (this) {
    Picasso.LoadedFrom.MEMORY -> DataSource.MEMORY
    Picasso.LoadedFrom.DISK -> DataSource.DISK
    Picasso.LoadedFrom.NETWORK -> DataSource.NETWORK
}

private fun Any.toRequestCreator(picasso: Picasso): RequestCreator = when (this) {
    is String -> picasso.load(this)
    is Uri -> picasso.load(this)
    is File -> picasso.load(this)
    is Int -> picasso.load(this)
    is HttpUrl -> picasso.load(Uri.parse(toString()))
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
    else -> throw IllegalArgumentException("Data is not of a type which Picasso supports: ${this::class.java}")
}
