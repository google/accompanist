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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp

internal object PlaceholderDefaults {
    val PlaceholderColor = Color.Gray.copy(alpha = 0.5f)
    val PlaceholderHighlightColor = Color.Gray.copy(alpha = 0.3f)
}

@Composable
fun fadeBrush(
    initialColor: Color = PlaceholderDefaults.PlaceholderColor,
    targetColor: Color = PlaceholderDefaults.PlaceholderHighlightColor,
    animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(
            delayMillis = 0,
            durationMillis = 500,
            easing = LinearEasing
        ),
        repeatMode = RepeatMode.Reverse
    ),
): PlaceholderAnimatedBrush {
    return Fade(
        initialColor = initialColor,
        targetColor = targetColor,
        animationSpec = animationSpec,
    )
}

@Composable
fun shimmerBrush(
    color: Color = PlaceholderDefaults.PlaceholderColor,
    highlightColor: Color = PlaceholderDefaults.PlaceholderHighlightColor,
    animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(
            delayMillis = 500,
            durationMillis = 500,
            easing = LinearEasing
        ),
        repeatMode = RepeatMode.Restart
    ),
): PlaceholderAnimatedBrush {
    return Shimmer(
        color = color,
        highlightColor = highlightColor,
        animationSpec = animationSpec
    )
}

sealed class PlaceholderAnimatedBrush {

    abstract fun initialValue(): Float

    abstract fun targetValue(): Float

    abstract fun animationSpec(): InfiniteRepeatableSpec<Float>

    abstract fun brush(progress: Float): Brush
}

internal class Fade(
    private val initialColor: Color,
    private val targetColor: Color,
    private val animationSpec: InfiniteRepeatableSpec<Float>
) : PlaceholderAnimatedBrush() {

    override fun initialValue(): Float = 0f

    override fun targetValue(): Float = 1f

    override fun animationSpec(): InfiniteRepeatableSpec<Float> = animationSpec

    override fun brush(progress: Float): Brush {
        return SolidColor(lerp(initialColor, targetColor, progress))
    }

    override fun hashCode(): Int {
        var result = initialColor.hashCode()
        result = 31 * result + targetColor.hashCode()
        result = 31 * result + animationSpec.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        val otherBrush = other as? Fade ?: return false
        return initialColor == otherBrush.initialColor &&
            targetColor == otherBrush.targetColor &&
            animationSpec == otherBrush.animationSpec
    }

    override fun toString(): String =
        "Fade(initialColor=$initialColor, targetColor=$targetColor, animationSpec=$animationSpec)"
}

internal class Shimmer(
    private val color: Color,
    private val highlightColor: Color,
    private val animationSpec: InfiniteRepeatableSpec<Float>
) : PlaceholderAnimatedBrush() {

    override fun initialValue(): Float = 0f - offset

    override fun targetValue(): Float = 1f + offset

    override fun animationSpec(): InfiniteRepeatableSpec<Float> = animationSpec

    override fun brush(progress: Float): Brush {
        return Brush.linearGradient(
            colorStops = arrayOf(
                (progress - offset) to color,
                progress to highlightColor,
                (progress + offset) to color,
            )
        )
    }

    override fun hashCode(): Int {
        var result = color.hashCode()
        result = 31 * result + highlightColor.hashCode()
        result = 31 * result + animationSpec.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        val otherBrush = other as? Shimmer ?: return false
        return color == otherBrush.color &&
            highlightColor == otherBrush.highlightColor &&
            animationSpec == otherBrush.animationSpec
    }

    override fun toString(): String =
        "Shimmer(color=$color, highlightColor=$highlightColor, animationSpec=$animationSpec)"

    companion object {
        private const val offset = 0.5f
    }
}
