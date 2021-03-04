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

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale

internal const val DefaultTransitionDuration = 1000

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
    val cf = if (fadeInEnabled) {
        val fadeInTransition = updateFadeInTransition(key = painter, durationMs = fadeInDurationMs)
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

    Image(
        painter = painter,
        contentDescription = contentDescription,
        alignment = alignment,
        contentScale = contentScale,
        colorFilter = cf,
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
internal fun updateFadeInTransition(key: Any, durationMs: Int): FadeInTransition {
    // Create our transition state, which allow us to control the state and target states
    val transitionState = remember(key) {
        MutableTransitionState(ImageLoadTransitionState.Empty).apply {
            targetState = ImageLoadTransitionState.Loaded
        }
    }

    // Our actual transition, which reads our transitionState
    val transition = updateTransition(transitionState)

    // Alpha animates over the first 50%
    val alpha = transition.animateFloat(
        transitionSpec = { tween(durationMillis = durationMs / 2) },
        targetValueByState = { if (it == ImageLoadTransitionState.Loaded) 1f else 0f }
    )

    // Brightness animates over the first 75%
    val brightness = transition.animateFloat(
        transitionSpec = { tween(durationMillis = durationMs * 3 / 4) },
        targetValueByState = { if (it == ImageLoadTransitionState.Loaded) 1f else 0.8f }
    )

    // Saturation animates over whole duration
    val saturation = transition.animateFloat(
        transitionSpec = { tween(durationMillis = durationMs) },
        targetValueByState = { if (it == ImageLoadTransitionState.Loaded) 1f else 0f }
    )

    return remember(transition) { FadeInTransition(alpha, brightness, saturation) }
}

@Stable
internal class FadeInTransition(
    alpha: State<Float> = mutableStateOf(0f),
    brightness: State<Float> = mutableStateOf(0f),
    saturation: State<Float> = mutableStateOf(0f),
) {
    val alpha by alpha
    val brightness by brightness
    val saturation by saturation
}

private enum class ImageLoadTransitionState { Loaded, Empty }

private fun ImageLoadState.Success.isFromMemory(): Boolean = source == DataSource.MEMORY

/**
 * Ideally we'd use setToSaturation. We can't use that though since it
 * resets the matrix before applying the values
 */
internal fun ColorMatrix.updateSaturation(saturation: Float) {
    val invSat = 1 - saturation
    val R = 0.213f * invSat
    val G = 0.715f * invSat
    val B = 0.072f * invSat
    this[0, 0] = R + saturation
    this[0, 1] = G
    this[0, 2] = B
    this[1, 0] = R
    this[1, 1] = G + saturation
    this[1, 2] = B
    this[2, 0] = R
    this[2, 1] = G
    this[2, 2] = B + saturation
}

internal fun ColorMatrix.updateBrightness(brightness: Float) {
    val darkening = (1f - brightness) * 255
    this[0, 4] = darkening
    this[1, 4] = darkening
    this[2, 4] = darkening
}

internal fun ColorMatrix.updateAlpha(alpha: Float) = set(row = 3, column = 3, v = alpha)
