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

@file:Suppress("NOTHING_TO_INLINE", "unused")

@file:JvmName("ComposeInsets")
@file:JvmMultifileClass

package dev.chrisbanes.accompanist.insets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset

/**
 * Selectively apply additional space which matches the width/height of any system bars present
 * on the respective edges of the screen.
 *
 * @param enabled Whether to apply padding using the system bars dimensions on the respective edges.
 * Defaults to `true`.
 */
fun Modifier.systemBarsPadding(
    enabled: Boolean = true
): Modifier = composed {
    InsetsPaddingModifier(
        insets = LocalWindowInsets.current.systemBars,
        applyLeft = enabled,
        applyTop = enabled,
        applyRight = enabled,
        applyBottom = enabled
    )
}

/**
 * Apply additional space which matches the height of the status bars height along the top edge
 * of the content.
 */
fun Modifier.statusBarsPadding(): Modifier = composed {
    InsetsPaddingModifier(
        insets = LocalWindowInsets.current.statusBars,
        applyTop = true
    )
}

/**
 * Apply additional space which matches the height of the navigation bars height
 * along the [bottom] edge of the content, and additional space which matches the width of
 * the navigation bars on the respective [left] and [right] edges.
 *
 * @param bottom Whether to apply padding to the bottom edge, which matches the navigation bars
 * height (if present) at the bottom edge of the screen. Defaults to `true`.
 * @param left Whether to apply padding to the left edge, which matches the navigation bars width
 * (if present) on the left edge of the screen. Defaults to `true`.
 * @param right Whether to apply padding to the right edge, which matches the navigation bars width
 * (if present) on the right edge of the screen. Defaults to `true`.
 */
fun Modifier.navigationBarsPadding(
    bottom: Boolean = true,
    left: Boolean = true,
    right: Boolean = true
): Modifier = composed {
    InsetsPaddingModifier(
        insets = LocalWindowInsets.current.navigationBars,
        applyLeft = left,
        applyRight = right,
        applyBottom = bottom
    )
}

/**
 * Apply additional space which matches the height of the [WindowInsets.ime] (on-screen keyboard)
 * height along the bottom edge of the content.
 *
 * This method has no special handling for the [WindowInsets.navigationBars], which usually
 * intersect the [WindowInsets.ime]. Most apps will usually want to use the
 * [Modifier.navigationBarsWithImePadding] modifier.
 */
fun Modifier.imePadding(): Modifier = composed {
    InsetsPaddingModifier(
        insets = LocalWindowInsets.current.ime,
        applyLeft = true,
        applyRight = true,
        applyBottom = true,
    )
}

/**
 * Apply additional space which matches the height of the [WindowInsets.ime] (on-screen keyboard)
 * height and [WindowInsets.navigationBars]. This is what apps should use to handle any insets
 * at the bottom of the screen.
 */
fun Modifier.navigationBarsWithImePadding(): Modifier = composed {
    InsetsPaddingModifier(
        insets = LocalWindowInsets.current.ime,
        minimumInsets = LocalWindowInsets.current.navigationBars,
        applyLeft = true,
        applyRight = true,
        applyBottom = true,
    )
}

private data class InsetsPaddingModifier(
    private val insets: Insets,
    private val minimumInsets: Insets? = null,
    private val applyLeft: Boolean = false,
    private val applyTop: Boolean = false,
    private val applyRight: Boolean = false,
    private val applyBottom: Boolean = false,
) : LayoutModifier {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val transformedInsets = if (minimumInsets != null) {
            // If we have a minimum insets, coerce each dimensions
            insets.coerceEachDimensionAtLeast(minimumInsets)
        } else insets

        val left = if (applyLeft) transformedInsets.left else 0
        val top = if (applyTop) transformedInsets.top else 0
        val right = if (applyRight) transformedInsets.right else 0
        val bottom = if (applyBottom) transformedInsets.bottom else 0
        val horizontal = left + right
        val vertical = top + bottom

        val placeable = measurable.measure(constraints.offset(-horizontal, -vertical))

        val width = (placeable.width + horizontal)
            .coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = (placeable.height + vertical)
            .coerceIn(constraints.minHeight, constraints.maxHeight)
        return layout(width, height) {
            placeable.place(left, top)
        }
    }
}

/**
 * Returns the current insets converted into a [PaddingValues].
 *
 * @param start Whether to apply the inset on the start dimension.
 * @param top Whether to apply the inset on the top dimension.
 * @param end Whether to apply the inset on the end dimension.
 * @param bottom Whether to apply the inset on the bottom dimension.
 */
@Composable
fun Insets.toPaddingValues(
    start: Boolean = true,
    top: Boolean = true,
    end: Boolean = true,
    bottom: Boolean = true
): PaddingValues = with(LocalDensity.current) {
    val layoutDirection = LocalLayoutDirection.current
    PaddingValues(
        start = when {
            start && layoutDirection == LayoutDirection.Ltr -> this@toPaddingValues.left.toDp()
            start && layoutDirection == LayoutDirection.Rtl -> this@toPaddingValues.right.toDp()
            else -> 0.dp
        },
        top = when {
            top -> this@toPaddingValues.top.toDp()
            else -> 0.dp
        },
        end = when {
            end && layoutDirection == LayoutDirection.Ltr -> this@toPaddingValues.right.toDp()
            end && layoutDirection == LayoutDirection.Rtl -> this@toPaddingValues.left.toDp()
            else -> 0.dp
        },
        bottom = when {
            bottom -> this@toPaddingValues.bottom.toDp()
            else -> 0.dp
        }
    )
}

/**
 * Returns a new [PaddingValues] with the provided values added to each relevant dimension.
 *
 * @param start Value to add to the start dimension.
 * @param top Value to add to the top dimension.
 * @param end Value to add to the end dimension.
 * @param bottom Value to add to the bottom dimension.
 */
@Suppress("UNUSED_PARAMETER")
inline fun PaddingValues.add(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp,
): PaddingValues {
    // FIXME
    return this
//    val current = this
//    return PaddingValues(
//        start = current.start + start,
//        top = current.top + top,
//        end = current.end + end,
//        bottom = current.bottom + bottom
//    )
}
