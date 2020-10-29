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
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.LayoutModifier
import androidx.compose.ui.Measurable
import androidx.compose.ui.MeasureScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.platform.LayoutDirectionAmbient
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
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
fun Modifier.statusBarsHeight(additional: Dp = 0.dp) = composed {
    InsetsSizeModifier(
        insets = AmbientWindowInsets.current.statusBars,
        heightSide = VerticalSide.Top,
        additionalHeight = additional
    )
}

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
 */
inline fun Modifier.statusBarsHeight() = statusBarsHeightPlus(0.dp)

/**
 * Declare the height of the content to match the height of the status bars, plus some
 * additional height passed in via [additional].
 *
 * As an example, this could be used with `Spacer` to push content below the status bar
 * and app bars:
 *
 * ```
 * Column {
 *     Spacer(Modifier.statusBarHeightPlus(56.dp))
 *
 *     // Content to be drawn below status bars and app bar (y-axis)
 * }
 * ```
 *
 * Internally this matches the behavior of the [Modifier.height] modifier.
 *
 * @param additional Any additional height to add to the status bars size.
 */
fun Modifier.statusBarsHeightPlus(additional: Dp) = composed {
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
 */
inline fun Modifier.navigationBarsHeight() = navigationBarsHeightPlus(0.dp)

/**
 * Declare the height of the content to match the height of the navigation bars, plus some
 * additional height passed in via [additional]
 *
 * As an example, this could be used with `Spacer` to push content above the navigation bar
 * and bottom app bars:
 *
 * ```
 * Column {
 *     // Content to be drawn above navigation bars and bottom app bar (y-axis)
 *
 *     Spacer(Modifier.statusBarHeightPlus(48.dp))
 * }
 * ```
 *
 * Internally this matches the behavior of the [Modifier.height] modifier.
 *
 * @param additional Any additional height to add to the status bars size.
 */
fun Modifier.navigationBarsHeightPlus(additional: Dp) = composed {
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
 */
inline fun Modifier.navigationBarWidth(side: HorizontalSide) = navigationBarsWidthPlus(side, 0.dp)

/**
 * Declare the preferred width of the content to match the width of the navigation bars,
 * on the given [side], plus additional width passed in via [additional].
 *
 * Internally this matches the behavior of the [Modifier.height] modifier.
 *
 * @param side The navigation bar side to use as the source for the width.
 * @param additional Any additional width to add to the status bars size.
 */
fun Modifier.navigationBarsWidthPlus(
    side: HorizontalSide,
    additional: Dp
) = composed {
    InsetsSizeModifier(
        insets = AmbientWindowInsets.current.navigationBars,
        widthSide = side,
        additionalWidth = additional
    )
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
): PaddingValues = with(DensityAmbient.current) {
    val layoutDirection = LayoutDirectionAmbient.current
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
    ): MeasureScope.MeasureResult {
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
