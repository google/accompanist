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

@file:JvmName("AppCompatTheme")
package dev.chrisbanes.accompanist.appcompattheme

import android.content.Context
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AmbientContext
import androidx.core.content.res.use

/**
 * This function creates the components of a [androidx.compose.material.MaterialTheme], reading the
 * values from the `Theme.AppCompat` Android theme.
 *
 * @param context The context to read the theme from.
 * @param readColors whether the read the MDC color palette from the context's theme.
 * If `false`, the current value of [MaterialTheme.colors] is preserved.
 * @param readTypography whether the read the MDC text appearances from [context]'s theme.
 * If `false`, the current value of [MaterialTheme.typography] is preserved.
 * @param shapes A set of shapes to be used by the components in this hierarchy.
 */
@Composable
fun AppCompatTheme(
    context: Context = AmbientContext.current,
    readColors: Boolean = true,
    readTypography: Boolean = true,
    shapes: Shapes = MaterialTheme.shapes,
    content: @Composable () -> Unit
) {
    val themeParams = remember(context.theme) {
        context.createAppCompatTheme(
            readColors = readColors,
            readTypography = readTypography
        )
    }

    MaterialTheme(
        colors = themeParams.colors ?: MaterialTheme.colors,
        typography = themeParams.typography ?: MaterialTheme.typography,
        shapes = shapes,
        content = content
    )
}

/**
 * This class contains the individual components of a [MaterialTheme]: [Colors], [Typography]
 * and [Shapes].
 */
data class ThemeParameters(
    val colors: Colors?,
    val typography: Typography?
)

/**
 * This function creates the components of a [androidx.compose.material.MaterialTheme], reading the
 * values from the `Theme.AppCompat` Android theme.
 *
 * The individual components of the returned [ThemeParameters] may be `null`, depending on the
 * matching 'read' parameter. For example, if you set [readColors] to `false`,
 * [ThemeParameters.colors] will be null.
 *
 * @param readColors whether the read the MDC color palette from the context's theme.
 * @param readTypography whether the read the MDC text appearances from [context]'s theme.
 *
 * @return [ThemeParameters] instance containing the resulting [Colors] and [Typography]
 */
fun Context.createAppCompatTheme(
    readColors: Boolean = true,
    readTypography: Boolean = true
): ThemeParameters = obtainStyledAttributes(R.styleable.AppCompatThemeAdapterTheme).use { ta ->
    require(ta.hasValue(R.styleable.AppCompatThemeAdapterTheme_windowActionBar)) {
        "createAppCompatTheme requires the host context's theme to extend Theme.AppCompat"
    }

    val colors = if (readColors) {
        val isLightTheme = ta.getBoolean(R.styleable.AppCompatThemeAdapterTheme_isLightTheme, true)

        val defaultColors = if (isLightTheme) lightColors() else darkColors()

        /* First we'll read the Material color palette */
        val primary = ta.getComposeColor(R.styleable.AppCompatThemeAdapterTheme_colorPrimary)
        val primaryVariant = ta.getComposeColor(R.styleable.AppCompatThemeAdapterTheme_colorPrimaryDark)
        val onPrimary = primary.calculateOnColor()

        val secondary = ta.getComposeColor(R.styleable.AppCompatThemeAdapterTheme_colorAccent)
        val secondaryVariant = secondary
        val onSecondary = secondary.calculateOnColor()

        // We try and use the android:textColorPrimary value (with forced 100% alpha) for the
        // onSurface and onBackground colors
        val textColorPrimary = ta.getComposeColor(
            R.styleable.AppCompatThemeAdapterTheme_android_textColorPrimary
        ).let { color ->
            // We only force the alpha value if it's not Unspecified
            if (color != Color.Unspecified) color.copy(alpha = 1f) else color
        }

        val surface = defaultColors.surface
        val onSurface = surface.calculateOnColorWithTextColorPrimary(textColorPrimary)

        val background = ta.getComposeColor(R.styleable.AppCompatThemeAdapterTheme_android_colorBackground)
        val onBackground = background.calculateOnColorWithTextColorPrimary(textColorPrimary)

        val error = ta.getComposeColor(R.styleable.AppCompatThemeAdapterTheme_colorError)
        val onError = error.calculateOnColor()

        defaultColors.copy(
            primary = primary,
            primaryVariant = primaryVariant,
            onPrimary = onPrimary,
            secondary = secondary,
            secondaryVariant = secondaryVariant,
            onSecondary = onSecondary,
            surface = surface,
            onSurface = onSurface,
            background = background,
            onBackground = onBackground,
            error = error,
            onError = onError
        )
    } else null

    /**
     * Next we'll create a typography instance. We only use the default app:fontFamily or
     * android:fontFamily set in the theme. If neither of these are set, we return null.
     */
    val typography = if (readTypography) {
        val fontFamily = ta.getFontFamilyOrNull(R.styleable.AppCompatThemeAdapterTheme_fontFamily)
            ?: ta.getFontFamilyOrNull(R.styleable.AppCompatThemeAdapterTheme_android_fontFamily)
        fontFamily?.let {
            Typography(defaultFontFamily = it.fontFamily)
        }
    } else null

    ThemeParameters(colors, typography)
}
