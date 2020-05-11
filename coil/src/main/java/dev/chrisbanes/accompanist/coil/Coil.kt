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
import androidx.ui.core.ContentScale
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.WithConstraints
import androidx.ui.core.hasBoundedHeight
import androidx.ui.core.hasBoundedWidth
import androidx.ui.core.hasFixedHeight
import androidx.ui.core.hasFixedWidth
import androidx.ui.foundation.Box
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

/**
 * A composable which loads an image using [Coil] into a [Box], using a crossfade when first
 * loaded.
 */
@Composable
fun CoilImageWithCrossfade(
    data: Any,
    requestBuilder: ((GetRequestBuilder) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    crossfadeDuration: Int = 1000,
    modifier: Modifier = Modifier
) {
    WithConstraints(modifier) { constraints, _ ->
        var imgLoadState by stateFor(data) { ImageLoadState.Empty }

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

        val requestWidth = when {
            constraints.hasFixedWidth -> constraints.maxWidth
            constraints.hasBoundedWidth -> constraints.maxWidth
            else -> constraints.minWidth
        }

        val requestHeight = when {
            constraints.hasFixedHeight -> constraints.maxHeight
            constraints.hasBoundedHeight -> constraints.maxHeight
            else -> constraints.minHeight
        }

        val result = GetRequest.Builder(ContextAmbient.current)
                .data(data)
                .size(requestWidth.value, requestHeight.value)
                .apply {
                    if (requestBuilder != null) {
                        requestBuilder(this)
                    }
                }
                .build()
                .executeAsComposable()

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
 * A simple composable which loads an image using [Coil] into a [Box]
 */
@Composable
fun CoilImage(
    data: Any,
    requestBuilder: ((GetRequestBuilder) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    modifier: Modifier = Modifier
) {
    WithConstraints(modifier) { constraints, _ ->
        val requestWidth = when {
            constraints.hasFixedWidth -> constraints.maxWidth
            constraints.hasBoundedWidth -> constraints.maxWidth
            else -> constraints.minWidth
        }

        val requestHeight = when {
            constraints.hasFixedHeight -> constraints.maxHeight
            constraints.hasBoundedHeight -> constraints.maxHeight
            else -> constraints.minHeight
        }

        val result = GetRequest.Builder(ContextAmbient.current)
                .data(data)
                .size(requestWidth.value, requestHeight.value)
                .apply {
                    if (requestBuilder != null) {
                        requestBuilder(this)
                    }
                }
                .build()
                .executeAsComposable()

        if (result?.drawable != null) {
            Image(
                painter = ImagePainter(result.drawable!!.toBitmap().asImageAsset()),
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
 * A simple [loadImage] composable, which loads [data].
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
