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

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.LayoutDirection

/**
 * If [visible], draws [shape] with a solid [color] instead of content.
 *
 * @sample com.google.accompanist.sample.placeholder.PlaceholderBasicSample
 *
 * @param visible defines whether the placeholder should be visible
 * @param color color to paint placeholder with
 * @param shape desired shape of the placeholder
 */
fun Modifier.placeholder(
    visible: Boolean,
    color: Color = PlaceholderDefaults.PlaceholderColor,
    shape: Shape = RectangleShape
): Modifier = takeIf { visible.not() } ?: composed(
    inspectorInfo = debugInspectorInfo {
        name = "placeholder"
        value = visible
        properties["visible"] = visible
        properties["color"] = color
        properties["shape"] = shape
    }
) {
    PlaceholderModifier(
        color = color,
        shape = shape
    )
}

/**
 * If [visible], draw [shape] with [animatedBrush] instead of content.
 *
 * @sample com.google.accompanist.sample.placeholder.PlaceholderFadeSample
 * @sample com.google.accompanist.sample.placeholder.PlaceholderShimmerSample
 *
 * @param visible defines whether the placeholder should be visible
 * @param animatedBrush animated brush to paint placeholder with
 * @param shape desired shape of the placeholder
 */
fun Modifier.placeholder(
    visible: Boolean,
    animatedBrush: PlaceholderAnimatedBrush,
    shape: Shape = RectangleShape
): Modifier = takeIf { visible.not() } ?: composed(
    inspectorInfo = debugInspectorInfo {
        name = "placeholder"
        value = visible
        properties["visible"] = visible
        properties["animatedBrush"] = animatedBrush
        properties["shape"] = shape
    }
) {
    val infiniteTransition = rememberInfiniteTransition()
    val progress by infiniteTransition.animateFloat(
        initialValue = animatedBrush.minimumProgress(),
        targetValue = animatedBrush.maximumProgress(),
        animationSpec = animatedBrush.animationSpec()
    )
    remember {
        PlaceholderModifier(
            brush = animatedBrush,
            shape = shape
        )
    }.apply {
        this.progress = progress
    }
}

private class PlaceholderModifier(
    private val color: Color? = null,
    private val brush: PlaceholderAnimatedBrush? = null,
    private val shape: Shape
) : DrawModifier {

    var progress: Float by mutableStateOf(0f)

    // naive cache outline calculation if size is the same
    private var lastSize: Size? = null
    private var lastLayoutDirection: LayoutDirection? = null
    private var lastOutline: Outline? = null

    override fun ContentDrawScope.draw() {
        if (shape === RectangleShape) {
            // shortcut to avoid Outline calculation and allocation
            drawRect()
        } else {
            drawOutline()
        }
    }

    private fun ContentDrawScope.drawRect() {
        color?.let { drawRect(color = it) }
        brush?.let { drawRect(brush = it.brush(progress)) }
    }

    private fun ContentDrawScope.drawOutline() {
        val outline =
            lastOutline.takeIf { size == lastSize && layoutDirection == lastLayoutDirection }
                ?: shape.createOutline(size, layoutDirection, this)
        color?.let { drawOutline(outline, color = color) }
        brush?.let { drawOutline(outline, brush = brush.brush(progress)) }
        lastOutline = outline
        lastSize = size
    }

    override fun toString(): String =
        "PlaceholderModifier(color=$color, brush=$brush, shape=$shape)"
}
