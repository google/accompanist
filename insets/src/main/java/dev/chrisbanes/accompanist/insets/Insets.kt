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

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Providers
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticAmbientOf
import androidx.compose.ui.platform.ViewAmbient
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type

/**
 * Main holder of our inset values.
 */
@Stable
class WindowInsets {
    /**
     * Inset values which match [WindowInsetsCompat.Type.systemBars]
     */
    val systemBars = Insets()

    /**
     * Inset values which match [WindowInsetsCompat.Type.systemGestures]
     */
    val systemGestures = Insets()

    /**
     * Inset values which match [WindowInsetsCompat.Type.navigationBars]
     */
    val navigationBars = Insets()

    /**
     * Inset values which match [WindowInsetsCompat.Type.statusBars]
     */
    val statusBars = Insets()

    /**
     * Inset values which match [WindowInsetsCompat.Type.ime]
     */
    val ime = Insets()
}

@Stable
class Insets {
    /**
     * The left dimension of these insets in pixels.
     */
    var left by mutableStateOf(0)
        internal set

    /**
     * The top dimension of these insets in pixels.
     */
    var top by mutableStateOf(0)
        internal set

    /**
     * The right dimension of these insets in pixels.
     */
    var right by mutableStateOf(0)
        internal set

    /**
     * The bottom dimension of these insets in pixels.
     */
    var bottom by mutableStateOf(0)
        internal set

    /**
     * Whether the insets are currently visible.
     */
    var isVisible by mutableStateOf(true)
        internal set
}

val AmbientWindowInsets = staticAmbientOf<WindowInsets> {
    error("AmbientInsets value not available. Are you using ProvideWindowInsets?")
}

/**
 * Applies any [WindowInsetsCompat] values to [AmbientWindowInsets], which are then available
 * within [content].
 *
 * @param consumeWindowInsets Whether to consume any [WindowInsetsCompat]s which are dispatched to
 * the host view. Defaults to `true`.
 */
@Composable
fun ProvideWindowInsets(
    consumeWindowInsets: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = ViewAmbient.current

    val windowInsets = remember { WindowInsets() }

    DisposableEffect(view) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, wic ->
            windowInsets.systemBars.updateFrom(wic, Type.systemBars())
            windowInsets.systemGestures.updateFrom(wic, Type.systemGestures())
            windowInsets.statusBars.updateFrom(wic, Type.statusBars())
            windowInsets.navigationBars.updateFrom(wic, Type.navigationBars())
            windowInsets.ime.updateFrom(wic, Type.ime())

            if (consumeWindowInsets) WindowInsetsCompat.CONSUMED else wic
        }

        // Add an OnAttachStateChangeListener to request an inset pass each time we're attached
        // to the window
        val attachListener = object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) = v.requestApplyInsets()
            override fun onViewDetachedFromWindow(v: View) = Unit
        }
        view.addOnAttachStateChangeListener(attachListener)

        if (view.isAttachedToWindow) {
            // If the view is already attached, we can request an inset pass now
            view.requestApplyInsets()
        }

        onDispose {
            view.removeOnAttachStateChangeListener(attachListener)
        }
    }

    Providers(AmbientWindowInsets provides windowInsets) {
        content()
    }
}

/**
 * Updates our mutable state backed [Insets] from an Android system insets.
 */
private fun Insets.updateFrom(windowInsets: WindowInsetsCompat, type: Int) {
    val insets = windowInsets.getInsets(type)
    left = insets.left
    top = insets.top
    right = insets.right
    bottom = insets.bottom

    isVisible = windowInsets.isVisible(type)
}

enum class HorizontalSide { Left, Right }
enum class VerticalSide { Top, Bottom }
