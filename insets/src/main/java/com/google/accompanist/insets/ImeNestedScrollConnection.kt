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

package com.google.accompanist.insets

import android.os.Build
import android.view.View
import android.view.WindowInsets
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.math.roundToInt

/**
 * Remembers a [NestedScrollConnection] which scrolls the Android on-screen keyboard on/off
 * screen as appropriate.
 *
 * @param scrollImeOffScreenWhenVisible Set to true to allow scrolling the IME off screen
 * (from being visible), by an downwards scroll. Defaults to `true`.
 * @param scrollImeOnScreenWhenNotVisible Set to true to allow scrolling the IME on screen
 * (from not being visible), by an upwards scroll. Defaults to `true`.
 */
@ExperimentalAnimatedInsets
@Composable
fun rememberImeNestedScrollConnection(
    scrollImeOffScreenWhenVisible: Boolean = true,
    scrollImeOnScreenWhenNotVisible: Boolean = true,
): NestedScrollConnection {
    val view = LocalView.current
    return remember(view, scrollImeOffScreenWhenVisible, scrollImeOnScreenWhenNotVisible) {
        ImeNestedScrollConnection(
            view = view,
            scrollImeOffScreenWhenVisible = scrollImeOffScreenWhenVisible,
            scrollImeOnScreenWhenNotVisible = scrollImeOnScreenWhenNotVisible
        )
    }
}

/**
 * A [NestedScrollConnection] which scrolls the Android on-screen keyboard on/off
 * screen as appropriate, when the user scrolls content. This class may be made an internal
 * library class in the future.
 *
 * You probably do not wish to use this directly, and should use
 * [rememberImeNestedScrollConnection] instead.
 *
 * @param view The host Compose [View]. Usually this comes from [LocalView].
 * @param scrollImeOffScreenWhenVisible Set to true to allow scrolling the IME off screen
 * (from being visible), by an downwards scroll. Defaults to `true`.
 * @param scrollImeOnScreenWhenNotVisible Set to true to allow scrolling the IME on screen
 * (from not being visible), by an upwards scroll. Defaults to `true`.
 */
@ExperimentalAnimatedInsets
class ImeNestedScrollConnection(
    private val view: View,
    private val scrollImeOffScreenWhenVisible: Boolean,
    private val scrollImeOnScreenWhenNotVisible: Boolean,
) : NestedScrollConnection {

    @delegate:RequiresApi(30)
    private val imeAnimController by lazy(LazyThreadSafetyMode.NONE, ::SimpleImeAnimationController)

    @get:RequiresApi(30)
    private val imeVisible: Boolean
        get() = view.rootWindowInsets.isVisible(WindowInsets.Type.ime())

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (Build.VERSION.SDK_INT < 30) {
            // SimpleImeAnimationController only works on API 30+
            return Offset.Zero
        }

        if (imeAnimController.isInsetAnimationRequestPending()) {
            // We're waiting for a controller to become ready. Consume and no-op the scroll
            return available
        }

        if (available.y > 0) {
            // If the user is scrolling down...

            if (imeAnimController.isInsetAnimationInProgress()) {
                // If we currently have control, we can update the IME insets using insetBy()
                return Offset(
                    x = 0f,
                    y = imeAnimController.insetBy(available.y.roundToInt()).toFloat()
                )
            }

            if (scrollImeOffScreenWhenVisible && imeVisible) {
                // If we're not in control, the IME is currently open, and,
                // 'scroll IME away when visible' is enabled, we start a control request
                imeAnimController.startControlRequest(view)

                // We consume the scroll to stop the list scrolling while we wait for a controller
                return available
            }
        }

        return Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (Build.VERSION.SDK_INT < 30) {
            // SimpleImeAnimationController only works on API 30+
            return Offset.Zero
        }

        if (available.y < 0) {
            // If the user is scrolling up, and the scrolling view isn't consuming the scroll...

            if (imeAnimController.isInsetAnimationInProgress()) {
                // If we currently have control, we can update the IME insets
                return Offset(
                    x = 0f,
                    y = imeAnimController.insetBy(available.y.roundToInt()).toFloat()
                )
            }

            if (scrollImeOnScreenWhenNotVisible &&
                !imeAnimController.isInsetAnimationRequestPending() &&
                !imeVisible
            ) {
                // If we don't currently have control, the IME is not shown,
                // the user is scrolling up, and the view can't scroll up any more
                // (i.e. over-scrolling), we can start to control the IME insets
                imeAnimController.startControlRequest(view = view)
                // We consume the scroll to stop the list scrolling while we wait for a controller
                return available
            }
        }

        return Offset.Zero
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity
    ): Velocity {
        if (Build.VERSION.SDK_INT < 30) {
            // SimpleImeAnimationController only works on API 30+
            return Velocity.Zero
        }

        if (imeAnimController.isInsetAnimationInProgress()) {
            // If we have an IME animation in progress, from the user scrolling, we can
            // animate to the end state using the velocity
            return suspendCancellableCoroutine { cont ->
                imeAnimController.animateToFinish(available.y) { remainingVelocity ->
                    cont.resume(
                        value = Velocity(x = 0f, y = remainingVelocity),
                        onCancellation = { imeAnimController.finish() }
                    )
                }
                // If the coroutine is cancelled, cancel the IME animation
                cont.invokeOnCancellation {
                    imeAnimController.cancel()
                }
            }
        }

        // If the fling is in a (upwards direction, and the IME is not visible)
        // start an control request with an immediate fling
        if (scrollImeOnScreenWhenNotVisible && available.y > 0 == imeVisible) {
            return suspendCancellableCoroutine { cont ->
                imeAnimController.startAndFling(view, available.y) { remainingVelocity ->
                    cont.resume(
                        value = Velocity(x = 0f, y = remainingVelocity),
                        onCancellation = { imeAnimController.finish() }
                    )
                }
                // If the coroutine is cancelled, cancel the IME animation
                cont.invokeOnCancellation {
                    imeAnimController.cancel()
                }
            }
        }

        // If we reach here we just return zero velocity
        return Velocity.Zero
    }
}
