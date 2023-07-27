/*
 * Copyright 2023 The Android Open Source Project
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

import android.util.Range
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.window.layout.DisplayFeature
import androidx.window.layout.FoldingFeature
import kotlin.math.roundToInt

/**
 * A simplified version of [Column] that places children in a fold-aware manner.
 *
 * The layout starts placing children from the top of the available space. If there is a horizontal
 * [separating](https://developer.android.com/reference/kotlin/androidx/window/layout/FoldingFeature#isSeparating())
 * fold present in the window, then the layout will check to see if any children overlap the fold.
 * If a child would overlap the fold in its current position, then the layout will increase its
 * y coordinate so that the child is now placed below the fold, and any subsequent children will
 * also be placed below the fold.
 *
 *
 * @param displayFeatures a list of display features the device currently has
 * @param modifier an optional modifier for the layout
 * @param foldPadding the optional padding to add around a fold
 * @param horizontalAlignment the horizontal alignment of the layout's children.
 */
@Composable
public fun FoldAwareColumn(
    displayFeatures: List<DisplayFeature>,
    modifier: Modifier = Modifier,
    foldPadding: PaddingValues = PaddingValues(),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable FoldAwareColumnScope.() -> Unit,
) {
    Layout(
        modifier = modifier,
        measurePolicy = foldAwareColumnMeasurePolicy(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = horizontalAlignment,
            fold = {
                // Extract folding feature if horizontal and separating
                displayFeatures.find {
                    it is FoldingFeature && it.orientation == FoldingFeature.Orientation.HORIZONTAL &&
                        it.isSeparating
                } as FoldingFeature?
            },
            foldPadding = foldPadding,
        ),
        content = { FoldAwareColumnScopeInstance.content() }
    )
}

/**
 * FoldAwareColumn version of [rowColumnMeasurePolicy] that uses [FoldAwareColumnMeasurementHelper.foldAwarePlaceHelper]
 * method instead of [RowColumnMeasurementHelper.placeHelper]
 */
// TODO: change from internal to private once metalava issue is solved https://issuetracker.google.com/issues/271539608
@Composable
internal fun foldAwareColumnMeasurePolicy(
    verticalArrangement: Arrangement.Vertical,
    horizontalAlignment: Alignment.Horizontal,
    fold: () -> FoldingFeature?,
    foldPadding: PaddingValues
) = remember(verticalArrangement, horizontalAlignment, fold, foldPadding) {

    val orientation = LayoutOrientation.Vertical
    val arrangement: (Int, IntArray, LayoutDirection, Density, IntArray) -> Unit =
        { totalSize, size, _, density, outPosition ->
            with(verticalArrangement) { density.arrange(totalSize, size, outPosition) }
        }
    val arrangementSpacing = verticalArrangement.spacing
    val crossAxisAlignment = CrossAxisAlignment.horizontal(horizontalAlignment)
    val crossAxisSize = SizeMode.Wrap

    object : MeasurePolicy {
        override fun MeasureScope.measure(
            measurables: List<Measurable>,
            constraints: Constraints
        ): MeasureResult {
            val placeables = arrayOfNulls<Placeable?>(measurables.size)
            val rowColumnMeasureHelper =
                FoldAwareColumnMeasurementHelper(
                    orientation,
                    arrangement,
                    arrangementSpacing,
                    crossAxisSize,
                    crossAxisAlignment,
                    measurables,
                    placeables
                )

            val measureResult = rowColumnMeasureHelper
                .measureWithoutPlacing(
                    this,
                    constraints, 0, measurables.size
                )

            val layoutWidth: Int
            val layoutHeight: Int
            if (orientation == LayoutOrientation.Horizontal) {
                layoutWidth = measureResult.mainAxisSize
                layoutHeight = measureResult.crossAxisSize
            } else {
                layoutWidth = measureResult.crossAxisSize
                layoutHeight = measureResult.mainAxisSize
            }

            // Calculate fold bounds in pixels (including any added fold padding)
            val foldBoundsPx = with(density) {
                val topPaddingPx = foldPadding.calculateTopPadding().roundToPx()
                val bottomPaddingPx = foldPadding.calculateBottomPadding().roundToPx()

                fold()?.bounds?.let {
                    Rect(
                        left = it.left.toFloat(),
                        top = it.top.toFloat() - topPaddingPx,
                        right = it.right.toFloat(),
                        bottom = it.bottom.toFloat() + bottomPaddingPx
                    )
                }
            }

            // We only know how much padding is added inside the placement scope, so just add fold height
            // and height of the largest child when laying out to cover the maximum possible height
            val heightPadding = foldBoundsPx?.let { bounds ->
                val largestChildHeight = rowColumnMeasureHelper.placeables.maxOfOrNull {
                    if ((it?.parentData as? RowColumnParentData)?.ignoreFold == true) {
                        0
                    } else {
                        it?.height ?: 0
                    }
                } ?: 0
                bounds.height.roundToInt() + largestChildHeight
            } ?: 0
            val paddedLayoutHeight = layoutHeight + heightPadding

            return layout(layoutWidth, paddedLayoutHeight) {
                rowColumnMeasureHelper.foldAwarePlaceHelper(
                    this,
                    measureResult,
                    0,
                    layoutDirection,
                    foldBoundsPx
                )
            }
        }

        override fun IntrinsicMeasureScope.minIntrinsicWidth(
            measurables: List<IntrinsicMeasurable>,
            height: Int
        ) = MinIntrinsicWidthMeasureBlock(orientation)(
            measurables,
            height,
            arrangementSpacing.roundToPx()
        )

        override fun IntrinsicMeasureScope.minIntrinsicHeight(
            measurables: List<IntrinsicMeasurable>,
            width: Int
        ) = MinIntrinsicHeightMeasureBlock(orientation)(
            measurables,
            width,
            arrangementSpacing.roundToPx()
        )

        override fun IntrinsicMeasureScope.maxIntrinsicWidth(
            measurables: List<IntrinsicMeasurable>,
            height: Int
        ) = MaxIntrinsicWidthMeasureBlock(orientation)(
            measurables,
            height,
            arrangementSpacing.roundToPx()
        )

        override fun IntrinsicMeasureScope.maxIntrinsicHeight(
            measurables: List<IntrinsicMeasurable>,
            width: Int
        ) = MaxIntrinsicHeightMeasureBlock(orientation)(
            measurables,
            width,
            arrangementSpacing.roundToPx()
        )
    }
}

/**
 * Inherits from [RowColumnMeasurementHelper] to place children in a fold-aware manner
 */
private class FoldAwareColumnMeasurementHelper(
    orientation: LayoutOrientation,
    arrangement: (Int, IntArray, LayoutDirection, Density, IntArray) -> Unit,
    arrangementSpacing: Dp,
    crossAxisSize: SizeMode,
    crossAxisAlignment: CrossAxisAlignment,
    measurables: List<Measurable>,
    placeables: Array<Placeable?>
) : RowColumnMeasurementHelper(
    orientation,
    arrangement,
    arrangementSpacing,
    crossAxisSize,
    crossAxisAlignment,
    measurables,
    placeables
) {
    /**
     * Copy of [placeHelper] that has been modified for FoldAwareColumn implementation
     */
    @OptIn(ExperimentalComposeUiApi::class)
    fun foldAwarePlaceHelper(
        placeableScope: Placeable.PlacementScope,
        measureResult: RowColumnMeasureHelperResult,
        crossAxisOffset: Int,
        layoutDirection: LayoutDirection,
        foldBoundsPx: Rect?
    ) {
        with(placeableScope) {
            val layoutBounds = coordinates!!.trueBoundsInWindow()

            var placeableY = 0

            for (i in measureResult.startIndex until measureResult.endIndex) {
                val placeable = placeables[i]!!
                val mainAxisPositions = measureResult.mainAxisPositions
                val crossAxisPosition = getCrossAxisPosition(
                    placeable,
                    (measurables[i].parentData as? RowColumnParentData),
                    measureResult.crossAxisSize,
                    layoutDirection,
                    measureResult.beforeCrossAxisAlignmentLine
                ) + crossAxisOffset
                if (orientation == LayoutOrientation.Horizontal) {
                    placeable.place(
                        mainAxisPositions[i - measureResult.startIndex],
                        crossAxisPosition
                    )
                } else {
                    val relativeBounds = Rect(
                        left = 0f,
                        top = placeableY.toFloat(),
                        right = placeable.width.toFloat(),
                        bottom = (placeableY + placeable.height).toFloat()
                    )
                    val absoluteBounds =
                        relativeBounds.translate(layoutBounds.left, layoutBounds.top)

                    // If placeable overlaps fold, push placeable below
                    if (foldBoundsPx?.overlapsVertically(absoluteBounds) == true &&
                        (placeable.parentData as? RowColumnParentData)?.ignoreFold != true
                    ) {
                        placeableY = (foldBoundsPx.bottom - layoutBounds.top).toInt()
                    }

                    placeable.place(crossAxisPosition, placeableY)

                    placeableY += placeable.height
                }
            }
        }
    }
}

/**
 * Copy of original [LayoutCoordinates.boundsInWindow], but without the nonzero dimension check.
 *
 * Instead of returning [Rect.Zero] for a layout with zero width/height, this method will still
 * return a Rect with the layout's bounds.
 */
@VisibleForTesting
internal fun LayoutCoordinates.trueBoundsInWindow(): Rect {
    val root = findRootCoordinates()
    val bounds = boundsInRoot()
    val rootWidth = root.size.width.toFloat()
    val rootHeight = root.size.height.toFloat()

    val boundsLeft = bounds.left.coerceIn(0f, rootWidth)
    val boundsTop = bounds.top.coerceIn(0f, rootHeight)
    val boundsRight = bounds.right.coerceIn(0f, rootWidth)
    val boundsBottom = bounds.bottom.coerceIn(0f, rootHeight)

    val topLeft = root.localToWindow(Offset(boundsLeft, boundsTop))
    val topRight = root.localToWindow(Offset(boundsRight, boundsTop))
    val bottomRight = root.localToWindow(Offset(boundsRight, boundsBottom))
    val bottomLeft = root.localToWindow(Offset(boundsLeft, boundsBottom))

    val left = minOf(topLeft.x, topRight.x, bottomLeft.x, bottomRight.x)
    val top = minOf(topLeft.y, topRight.y, bottomLeft.y, bottomRight.y)
    val right = maxOf(topLeft.x, topRight.x, bottomLeft.x, bottomRight.x)
    val bottom = maxOf(topLeft.y, topRight.y, bottomLeft.y, bottomRight.y)

    return Rect(left, top, right, bottom)
}

/**
 * Checks if the vertical ranges of the two Rects overlap (inclusive)
 */
private fun Rect.overlapsVertically(other: Rect): Boolean {
    val verticalRange = Range(top, bottom)
    val otherVerticalRange = Range(other.top, other.bottom)
    return verticalRange.overlaps(otherVerticalRange)
}

/**
 * Inclusive check to see if the given float ranges overlap
 */
private fun Range<Float>.overlaps(other: Range<Float>): Boolean {
    return (lower >= other.lower && lower <= other.upper) || (upper >= other.lower && upper <= other.upper)
}

/**
 * Copy of [RowColumnParentData] that has been modified to include the new ignoreFold field.
 */
internal data class RowColumnParentData(
    var weight: Float = 0f,
    var fill: Boolean = true,
    var crossAxisAlignment: CrossAxisAlignment? = null,
    var ignoreFold: Boolean = false
)

internal class IgnoreFoldModifier(
    inspectorInfo: InspectorInfo.() -> Unit
) : ParentDataModifier, InspectorValueInfo(inspectorInfo) {
    override fun Density.modifyParentData(parentData: Any?) =
        ((parentData as? RowColumnParentData) ?: RowColumnParentData()).also {
            it.ignoreFold = true
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is IgnoreFoldModifier
    }

    override fun hashCode(): Int {
        return 0
    }

    override fun toString(): String =
        "IgnoreFoldModifier(ignoreFold=true)"
}
