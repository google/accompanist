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

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.LayoutDirection
/**
 * Contains default values used by [Modifier.placeholder] and [PlaceholderHighlight].
 */
object PlaceholderDefaults {
    /**
     * The default [InfiniteRepeatableSpec] to use for [fade].
     */
    val fadeAnimationSpec: InfiniteRepeatableSpec<Float> by lazy {
        infiniteRepeatable(
            animation = tween(delayMillis = 200, durationMillis = 600),
            repeatMode = RepeatMode.Reverse,
        )
    }

    /**
     * The default [InfiniteRepeatableSpec] to use for [shimmer].
     */
    val shimmerAnimationSpec: InfiniteRepeatableSpec<Float> by lazy {
        infiniteRepeatable(
            animation = tween(durationMillis = 1700, delayMillis = 200),
            repeatMode = RepeatMode.Restart
        )
    }
}

/**
 * Draws some skeleton UI which is typically used whilst content is 'loading'.
 *
 * A version of this modifier which uses appropriate values for Material themed apps is available
 * in the 'Placeholder Material' library.
 *
 * You can provide a [PlaceholderHighlight] which runs an highlight animation on the placeholder.
 * The [shimmer] and [fade] implementations are provided for easy usage.
 *
 * You can find more information on the pattern at the Material Theming
 * [Placeholder UI](https://material.io/design/communication/launch-screen.html#placeholder-ui)
 * guidelines.
 *
 * @param visible whether the placeholder should be visible or not.
 * @param color the color used to draw the placeholder UI.
 * @param highlight optional highlight animation.
 * @param shape desired shape of the placeholder.
 */
fun Modifier.placeholder(
    visible: Boolean,
    color: Color,
    highlight: PlaceholderHighlight? = null,
    shape: Shape = RectangleShape,
): Modifier = takeIf { visible.not() } ?: composed(
    inspectorInfo = debugInspectorInfo {
        name = "placeholder"
        value = visible
        properties["visible"] = visible
        properties["color"] = color
        properties["highlight"] = highlight
        properties["shape"] = shape
    }
) {
    // TODO: fade the placeholder in and out

    var lastSize: Size? by remember { mutableStateOf(null) }
    var lastLayoutDirection: LayoutDirection? by remember { mutableStateOf(null) }
    var lastOutline: Outline? by remember { mutableStateOf(null) }
    var progress: Float by remember { mutableStateOf(0f) }

    // Run the optional animation spec and update the progress
    progress = highlight?.animationSpec?.let { spec ->
        val infiniteTransition = rememberInfiniteTransition()
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = spec,
        ).value
    } ?: 0f

    remember(color, shape, highlight) {
        drawWithContent {
            // Draw normal composable content first
            drawContent()

            if (shape === RectangleShape) {
                // shortcut to avoid Outline calculation and allocation
                // Draw the initial background color
                drawRect(color = color)

                if (highlight != null) {
                    drawRect(
                        brush = highlight.brush(progress, size),
                        alpha = highlight.alpha(progress),
                    )
                }
            } else {
                val outline = lastOutline.takeIf {
                    size == lastSize && layoutDirection == lastLayoutDirection
                } ?: shape.createOutline(size, layoutDirection, this)

                // Draw the initial background color
                drawOutline(outline = outline, color = color)

                if (highlight != null) {
                    drawOutline(
                        outline = outline,
                        brush = highlight.brush(progress, size),
                        alpha = highlight.alpha(progress),
                    )
                }

                // Keep track of our outline
                lastOutline = outline
            }

            // Keep track of the last size & layout direction
            lastSize = size
            lastLayoutDirection = layoutDirection
        }
    }
}
