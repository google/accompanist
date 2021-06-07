/*
 * Copyright 2021 The Android Open Source Project
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

package com.google.accompanist.placeholder

import androidx.annotation.FloatRange
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.util.lerp
import kotlin.math.max

/**
 * A class which provides a brush to paint placeholder based on progress.
 */
@Stable
interface PlaceholderHighlight {
    /**
     * An [AnimationSpec] that loops infinitely.
     */
    val animationSpec: InfiniteRepeatableSpec<Float>

    /**
     * Create a [Brush] along to the given [progress].
     *
     * @param progress this animated progress in the range of 0f..1f.
     */
    fun brush(progress: Float, size: Size): Brush

    /**
     * The desired alpha value used when drawing the [Brush] returned from [brush].
     *
     * @param progress this animated progress in the range of 0f..1f.
     */
    @FloatRange(from = 0.0, to = 1.0)
    fun alpha(progress: Float): Float

    companion object
}

/**
 * Creates a [Fade] brush with the given initial and target colors.
 *
 * @sample com.google.accompanist.sample.placeholder.DocSample_PlaceholderFade
 *
 * @param highlightColor the color of the highlight which is faded in/out.
 * @param animationSpec the [AnimationSpec] to configure the animation.
 */
fun PlaceholderHighlight.Companion.fade(
    highlightColor: Color,
    animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(
            delayMillis = 200,
            durationMillis = 600,
        ),
        repeatMode = RepeatMode.Reverse
    ),
): PlaceholderHighlight = Fade(
    highlightColor = highlightColor,
    animationSpec = animationSpec,
)

/**
 * Creates a [Shimmer] brush with a highlight color over the given color.
 *
 * @sample com.google.accompanist.sample.placeholder.DocSample_PlaceholderShimmer
 *
 * @param highlightColor the color of the highlight 'shimmer'.
 * @param animationSpec the [AnimationSpec] to configure the animation.
 */
fun PlaceholderHighlight.Companion.shimmer(
    highlightColor: Color,
    animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis = 1700, delayMillis = 200),
        repeatMode = RepeatMode.Restart
    ),
): PlaceholderHighlight = Shimmer(
    highlightColor = highlightColor,
    animationSpec = animationSpec
)

private data class Fade(
    private val highlightColor: Color,
    override val animationSpec: InfiniteRepeatableSpec<Float>,
) : PlaceholderHighlight {
    private val brush = SolidColor(highlightColor)

    override fun brush(progress: Float, size: Size): Brush = brush
    override fun alpha(progress: Float): Float = progress
}

private data class Shimmer(
    private val highlightColor: Color,
    override val animationSpec: InfiniteRepeatableSpec<Float>,
) : PlaceholderHighlight {
    override fun brush(
        progress: Float,
        size: Size,
    ): Brush = Brush.radialGradient(
        colors = listOf(
            highlightColor.copy(alpha = 0f),
            highlightColor,
            highlightColor.copy(alpha = 0f),
        ),
        center = Offset(x = 0f, y = 0f),
        radius = (max(size.width, size.height) * progress * 2).coerceAtLeast(0.01f),
    )

    override fun alpha(progress: Float): Float = when {
        // From 0f...ProgressForOpaqueAlpha we animate from 0..1
        progress <= ProgressForOpaqueAlpha -> {
            lerp(
                start = 0f,
                stop = 1f,
                fraction = progress / ProgressForOpaqueAlpha
            )
        }
        // From ProgressForOpaqueAlpha..1f we animate from 1..0
        else -> {
            lerp(
                start = 1f,
                stop = 0f,
                fraction = (progress - ProgressForOpaqueAlpha) / (1f - ProgressForOpaqueAlpha)
            )
        }
    }

    private companion object {
        private const val ProgressForOpaqueAlpha = 0.6f
    }
}
