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
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import dev.chrisbanes.accompanist.imageloading.DataSource
import dev.chrisbanes.accompanist.imageloading.DefaultRefetchOnSizeChangeLambda
import dev.chrisbanes.accompanist.imageloading.ImageLoadState
import dev.chrisbanes.accompanist.imageloading.ImageManager
import dev.chrisbanes.accompanist.imageloading.toPainter
import dev.chrisbanes.accompanist.imageloading.updateAlpha
import dev.chrisbanes.accompanist.imageloading.updateBrightness
import dev.chrisbanes.accompanist.imageloading.updateFadeInTransition
import dev.chrisbanes.accompanist.imageloading.updateSaturation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.HttpUrl
import java.io.File

/**
 * Composition local containing the preferred [Picasso] to use in [PicassoImage]. Defaults to [Picasso.get].
 */
val LocalPicasso = staticCompositionLocalOf { Picasso.get() }

private fun picassoImageManager(
    data: Any,
    picasso: Picasso,
    requestBuilder: (RequestCreator.(size: IntSize) -> RequestCreator)?,
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean,
    onRequestCompleted: (ImageLoadState) -> Unit,
) = ImageManager(
    request = data.toRequestCreator(picasso),
    executeRequest = { r ->
        @OptIn(ExperimentalCoroutinesApi::class)
        suspendCancellableCoroutine { cont ->
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
    },
    transformRequestForSize = { r, size ->
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
)

/**
 * Creates a composable that will attempt to load the given [data] using [Picasso], and then
 * display the result in an [Image].
 *
 * This version of the function is more opinionated, providing:
 *
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
 * @param fadeIn Whether to run a fade-in animation when images are successfully loaded.
 * Default: `false`.
 * @param picasso The [Picasso] instance to use for requests. Defaults to the current value
 * of [LocalPicasso].
 * @param requestBuilder Optional builder for the [RequestCreator].
 * @param shouldRefetchOnSizeChange Lambda which will be invoked when the size changes, allowing
 * optional re-fetching of the image. Return true to re-fetch the image.
 * @param onRequestCompleted Listener which will be called when the loading request has finished.
 */
@Composable
fun PicassoImage(
    data: Any,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    fadeIn: Boolean = false,
    fadeInDurationMs: Int = 1000,
    picasso: Picasso = LocalPicasso.current,
    requestBuilder: (RequestCreator.(size: IntSize) -> RequestCreator)? = null,
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = DefaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (ImageLoadState) -> Unit = {},
) {
    val imageMgr = remember(data, picasso) {
        picassoImageManager(
            data = data,
            requestBuilder = requestBuilder,
            picasso = picasso,
            shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
            onRequestCompleted = onRequestCompleted,
        )
    }

    val cf = if (fadeIn) {
        val fadeInTransition = updateFadeInTransition(key = data, durationMs = fadeInDurationMs)
        remember { ColorMatrix() }
            .apply {
                updateAlpha(fadeInTransition.alpha)
                updateBrightness(fadeInTransition.brightness)
                updateSaturation(fadeInTransition.saturation)
            }
            .let { matrix ->
                ColorFilter.colorMatrix(matrix)
            }
    } else {
        // If fade in isn't enable, just use the provided `colorFilter`
        colorFilter
    }

    // NOTE: All of the things that we want to be able to change without recreating the whole object
    // we want to do inside of here.
    SideEffect {
        // TODO: requestBuilder
        imageMgr.shouldRefetchOnSizeChange = shouldRefetchOnSizeChange
        imageMgr.onRequestCompleted = onRequestCompleted

        imageMgr.contentScale = contentScale
        imageMgr.colorFilter = cf
        imageMgr.alignment = alignment
    }

    // NOTE: It's important that this is Box and not BoxWithConstraints. This is dramatically cheaper,
    // and also has not children. You could use Box(modifier, content) here if you want, and add a
    // content lambda, but that would be for content inside / on top of the image, and not for the
    // image itself like the current implementation.

    val semantics = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else Modifier

    Box(
        modifier
            .then(semantics)
            .then(imageMgr.modifier)
    )
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
