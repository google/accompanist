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

@file:Suppress("DEPRECATION")

package com.google.accompanist.insets

import android.view.View
import android.view.WindowInsetsAnimation
import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat

/**
 * The main insets holder, containing instances of [WindowInsets.Type] which each refer to different
 * types of system display insets.
 */
@Stable
@Deprecated(
"""
accompanist/insets is deprecated.
For more migration information, please visit https://google.github.io/accompanist/insets/#migration
""",
    replaceWith = ReplaceWith(
        "WindowInsets",
        "androidx.compose.foundation.layout.WindowInsets"
    )
)
interface WindowInsets {

    /**
     * Inset values which match [WindowInsetsCompat.Type.navigationBars]
     */
    val navigationBars: Type

    /**
     * Inset values which match [WindowInsetsCompat.Type.statusBars]
     */
    val statusBars: Type

    /**
     * Inset values which match [WindowInsetsCompat.Type.ime]
     */
    val ime: Type

    /**
     * Inset values which match [WindowInsetsCompat.Type.systemGestures]
     */
    val systemGestures: Type

    /**
     * Inset values which match [WindowInsetsCompat.Type.systemBars]
     */
    val systemBars: Type

    /**
     * Inset values which match [WindowInsetsCompat.Type.displayCutout]
     */
    val displayCutout: Type

    /**
     * Returns a copy of this instance with the given values.
     */
    fun copy(
        navigationBars: Type = this.navigationBars,
        statusBars: Type = this.statusBars,
        systemGestures: Type = this.systemGestures,
        ime: Type = this.ime,
        displayCutout: Type = this.displayCutout,
    ): WindowInsets = ImmutableWindowInsets(
        systemGestures = systemGestures,
        navigationBars = navigationBars,
        statusBars = statusBars,
        ime = ime,
        displayCutout = displayCutout
    )

    companion object {
        /**
         * Empty and immutable instance of [WindowInsets].
         */
        val Empty: WindowInsets = ImmutableWindowInsets()
    }

    /**
     * Represents the values for a type of insets, and stores information about the layout insets,
     * animating insets, and visibility of the insets.
     *
     * [WindowInsets.Type] instances are commonly stored in a [WindowInsets] instance.
     */
    @Stable
    @Deprecated(
        "accompanist/insets is deprecated",
        replaceWith = ReplaceWith(
            "WindowInsets",
            "androidx.compose.foundation.layout.WindowInsets"
        )
    )
    interface Type : Insets {
        /**
         * The layout insets for this [WindowInsets.Type]. These are the insets which are defined from the
         * current window layout.
         *
         * You should not normally need to use this directly, and instead use [left], [top],
         * [right], and [bottom] to return the correct value for the current state.
         */
        val layoutInsets: Insets

        /**
         * The animated insets for this [WindowInsets.Type]. These are the insets which are updated from
         * any on-going animations. If there are no animations in progress, the returned [Insets] will
         * be empty.
         *
         * You should not normally need to use this directly, and instead use [left], [top],
         * [right], and [bottom] to return the correct value for the current state.
         */
        val animatedInsets: Insets

        /**
         * Whether the insets are currently visible.
         */
        val isVisible: Boolean

        /**
         * Whether this insets type is being animated at this moment.
         */
        val animationInProgress: Boolean

        /**
         * The left dimension of the insets in pixels.
         */
        override val left: Int
            get() = (if (animationInProgress) animatedInsets else layoutInsets).left

        /**
         * The top dimension of the insets in pixels.
         */
        override val top: Int
            get() = (if (animationInProgress) animatedInsets else layoutInsets).top

        /**
         * The right dimension of the insets in pixels.
         */
        override val right: Int
            get() = (if (animationInProgress) animatedInsets else layoutInsets).right

        /**
         * The bottom dimension of the insets in pixels.
         */
        override val bottom: Int
            get() = (if (animationInProgress) animatedInsets else layoutInsets).bottom

        /**
         * The progress of any ongoing animations, in the range of 0 to 1.
         * If there is no animation in progress, this will return 0.
         */
        @get:FloatRange(from = 0.0, to = 1.0)
        val animationFraction: Float

        companion object {
            /**
             * Empty and immutable instance of [WindowInsets.Type].
             */
            val Empty: Type = ImmutableWindowInsetsType()
        }
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
@Deprecated(
"""
accompanist/insets is deprecated.
ViewWindowInsetObserver is not necessary in androidx.compose and can be removed.
For more migration information, please visit https://google.github.io/accompanist/insets/#migration
"""
)
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
     * @param windowInsetsAnimationsEnabled Whether to listen for [WindowInsetsAnimation]s, such as
     * IME animations.
     * @param consumeWindowInsets Whether to consume any [WindowInsetsCompat]s which are
     * dispatched to the host view. Defaults to `true`.
     */
    fun start(
        consumeWindowInsets: Boolean = true,
        windowInsetsAnimationsEnabled: Boolean = true,
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
                layoutInsets.updateFrom(wic.getInsets(WindowInsetsCompat.Type.statusBars()))
                isVisible = wic.isVisible(WindowInsetsCompat.Type.statusBars())
            }
            windowInsets.navigationBars.run {
                layoutInsets.updateFrom(wic.getInsets(WindowInsetsCompat.Type.navigationBars()))
                isVisible = wic.isVisible(WindowInsetsCompat.Type.navigationBars())
            }
            windowInsets.systemGestures.run {
                layoutInsets.updateFrom(wic.getInsets(WindowInsetsCompat.Type.systemGestures()))
                isVisible = wic.isVisible(WindowInsetsCompat.Type.systemGestures())
            }
            windowInsets.ime.run {
                layoutInsets.updateFrom(wic.getInsets(WindowInsetsCompat.Type.ime()))
                isVisible = wic.isVisible(WindowInsetsCompat.Type.ime())
            }
            windowInsets.displayCutout.run {
                layoutInsets.updateFrom(wic.getInsets(WindowInsetsCompat.Type.displayCutout()))
                isVisible = wic.isVisible(WindowInsetsCompat.Type.displayCutout())
            }

            if (consumeWindowInsets) WindowInsetsCompat.CONSUMED else wic
        }

        // Add an OnAttachStateChangeListener to request an inset pass each time we're attached
        // to the window
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
 * @param windowInsetsAnimationsEnabled Whether to listen for [WindowInsetsAnimation]s, such as
 * IME animations.
 * @param consumeWindowInsets Whether to consume any [WindowInsetsCompat]s which are dispatched to
 * the host view. Defaults to `true`.
 */
@Deprecated(
"""
accompanist/insets is deprecated.
For more migration information, please visit https://google.github.io/accompanist/insets/#migration
""",
    replaceWith = ReplaceWith("content")
)
@Composable
fun ProvideWindowInsets(
    consumeWindowInsets: Boolean = true,
    windowInsetsAnimationsEnabled: Boolean = true,
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
        if (animation.typeMask and WindowInsetsCompat.Type.displayCutout() != 0) {
            windowInsets.displayCutout.onAnimationStart()
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
        windowInsets.displayCutout.updateAnimation(
            platformInsets = platformInsets,
            runningAnimations = runningAnimations,
            type = WindowInsetsCompat.Type.displayCutout()
        )
        return platformInsets
    }

    private fun MutableWindowInsetsType.updateAnimation(
        platformInsets: WindowInsetsCompat,
        runningAnimations: List<WindowInsetsAnimationCompat>,
        type: Int,
    ) {
        // If there are animations of the given type...
        if (runningAnimations.any { it.typeMask or type != 0 }) {
            // Update our animated inset values
            animatedInsets.updateFrom(platformInsets.getInsets(type))
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
        if (animation.typeMask and WindowInsetsCompat.Type.displayCutout() != 0) {
            windowInsets.displayCutout.onAnimationEnd()
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
    override val systemGestures: MutableWindowInsetsType = MutableWindowInsetsType()

    /**
     * Inset values which match [WindowInsetsCompat.Type.navigationBars]
     */
    override val navigationBars: MutableWindowInsetsType = MutableWindowInsetsType()

    /**
     * Inset values which match [WindowInsetsCompat.Type.statusBars]
     */
    override val statusBars: MutableWindowInsetsType = MutableWindowInsetsType()

    /**
     * Inset values which match [WindowInsetsCompat.Type.ime]
     */
    override val ime: MutableWindowInsetsType = MutableWindowInsetsType()

    /**
     * Inset values which match [WindowInsetsCompat.Type.displayCutout]
     */
    override val displayCutout: MutableWindowInsetsType = MutableWindowInsetsType()

    /**
     * Inset values which match [WindowInsetsCompat.Type.systemBars]
     */
    override val systemBars: WindowInsets.Type = derivedWindowInsetsTypeOf(statusBars, navigationBars)
}

/**
 * Shallow-immutable implementation of [WindowInsets].
 */
internal class ImmutableWindowInsets(
    override val systemGestures: WindowInsets.Type = WindowInsets.Type.Empty,
    override val navigationBars: WindowInsets.Type = WindowInsets.Type.Empty,
    override val statusBars: WindowInsets.Type = WindowInsets.Type.Empty,
    override val ime: WindowInsets.Type = WindowInsets.Type.Empty,
    override val displayCutout: WindowInsets.Type = WindowInsets.Type.Empty,
) : WindowInsets {
    override val systemBars: WindowInsets.Type = derivedWindowInsetsTypeOf(statusBars, navigationBars)
}

@RequiresOptIn(message = "Animated Insets support is experimental. The API may be changed in the future.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ExperimentalAnimatedInsets

/**
 * Composition local containing the current [WindowInsets].
 */
@Deprecated(
"""
accompanist/insets is deprecated.
The androidx.compose equivalent of LocalWindowInsets is the extensions on WindowInsets.
For more migration information, please visit https://google.github.io/accompanist/insets/#migration
"""
)
val LocalWindowInsets: ProvidableCompositionLocal<WindowInsets> =
    staticCompositionLocalOf { WindowInsets.Empty }
