/*
 * Copyright 2022 The Android Open Source Project
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

package com.google.accompanist.testharness

import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.util.DisplayMetrics
import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import kotlin.math.floor

/**
 * Render [content] in a [Box] within a harness, overriding various device configuration values to
 * make testing easier.
 *
 * @param size if not [DpSize.Unspecified], the [content] will be forced to be drawn with at this
 * size, overriding [LocalDensity] if necessary to ensure that there is enough space. This
 * defaults to [DpSize.Unspecified].
 *
 * @param darkMode if true, the content will be rendered with dark mode. This defaults to the
 * current dark mode value as reported by [isSystemInDarkTheme].
 *
 * @param locales the list of locales to render the app with. This defaults to the list of locales
 * returned by [LocalConfiguration.current].
 *
 * @param layoutDirection an overriding layout direction. This defaults to `null`, which means
 * that the layout direction from the [locales] is used instead.
 *
 * @param fontScale the font scale to render text at. This defaults to the current
 * [Density.fontScale].
 *
 * @param fontWeightAdjustment the font weight adjustment for fonts. This defaults to the current
 * [fontWeightAdjustment] (if any). If `null`, the [fontWeightAdjustment] will be left unchanged.
 *
 * @param isScreenRound the device roundness. This defaults to null. If `null`,
 * the [isScreenRound] will be left unchanged.
 */
@Composable
@Deprecated(
    replaceWith = ReplaceWith(
        "DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(size) " +
            "then DeviceConfigurationOverride.DarkMode(darkMode) " +
            "then DeviceConfigurationOverride.Locales(LocaleList(locales.toLanguageTags()))" +
            "then DeviceConfigurationOverride.LayoutDirection(layoutDirection)" +
            "then DeviceConfigurationOverride.FontScale(fontScale)" +
            "then DeviceConfigurationOverride.FontWeightAdjustment(fontWeightAdjustment)" +
            "then DeviceConfigurationOverride.RoundScreen(isScreenRound), " +
            "content)",
        "androidx.compose.ui.test.DeviceConfigurationOverride",
        "androidx.compose.ui.test.ForcedSize",
        "androidx.compose.ui.test.DarkMode",
        "androidx.compose.ui.test.Locales",
        "androidx.compose.ui.test.LayoutDirection",
        "androidx.compose.ui.test.FontScale",
        "androidx.compose.ui.test.FontWeightAdjustment",
        "androidx.compose.ui.test.RoundScreen",
        "androidx.compose.ui.test.then",
        "androidx.compose.ui.text.intl.LocaleList",
    ),
    message = "TestHarness has been superceded by DeviceConfigurationOverride in ui-test. " +
        "Each argument in TestHarness have been replaced with an individual " +
        "DeviceConfigurationOverride, so the suggested replacement is likely unnecessarily " +
        "adding overrides for anything that was previously using the default arguments."
)
public fun TestHarness(
    size: DpSize = DpSize.Unspecified,
    darkMode: Boolean = isSystemInDarkTheme(),
    locales: LocaleListCompat = ConfigurationCompat.getLocales(LocalConfiguration.current),
    layoutDirection: LayoutDirection? = null,
    fontScale: Float = LocalDensity.current.fontScale,
    fontWeightAdjustment: Int? =
        if (Build.VERSION.SDK_INT >= 31) LocalConfiguration.current.fontWeightAdjustment else null,
    isScreenRound: Boolean? = null,
    content: @Composable () -> Unit
) {
    // Use the DensityForcedSize content wrapper if specified
    val sizeContentWrapper: @Composable (@Composable () -> Unit) -> Unit =
        if (size == DpSize.Unspecified) {
            { it() }
        } else {
            { DensityForcedSize(size, it) }
        }

    // First override the density. Doing this first allows using the resulting density in the
    // overridden configuration.
    sizeContentWrapper {
        // Second, override the configuration, with the current configuration modified by the
        // given parameters.
        OverriddenConfiguration(
            configuration = Configuration().apply {
                // Initialize from the current configuration
                updateFrom(LocalConfiguration.current)
                // Set dark mode directly
                uiMode = uiMode and Configuration.UI_MODE_NIGHT_MASK.inv() or if (darkMode) {
                    Configuration.UI_MODE_NIGHT_YES
                } else {
                    Configuration.UI_MODE_NIGHT_NO
                }
                // Update the locale list
                if (Build.VERSION.SDK_INT >= 24) {
                    setLocales(LocaleList.forLanguageTags(locales.toLanguageTags()))
                } else {
                    setLocale(locales[0])
                }
                // Override densityDpi
                densityDpi =
                    floor(LocalDensity.current.density * DisplayMetrics.DENSITY_DEFAULT).toInt()
                // Override font scale
                this.fontScale = fontScale
                // Maybe override fontWeightAdjustment
                if (Build.VERSION.SDK_INT >= 31 && fontWeightAdjustment != null) {
                    this.fontWeightAdjustment = fontWeightAdjustment
                }
                // override isRound for Wear
                if (Build.VERSION.SDK_INT >= 23 && isScreenRound != null) {
                    screenLayout = when (isScreenRound) {
                        true -> (screenLayout and Configuration.SCREENLAYOUT_ROUND_MASK.inv()) or
                            Configuration.SCREENLAYOUT_ROUND_YES
                        false -> (screenLayout and Configuration.SCREENLAYOUT_ROUND_MASK.inv()) or
                            Configuration.SCREENLAYOUT_ROUND_NO
                    }
                }
            },
        ) {
            // Finally, override the layout direction again if manually specified, potentially
            // overriding the one from the locale.
            CompositionLocalProvider(
                LocalLayoutDirection provides (layoutDirection ?: LocalLayoutDirection.current)
            ) {
                content()
            }
        }
    }
}

/**
 * Overrides the compositions locals related to the given [configuration].
 *
 * There currently isn't a single source of truth for these values, so we update them all
 * according to the given [configuration].
 */
@Composable
internal fun OverriddenConfiguration(
    configuration: Configuration,
    content: @Composable () -> Unit
) {
    // We don't override the theme, but we do want to override the configuration and this seems
    // convenient to do so
    val newContext = ContextThemeWrapper(LocalContext.current, 0).apply {
        applyOverrideConfiguration(configuration)
    }

    CompositionLocalProvider(
        LocalContext provides newContext,
        LocalConfiguration provides configuration,
        LocalLayoutDirection provides
            if (configuration.layoutDirection == View.LAYOUT_DIRECTION_LTR) {
                LayoutDirection.Ltr
            } else {
                LayoutDirection.Rtl
            },
        LocalDensity provides Density(
            configuration.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT,
            configuration.fontScale
        ),
        LocalFontFamilyResolver provides createFontFamilyResolver(newContext),
        content = content
    )
}

/**
 * Render [content] in a [Box] that is forced to have the given [size] without clipping.
 *
 * This is only suitable for tests, since this will override [LocalDensity] to ensure that the
 * [size] is met (as opposed to [Modifier.requiredSize] which will result in clipping).
 */
@Composable
internal fun DensityForcedSize(
    size: DpSize,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(
        // Try to set the size naturally, we'll be overriding the density below if this fails
        modifier = Modifier.size(size)
    ) {
        // Compute the minimum density required so that both the requested width and height both
        // fit
        val density = LocalDensity.current.density * minOf(
            maxWidth / maxOf(maxWidth, size.width),
            maxHeight / maxOf(maxHeight, size.height),
        )
        // Configuration requires the density DPI to be an integer, so round down to ensure we
        // have enough space
        val densityDpi = floor(density * DisplayMetrics.DENSITY_DEFAULT).toInt()

        CompositionLocalProvider(
            LocalDensity provides Density(
                // Override the density with the factor needed to meet both the minimum width and
                // height requirements, and the configuration override requirements.
                density = densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT,
                // Pass through the font scale
                fontScale = LocalDensity.current.fontScale
            )
        ) {
            Box(
                // This size will now be guaranteed to be able to match the constraints
                modifier = Modifier
                    .size(size)
                    .fillMaxSize()
            ) {
                content()
            }
        }
    }
}
