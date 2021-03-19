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

@file:Suppress("unused", "UNUSED_VARIABLE")

package com.google.accompanist.swiperefresh

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

/**
 * A private class to do all the drawing of CircularProgressDrawable, which includes background,
 * progress spinner and the arrow. This class is to separate drawing from animation.
 */
internal class ProgressRingPainter : Painter() {

    override val intrinsicSize: Size
        get() = Size(40f, 40f)

    var startTrim by mutableStateOf(0f)
    var endTrim by mutableStateOf(0f)
    var rotation by mutableStateOf(0f)

    private var strokeWidth by mutableStateOf(2.dp)

    // mColorIndex represents the offset into the available mColors that the
    // progress circle should currently display. As the progress circle is
    // animating, the mColorIndex moves by one to the next available color.
    var colors by mutableStateOf(emptyList<Color>())

    private var colorIndex = 0

    private val nextColorIndex: Int
        get() = (colorIndex + 1) % colors.size

    private val currentColor: Color
        get() = colors[colorIndex]

    private val nextColor: Color
        get() = colors[nextColorIndex]

    private var startingStartTrim by mutableStateOf(0f)
    private var startingEndTrim by mutableStateOf(0f)

    /**
     * @return The amount the progress spinner is currently rotated, between [0..1].
     */
    private var startingRotation by mutableStateOf(0f)
    private var drawArrow by mutableStateOf(true)

    private val arrow: Path by lazy(LazyThreadSafetyMode.NONE) {
        Path().apply { fillType = PathFillType.EvenOdd }
    }
    private var arrowScale by mutableStateOf(1f)

    private var centerRadius by mutableStateOf(0f)

    private var arrowWidth by mutableStateOf(6.dp)
    private var arrowHeight by mutableStateOf(3.dp)

    private var alpha by mutableStateOf(1f)

    override fun DrawScope.onDraw() {
        val startAngle = (startTrim + rotation) * 360
        val endAngle = (endTrim + rotation) * 360
        val sweepAngle = endAngle - startAngle

        val arcOffset = maxOf((arrowWidth * arrowScale) / 2f, strokeWidth / 2f).toPx()

        val arcSize = Size(
            width = size.width - arcOffset * 2,
            height = size.height - arcOffset * 2
        )

        drawArc(
            color = currentColor,
            alpha = alpha,
            useCenter = false,
            topLeft = Offset(x = arcOffset, y = arcOffset),
            size = arcSize,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            style = Stroke(width = strokeWidth.toPx()),
        )

        if (drawArrow) {
            val centerRadius = minOf(arcSize.width, arcSize.height) / 2f

            arrow.reset()
            arrow.moveTo(0f, 0f)
            arrow.lineTo(arrowWidth.toPx(), 0f)
            arrow.lineTo(x = arrowWidth.toPx() / 2, y = arrowHeight.toPx())
            arrow.close()
            arrow.translate(
                Offset(
                    x = size.width - arcOffset - (arrowWidth.toPx() / 2),
                    y = arcOffset + arcSize.center.y
                )
            )

            withTransform(
                transformBlock = {
                    scale(scale = arrowScale)
                    translate(
                        left = size.width - arcOffset - (arrowWidth.toPx() / 2),
                        top = arcOffset + arcSize.center.y
                    )
                    rotate(degrees = startAngle + sweepAngle)
                }
            ) {
                drawPath(arrow, color = currentColor, alpha = alpha)
            }
        }
    }

    // if colors are reset, make sure to reset the color index as well

    /**
     * Proceed to the next available ring color. This will automatically
     * wrap back to the beginning of colors.
     */
    private fun goToNextColor() {
        colorIndex = nextColorIndex
    }

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        return super.applyColorFilter(colorFilter)
    }

    /**
     * If the start / end trim are offset to begin with, store them so that animation starts
     * from that offset.
     */
    fun storeOriginals() {
        startingStartTrim = startTrim
        startingEndTrim = endTrim
        startingRotation = rotation
    }

    /**
     * Reset the progress spinner to default rotation, start and end angles.
     */
    fun reset() {
        startingStartTrim = 0f
        startingEndTrim = 0f
        startingRotation = 0f
        startTrim = 0f
        endTrim = 0f
        rotation = 0f
    }
}
