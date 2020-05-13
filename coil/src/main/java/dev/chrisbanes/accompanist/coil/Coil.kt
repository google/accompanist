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

package dev.chrisbanes.accompanist.coil

import android.graphics.ColorMatrixColorFilter
import androidx.animation.FloatPropKey
import androidx.animation.transitionDefinition
import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.onCommit
import androidx.compose.remember
import androidx.compose.setValue
import androidx.compose.stateFor
import androidx.core.graphics.drawable.toBitmap
import androidx.ui.animation.Transition
import androidx.ui.core.Alignment
import androidx.ui.core.Constraints
import androidx.ui.core.ContentScale
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.WithConstraints
import androidx.ui.core.hasBoundedHeight
import androidx.ui.core.hasBoundedWidth
import androidx.ui.core.hasFixedHeight
import androidx.ui.core.hasFixedWidth
import androidx.ui.foundation.Image
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Canvas
import androidx.ui.graphics.ColorFilter
import androidx.ui.graphics.ImageAsset
import androidx.ui.graphics.Paint
import androidx.ui.graphics.asImageAsset
import androidx.ui.graphics.painter.ImagePainter
import androidx.ui.graphics.painter.Painter
import androidx.ui.unit.IntPx
import androidx.ui.unit.PxSize
import coil.Coil
import coil.request.GetRequest
import coil.request.GetRequestBuilder
import coil.request.RequestResult
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private enum class ImageLoadState {
    Loaded,
    Empty
}

private val alpha = FloatPropKey()
private val brightness = FloatPropKey()
private val saturation = FloatPropKey()

private val Constraints.requestWidth
    get() = if (hasFixedWidth || hasBoundedWidth) maxWidth else minWidth

private val Constraints.requestHeight
    get() = if (hasFixedHeight || hasBoundedHeight) maxHeight else minHeight

private const val defaultTransitionDuration = 1000

/**
 * Creates a composable that will attempt to load the given [data] using [Coil], and then
 * display the result in an [Image], using a crossfade when first loaded.
 *
 * The animation fades in the image's saturation, alpha and exposure. More information on the
 * pattern can be seen [here](https://material.io/archive/guidelines/patterns/loading-images.html).
 *
 * @param data The data to load. See [GetRequestBuilder.data] for the types allowed.
 * @param alignment Optional alignment parameter used to place the loaded [ImageAsset] in the
 * given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 * used if the bounds are a different size from the intrinsic size of the loaded [ImageAsset].
 * @param crossfadeDuration The duration of the crossfade animation in milliseconds.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content (ex.
 * background)
 */
@Composable
fun CoilImageWithCrossfade(
    data: Any,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    crossfadeDuration: Int = defaultTransitionDuration,
    modifier: Modifier = Modifier
) {
    CoilImageWithCrossfade(
        request = GetRequest.Builder(ContextAmbient.current).data(data).build(),
        alignment = alignment,
        contentScale = contentScale,
        crossfadeDuration = crossfadeDuration,
        modifier = modifier
    )
}

/**
 * Creates a composable that will attempt to load the given [request] using [Coil], and then
 * display the result in an [Image], using a crossfade animation when first loaded.
 *
 * The animation fades in the image's saturation, alpha and exposure. More information on the
 * pattern can be seen [here](https://material.io/archive/guidelines/patterns/loading-images.html).
 *
 * @param request The request to execute. If the request does not have a [GetRequest.sizeResolver]
 * set, one will be set on the request using the layout constraints.
 * @param alignment Optional alignment parameter used to place the loaded [ImageAsset] in the
 * given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 * used if the bounds are a different size from the intrinsic size of the loaded [ImageAsset].
 * @param crossfadeDuration The duration of the crossfade animation in milliseconds.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content (ex.
 * background)
 */
@Composable
fun CoilImageWithCrossfade(
    request: GetRequest,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    crossfadeDuration: Int = defaultTransitionDuration,
    modifier: Modifier = Modifier
) {
    WithConstraints(modifier) { constraints, _ ->
        var imgLoadState by stateFor(request) { ImageLoadState.Empty }

        val transitionDef = remember(crossfadeDuration) {
            transitionDefinition {
                state(ImageLoadState.Empty) {
                    this[alpha] = 0f
                    this[brightness] = 0.8f
                    this[saturation] = 0f
                }
                state(ImageLoadState.Loaded) {
                    this[alpha] = 1f
                    this[brightness] = 1f
                    this[saturation] = 1f
                }

                transition {
                    alpha using tween {
                        duration = crossfadeDuration / 2
                    }
                    brightness using tween {
                        duration = (crossfadeDuration * 0.75f).roundToInt()
                    }
                    saturation using tween {
                        duration = crossfadeDuration
                    }
                }
            }
        }

        // Execute the request using executeAsComposable(), which guards the actual execution
        // so that the request is only run if the request changes.
        val result = if (request.sizeResolver != null) {
            // If the request has a sizeResolver set, we can execute it now
            request.executeAsComposable()
        } else {
            // Otherwise we need to modify the request with some a request size
            request.newBuilder()
                    .size(constraints.requestWidth.value, constraints.requestHeight.value)
                    .build()
                    .executeAsComposable()
        }

        if (result != null && imgLoadState == ImageLoadState.Empty) {
            imgLoadState = ImageLoadState.Loaded
        }

        Transition(definition = transitionDef, toState = imgLoadState) { transitionState ->
            if (result?.drawable != null) {
                val asset = result.drawable!!.toBitmap().asImageAsset()

                // Create and update the ImageLoadingColorMatrix from the transition state
                val matrix = remember(asset) { ImageLoadingColorMatrix() }
                matrix.saturationFraction = transitionState[saturation]
                matrix.alphaFraction = transitionState[alpha]
                matrix.brightnessFraction = transitionState[brightness]

                // Unfortunately ColorMatrixColorFilter is not mutable so we have to create a new
                // instance every time
                val cf = ColorMatrixColorFilter(matrix)
                Image(
                    painter = AndroidColorMatrixImagePainter(asset, cf),
                    contentScale = contentScale,
                    alignment = alignment
                )
            }
        }
    }
}

/**
 * Creates a composable that will attempt to load the given [data] using [Coil], and then
 * display the result in an [Image].
 *
 * @param data The data to load. See [GetRequestBuilder.data] for the types allowed.
 * @param alignment Optional alignment parameter used to place the loaded [ImageAsset] in the
 * given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 * used if the bounds are a different size from the intrinsic size of the loaded [ImageAsset].
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content (ex.
 * background)
 */
@Composable
fun CoilImage(
    data: Any,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    modifier: Modifier = Modifier
) {
    CoilImage(
        request = GetRequest.Builder(ContextAmbient.current).data(data).build(),
        alignment = alignment,
        contentScale = contentScale,
        colorFilter = colorFilter,
        modifier = modifier
    )
}

/**
 * Creates a composable that will attempt to load the given [data] using [Coil], and then
 * display the result in an [Image].
 *
 * @param request The request to execute. If the request does not have a [GetRequest.sizeResolver]
 * set, one will be set on the request using the layout constraints.
 * @param alignment Optional alignment parameter used to place the loaded [ImageAsset] in the
 * given bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 * used if the bounds are a different size from the intrinsic size of the loaded [ImageAsset].
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content (ex.
 * background)
 */
@Composable
fun CoilImage(
    request: GetRequest,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    modifier: Modifier = Modifier
) {
    WithConstraints(modifier) { constraints, _ ->
        // Execute the request using executeAsComposable(), which guards the actual execution
        // so that the request is only run if the request changes.
        val result = if (request.sizeResolver != null) {
            // If the request has a sizeResolver set, we can execute it now
            request.executeAsComposable()
        } else {
            // Otherwise we need to modify the request with some a request size
            request.newBuilder()
                    .size(constraints.requestWidth.value, constraints.requestHeight.value)
                    .build()
                    .executeAsComposable()
        }

        val resultDrawable = result?.drawable
        if (resultDrawable != null) {
            val painter = remember(resultDrawable) {
                ImagePainter(resultDrawable.toBitmap().asImageAsset())
            }
            Image(
                painter = painter,
                contentScale = contentScale,
                alignment = alignment,
                colorFilter = colorFilter
            )
        }
    }
}

/**
 * An [ImagePainter] which draws the image with the given Android framework
 * [android.graphics.ColorFilter].
 */
private class AndroidColorMatrixImagePainter(
    private val image: ImageAsset,
    colorFilter: android.graphics.ColorFilter
) : Painter() {
    private val paint = Paint()
    private val size = PxSize(IntPx(image.width), IntPx(image.height))

    init {
        paint.asFrameworkPaint().colorFilter = colorFilter
    }

    override fun onDraw(canvas: Canvas, bounds: PxSize) {
        // Always draw the image in the top left as we expect it to be translated and scaled
        // in the appropriate position
        canvas.drawImage(image, Offset.zero, paint)
    }

    /**
     * Return the dimension of the underlying [Image] as it's intrinsic width and height
     */
    override val intrinsicSize: PxSize get() = size
}

/**
 * This will execute the [GetRequest] within a composable, ensuring that the request is only
 * execute once and storing the result, and cancelling requests as required.
 *
 * @return the result from the request execution, or `null` if the request has not finished yet.
 */
@Composable
fun GetRequest.executeAsComposable(): RequestResult? {
    var result by stateFor<RequestResult?>(this) { null }
    val context = ContextAmbient.current

    // Launch and execute a new request when it changes
    onCommit(this) {
        val job = CoroutineScope(Dispatchers.Main).launch {
            // Start loading the image and await the result
            result = Coil.imageLoader(context).execute(this@executeAsComposable)
        }

        // Cancel the request if the input to onCommit changes or
        // the Composition is removed from the composition tree.
        onDispose { job.cancel() }
    }

    return result
}
