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

package com.google.accompanist.imageloading

import android.graphics.drawable.Animatable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asAndroidColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.graphics.drawable.DrawableCompat
import kotlin.math.roundToInt

private val MAIN_HANDLER by lazy(LazyThreadSafetyMode.NONE) {
    Handler(Looper.getMainLooper())
}

/**
 * A [Painter] which draws an Android [Drawable]. Supports [Animatable] drawables.
 *
 * Taken from https://goo.gle/compose-drawable-painter
 */
class AndroidDrawablePainter(
    private val drawable: Drawable
) : Painter() {
    private var invalidateTick by mutableStateOf(0)
    private var startedAnimatable = drawable is Animatable && drawable.isRunning

    init {
        drawable.callback = object : Drawable.Callback {
            override fun invalidateDrawable(d: Drawable) {
                // Update the tick so that we get re-drawn
                invalidateTick++
            }

            override fun scheduleDrawable(d: Drawable, what: Runnable, time: Long) {
                MAIN_HANDLER.postAtTime(what, time)
            }

            override fun unscheduleDrawable(d: Drawable, what: Runnable) {
                MAIN_HANDLER.removeCallbacks(what)
            }
        }
    }

    override fun applyAlpha(alpha: Float): Boolean {
        drawable.alpha = (alpha * 255).roundToInt().coerceIn(0, 255)
        return true
    }

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        drawable.colorFilter = colorFilter?.asAndroidColorFilter()
        return true
    }

    override fun applyLayoutDirection(layoutDirection: LayoutDirection): Boolean {
        return DrawableCompat.setLayoutDirection(
            drawable,
            when (layoutDirection) {
                LayoutDirection.Ltr -> View.LAYOUT_DIRECTION_LTR
                LayoutDirection.Rtl -> View.LAYOUT_DIRECTION_RTL
            }
        )
    }

    override val intrinsicSize: Size
        get() = Size(
            width = drawable.intrinsicWidth.toFloat(),
            height = drawable.intrinsicHeight.toFloat()
        )

    override fun DrawScope.onDraw() {
        if (!startedAnimatable && drawable is Animatable && !drawable.isRunning) {
            // If the drawable is Animatable, start it on the first draw
            drawable.start()
            startedAnimatable = true
        }

        drawIntoCanvas { canvas ->
            // Reading this ensures that we invalidate when invalidateDrawable() is called
            invalidateTick

            drawable.setBounds(0, 0, size.width.toInt(), size.height.toInt())

            canvas.withSave {
                // Painters are responsible for scaling content to meet the canvas size
                if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                    canvas.scale(
                        sx = size.width / drawable.intrinsicWidth,
                        sy = size.height / drawable.intrinsicHeight
                    )
                }
                drawable.draw(canvas.nativeCanvas)
            }
        }
    }
}

/**
 * Allows wrapping of a [Drawable] into a [Painter], attempting to un-wrap the drawable contents
 * and use Compose primitives where possible.
 */
fun Drawable.toPainter(): Painter = when (this) {
    is BitmapDrawable -> BitmapPainter(bitmap.asImageBitmap())
    is ColorDrawable -> ColorPainter(Color(color))
    else -> AndroidDrawablePainter(mutate())
}
