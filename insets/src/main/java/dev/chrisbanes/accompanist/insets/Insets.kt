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
import android.view.WindowInsetsAnimation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Providers
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticAmbientOf
import androidx.compose.ui.platform.AmbientView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat

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

    internal var ongoingAnimations by mutableStateOf(0)

    fun copy(
        left: Int = this.left,
        top: Int = this.top,
        right: Int = this.right,
        bottom: Int = this.bottom,
        isVisible: Boolean = this.isVisible,
    ): Insets = Insets().apply {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
        this.isVisible = isVisible
        this.ongoingAnimations = ongoingAnimations
    }

    operator fun minus(other: Insets): Insets = copy(
        left = this@Insets.left - other.left,
        top = this@Insets.top - other.top,
        right = this@Insets.right - other.right,
        bottom = this@Insets.bottom - other.bottom,
    )

    operator fun plus(other: Insets): Insets = copy(
        left = this@Insets.left + other.left,
        top = this@Insets.top + other.top,
        right = this@Insets.right + other.right,
        bottom = this@Insets.bottom + other.bottom,
    )
}

val AmbientWindowInsets = staticAmbientOf<WindowInsets> {
    error("AmbientWindowInsets value not available. Are you using ProvideWindowInsets?")
}

/**
 * This class sets up the necessary listeners on the given [view] to be able to observe
 * [WindowInsetsCompat] instances dispatched by the system.
 *
 * This class is useful for when you prefer to handle the ownership of the [WindowInsets]
 * yourself. One example of this is if you find yourself using [ProvideWindowInsets] in fragments.
 *
 * It is convenient to use [ProvideWindowInsets] in fragments, but that can result in a
 * delay in the initial inset update, which results in a visual flicker.
 * See [this issue](https://github.com/chrisbanes/accompanist/issues/155) for more information.
 *
 * The alternative is for fragments to manage the [WindowInsets] themselves, like so:
 *
 * ```
 * override fun onCreateView(
 *     inflater: LayoutInflater,
 *     container: ViewGroup?,
 *     savedInstanceState: Bundle?
 * ): View = ComposeView(requireContext()).apply {
 *     layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
 *
 *     // Create an ViewWindowInsetObserver using this view
 *     val observer = ViewWindowInsetObserver(this)
 *
 *     // Call start() to start listening now.
 *     // The WindowInsets instance is returned to us.
 *     val windowInsets = observer.start()
 *
 *     setContent {
 *         // Instead of calling ProvideWindowInsets, we use Providers to provide
 *         // the WindowInsets instance from above to AmbientWindowInsets
 *         Providers(AmbientWindowInsets provides windowInsets) {
 *             /* Content */
 *         }
 *     }
 * }
 * ```
 *
 * @param view The view to observe [WindowInsetsCompat]s from.
 */
class ViewWindowInsetObserver(private val view: View) {
    private val attachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) = v.requestApplyInsets()
        override fun onViewDetachedFromWindow(v: View) = Unit
    }

    /**
     * Whether this [ViewWindowInsetObserver] is currently observing.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var isObserving: Boolean = false
        private set

    /**
     * Start observing window insets from [view]. Make sure to call [stop] if required.
     *
     * @param consumeWindowInsets Whether to consume any [WindowInsetsCompat]s which are
     * dispatched to the host view. Defaults to `true`.
     */
    fun start(
        consumeWindowInsets: Boolean = true
    ): WindowInsets {
        return WindowInsets().apply {
            observeInto(
                windowInsets = this,
                consumeWindowInsets = consumeWindowInsets,
                windowInsetsAnimationsEnabled = false
            )
        }
    }

    /**
     * Start observing window insets from [view]. Make sure to call [stop] if required.
     *
     * @param windowInsetsAnimationsEnabled Whether to listen for [WindowInsetsAnimation]s, such as
     * IME animations.
     * @param consumeWindowInsets Whether to consume any [WindowInsetsCompat]s which are
     * dispatched to the host view. Defaults to `true`.
     */
    @ExperimentalAnimatedInsets
    fun start(
        windowInsetsAnimationsEnabled: Boolean,
        consumeWindowInsets: Boolean = true,
    ): WindowInsets {
        return WindowInsets().apply {
            observeInto(
                windowInsets = this,
                consumeWindowInsets = consumeWindowInsets,
                windowInsetsAnimationsEnabled = windowInsetsAnimationsEnabled
            )
        }
    }

    internal fun observeInto(
        windowInsets: WindowInsets,
        consumeWindowInsets: Boolean,
        windowInsetsAnimationsEnabled: Boolean,
    ) {
        require(!isObserving) {
            "start() called, but this ViewWindowInsetObserver is already observing"
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, wic ->
            // Go through each inset type and update it from the WindowInsetsCompat.
            //
            // If the inset type is currently being animated we ignore this pass. This is
            // because WindowInsetsAnimation is built with the view system in mind, such that
            // it forces consumers to be laid out in the final state, then immediately transform
            // itself to look like the start state (because we shouldn't use layout
            // animations in views). For more information, see:
            // https://medium.com/androiddevelopers/animating-your-keyboard-reacting-to-inset-animations-839be3d4c31b
            //
            // Compose (hopefully) doesn't need that, so we can just apply the animated insets
            // directly to the layout.
            windowInsets.statusBars.run {
                if (ongoingAnimations == 0) {
                    updateFrom(wic, WindowInsetsCompat.Type.statusBars())
                }
            }
            windowInsets.navigationBars.run {
                if (ongoingAnimations == 0) {
                    updateFrom(wic, WindowInsetsCompat.Type.navigationBars())
                }
            }
            windowInsets.systemBars.run {
                if (ongoingAnimations == 0) {
                    updateFrom(wic, WindowInsetsCompat.Type.systemBars())
                }
            }
            windowInsets.systemGestures.run {
                if (ongoingAnimations == 0) {
                    updateFrom(wic, WindowInsetsCompat.Type.systemGestures())
                }
            }
            windowInsets.ime.run {
                if (ongoingAnimations == 0) {
                    updateFrom(wic, WindowInsetsCompat.Type.ime())
                }
            }

            if (consumeWindowInsets) WindowInsetsCompat.CONSUMED else wic
        }

        // Add an OnAttachStateChangeListener to request an inset pass each time we're attached
        // to the window
        val attachListener = object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) = v.requestApplyInsets()
            override fun onViewDetachedFromWindow(v: View) = Unit
        }
        view.addOnAttachStateChangeListener(attachListener)

        if (windowInsetsAnimationsEnabled) {
            ViewCompat.setWindowInsetsAnimationCallback(
                view,
                InnerWindowInsetsAnimationCallback(windowInsets)
            )
        } else {
            ViewCompat.setWindowInsetsAnimationCallback(view, null)
        }

        if (view.isAttachedToWindow) {
            // If the view is already attached, we can request an inset pass now
            view.requestApplyInsets()
        }

        isObserving = true
    }

    /**
     * Removes any listeners from the [view] so that we no longer observe inset changes.
     *
     * This is only required to be called from hosts which have a shorter lifetime than the [view].
     * For example, if you're using [ViewWindowInsetObserver] from a `@Composable` function,
     * you should call [stop] from an `onDispose` block, like so:
     *
     * ```
     * DisposableEffect(view) {
     *     val observer = ViewWindowInsetObserver(view)
     *     // ...
     *     onDispose {
     *         observer.stop()
     *     }
     * }
     * ```
     *
     * Whereas if you're using this class from a fragment (or similar), it is not required to
     * call this function since it will live as least as longer as the view.
     */
    fun stop() {
        require(isObserving) {
            "stop() called, but this ViewWindowInsetObserver is not currently observing"
        }
        view.removeOnAttachStateChangeListener(attachListener)
        ViewCompat.setOnApplyWindowInsetsListener(view, null)
        isObserving = false
    }
}

/**
 * Applies any [WindowInsetsCompat] values to [AmbientWindowInsets], which are then available
 * within [content].
 *
 * If you're using this in fragments, you may wish to take a look at
 * [ViewWindowInsetObserver] for a more optimal solution.
 *
 * @param consumeWindowInsets Whether to consume any [WindowInsetsCompat]s which are dispatched to
 * the host view. Defaults to `true`.
 */
@Composable
fun ProvideWindowInsets(
    consumeWindowInsets: Boolean = true,
    content: @Composable () -> Unit,
) {
    val view = AmbientView.current
    val windowInsets = remember { WindowInsets() }

    DisposableEffect(view) {
        val observer = ViewWindowInsetObserver(view)
        observer.observeInto(
            windowInsets = windowInsets,
            consumeWindowInsets = consumeWindowInsets,
            windowInsetsAnimationsEnabled = false
        )
        onDispose {
            observer.stop()
        }
    }

    Providers(AmbientWindowInsets provides windowInsets) {
        content()
    }
}

/**
 * Applies any [WindowInsetsCompat] values to [AmbientWindowInsets], which are then available
 * within [content].
 *
 * If you're using this in fragments, you may wish to take a look at
 * [ViewWindowInsetObserver] for a more optimal solution.
 *
 * @param windowInsetsAnimationsEnabled Whether to listen for [WindowInsetsAnimation]s, such as
 * IME animations.
 * @param consumeWindowInsets Whether to consume any [WindowInsetsCompat]s which are dispatched to
 * the host view. Defaults to `true`.
 */
@ExperimentalAnimatedInsets
@Composable
fun ProvideWindowInsets(
    windowInsetsAnimationsEnabled: Boolean,
    consumeWindowInsets: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = AmbientView.current
    val windowInsets = remember { WindowInsets() }

    DisposableEffect(view) {
        val observer = ViewWindowInsetObserver(view)
        observer.observeInto(
            windowInsets = windowInsets,
            consumeWindowInsets = consumeWindowInsets,
            windowInsetsAnimationsEnabled = windowInsetsAnimationsEnabled
        )
        onDispose {
            observer.stop()
        }
    }

    Providers(AmbientWindowInsets provides windowInsets) {
        content()
    }
}

private class InnerWindowInsetsAnimationCallback(
    private val windowInsets: WindowInsets,
) : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
    override fun onPrepare(animation: WindowInsetsAnimationCompat) {
        // Go through each type and flag that an animation has started
        if (animation.typeMask and WindowInsetsCompat.Type.ime() != 0) {
            windowInsets.ime.ongoingAnimations++
        }
        if (animation.typeMask and WindowInsetsCompat.Type.statusBars() != 0) {
            windowInsets.statusBars.ongoingAnimations++
        }
        if (animation.typeMask and WindowInsetsCompat.Type.navigationBars() != 0) {
            windowInsets.navigationBars.ongoingAnimations++
        }
        if (animation.typeMask and WindowInsetsCompat.Type.systemBars() != 0) {
            windowInsets.systemBars.ongoingAnimations++
        }
        if (animation.typeMask and WindowInsetsCompat.Type.systemGestures() != 0) {
            windowInsets.systemGestures.ongoingAnimations++
        }
    }

    override fun onProgress(
        platformInsets: WindowInsetsCompat,
        runningAnimations: List<WindowInsetsAnimationCompat>
    ): WindowInsetsCompat {
        val runningTypes = runningAnimations.fold(0) { acc, animation ->
            acc or animation.typeMask
        }
        if (runningTypes and WindowInsetsCompat.Type.ime() != 0) {
            windowInsets.ime.updateFrom(
                windowInsets = platformInsets,
                type = WindowInsetsCompat.Type.ime()
            )
        }
        if (runningTypes and WindowInsetsCompat.Type.statusBars() != 0) {
            windowInsets.statusBars.updateFrom(
                windowInsets = platformInsets,
                type = WindowInsetsCompat.Type.statusBars()
            )
        }
        if (runningTypes and WindowInsetsCompat.Type.navigationBars() != 0) {
            windowInsets.navigationBars.updateFrom(
                windowInsets = platformInsets,
                type = WindowInsetsCompat.Type.navigationBars()
            )
        }
        if (runningTypes and WindowInsetsCompat.Type.systemBars() != 0) {
            windowInsets.systemBars.updateFrom(
                windowInsets = platformInsets,
                type = WindowInsetsCompat.Type.systemBars()
            )
        }
        if (runningTypes and WindowInsetsCompat.Type.systemGestures() != 0) {
            windowInsets.systemGestures.updateFrom(
                windowInsets = platformInsets,
                type = WindowInsetsCompat.Type.systemGestures()
            )
        }
        return platformInsets
    }

    override fun onEnd(animation: WindowInsetsAnimationCompat) {
        // Go through each type and flag that an animation has ended
        if (animation.typeMask and WindowInsetsCompat.Type.ime() != 0) {
            windowInsets.ime.ongoingAnimations--
        }
        if (animation.typeMask and WindowInsetsCompat.Type.statusBars() != 0) {
            windowInsets.statusBars.ongoingAnimations--
        }
        if (animation.typeMask and WindowInsetsCompat.Type.navigationBars() != 0) {
            windowInsets.navigationBars.ongoingAnimations--
        }
        if (animation.typeMask and WindowInsetsCompat.Type.systemBars() != 0) {
            windowInsets.systemBars.ongoingAnimations--
        }
        if (animation.typeMask and WindowInsetsCompat.Type.systemGestures() != 0) {
            windowInsets.systemGestures.ongoingAnimations--
        }
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

internal fun Insets.coerceEachDimensionAtLeast(other: Insets): Insets {
    // Fast path, no need to copy if `this` >= `other`
    if (left >= other.left && top >= other.top && right >= other.right && bottom >= other.bottom) {
        return this
    }
    return copy(
        left = left.coerceAtLeast(other.left),
        top = top.coerceAtLeast(other.top),
        right = right.coerceAtLeast(other.right),
        bottom = bottom.coerceAtLeast(other.bottom),
    )
}

enum class HorizontalSide { Left, Right }
enum class VerticalSide { Top, Bottom }

@RequiresOptIn(message = "Animated Insets support is experimental. The API may be changed in the future.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ExperimentalAnimatedInsets
