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

import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Declare the height of the content to match the height of the status bars exactly.
 *
 * This is very handy when used with `Spacer` to push content below the status bars:
 * ```
 * Column {
 *     Spacer(Modifier.statusBarHeight())
 *
 *     // Content to be drawn below status bars (y-axis)
 * }
 * ```
 *
 * It's also useful when used to draw a scrim which matches the status bars:
 * ```
 * Spacer(
 *     Modifier.statusBarHeight()
 *         .fillMaxWidth()
 *         .drawBackground(MaterialTheme.colors.background.copy(alpha = 0.3f)
 * )
 * ```
 *
 * Internally this matches the behavior of the [Modifier.height] modifier.
 *
 * @param additional Any additional height to add to the status bars size.
 */
fun Modifier.statusBarsHeight(
    additional: Dp = 0.dp
): Modifier = composed {
    InsetsSizeModifier(
        insets = AmbientWindowInsets.current.statusBars,
        heightSide = VerticalSide.Top,
        additionalHeight = additional
    )
}

/**
 * Declare the preferred height of the content to match the height of the navigation bars when
 * present at the bottom of the screen.
 *
 * This is very handy when used with `Spacer` to push content below the navigation bars:
 * ```
 * Column {
 *     // Content to be drawn above status bars (y-axis)
 *     Spacer(Modifier.navigationBarHeight())
 * }
 * ```
 *
 * It's also useful when used to draw a scrim which matches the navigation bars:
 * ```
 * Spacer(
 *     Modifier.navigationBarHeight()
 *         .fillMaxWidth()
 *         .drawBackground(MaterialTheme.colors.background.copy(alpha = 0.3f)
 * )
 * ```
 *
 * Internally this matches the behavior of the [Modifier.height] modifier.
 *
 * @param additional Any additional height to add to the status bars size.
 */
fun Modifier.navigationBarsHeight(
    additional: Dp = 0.dp
): Modifier = composed {
    InsetsSizeModifier(
        insets = AmbientWindowInsets.current.navigationBars,
        heightSide = VerticalSide.Bottom,
        additionalHeight = additional
    )
}

/**
 * Declare the preferred width of the content to match the width of the navigation bars,
 * on the given [side].
 *
 * This is very handy when used with `Spacer` to push content inside from any vertical
 * navigation bars (typically when the device is in landscape):
 * ```
 * Row {
 *     Spacer(Modifier.navigationBarWidth(HorizontalSide.Left))
 *
 *     // Content to be inside the navigation bars (x-axis)
 *
 *     Spacer(Modifier.navigationBarWidth(HorizontalSide.Right))
 * }
 * ```
 *
 * It's also useful when used to draw a scrim which matches the navigation bars:
 * ```
 * Spacer(
 *     Modifier.navigationBarWidth(HorizontalSide.Left)
 *         .fillMaxHeight()
 *         .drawBackground(MaterialTheme.colors.background.copy(alpha = 0.3f)
 * )
 * ```
 *
 * Internally this matches the behavior of the [Modifier.height] modifier.
 *
 * @param side The navigation bar side to use as the source for the width.
 * @param additional Any additional width to add to the status bars size.
 */
fun Modifier.navigationBarsWidth(
    side: HorizontalSide,
    additional: Dp = 0.dp
): Modifier = composed {
    InsetsSizeModifier(
        insets = AmbientWindowInsets.current.navigationBars,
        widthSide = side,
        additionalWidth = additional
    )
}

/**
 * [Modifier] class which powers the modifiers above. This is the lower level modifier which
 * supports the functionality through a number of parameters.
 *
 * We may make this public at some point. If you need this, please let us know via the
 * issue tracker.
 */
private data class InsetsSizeModifier(
    private val insets: Insets,
    private val widthSide: HorizontalSide? = null,
    private val additionalWidth: Dp = 0.dp,
    private val heightSide: VerticalSide? = null,
    private val additionalHeight: Dp = 0.dp
) : LayoutModifier {
    private val Density.targetConstraints: Constraints
        get() {
            val additionalWidthPx = additionalWidth.toIntPx()
            val additionalHeightPx = additionalHeight.toIntPx()
            return Constraints(
                minWidth = additionalWidthPx + when (widthSide) {
                    HorizontalSide.Left -> insets.left
                    HorizontalSide.Right -> insets.right
                    null -> 0
                },
                minHeight = additionalHeightPx + when (heightSide) {
                    VerticalSide.Top -> insets.top
                    VerticalSide.Bottom -> insets.bottom
                    null -> 0
                },
                maxWidth = when (widthSide) {
                    HorizontalSide.Left -> insets.left + additionalWidthPx
                    HorizontalSide.Right -> insets.right + additionalWidthPx
                    null -> Constraints.Infinity
                },
                maxHeight = when (heightSide) {
                    VerticalSide.Top -> insets.top + additionalHeightPx
                    VerticalSide.Bottom -> insets.bottom + additionalHeightPx
                    null -> Constraints.Infinity
                }
            )
        }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val wrappedConstraints = targetConstraints.let { targetConstraints ->
            val resolvedMinWidth = if (widthSide != null) {
                targetConstraints.minWidth
            } else {
                constraints.minWidth.coerceAtMost(targetConstraints.maxWidth)
            }
            val resolvedMaxWidth = if (widthSide != null) {
                targetConstraints.maxWidth
            } else {
                constraints.maxWidth.coerceAtLeast(targetConstraints.minWidth)
            }
            val resolvedMinHeight = if (heightSide != null) {
                targetConstraints.minHeight
            } else {
                constraints.minHeight.coerceAtMost(targetConstraints.maxHeight)
            }
            val resolvedMaxHeight = if (heightSide != null) {
                targetConstraints.maxHeight
            } else {
                constraints.maxHeight.coerceAtLeast(targetConstraints.minHeight)
            }
            Constraints(
                resolvedMinWidth,
                resolvedMaxWidth,
                resolvedMinHeight,
                resolvedMaxHeight
            )
        }
        val placeable = measurable.measure(wrappedConstraints)
        return layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ) = measurable.minIntrinsicWidth(height).let {
        val constraints = targetConstraints
        it.coerceIn(constraints.minWidth, constraints.maxWidth)
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ) = measurable.maxIntrinsicWidth(height).let {
        val constraints = targetConstraints
        it.coerceIn(constraints.minWidth, constraints.maxWidth)
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ) = measurable.minIntrinsicHeight(width).let {
        val constraints = targetConstraints
        it.coerceIn(constraints.minHeight, constraints.maxHeight)
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ) = measurable.maxIntrinsicHeight(width).let {
        val constraints = targetConstraints
        it.coerceIn(constraints.minHeight, constraints.maxHeight)
    }
}
