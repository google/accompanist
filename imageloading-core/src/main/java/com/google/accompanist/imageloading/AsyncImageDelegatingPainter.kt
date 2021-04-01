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

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.Painter

/**
 * [Painter] class which delegates everything to the painter returned from the [painter] block.
 * If [painter] reads any state values, any changes to those values will cause this painter's
 * draw/intrinsicSize to be restarted.
 *
 * Similarly, this applies the transition [ColorFilter] as appropriate, again through state reads.
 */
internal class AsyncImageDelegatingPainter(
    private val painter: () -> Painter,
    private val transitionColorFilter: () -> ColorFilter?,
) : Painter() {
    private val paint by lazy(LazyThreadSafetyMode.NONE) { Paint() }

    private var alpha: Float = 1f
    private var colorFilter: ColorFilter? = null

    override val intrinsicSize: Size
        get() = painter().intrinsicSize

    override fun applyAlpha(alpha: Float): Boolean {
        this.alpha = alpha
        return true
    }

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        this.colorFilter = colorFilter
        return true
    }

    override fun DrawScope.onDraw() {
        val transitionColorFilter = transitionColorFilter()

        if (colorFilter != null && transitionColorFilter != null) {
            // If we have a transition color filter, and a specified color filter we need to
            // draw the content in a layer for both to apply.
            // See https://github.com/google/accompanist/issues/262
            drawIntoCanvas { canvas ->
                paint.colorFilter = transitionColorFilter
                canvas.saveLayer(bounds = size.toRect(), paint = paint)
                with(painter()) {
                    draw(size, alpha, colorFilter)
                }
                canvas.restore()
            }
        } else {
            // Otherwise we just draw the content directly, using the filter
            with(painter()) {
                draw(size, alpha, colorFilter ?: transitionColorFilter)
            }
        }
    }
}
