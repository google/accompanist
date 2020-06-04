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

package dev.chrisbanes.accompanist.coil

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.core.util.Pools
import androidx.ui.geometry.Offset
import androidx.ui.geometry.Size
import androidx.ui.graphics.ImageAsset
import androidx.ui.graphics.Paint
import androidx.ui.graphics.drawscope.DrawScope
import androidx.ui.graphics.drawscope.drawCanvas
import androidx.ui.graphics.painter.Painter

/**
 * An [ImagePainter] which draws the image with the given Android framework
 * [android.graphics.ColorMatrix].
 */
internal class ColorMatrixImagePainter(
    private val image: ImageAsset,
    private val srcOffset: Offset = Offset.zero,
    private val srcSize: Size = Size(image.width.toFloat(), image.height.toFloat()),
    private val colorMatrix: ColorMatrix? = null
) : Painter() {
    override fun DrawScope.onDraw() {
        val paint = paintPool.acquire() ?: Paint()
        paint.asFrameworkPaint().colorFilter = colorMatrix?.let(::ColorMatrixColorFilter)

        drawCanvas { canvas, _ ->
            canvas.drawImageRect(image, srcOffset, srcSize, Offset.zero, size, paint)
        }

        paintPool.release(paint)
    }

    /**
     * Return the dimension of the underlying [Image] as it's intrinsic width and height
     */
    override val intrinsicSize: Size get() = srcSize
}

/**
 * A pool which allows us to cache and re-use [Paint] instances, which are relatively expensive
 * to create.
 */
private val paintPool = Pools.SimplePool<Paint>(2)
