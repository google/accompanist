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

package com.google.accompanist.swiperefresh

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * A private class to do all the drawing of SwipeRefreshIndicator, which includes progress spinner
 * and the arrow. This class is to separate drawing from animation.
 * Adapted from CircularProgressDrawable.
 */
internal class CircularProgressPainter : Painter() {
    var color by mutableStateOf(Color.Unspecified)
    var alpha by mutableStateOf(1f)
    var arcRadius by mutableStateOf(0.dp)
    var strokeWidth by mutableStateOf(5.dp)
    var arrowEnabled by mutableStateOf(false)
    var arrowWidth by mutableStateOf(0.dp)
    var arrowHeight by mutableStateOf(0.dp)
    var arrowScale by mutableStateOf(1f)

    private val arrow: Path by lazy {
        Path().apply { fillType = PathFillType.EvenOdd }
    }

    var startTrim by mutableStateOf(0f)
    var endTrim by mutableStateOf(0f)
    var rotation by mutableStateOf(0f)

    override val intrinsicSize: Size
        get() = Size.Unspecified

    override fun applyAlpha(alpha: Float): Boolean {
        this.alpha = alpha
        return true
    }

    override fun DrawScope.onDraw() {
        rotate(degrees = rotation) {
            val arcRadius = arcRadius.toPx() + strokeWidth.toPx() / 2f
            val arcBounds = Rect(
                size.center.x - arcRadius,
                size.center.y - arcRadius,
                size.center.x + arcRadius,
                size.center.y + arcRadius
            )
            val startAngle = (startTrim + rotation) * 360
            val endAngle = (endTrim + rotation) * 360
            val sweepAngle = endAngle - startAngle
            drawArc(
                color = color,
                alpha = alpha,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = arcBounds.topLeft,
                size = arcBounds.size,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Square
                )
            )
            if (arrowEnabled) {
                drawArrow(startAngle, sweepAngle, arcBounds)
            }
        }
    }

    private fun DrawScope.drawArrow(startAngle: Float, sweepAngle: Float, bounds: Rect) {
        arrow.reset()
        arrow.moveTo(0f, 0f)
        arrow.lineTo(
            x = arrowWidth.toPx() * arrowScale,
            y = 0f
        )
        arrow.lineTo(
            x = arrowWidth.toPx() * arrowScale / 2,
            y = arrowHeight.toPx() * arrowScale
        )
        val radius = min(bounds.width, bounds.height) / 2f
        val inset = arrowWidth.toPx() * arrowScale / 2f
        arrow.translate(
            Offset(
                x = radius + bounds.center.x - inset,
                y = bounds.center.y + strokeWidth.toPx() / 2f
            )
        )
        arrow.close()
        rotate(degrees = startAngle + sweepAngle) {
            drawPath(
                path = arrow,
                color = color,
                alpha = alpha
            )
        }
    }
}
