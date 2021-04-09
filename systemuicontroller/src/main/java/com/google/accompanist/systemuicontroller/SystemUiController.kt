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

package com.google.accompanist.systemuicontroller

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.View
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat

/**
 * A class which provides easy-to-use utilities for updating the System UI bar
 * colors within Jetpack Compose.
 *
 * @sample com.google.accompanist.sample.systemuicontroller.MyHomeScreen
 */
@Stable
interface SystemUiController {
    /**
     * Set the status bar color.
     *
     * @param color The **desired** [Color] to set. This may require modification if running on an
     * API level that only supports white status bar icons.
     * @param darkIcons Whether dark status bar icons would be preferable.
     * @param transformColorForLightContent A lambda which will be invoked to transform [color] if
     * dark icons were requested but are not available. Defaults to applying a black scrim.
     */
    fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean = color.luminance() > 0.5f,
        transformColorForLightContent: (Color) -> Color = BlackScrimmed
    ) = Unit

    /**
     * Set the navigation bar color.
     *
     * @param color The **desired** [Color] to set. This may require modification if running on an
     * API level that only supports white navigation bar icons. Additionally this will be ignored
     * and [Color.Transparent] will be used on API 29+ where gesture navigation is preferred or the
     * system UI automatically applies background protection in other navigation modes.
     * @param darkIcons Whether dark navigation bar icons would be preferable.
     * @param navigationBarContrastEnforced Whether the system should ensure that the navigation
     * bar has enough contrast when a fully transparent background is requested. Only supported on
     * API 29+.
     * @param transformColorForLightContent A lambda which will be invoked to transform [color] if
     * dark icons were requested but are not available. Defaults to applying a black scrim.
     */
    fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean = color.luminance() > 0.5f,
        navigationBarContrastEnforced: Boolean = true,
        transformColorForLightContent: (Color) -> Color = BlackScrimmed
    ) = Unit

    /**
     * Set the status and navigation bars to [color].
     *
     * @see setStatusBarColor
     * @see setNavigationBarColor
     */
    fun setSystemBarsColor(
        color: Color,
        darkIcons: Boolean = color.luminance() > 0.5f,
        isNavigationBarContrastEnforced: Boolean = true,
        transformColorForLightContent: (Color) -> Color = BlackScrimmed
    ) {
        setStatusBarColor(color, darkIcons, transformColorForLightContent)
        setNavigationBarColor(
            color,
            darkIcons,
            isNavigationBarContrastEnforced,
            transformColorForLightContent
        )
    }

    /**
     * Returns whether the system is ensuring that the navigation bar has enough contrast when a
     * fully transparent background is requested.
     *
     * @return true, if API is 29+ and the system is ensuring contrast, false otherwise.
     */
    fun isNavigationBarContrastEnforced(): Boolean = false
}

/**
 * Remembers a [SystemUiController] which supports Android devices.
 */
@Composable
fun rememberAndroidSystemUiController(
    view: View = LocalView.current
): SystemUiController = remember(view) {
    AndroidSystemUiController(view)
}

/**
 * A helper class for setting the navigation and status bar colors for a [View], gracefully
 * degrading behavior based upon API level.
 *
 * Typically you would use [rememberAndroidSystemUiController] to remember an instance of this.
 */
class AndroidSystemUiController(view: View) : SystemUiController {
    private val window = view.context.findWindow()
    private val windowInsetsController = ViewCompat.getWindowInsetsController(view)

    override fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean,
        transformColorForLightContent: (Color) -> Color
    ) {
        windowInsetsController?.isAppearanceLightStatusBars = darkIcons

        window?.statusBarColor = when {
            darkIcons && windowInsetsController?.isAppearanceLightStatusBars != true -> {
                // If we're set to use dark icons, but our windowInsetsController call didn't
                // succeed (usually due to API level), we instead transform the color to maintain
                // contrast
                transformColorForLightContent(color)
            }
            else -> color
        }.toArgb()
    }

    override fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean,
        navigationBarContrastEnforced: Boolean,
        transformColorForLightContent: (Color) -> Color
    ) {
        windowInsetsController?.isAppearanceLightNavigationBars = darkIcons

        window?.navigationBarColor = when {
            darkIcons && windowInsetsController?.isAppearanceLightNavigationBars != true -> {
                // If we're set to use dark icons, but our windowInsetsController call didn't
                // succeed (usually due to API level), we instead transform the color to maintain
                // contrast
                transformColorForLightContent(color)
            }
            else -> color
        }.toArgb()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window?.isNavigationBarContrastEnforced = navigationBarContrastEnforced
        }
    }

    override fun isNavigationBarContrastEnforced(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            window?.isNavigationBarContrastEnforced == true
    }

    private fun Context.findWindow(): Window? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context.window
            context = context.baseContext
        }
        return null
    }
}

/**
 * An [androidx.compose.runtime.CompositionLocalProvider] holding the current
 * [LocalSystemUiController]. Defaults to a no-op controller; consumers should
 * [provide][androidx.compose.runtime.CompositionLocalProvider] a real one.
 *
 * @sample com.google.accompanist.sample.systemuicontroller.MyApp
 */
val LocalSystemUiController = staticCompositionLocalOf<SystemUiController> {
    NoOpSystemUiController
}

private val BlackScrim = Color(0f, 0f, 0f, 0.3f) // 30% opaque black
private val BlackScrimmed: (Color) -> Color = { original ->
    BlackScrim.compositeOver(original)
}

/**
 * A no-op implementation, useful as the default value for [LocalSystemUiController].
 */
private object NoOpSystemUiController : SystemUiController
