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
 * This function sets up the necessary listeners on the given [view] to be able to observe
 * [WindowInsetsCompat] instance dispatched by the system.
 *
 * This function is useful for when you prefer to handle the ownership of the [WindowInsets]
 * yourself. One example of this is if you find yourself using [ProvideWindowInsets] in fragments.
 *
 * It is convenient to use [ProvideWindowInsets] in a fragment, but that can result in a
 * delay in the initial inset update, which results in a visual flicker.
 * See [this issue](https://github.com/chrisbanes/accompanist/issues/155) for more information.
 *
 * The alternative is for fragments to manage the [WindowInsets] themselves, and call this function
 * in `onCreateView()`:
 *
 * ```
 * override fun onCreateView(
 *     inflater: LayoutInflater,
 *     container: ViewGroup?,
 *     savedInstanceState: Bundle?
 * ): View = ComposeView(requireContext()).apply {
 *     layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
 *
 *     // We create a WindowInsets instance ourselves...
 *     val windowInsets = WindowInsets()
 *
 *     // Since we're self-managing our own WindowInsets, we
 *     // call observeFromView() to setup the necessary listeners.
 *     windowInsets.observeFromView(this)
 *
 *     setContent {
 *         // Instead of calling ProvideWindowInsets, we use
 *         // Providers to provide our self-managed WindowInsets
 *         // instance to AmbientWindowInsets
 *         Providers(AmbientWindowInsets provides windowInsets) {
 *             /* Content */
 *         }
 *     }
 * }
 * ```
 *
 * @param view The view to observe [WindowInsetsCompat]s from.
 * @param consumeWindowInsets Whether to consume any [WindowInsetsCompat]s which are dispatched to
 * the host view. Defaults to `true`.
 * @return A lambda to be invoked if this observation should be cancelled.
 */
fun WindowInsets.observeFromView(
    view: View,
    consumeWindowInsets: Boolean = true,
): () -> Unit {
    ViewCompat.setOnApplyWindowInsetsListener(view) { _, wic ->
        systemBars.updateFrom(wic, Type.systemBars())
        systemGestures.updateFrom(wic, Type.systemGestures())
        statusBars.updateFrom(wic, Type.statusBars())
        navigationBars.updateFrom(wic, Type.navigationBars())
        ime.updateFrom(wic, Type.ime())

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

    return { view.removeOnAttachStateChangeListener(attachListener) }
}

/**
 * Applies any [WindowInsetsCompat] values to [AmbientWindowInsets], which are then available
 * within [content].
 *
 * If you're using this in fragments, you may wish to take a look at
 * [WindowInsets.observeFromView] for a more optimal solution.
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
        val disposeHandle = windowInsets.observeFromView(view, consumeWindowInsets)
        onDispose {
            disposeHandle()
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
