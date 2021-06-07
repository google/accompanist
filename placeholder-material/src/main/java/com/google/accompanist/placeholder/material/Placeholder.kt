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

package com.google.accompanist.placeholder.material

import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder

/**
 * Contains default values used by [Modifier.placeholder] and [PlaceholderHighlight].
 */
object PlaceholderDefaults {
    /**
     * Returns the value used as the the `color` parameter value on [Modifier.placeholder].
     *
     * @param backgroundColor The current background color of the layout. Defaults to
     * `MaterialTheme.colors.surface`.
     */
    @Composable
    fun color(
        backgroundColor: Color = MaterialTheme.colors.surface
    ): Color {
        return contentColorFor(backgroundColor).copy(alpha = 0.1f).compositeOver(backgroundColor)
    }

    /**
     * Returns the value used as the the `highlightColor` parameter value of
     * [PlaceholderHighlight.Companion.fade].
     *
     * @param backgroundColor The current background color of the layout. Defaults to
     * `MaterialTheme.colors.surface`.
     */
    @Composable
    fun fadeHighlightColor(
        backgroundColor: Color = MaterialTheme.colors.surface
    ): Color {
        return backgroundColor.copy(alpha = 0.3f)
    }

    /**
     * Returns the value used as the the `highlightColor` parameter value of
     * [PlaceholderHighlight.Companion.shimmer].
     *
     * @param backgroundColor The current background color of the layout. Defaults to
     * `MaterialTheme.colors.surface`.
     */
    @Composable
    fun shimmerHighlightColor(
        backgroundColor: Color = MaterialTheme.colors.surface
    ): Color {
        return backgroundColor.copy(alpha = 0.75f)
    }
}

/**
 * Draws some skeleton UI which is typically used whilst content is 'loading'.
 *
 * To customize the color and shape of the placeholder, you can use the foundation version of
 * [Modifier.placeholder], along with the values provided bu [PlaceholderDefaults].
 *
 * You can provide a [PlaceholderHighlight] which runs an highlight animation on the placeholder.
 * The [shimmer] and [fade] implementations are provided for easy usage.
 *
 * @sample com.google.accompanist.sample.placeholder.PlaceholderFadeSample
 * @sample com.google.accompanist.sample.placeholder.PlaceholderShimmerSample
 *
 * @param visible whether the placeholder should be visible or not.
 * @param highlight optional highlight animation.
 */
fun Modifier.placeholder(
    visible: Boolean,
    highlight: PlaceholderHighlight? = null,
): Modifier = composed {
    Modifier.placeholder(
        visible = visible,
        color = PlaceholderDefaults.color(),
        highlight = highlight,
        shape = MaterialTheme.shapes.small,
    )
}
