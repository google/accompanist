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

import android.R
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowManager
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
import androidx.core.view.WindowInsetsCompat


/**
 * A class which provides easy-to-use utilities for updating the System UI bar
 * colors within Jetpack Compose.
 *
 * @sample com.google.accompanist.sample.systemuicontroller.SystemUiControllerSample
 */
@Stable
interface SystemUiController {

    /**
     * If the value is true, the status bar will be displayed,
     * otherwise the status bar will be hidden.
     */
    var isStatusBarVisible: Boolean

    /**
     * If the value is true, the navigation bar will be displayed,
     * otherwise the navigation bar will be hidden.
     */
    var isNavigationBarVisible: Boolean

    /**
     * If the value is true, both the status bar and the navigation bar will be displayed,
     * otherwise they will be hidden.
     *
     * When any system bar is visible, this value is false.
     */
    var isSystemBarsVisible: Boolean

    /**
     * Set the status bar color.
     *
     * @param color The **desired** [Color] to set. This may require modification if running on an
     * API level that only supports white status bar icons.
     * @param darkIcons Whether dark status bar icons would be preferable.
     * @param transformColorForLightContent A lambda which will be invoked to transform [color] if
     * dark icons were requested but are not available. Defaults to applying a black scrim.
     *
     * @see setStatusBarDarkIcons
     */
    fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean = color.luminance() > 0.5f,
        transformColorForLightContent: (Color) -> Color = BlackScrimmed
    )

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
     *
     * @see setNavigationBarDarkIcons
     */
    fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean = color.luminance() > 0.5f,
        navigationBarContrastEnforced: Boolean = true,
        transformColorForLightContent: (Color) -> Color = BlackScrimmed
    )

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
     * Set the status bar icons darkened.
     *
     * @param enabled If the value is true, the status bar icon will change to a dark color,
     * but if the system does not support the status bar dark mode, nothing will happen.
     */
    fun setStatusBarDarkIcons(enabled: Boolean = true)

    /**
     * Set the navigation bar icons darkened.
     *
     * @param enabled If the value is true, the navigation bar icon will change to a dark color,
     * but if the system does not support the navigation bar dark mode, nothing will happen.
     */
    fun setNavigationBarDarkIcons(enabled: Boolean = true)

    /**
     * Set the status and navigation bars icons darkened.
     *
     * @param statusBarEnabled If the value is true, the status bar icon will change to a dark
     * color, but if the system does not support the status bar dark mode, nothing will happen.
     * @param navigationBarEnabled If the value is true, the navigation bar icon will change to a
     * dark color, but if the system does not support the status bar dark mode, nothing will happen.
     */
    fun setSystemBarsDarkIcons(
        statusBarEnabled: Boolean = true,
        navigationBarEnabled: Boolean = statusBarEnabled,
    ) {
        setStatusBarDarkIcons(statusBarEnabled)
        setNavigationBarDarkIcons(navigationBarEnabled)
    }

    /**
     * Returns true when the icons color of the status bar is dark.
     *
     * @see setStatusBarDarkIcons
     */
    fun isDarkStatusBarIcons(): Boolean

    /**
     * Returns true when the icons color of the navigation bar is dark.
     *
     * @see setNavigationBarDarkIcons
     */
    fun isDarkNavigationBarIcons(): Boolean

    /**
     * Returns true when the icons color of the status and navigation bars is dark.
     *
     * @see setStatusBarDarkIcons
     * @see setNavigationBarDarkIcons
     */
    fun isDarkSystemBarsIcons(): Boolean = isDarkStatusBarIcons() && isDarkNavigationBarIcons()

    /**
     * Returns whether the system is ensuring that the navigation bar has enough contrast when a
     * fully transparent background is requested.
     *
     * @return true, if API is 29+ and the system is ensuring contrast, false otherwise.
     */
    fun isNavigationBarContrastEnforced(): Boolean
}

/**
 * Remembers a [SystemUiController] for the current device.
 */
@Composable
fun rememberSystemUiController(): SystemUiController {
    val view = LocalView.current
    return remember(view) { AndroidSystemUiController(view) }
}

@Deprecated(
    "Migrate to rememberSystemUiController()",
    ReplaceWith(
        "rememberSystemUiController()",
        "com.google.accompanist.systemuicontroller.rememberSystemUiController"
    )
)
@Composable
fun rememberAndroidSystemUiController(
    view: View = LocalView.current
): SystemUiController = remember(view) { AndroidSystemUiController(view) }

/**
 * A helper class for setting the navigation and status bar colors for a [View], gracefully
 * degrading behavior based upon API level.
 *
 * Typically you would use [rememberSystemUiController] to remember an instance of this.
 */
class AndroidSystemUiController(view: View) : SystemUiController {
    private val window = view.context.findWindow()
    private val windowInsetsController = ViewCompat.getWindowInsetsController(view)

    override var isStatusBarVisible: Boolean
        get() = window?.attributes?.flags?.let {
            @Suppress("DEPRECATION")
            it and WindowManager.LayoutParams.FLAG_FULLSCREEN == 0
        } ?: true
        set(value) {
            if (value) {
                windowInsetsController?.show(WindowInsetsCompat.Type.statusBars())
            } else {
                windowInsetsController?.hide(WindowInsetsCompat.Type.statusBars())
            }
        }

    override var isNavigationBarVisible: Boolean
        @Suppress("DEPRECATION")
        get() {
            val window = window ?: return true
            val decorView = window.decorView
            val display = window.windowManager.defaultDisplay

            val realDisplayMetrics = DisplayMetrics()
            display.getRealMetrics(realDisplayMetrics)
            val realHeight = realDisplayMetrics.heightPixels
            val realWidth = realDisplayMetrics.widthPixels
            val displayMetrics = DisplayMetrics()
            display.getMetrics(displayMetrics)
            val displayHeight = displayMetrics.heightPixels
            val displayWidth = displayMetrics.widthPixels
            // The actual screen is the same as the display
            val correctSize = (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0

            val point = Point()
            display.getRealSize(point)
            return if (Configuration.ORIENTATION_LANDSCAPE == window.context.resources.configuration.orientation) {
                (point.x != decorView.findViewById<View>(R.id.content).width)
            } else {
                val rect = Rect()
                decorView.getWindowVisibleDisplayFrame(rect)
                (rect.bottom != point.y)
            } && correctSize
        }
        set(value) {
            if (value) {
                windowInsetsController?.show(WindowInsetsCompat.Type.navigationBars())
            } else {
                windowInsetsController?.hide(WindowInsetsCompat.Type.navigationBars())
            }
        }

    override var isSystemBarsVisible: Boolean
        get() = isStatusBarVisible && isNavigationBarVisible
        set(value) {
            if (value) {
                windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
            } else {
                windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
            }
        }

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

        if (Build.VERSION.SDK_INT >= 29) {
            window?.isNavigationBarContrastEnforced = navigationBarContrastEnforced
        }
    }

    override fun setStatusBarDarkIcons(enabled: Boolean) {
        windowInsetsController?.isAppearanceLightStatusBars = enabled
    }

    override fun setNavigationBarDarkIcons(enabled: Boolean) {
        windowInsetsController?.isAppearanceLightNavigationBars = enabled
    }

    override fun isDarkStatusBarIcons(): Boolean =
        windowInsetsController?.isAppearanceLightStatusBars ?: false

    override fun isDarkNavigationBarIcons(): Boolean =
        windowInsetsController?.isAppearanceLightNavigationBars ?: false

    override fun isNavigationBarContrastEnforced(): Boolean {
        return Build.VERSION.SDK_INT >= 29 && window?.isNavigationBarContrastEnforced == true
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
 * @sample com.google.accompanist.sample.systemuicontroller.SystemUiControllerSample
 */
@Deprecated("Use rememberSystemUiController()")
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
private object NoOpSystemUiController : SystemUiController {
    override var isStatusBarVisible: Boolean = true

    override var isNavigationBarVisible: Boolean = true

    override var isSystemBarsVisible: Boolean = true

    override fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean,
        transformColorForLightContent: (Color) -> Color
    ) = Unit

    override fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean,
        navigationBarContrastEnforced: Boolean,
        transformColorForLightContent: (Color) -> Color
    ) = Unit

    override fun setStatusBarDarkIcons(enabled: Boolean) = Unit

    override fun setNavigationBarDarkIcons(enabled: Boolean) = Unit

    override fun isDarkStatusBarIcons(): Boolean = false

    override fun isDarkNavigationBarIcons(): Boolean = false

    override fun isNavigationBarContrastEnforced(): Boolean = false
}
