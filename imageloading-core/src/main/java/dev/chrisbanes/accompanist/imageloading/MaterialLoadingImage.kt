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

@file:JvmName("MaterialLoadingImage")

package dev.chrisbanes.accompanist.imageloading

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.layout.ContentScale
import androidx.core.util.Pools

private const val DefaultTransitionDuration = 1000

/**
 * A wrapper around [Image] which implements the
 * [Material Image Loading](https://material.io/archive/guidelines/patterns/loading-images.html)
 * pattern.
 *
 * @param asset The [ImageBitmap] to draw.
 * @param contentDescription text used by accessibility services to describe what this image
 * represents. This should always be provided unless this image is used for decorative purposes,
 * and does not represent a meaningful action that a user can take. This text should be
 * localized, such as by using [androidx.compose.ui.res.stringResource] or similar.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content (ex.
 * background)
 * @param alignment Optional alignment parameter used to place the [ImageBitmap] in the given
 * bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be used
 * if the bounds are a different size from the intrinsic size of the [ImageBitmap].
 * @param colorFilter Optional ColorFilter to apply for the [ImageBitmap] when it is rendered
 * onscreen
 * @param fadeInEnabled Whether the fade-in animation should be used or not.
 * @param fadeInDurationMs The duration of the fade-in animation in milliseconds.
 */
@Composable
fun MaterialLoadingImage(
    asset: ImageBitmap,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    fadeInEnabled: Boolean = true,
    fadeInDurationMs: Int = DefaultTransitionDuration
) {
    MaterialLoadingImage(
        painter = BitmapPainter(asset),
        contentDescription = contentDescription,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        colorFilter = colorFilter,
        fadeInEnabled = fadeInEnabled,
        fadeInDurationMs = fadeInDurationMs
    )
}

/**
 * A wrapper around [Image] which implements the
 * [Material Image Loading](https://material.io/archive/guidelines/patterns/loading-images.html)
 * pattern.
 *
 * @param painter The [Painter] to draw.
 * @param contentDescription text used by accessibility services to describe what this image
 * represents. This should always be provided unless this image is used for decorative purposes,
 * and does not represent a meaningful action that a user can take. This text should be
 * localized, such as by using [androidx.compose.ui.res.stringResource] or similar.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content (ex.
 * background)
 * @param alignment Optional alignment parameter used to place the [painter] in the given
 * bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be used
 * if the bounds are a different size from the intrinsic size of the [ImageBitmap].
 * @param colorFilter Optional ColorFilter to apply for the [ImageBitmap] when it is rendered
 * onscreen
 * @param fadeInEnabled Whether the fade-in animation should be used or not.
 * @param fadeInDurationMs The duration of the fade-in animation in milliseconds.
 */
@Composable
fun MaterialLoadingImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    fadeInEnabled: Boolean = true,
    fadeInDurationMs: Int = DefaultTransitionDuration
) {
    Image(
        painter = when {
            fadeInEnabled -> painter.fadeIn(durationMs = fadeInDurationMs)
            else -> painter
        },
        contentDescription = contentDescription,
        alignment = alignment,
        contentScale = contentScale,
        colorFilter = colorFilter,
        modifier = modifier,
    )
}

/**
 * A wrapper around [Image] which implements the
 * [Material Image Loading](https://material.io/archive/guidelines/patterns/loading-images.html)
 * pattern.
 *
 * @param result A [ImageLoadState.Success] instance.
 * @param contentDescription text used by accessibility services to describe what this image
 * represents. This should always be provided unless this image is used for decorative purposes,
 * and does not represent a meaningful action that a user can take. This text should be
 * localized, such as by using [androidx.compose.ui.res.stringResource] or similar.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content (ex.
 * background)
 * @param alignment Optional alignment parameter used to place the [ImageBitmap] in the given
 * bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be used
 * if the bounds are a different size from the intrinsic size of the [ImageBitmap].
 * @param colorFilter Optional ColorFilter to apply for the [ImageBitmap] when it is rendered
 * onscreen
 * @param skipFadeWhenLoadedFromMemory Whether the fade animation should be skipped when the result
 * has been loaded from memory.
 * @param fadeInEnabled Whether the fade-in animation should be used or not.
 * @param fadeInDurationMs The duration of the fade-in animation in milliseconds.
 */
@Composable
fun MaterialLoadingImage(
    result: ImageLoadState.Success,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
    skipFadeWhenLoadedFromMemory: Boolean = true,
    fadeInEnabled: Boolean = true,
    fadeInDurationMs: Int = DefaultTransitionDuration
) {
    MaterialLoadingImage(
        painter = result.painter,
        contentDescription = contentDescription,
        alignment = alignment,
        contentScale = contentScale,
        colorFilter = colorFilter,
        modifier = modifier,
        fadeInEnabled = fadeInEnabled && !(skipFadeWhenLoadedFromMemory && result.isFromMemory()),
        fadeInDurationMs = fadeInDurationMs,
    )
}

@Composable
private fun Painter.fadeIn(durationMs: Int): Painter {
    // Create our transition state, which allow us to control the state and target states
    val transitionState = remember { MutableTransitionState(ImageLoadTransitionState.Empty) }
    transitionState.targetState = ImageLoadTransitionState.Loaded

    // Our actual transition, which reads our transitionState
    val transition = updateTransition(transitionState)

    // An ImageLoadingColorMatrix which we update from the transition
    val matrix = remember { ImageLoadingColorMatrix() }

    // Alpha animates over the first 50%
    matrix.alphaFraction = transition.animateFloat(
        transitionSpec = { tween(durationMillis = durationMs / 2) },
        targetValueByState = { if (it == ImageLoadTransitionState.Loaded) 1f else 0f }
    ).value

    // Brightness animates over the first 75%
    matrix.brightnessFraction = transition.animateFloat(
        transitionSpec = { tween(durationMillis = durationMs * 3 / 4) },
        targetValueByState = { if (it == ImageLoadTransitionState.Loaded) 1f else 0.8f }
    ).value

    // Saturation animates over whole duration
    matrix.saturationFraction = transition.animateFloat(
        transitionSpec = { tween(durationMillis = durationMs) },
        targetValueByState = { if (it == ImageLoadTransitionState.Loaded) 1f else 0f }
    ).value

    // Return our remembered ColorMatrixPainter, and update it's matrix
    return remember(this) {
        ColorMatrixPainter(this)
    }.apply {
        this.matrix = matrix
    }
}

/**
 * A [Painter] which draws the given [painter] using a color filter that transforms colors
 * through a 4x5 color [matrix].
 */
private class ColorMatrixPainter(
    private val painter: Painter
) : Painter() {
    // We use the NeverEqual equivalence check since this is a mutable entity.
    var matrix: ColorMatrix? by mutableStateOf(value = null, policy = neverEqualPolicy())

    override fun DrawScope.onDraw() {
        val paint = paintPool.acquire() ?: Paint()

        try {
            // If he a matrix, set on it on Android Painter within a ColorMatrixColorFilter
            matrix?.let { paint.asFrameworkPaint().colorFilter = ColorMatrixColorFilter(it) }

            drawIntoCanvas { canvas ->
                canvas.withSaveLayer(size.toRect(), paint) {
                    with(painter) { draw(size) }
                }
            }
        } finally {
            // Reset the Paint instance and release it back to the pool
            paint.asFrameworkPaint().reset()
            paintPool.release(paint)
        }
    }

    /**
     * Return the dimension of the underlying [painter] as its intrinsic width and height
     */
    override val intrinsicSize: Size get() = painter.intrinsicSize
}

private enum class ImageLoadTransitionState {
    Loaded, Empty
}

/**
 * A pool which allows us to cache and re-use [Paint] instances, which are relatively expensive
 * to create.
 */
private val paintPool = Pools.SimplePool<Paint>(2)

private fun ImageLoadState.Success.isFromMemory(): Boolean = source == DataSource.MEMORY
