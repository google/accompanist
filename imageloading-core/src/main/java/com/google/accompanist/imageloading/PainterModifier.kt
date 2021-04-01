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

package com.google.accompanist.imageloading

import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.times
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Copy of PainterModifier:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/ui/ui/src/commonMain/kotlin/androidx/compose/ui/draw/PainterModifier.kt
 */
internal abstract class PainterModifier : LayoutModifier, DrawModifier {
    abstract val painter: Painter
    open val sizeToIntrinsics: Boolean = true
    open val alignment: Alignment = Alignment.Center
    open val contentScale: ContentScale = ContentScale.Inside
    open val alpha: Float = 1f
    open val colorFilter: ColorFilter? = null

    /**
     * Helper property to determine if we should size content to the intrinsic
     * size of the Painter or not. This is only done if [sizeToIntrinsics] is true
     * and the Painter has an intrinsic size
     */
    private val useIntrinsicSize: Boolean
        get() = sizeToIntrinsics && painter.intrinsicSize.isSpecified

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(modifyConstraints(constraints))
        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ): Int {
        return if (useIntrinsicSize) {
            val constraints = Constraints(maxHeight = height)
            val layoutWidth =
                measurable.minIntrinsicWidth(modifyConstraints(constraints).maxHeight)
            val scaledSize = calculateScaledSize(Size(layoutWidth.toFloat(), height.toFloat()))
            max(scaledSize.width.roundToInt(), layoutWidth)
        } else {
            measurable.minIntrinsicWidth(height)
        }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ): Int {
        return if (useIntrinsicSize) {
            val constraints = Constraints(maxHeight = height)
            val layoutWidth =
                measurable.maxIntrinsicWidth(modifyConstraints(constraints).maxHeight)
            val scaledSize = calculateScaledSize(Size(layoutWidth.toFloat(), height.toFloat()))
            max(scaledSize.width.roundToInt(), layoutWidth)
        } else {
            measurable.maxIntrinsicWidth(height)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ): Int {
        return if (useIntrinsicSize) {
            val constraints = Constraints(maxWidth = width)
            val layoutHeight =
                measurable.minIntrinsicHeight(modifyConstraints(constraints).maxWidth)
            val scaledSize = calculateScaledSize(Size(width.toFloat(), layoutHeight.toFloat()))
            max(scaledSize.height.roundToInt(), layoutHeight)
        } else {
            measurable.minIntrinsicHeight(width)
        }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ): Int {
        return if (useIntrinsicSize) {
            val constraints = Constraints(maxWidth = width)
            val layoutHeight =
                measurable.maxIntrinsicHeight(modifyConstraints(constraints).maxWidth)
            val scaledSize = calculateScaledSize(Size(width.toFloat(), layoutHeight.toFloat()))
            max(scaledSize.height.roundToInt(), layoutHeight)
        } else {
            measurable.maxIntrinsicHeight(width)
        }
    }

    private fun calculateScaledSize(dstSize: Size): Size {
        return if (!useIntrinsicSize) {
            dstSize
        } else {
            val srcWidth = if (!painter.intrinsicSize.hasSpecifiedAndFiniteWidth()) {
                dstSize.width
            } else {
                painter.intrinsicSize.width
            }

            val srcHeight = if (!painter.intrinsicSize.hasSpecifiedAndFiniteHeight()) {
                dstSize.height
            } else {
                painter.intrinsicSize.height
            }

            val srcSize = Size(srcWidth, srcHeight)
            if (dstSize.width != 0f && dstSize.height != 0f) {
                srcSize * contentScale.computeScaleFactor(srcSize, dstSize)
            } else {
                Size.Zero
            }
        }
    }

    private fun modifyConstraints(constraints: Constraints): Constraints {
        val hasBoundedDimens = constraints.hasBoundedWidth && constraints.hasBoundedHeight
        val hasFixedDimens = constraints.hasFixedWidth && constraints.hasFixedHeight
        if ((!useIntrinsicSize && hasBoundedDimens) || hasFixedDimens) {
            // If we have fixed constraints or we are not attempting to size the
            // composable based on the size of the Painter, do not attempt to
            // modify them. Otherwise rely on Alignment and ContentScale
            // to determine how to position the drawing contents of the Painter within
            // the provided bounds
            return constraints.copy(
                minWidth = constraints.maxWidth,
                minHeight = constraints.maxHeight
            )
        }

        val intrinsicSize = painter.intrinsicSize
        val intrinsicWidth =
            if (intrinsicSize.hasSpecifiedAndFiniteWidth()) {
                intrinsicSize.width.roundToInt()
            } else {
                constraints.minWidth
            }

        val intrinsicHeight =
            if (intrinsicSize.hasSpecifiedAndFiniteHeight()) {
                intrinsicSize.height.roundToInt()
            } else {
                constraints.minHeight
            }

        // Scale the width and height appropriately based on the given constraints
        // and ContentScale
        val constrainedWidth = constraints.constrainWidth(intrinsicWidth)
        val constrainedHeight = constraints.constrainHeight(intrinsicHeight)
        val scaledSize = calculateScaledSize(
            Size(constrainedWidth.toFloat(), constrainedHeight.toFloat())
        )

        // For both width and height constraints, consume the minimum of the scaled width
        // and the maximum constraint as some scale types can scale larger than the maximum
        // available size (ex ContentScale.Crop)
        // In this case the larger of the 2 dimensions is used and the aspect ratio is
        // maintained. Even if the size of the composable is smaller, the painter will
        // draw its content clipped
        val minWidth = constraints.constrainWidth(scaledSize.width.roundToInt())
        val minHeight = constraints.constrainHeight(scaledSize.height.roundToInt())
        return constraints.copy(minWidth = minWidth, minHeight = minHeight)
    }

    override fun ContentDrawScope.draw() {
        val intrinsicSize = painter.intrinsicSize
        val srcWidth = if (intrinsicSize.hasSpecifiedAndFiniteWidth()) {
            intrinsicSize.width
        } else {
            size.width
        }

        val srcHeight = if (intrinsicSize.hasSpecifiedAndFiniteHeight()) {
            intrinsicSize.height
        } else {
            size.height
        }

        val srcSize = Size(srcWidth, srcHeight)

        // Compute the offset to translate the content based on the given alignment
        // and size to draw based on the ContentScale parameter
        val scaledSize = if (size.width != 0f && size.height != 0f) {
            srcSize * contentScale.computeScaleFactor(srcSize, size)
        } else {
            Size.Zero
        }

        val alignedPosition = alignment.align(
            IntSize(scaledSize.width.roundToInt(), scaledSize.height.roundToInt()),
            IntSize(size.width.roundToInt(), size.height.roundToInt()),
            layoutDirection
        )

        val dx = alignedPosition.x.toFloat()
        val dy = alignedPosition.y.toFloat()

        // Only translate the current drawing position while delegating the Painter to draw
        // with scaled size.
        // Individual Painter implementations should be responsible for scaling their drawing
        // content accordingly to fit within the drawing area.
        translate(dx, dy) {
            with(painter) {
                draw(size = scaledSize, alpha = alpha, colorFilter = colorFilter)
            }
        }
    }

    private fun Size.hasSpecifiedAndFiniteWidth() = this != Size.Unspecified && width.isFinite()
    private fun Size.hasSpecifiedAndFiniteHeight() = this != Size.Unspecified && height.isFinite()

    override fun hashCode(): Int {
        var result = painter.hashCode()
        result = 31 * result + sizeToIntrinsics.hashCode()
        result = 31 * result + alignment.hashCode()
        result = 31 * result + contentScale.hashCode()
        result = 31 * result + alpha.hashCode()
        result = 31 * result + (colorFilter?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        val otherModifier = other as? PainterModifier ?: return false
        return painter == otherModifier.painter &&
            sizeToIntrinsics == otherModifier.sizeToIntrinsics &&
            alignment == otherModifier.alignment &&
            contentScale == otherModifier.contentScale &&
            alpha == otherModifier.alpha &&
            colorFilter == otherModifier.colorFilter
    }

    override fun toString(): String =
        "PainterModifier(" +
            "painter=$painter, " +
            "sizeToIntrinsics=$sizeToIntrinsics, " +
            "alignment=$alignment, " +
            "alpha=$alpha, " +
            "colorFilter=$colorFilter)"
}
