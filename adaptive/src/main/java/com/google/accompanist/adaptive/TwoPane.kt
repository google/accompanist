/*
 * Copyright 2022 The Android Open Source Project
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

package com.google.accompanist.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.constrain
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.window.layout.FoldingFeature
import kotlin.math.roundToInt

/**
 * A layout that places two different pieces of content defined by the [first] and [second]
 * slots where the arrangement, sizes and separation behaviour is controlled by [TwoPaneStrategy].
 *
 * The default implementations of [TwoPaneStrategy] are fold and hinges aware, meaning that the two
 * pane will adopt its layout to properly separate [first] and [second] panes so they don't
 * interfere with hardware hinges (vertical or horizontal), or respect folds when needed
 * (for example, when foldable is half-folded (90-degree fold AKA tabletop) the split will become
 * on the bend).
 *
 * The [TwoPane] layout will always place both [first] and [second], based on the provided
 * [strategy] and window environment. If you instead only want to place one or the other,
 * that should be controlled at a higher level and not calling [TwoPane] if placing both is not
 * desired.
 *
 * @param first the first content of the layout, a left-most in LTR, a right-most in RTL and
 * top-most in a vertical split based on the [SplitResult] of [TwoPaneStrategy.calculateSplitResult]
 * @param second the second content of the layout, a right-most in the LTR, a left-most in the RTL
 * and the bottom-most in a vertical split based on the [SplitResult] of
 * [TwoPaneStrategy.calculateSplitResult]
 * @param strategy strategy of the two pane that controls the arrangement of the layout
 * @param modifier an optional modifier for the layout
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
public fun TwoPane(
    first: @Composable () -> Unit,
    second: @Composable () -> Unit,
    strategy: TwoPaneStrategy,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    Layout(
        modifier = modifier.wrapContentSize(),
        content = {
            Box(Modifier.layoutId("first")) {
                first()
            }
            Box(Modifier.layoutId("second")) {
                second()
            }
        }
    ) { measurable, constraints ->
        val firstMeasurable = measurable.find { it.layoutId == "first" }!!
        val secondMeasurable = measurable.find { it.layoutId == "second" }!!

        layout(constraints.maxWidth, constraints.maxHeight) {
            val splitResult = strategy.calculateSplitResult(
                density = density,
                layoutDirection = layoutDirection,
                layoutCoordinates = checkNotNull(coordinates) {
                    "TwoPane does not support the use of alignment lines!"
                }
            )

            val isHorizontalSplit = splitResult.isHorizontalSplit
            val gapBounds = splitResult.gapBounds

            val gapLeft = constraints.constrainWidth(gapBounds.left.roundToInt())
            val gapRight = constraints.constrainWidth(gapBounds.right.roundToInt())
            val gapTop = constraints.constrainHeight(gapBounds.top.roundToInt())
            val gapBottom = constraints.constrainHeight(gapBounds.bottom.roundToInt())
            val firstConstraints =
                if (isHorizontalSplit) {
                    val width = when (layoutDirection) {
                        LayoutDirection.Ltr -> gapLeft
                        LayoutDirection.Rtl -> constraints.maxWidth - gapRight
                    }

                    constraints.copy(minWidth = width, maxWidth = width)
                } else {
                    constraints.copy(minHeight = gapTop, maxHeight = gapTop)
                }
            val secondConstraints =
                if (isHorizontalSplit) {
                    val width = when (layoutDirection) {
                        LayoutDirection.Ltr -> constraints.maxWidth - gapRight
                        LayoutDirection.Rtl -> gapLeft
                    }
                    constraints.copy(minWidth = width, maxWidth = width)
                } else {
                    val height = constraints.maxHeight - gapBottom
                    constraints.copy(minHeight = height, maxHeight = height)
                }
            val firstPlaceable = firstMeasurable.measure(constraints.constrain(firstConstraints))
            val secondPlaceable = secondMeasurable.measure(constraints.constrain(secondConstraints))

            firstPlaceable.placeRelative(0, 0)
            val detailOffsetX =
                if (isHorizontalSplit) {
                    constraints.maxWidth - secondPlaceable.width
                } else {
                    0
                }
            val detailOffsetY =
                if (isHorizontalSplit) {
                    0
                } else {
                    constraints.maxHeight - secondPlaceable.height
                }
            secondPlaceable.placeRelative(detailOffsetX, detailOffsetY)
        }
    }
}

/**
 * Returns the specification for where to place a split in [TwoPane] as a result of
 * [TwoPaneStrategy.calculateSplitResult]
 */
public class SplitResult(

    /**
     * Whether the split is vertical or horizontal
     */
    public val isHorizontalSplit: Boolean,

    /**
     * The bounds that are nether a `start` pane or an `end` pane, but a separation between those
     * two. In case width or height is 0 - it means that the gap itself is a 0 width/height, but the
     * place within the layout is still defined.
     *
     * The [gapBounds] should be defined in local bounds to the [TwoPane].
     */
    public val gapBounds: Rect,
)

/**
 * A strategy for configuring the [TwoPane] component, that is responsible for the meta-data
 * corresponding to the arrangement of the two panes of the layout.
 */
public fun interface TwoPaneStrategy {
    /**
     * Calculates the split result in local bounds of the [TwoPane].
     *
     * @param density the [Density] for measuring and laying out
     * @param layoutDirection the [LayoutDirection] for measuring and laying out
     * @param layoutCoordinates the [LayoutCoordinates] of the [TwoPane]
     */
    public fun calculateSplitResult(
        density: Density,
        layoutDirection: LayoutDirection,
        layoutCoordinates: LayoutCoordinates
    ): SplitResult
}

/**
 * A strategy for configuring the [TwoPane] component, that is responsible for the meta-data
 * corresponding to the arrangement of the two panes of the layout.
 *
 * This strategy can be conditional: If `null` is returned from [calculateSplitResult], then this
 * strategy did not produce a split result to use, and a different strategy should be used.
 */
private fun interface ConditionalTwoPaneStrategy {
    /**
     * Calculates the split result in local bounds of the [TwoPane], or `null` if this strategy
     * does not apply.
     *
     * @param density the [Density] for measuring and laying out
     * @param layoutDirection the [LayoutDirection] for measuring and laying out
     * @param layoutCoordinates the [LayoutCoordinates] of the [TwoPane]
     */
    public fun calculateSplitResult(
        density: Density,
        layoutDirection: LayoutDirection,
        layoutCoordinates: LayoutCoordinates
    ): SplitResult?
}

/**
 * Returns a [TwoPaneStrategy] that will place the slots vertically or horizontally if there is a
 * horizontal or vertical fold respectively.
 *
 * If there is no fold, then the [defaultStrategy] will be used instead.
 */
public fun TwoPaneStrategy(
    windowGeometry: WindowGeometry,
    defaultStrategy: TwoPaneStrategy,
): TwoPaneStrategy = TwoPaneStrategy(
    FoldAwareHorizontalTwoPaneStrategy(windowGeometry),
    FoldAwareVerticalTwoPaneStrategy(windowGeometry),
    defaultStrategy = defaultStrategy
)

/**
 * Returns a [TwoPaneStrategy] that will place the slots horizontally if there is a vertical fold.
 *
 * If there is no fold, then the [defaultStrategy] will be used instead.
 */
public fun HorizontalTwoPaneStrategy(
    windowGeometry: WindowGeometry,
    defaultStrategy: TwoPaneStrategy,
): TwoPaneStrategy = TwoPaneStrategy(
    FoldAwareHorizontalTwoPaneStrategy(windowGeometry),
    defaultStrategy = defaultStrategy
)

/**
 * Returns a [TwoPaneStrategy] that will place the slots horizontally.
 *
 * If there is a vertical fold, then the gap will be placed along the fold.
 *
 * Otherwise, the gap will be placed at the given [splitFraction] from start, with the given
 * [gapWidth].
 */
public fun HorizontalTwoPaneStrategy(
    windowGeometry: WindowGeometry,
    splitFraction: Float,
    gapWidth: Dp = 0.dp,
): TwoPaneStrategy = TwoPaneStrategy(
    FoldAwareHorizontalTwoPaneStrategy(windowGeometry),
    defaultStrategy = FractionHorizontalTwoPaneStrategy(
        splitFraction = splitFraction,
        gapWidth = gapWidth
    )
)

/**
 * Returns a [TwoPaneStrategy] that will place the slots horizontally.
 *
 * If there is a vertical fold, then the gap will be placed along the fold.
 *
 * Otherwise, the gap will be placed at [splitOffset] either from the start or end based on
 * [offsetFromStart], with the given [gapWidth].
 */
public fun HorizontalTwoPaneStrategy(
    windowGeometry: WindowGeometry,
    splitOffset: Dp,
    offsetFromStart: Boolean,
    gapWidth: Dp = 0.dp,
): TwoPaneStrategy = TwoPaneStrategy(
    FoldAwareHorizontalTwoPaneStrategy(windowGeometry),
    defaultStrategy = FixedOffsetHorizontalTwoPaneStrategy(
        splitOffset = splitOffset,
        offsetFromStart = offsetFromStart,
        gapWidth = gapWidth
    )
)

/**
 * Returns a [TwoPaneStrategy] that will place the slots vertically if there is a horizontal fold.
 *
 * If there is no fold, then the [defaultStrategy] will be used instead.
 */
public fun VerticalTwoPaneStrategy(
    windowGeometry: WindowGeometry,
    defaultStrategy: TwoPaneStrategy,
): TwoPaneStrategy = TwoPaneStrategy(
    FoldAwareVerticalTwoPaneStrategy(windowGeometry),
    defaultStrategy = defaultStrategy
)

/**
 * Returns a [TwoPaneStrategy] that will place the slots horizontally.
 *
 * If there is a vertical fold, then the gap will be placed along the fold.
 *
 * Otherwise, the gap will be placed at the given [splitFraction] from top, with the given
 * [gapHeight].
 */
public fun VerticalTwoPaneStrategy(
    windowGeometry: WindowGeometry,
    splitFraction: Float,
    gapHeight: Dp = 0.dp,
): TwoPaneStrategy = TwoPaneStrategy(
    FoldAwareHorizontalTwoPaneStrategy(windowGeometry),
    defaultStrategy = FractionVerticalTwoPaneStrategy(
        splitFraction = splitFraction,
        gapHeight = gapHeight
    )
)

/**
 * Returns a [TwoPaneStrategy] that will place the slots horizontally.
 *
 * If there is a vertical fold, then the gap will be placed along the fold.
 *
 * Otherwise, the gap will be placed at [splitOffset] either from the top or bottom based on
 * [offsetFromTop], with the given [gapHeight].
 */
public fun VerticalTwoPaneStrategy(
    windowGeometry: WindowGeometry,
    splitOffset: Dp,
    offsetFromTop: Boolean,
    gapHeight: Dp = 0.dp,
): TwoPaneStrategy = TwoPaneStrategy(
    FoldAwareHorizontalTwoPaneStrategy(windowGeometry),
    defaultStrategy = FixedOffsetVerticalTwoPaneStrategy(
        splitOffset = splitOffset,
        offsetFromTop = offsetFromTop,
        gapHeight = gapHeight
    )
)

/**
 * Returns a composite [TwoPaneStrategy].
 *
 * The conditional strategies (if any) will be attempted in order, and their split result used
 * if they return one. If none return a split result, then the [defaultStrategy] will be used,
 * which guarantees returning a [SplitResult].
 */
private fun TwoPaneStrategy(
    vararg conditionalStrategies: ConditionalTwoPaneStrategy,
    defaultStrategy: TwoPaneStrategy
): TwoPaneStrategy = TwoPaneStrategy { density, layoutDirection, layoutCoordinates ->
    conditionalStrategies.firstNotNullOfOrNull { conditionalTwoPaneStrategy ->
        conditionalTwoPaneStrategy.calculateSplitResult(
            density = density,
            layoutDirection = layoutDirection,
            layoutCoordinates = layoutCoordinates
        )
    } ?: defaultStrategy.calculateSplitResult(
        density = density,
        layoutDirection = layoutDirection,
        layoutCoordinates = layoutCoordinates
    )
}

/**
 * Returns a [ConditionalTwoPaneStrategy] that will place the slots horizontally if there is a
 * vertical fold, or `null` if there is no fold.
 */
private fun FoldAwareHorizontalTwoPaneStrategy(
    windowGeometry: WindowGeometry,
): ConditionalTwoPaneStrategy = ConditionalTwoPaneStrategy { _, _, layoutCoordinates ->
    val verticalFold = windowGeometry.displayFeatures.find {
        it is FoldingFeature && it.orientation == FoldingFeature.Orientation.VERTICAL
    } as FoldingFeature?

    if (verticalFold != null &&
        (
            verticalFold.isSeparating ||
                verticalFold.occlusionType == FoldingFeature.OcclusionType.FULL
            ) &&
        verticalFold.bounds.toComposeRect().overlaps(layoutCoordinates.boundsInWindow())
    ) {
        val foldBounds = verticalFold.bounds.toComposeRect()
        SplitResult(
            isHorizontalSplit = true,
            gapBounds = Rect(
                layoutCoordinates.windowToLocal(foldBounds.topLeft),
                layoutCoordinates.windowToLocal(foldBounds.bottomRight)
            )
        )
    } else {
        null
    }
}

/**
 * Returns a [ConditionalTwoPaneStrategy] that will place the slots vertically if there is a
 * horizontal fold, or `null` if there is no fold.
 */
private fun FoldAwareVerticalTwoPaneStrategy(
    windowGeometry: WindowGeometry,
): ConditionalTwoPaneStrategy = ConditionalTwoPaneStrategy { _, _, layoutCoordinates ->
    val horizontalFold = windowGeometry.displayFeatures.find {
        it is FoldingFeature && it.orientation == FoldingFeature.Orientation.HORIZONTAL
    } as FoldingFeature?

    if (horizontalFold != null &&
        (
            horizontalFold.isSeparating ||
                horizontalFold.occlusionType == FoldingFeature.OcclusionType.FULL
            ) &&
        horizontalFold.bounds.toComposeRect().overlaps(layoutCoordinates.boundsInWindow())
    ) {
        val foldBounds = horizontalFold.bounds.toComposeRect()
        SplitResult(
            isHorizontalSplit = false,
            gapBounds = Rect(
                layoutCoordinates.windowToLocal(foldBounds.topLeft),
                layoutCoordinates.windowToLocal(foldBounds.bottomRight)
            )
        )
    } else {
        null
    }
}

/**
 * Returns a [TwoPaneStrategy] that will place the slots horizontally.
 *
 * The gap will be placed at the given [splitFraction] from start, with the given [gapWidth].
 *
 * This strategy is _not_ fold aware.
 */
internal fun FractionHorizontalTwoPaneStrategy(
    splitFraction: Float,
    gapWidth: Dp = 0.dp,
): TwoPaneStrategy = TwoPaneStrategy { density, layoutDirection, layoutCoordinates ->
    val splitX = layoutCoordinates.size.width * when (layoutDirection) {
        LayoutDirection.Ltr -> splitFraction
        LayoutDirection.Rtl -> 1 - splitFraction
    }
    val splitWidthPixel = with(density) { gapWidth.toPx() }

    SplitResult(
        isHorizontalSplit = true,
        gapBounds = Rect(
            left = splitX - splitWidthPixel / 2f,
            top = 0f,
            right = splitX + splitWidthPixel / 2f,
            bottom = layoutCoordinates.size.height.toFloat(),
        )
    )
}

/**
 * Returns a [TwoPaneStrategy] that will place the slots horizontally.
 *
 * The gap will be placed at [splitOffset] either from the start or end based on
 * [offsetFromStart], with the given [gapWidth].
 *
 * This strategy is _not_ fold aware.
 */
internal fun FixedOffsetHorizontalTwoPaneStrategy(
    splitOffset: Dp,
    offsetFromStart: Boolean,
    gapWidth: Dp = 0.dp,
): TwoPaneStrategy = TwoPaneStrategy { density, layoutDirection, layoutCoordinates ->
    val splitOffsetPixel = with(density) { splitOffset.toPx() }
    val splitX = when (layoutDirection) {
        LayoutDirection.Ltr ->
            if (offsetFromStart) {
                splitOffsetPixel
            } else {
                layoutCoordinates.size.width - splitOffsetPixel
            }
        LayoutDirection.Rtl ->
            if (offsetFromStart) {
                layoutCoordinates.size.width - splitOffsetPixel
            } else {
                splitOffsetPixel
            }
    }
    val splitWidthPixel = with(density) { gapWidth.toPx() }

    SplitResult(
        isHorizontalSplit = true,
        gapBounds = Rect(
            left = splitX - splitWidthPixel / 2f,
            top = 0f,
            right = splitX + splitWidthPixel / 2f,
            bottom = layoutCoordinates.size.height.toFloat(),
        )
    )
}

/**
 * Returns a [TwoPaneStrategy] that will place the slots horizontally.
 *
 * The split will be placed at the given [splitFraction] from start, with the given [gapHeight].
 *
 * This strategy is _not_ fold aware.
 */
internal fun FractionVerticalTwoPaneStrategy(
    splitFraction: Float,
    gapHeight: Dp = 0.dp,
): TwoPaneStrategy = TwoPaneStrategy { density, _, layoutCoordinates ->
    val splitY = layoutCoordinates.size.height * splitFraction
    val splitHeightPixel = with(density) { gapHeight.toPx() }

    SplitResult(
        isHorizontalSplit = false,
        gapBounds = Rect(
            left = 0f,
            top = splitY - splitHeightPixel / 2f,
            right = layoutCoordinates.size.width.toFloat(),
            bottom = splitY + splitHeightPixel / 2f,
        )
    )
}

/**
 * Returns a [TwoPaneStrategy] that will place the slots horizontally.
 *
 * The split will be placed at [splitOffset] either from the top or bottom based on
 * [offsetFromTop], with the given [gapHeight].
 *
 * This strategy is _not_ fold aware.
 */
internal fun FixedOffsetVerticalTwoPaneStrategy(
    splitOffset: Dp,
    offsetFromTop: Boolean,
    gapHeight: Dp = 0.dp,
): TwoPaneStrategy = TwoPaneStrategy { density, _, layoutCoordinates ->
    val splitOffsetPixel = with(density) { splitOffset.toPx() }
    val splitY =
        if (offsetFromTop) {
            splitOffsetPixel
        } else {
            layoutCoordinates.size.height - splitOffsetPixel
        }
    val splitHeightPixel = with(density) { gapHeight.toPx() }

    SplitResult(
        isHorizontalSplit = false,
        gapBounds = Rect(
            left = 0f,
            top = splitY - splitHeightPixel / 2f,
            right = layoutCoordinates.size.width.toFloat(),
            bottom = splitY + splitHeightPixel / 2f,
        )
    )
}
