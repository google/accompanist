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

package com.google.accompanist.insets

import android.view.View
import android.view.WindowInsetsAnimation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat

/**
 * The main insets holder, containing instances of [InsetsType] which each refer to different
 * types of system display insets.
 */
@Stable
interface WindowInsets {

    /**
     * Inset values which match [WindowInsetsCompat.Type.navigationBars]
     */
    val navigationBars: InsetsType

    /**
     * Inset values which match [WindowInsetsCompat.Type.statusBars]
     */
    val statusBars: InsetsType

    /**
     * Inset values which match [WindowInsetsCompat.Type.ime]
     */
    val ime: InsetsType

    /**
     * Inset values which match [WindowInsetsCompat.Type.systemGestures]
     */
    val systemGestures: InsetsType

    /**
     * Inset values which match [WindowInsetsCompat.Type.systemBars]
     */
    val systemBars: InsetsType

    /**
     * Returns a copy of this instance with the given values.
     */
    fun copy(
        navigationBars: InsetsType = this.navigationBars,
        statusBars: InsetsType = this.statusBars,
        systemGestures: InsetsType = this.systemGestures,
        ime: InsetsType = this.ime,
    ): WindowInsets = ImmutableWindowInsets(
        systemGestures = systemGestures,
        navigationBars = navigationBars,
        statusBars = statusBars,
        ime = ime,
    )

    companion object {
        /**
         * Empty and immutable instance of [WindowInsets].
         */
        val Empty: WindowInsets = ImmutableWindowInsets()
    }
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
 * See [this issue](https://github.com/google/accompanist/issues/155) for more information.
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
 *         // Instead of calling ProvideWindowInsets, we use CompositionLocalProvider to provide
 *         // the WindowInsets instance from above to LocalWindowInsets
 *         CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
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
    ): WindowInsets = RootWindowInsets().also {
        observeInto(
            windowInsets = it,
            consumeWindowInsets = consumeWindowInsets,
            windowInsetsAnimationsEnabled = false
        )
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
    ): WindowInsets = RootWindowInsets().also {
        observeInto(
            windowInsets = it,
            consumeWindowInsets = consumeWindowInsets,
            windowInsetsAnimationsEnabled = windowInsetsAnimationsEnabled
        )
    }

    internal fun observeInto(
        windowInsets: RootWindowInsets,
        consumeWindowInsets: Boolean,
        windowInsetsAnimationsEnabled: Boolean,
    ) {
        require(!isObserving) {
            "start() called, but this ViewWindowInsetObserver is already observing"
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, wic ->
            // Go through each inset type and update its layoutInsets from the
            // WindowInsetsCompat values
            windowInsets.statusBars.run {
                mutableLayoutInsets.updateFrom(wic.getInsets(WindowInsetsCompat.Type.statusBars()))
                isVisible = wic.isVisible(WindowInsetsCompat.Type.statusBars())
            }
            windowInsets.navigationBars.run {
                mutableLayoutInsets.updateFrom(wic.getInsets(WindowInsetsCompat.Type.navigationBars()))
                isVisible = wic.isVisible(WindowInsetsCompat.Type.navigationBars())
            }
            windowInsets.systemGestures.run {
                mutableLayoutInsets.updateFrom(wic.getInsets(WindowInsetsCompat.Type.systemGestures()))
                isVisible = wic.isVisible(WindowInsetsCompat.Type.systemGestures())
            }
            windowInsets.ime.run {
                mutableLayoutInsets.updateFrom(wic.getInsets(WindowInsetsCompat.Type.ime()))
                isVisible = wic.isVisible(WindowInsetsCompat.Type.ime())
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
 * Applies any [WindowInsetsCompat] values to [LocalWindowInsets], which are then available
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
    val view = LocalView.current
    val windowInsets = RootWindowInsets()

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

    CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
        content()
    }
}

/**
 * Applies any [WindowInsetsCompat] values to [LocalWindowInsets], which are then available
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
    val view = LocalView.current
    val windowInsets = remember { RootWindowInsets() }

    DisposableEffect(view) {
        val observer = ViewWindowInsetObserver(view)
        observer.observeInto(
            windowInsets = windowInsets,
            consumeWindowInsets = consumeWindowInsets,
            windowInsetsAnimationsEnabled = windowInsetsAnimationsEnabled
        )
        onDispose { observer.stop() }
    }

    CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
        content()
    }
}

private class InnerWindowInsetsAnimationCallback(
    private val windowInsets: RootWindowInsets,
) : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
    override fun onPrepare(animation: WindowInsetsAnimationCompat) {
        // Go through each type and flag that an animation has started
        if (animation.typeMask and WindowInsetsCompat.Type.ime() != 0) {
            windowInsets.ime.onAnimationStart()
        }
        if (animation.typeMask and WindowInsetsCompat.Type.statusBars() != 0) {
            windowInsets.statusBars.onAnimationStart()
        }
        if (animation.typeMask and WindowInsetsCompat.Type.navigationBars() != 0) {
            windowInsets.navigationBars.onAnimationStart()
        }
        if (animation.typeMask and WindowInsetsCompat.Type.systemGestures() != 0) {
            windowInsets.systemGestures.onAnimationStart()
        }
    }

    override fun onProgress(
        platformInsets: WindowInsetsCompat,
        runningAnimations: List<WindowInsetsAnimationCompat>
    ): WindowInsetsCompat {
        // Update each inset type with the given parameters
        windowInsets.ime.updateAnimation(
            platformInsets = platformInsets,
            runningAnimations = runningAnimations,
            type = WindowInsetsCompat.Type.ime()
        )
        windowInsets.statusBars.updateAnimation(
            platformInsets = platformInsets,
            runningAnimations = runningAnimations,
            type = WindowInsetsCompat.Type.statusBars()
        )
        windowInsets.navigationBars.updateAnimation(
            platformInsets = platformInsets,
            runningAnimations = runningAnimations,
            type = WindowInsetsCompat.Type.navigationBars()
        )
        windowInsets.systemGestures.updateAnimation(
            platformInsets = platformInsets,
            runningAnimations = runningAnimations,
            type = WindowInsetsCompat.Type.systemGestures()
        )
        return platformInsets
    }

    private fun MutableInsetsType.updateAnimation(
        platformInsets: WindowInsetsCompat,
        runningAnimations: List<WindowInsetsAnimationCompat>,
        type: Int,
    ) {
        // If there are animations of the given type...
        if (runningAnimations.any { it.typeMask or type != 0 }) {
            // Update our animated inset values
            mutableAnimatedInsets.updateFrom(platformInsets.getInsets(type))
            // And update the animation fraction. We use the maximum animation progress of any
            // ongoing animations for this type.
            animationFraction = runningAnimations.maxOf { it.fraction }
        }
    }

    override fun onEnd(animation: WindowInsetsAnimationCompat) {
        // Go through each type and flag that an animation has ended
        if (animation.typeMask and WindowInsetsCompat.Type.ime() != 0) {
            windowInsets.ime.onAnimationEnd()
        }
        if (animation.typeMask and WindowInsetsCompat.Type.statusBars() != 0) {
            windowInsets.statusBars.onAnimationEnd()
        }
        if (animation.typeMask and WindowInsetsCompat.Type.navigationBars() != 0) {
            windowInsets.navigationBars.onAnimationEnd()
        }
        if (animation.typeMask and WindowInsetsCompat.Type.systemGestures() != 0) {
            windowInsets.systemGestures.onAnimationEnd()
        }
    }
}

/**
 * Holder of our root inset values.
 */
internal class RootWindowInsets : WindowInsets {
    /**
     * Inset values which match [WindowInsetsCompat.Type.systemGestures]
     */
    override val systemGestures: MutableInsetsType = MutableInsetsType()

    /**
     * Inset values which match [WindowInsetsCompat.Type.navigationBars]
     */
    override val navigationBars: MutableInsetsType = MutableInsetsType()

    /**
     * Inset values which match [WindowInsetsCompat.Type.statusBars]
     */
    override val statusBars: MutableInsetsType = MutableInsetsType()

    /**
     * Inset values which match [WindowInsetsCompat.Type.ime]
     */
    override val ime: MutableInsetsType = MutableInsetsType()

    /**
     * Inset values which match [WindowInsetsCompat.Type.systemBars]
     */
    override val systemBars: InsetsType = CalculatedInsetsType(statusBars, navigationBars)
}

/**
 * Shallow-immutable implementation of [WindowInsets].
 */
internal class ImmutableWindowInsets(
    override val systemGestures: InsetsType = InsetsType.Empty,
    override val navigationBars: InsetsType = InsetsType.Empty,
    override val statusBars: InsetsType = InsetsType.Empty,
    override val ime: InsetsType = InsetsType.Empty,
) : WindowInsets {
    override val systemBars: InsetsType = CalculatedInsetsType(statusBars, navigationBars)
}

@RequiresOptIn(message = "Animated Insets support is experimental. The API may be changed in the future.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ExperimentalAnimatedInsets

/**
 * Composition local containing the current [WindowInsets].
 */
val LocalWindowInsets = staticCompositionLocalOf { WindowInsets.Empty }
