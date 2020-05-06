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

package dev.chrisbanes.accompanist.mdctheme

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Typeface
import android.os.Build
import android.util.Log
import android.util.TypedValue
import androidx.annotation.StyleRes
import androidx.compose.Composable
import androidx.compose.remember
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.use
import androidx.ui.core.ContextAmbient
import androidx.ui.core.DensityAmbient
import androidx.ui.foundation.isSystemInDarkTheme
import androidx.ui.foundation.shape.corner.CornerBasedShape
import androidx.ui.foundation.shape.corner.CornerSize
import androidx.ui.foundation.shape.corner.CutCornerShape
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Color
import androidx.ui.graphics.Shadow
import androidx.ui.material.ColorPalette
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Shapes
import androidx.ui.material.Typography
import androidx.ui.material.darkColorPalette
import androidx.ui.material.lightColorPalette
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontFamily
import androidx.ui.text.font.FontStyle
import androidx.ui.text.font.FontWeight
import androidx.ui.text.font.asFontFamily
import androidx.ui.text.font.font
import androidx.ui.unit.Density
import androidx.ui.unit.TextUnit
import androidx.ui.unit.dp
import androidx.ui.unit.em
import androidx.ui.unit.px
import androidx.ui.unit.sp
import java.lang.reflect.Method
import kotlin.concurrent.getOrSet

/**
 * A [MaterialTheme] which reads the corresponding values from an
 * Material Design Components Android theme in the given [context].
 *
 * By default the text colors from any associated `TextAppearance`s from the theme are *not* read.
 * This is because setting a fixed color in the resulting [TextStyle] breaks the usage of
 * [androidx.ui.material.Emphasis] through [androidx.ui.material.ProvideEmphasis].
 * You can customize this through the [useTextColors] parameter.
 *
 * @param context The context to read the theme from
 * @param readColors whether the read the MDC color palette from the context's theme
 * @param readTypography whether the read the MDC typography text appearances from the context's theme
 * @param readShapes whether the read the MDC shape appearances from the context's theme
 * @param useTextColors whether to read the colors from the `TextAppearance`s associated from the
 * theme. Defaults to `false`
 */
@Composable
fun MaterialThemeFromMdcTheme(
    context: Context = ContextAmbient.current,
    readColors: Boolean = true,
    readTypography: Boolean = true,
    readShapes: Boolean = true,
    useTextColors: Boolean = false,
    children: @Composable() () -> Unit
) {
    // We try and use the theme key value if available, which should be a perfect key for caching
    // and avoid the expensive theme lookups in re-compositions.
    //
    // If the key is not available, we use the Theme itself as a rough approximation. Using the
    // Theme instance as the key is not perfect, but it should work for 90% of cases.
    // It falls down when the theme is manually mutated after a composition has happened
    // (via `applyStyle()`, `rebase()`, `setTo()`), but the majority of apps do not use those.
    val key = context.theme.key ?: context.theme

    val (colors, type, shapes) = remember(key) {
        generateMaterialThemeFromMdcTheme(
            context,
            readColors,
            readTypography,
            readShapes,
            useTextColors
        )
    }

    MaterialTheme(
        typography = type,
        colors = colors,
        shapes = shapes,
        content = children
    )
}

/**
 * This effect generates the components of an [androidx.ui.material.MaterialTheme], reading the
 * values from an Material Design Components Android theme.
 *
 * By default the text colors from any associated `TextAppearance`s from the theme are *not* read.
 * This is because setting a fixed color in the resulting [TextStyle] breaks the usage of
 * [androidx.ui.material.Emphasis] through [androidx.ui.material.ProvideEmphasis].
 * You can customize this through the [useTextColors] parameter.
 *
 * @param context The context to read the theme from
 * @param readColors whether the read the MDC color palette from the context's theme
 * @param readTypography whether the read the MDC typography text appearances from the context's theme
 * @param readShapes whether the read the MDC shape appearances from the context's theme
 * @param useTextColors whether to read the colors from the `TextAppearance`s associated from the
 * theme. Defaults to `false`
 */
@Composable
fun generateMaterialThemeFromMdcTheme(
    context: Context = ContextAmbient.current,
    readColors: Boolean = true,
    readTypography: Boolean = true,
    readShapes: Boolean = true,
    useTextColors: Boolean = false
): Triple<ColorPalette, Typography, Shapes> {
    return context.obtainStyledAttributes(R.styleable.AccompanistMdcTheme).use { ta ->
        require(ta.hasValue(R.styleable.AccompanistMdcTheme_colorPrimary)) {
            "MaterialThemeUsingMdcTheme requires the host context's theme " +
                    "to extend Theme.MaterialComponents"
        }

        val colors: ColorPalette = if (readColors) {
            /* First we'll read the Material color palette */
            val primary = ta.getComposeColor(R.styleable.AccompanistMdcTheme_colorPrimary)
            val primaryVariant = ta.getComposeColor(R.styleable.AccompanistMdcTheme_colorPrimaryVariant)
            val onPrimary = ta.getComposeColor(R.styleable.AccompanistMdcTheme_colorOnPrimary)
            val secondary = ta.getComposeColor(R.styleable.AccompanistMdcTheme_colorSecondary)
            val secondaryVariant = ta.getComposeColor(R.styleable.AccompanistMdcTheme_colorSecondaryVariant)
            val onSecondary = ta.getComposeColor(R.styleable.AccompanistMdcTheme_colorOnSecondary)
            val background = ta.getComposeColor(R.styleable.AccompanistMdcTheme_android_colorBackground)
            val onBackground = ta.getComposeColor(R.styleable.AccompanistMdcTheme_colorOnBackground)
            val surface = ta.getComposeColor(R.styleable.AccompanistMdcTheme_colorSurface)
            val onSurface = ta.getComposeColor(R.styleable.AccompanistMdcTheme_colorOnSurface)
            val error = ta.getComposeColor(R.styleable.AccompanistMdcTheme_colorError)
            val onError = ta.getComposeColor(R.styleable.AccompanistMdcTheme_colorOnError)

            val isLightTheme = ta.getBoolean(R.styleable.AccompanistMdcTheme_isLightTheme, true)

            if (isLightTheme) {
                lightColorPalette(
                    primary = primary,
                    primaryVariant = primaryVariant,
                    onPrimary = onPrimary,
                    secondary = secondary,
                    secondaryVariant = secondaryVariant,
                    onSecondary = onSecondary,
                    background = background,
                    onBackground = onBackground,
                    surface = surface,
                    onSurface = onSurface,
                    error = error,
                    onError = onError
                )
            } else {
                darkColorPalette(
                    primary = primary,
                    primaryVariant = primaryVariant,
                    onPrimary = onPrimary,
                    secondary = secondary,
                    onSecondary = onSecondary,
                    background = background,
                    onBackground = onBackground,
                    surface = surface,
                    onSurface = onSurface,
                    error = error,
                    onError = onError
                )
            }
        } else {
            // Else we create an empty color palette based on the configuration's uiMode
            if (isSystemInDarkTheme()) darkColorPalette() else lightColorPalette()
        }

        /**
         * Next we'll generate a typography instance, using the Material Theme text appearances
         * for TextStyles.
         *
         * We create a normal 'empty' instance first to start from the defaults, then merge in our
         * generated text styles from the Android theme.
         */
        var typography = Typography()

        if (readTypography) {
            typography = typography.merge(
                h1 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.AccompanistMdcTheme_textAppearanceHeadline1),
                    useTextColors
                ),
                h2 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.AccompanistMdcTheme_textAppearanceHeadline2),
                    useTextColors
                ),
                h3 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.AccompanistMdcTheme_textAppearanceHeadline3),
                    useTextColors
                ),
                h4 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.AccompanistMdcTheme_textAppearanceHeadline4),
                    useTextColors
                ),
                h5 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.AccompanistMdcTheme_textAppearanceHeadline5),
                    useTextColors
                ),
                h6 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.AccompanistMdcTheme_textAppearanceHeadline6),
                    useTextColors
                ),
                subtitle1 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.AccompanistMdcTheme_textAppearanceSubtitle1),
                    useTextColors
                ),
                subtitle2 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.AccompanistMdcTheme_textAppearanceSubtitle2),
                    useTextColors
                ),
                body1 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.AccompanistMdcTheme_textAppearanceBody1),
                    useTextColors
                ),
                body2 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.AccompanistMdcTheme_textAppearanceBody2),
                    useTextColors
                ),
                button = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.AccompanistMdcTheme_textAppearanceButton),
                    useTextColors
                ),
                caption = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.AccompanistMdcTheme_textAppearanceCaption),
                    useTextColors
                ),
                overline = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.AccompanistMdcTheme_textAppearanceOverline),
                    useTextColors
                )
            )
        }

        /**
         * Now read the shape appearances
         */
        val shapes = if (readShapes) {
            Shapes(
                small = readShapeAppearance(
                    context = context,
                    id = ta.getResourceIdOrThrow(R.styleable.AccompanistMdcTheme_shapeAppearanceSmallComponent),
                    fallbackSize = CornerSize(4.dp)
                ),
                medium = readShapeAppearance(
                    context = context,
                    id = ta.getResourceIdOrThrow(R.styleable.AccompanistMdcTheme_shapeAppearanceMediumComponent),
                    fallbackSize = CornerSize(4.dp)
                ),
                large = readShapeAppearance(
                    context = context,
                    id = ta.getResourceIdOrThrow(R.styleable.AccompanistMdcTheme_shapeAppearanceLargeComponent),
                    fallbackSize = CornerSize(0.dp)
                )
            )
        } else {
            Shapes()
        }

        Triple(colors, typography, shapes)
    }
}

@Composable
private fun textStyleFromTextAppearance(
    context: Context,
    @StyleRes id: Int,
    useTextColor: Boolean
): TextStyle {
    return context.obtainStyledAttributes(id, R.styleable.AccompanistMdcTextAppearance).use { a ->
        val textStyle = a.getInt(R.styleable.AccompanistMdcTextAppearance_android_textStyle, -1)
        val textFontWeight = a.getInt(R.styleable.AccompanistMdcTextAppearance_android_textFontWeight, -1)
        val typeface = a.getInt(R.styleable.AccompanistMdcTextAppearance_android_typeface, -1)

        // TODO read and expand android:fontVariationSettings.
        // Variable fonts are not supported in Compose yet

        val density = DensityAmbient.current

        TextStyle(
            color = if (useTextColor) {
                a.getComposeColor(R.styleable.AccompanistMdcTextAppearance_android_textColor)
            } else Color.Unset,
            fontSize = a.getTextUnit(R.styleable.AccompanistMdcTextAppearance_android_textSize, density),
            lineHeight = a.getTextUnit(R.styleable.AccompanistMdcTextAppearance_android_lineHeight, density),
            fontFamily = when {
                // FYI, this only works with static font files in assets
                a.hasValue(R.styleable.AccompanistMdcTextAppearance_android_fontFamily) -> {
                    a.getFontFamilyOrNull(R.styleable.AccompanistMdcTextAppearance_android_fontFamily)
                }
                a.hasValue(R.styleable.AccompanistMdcTextAppearance_fontFamily) -> {
                    a.getFontFamilyOrNull(R.styleable.AccompanistMdcTextAppearance_fontFamily)
                }
                // Values below are from frameworks/base attrs.xml
                typeface == 1 -> FontFamily.SansSerif
                typeface == 2 -> FontFamily.Serif
                typeface == 3 -> FontFamily.Monospace
                else -> null
            },
            fontStyle = if ((textStyle and Typeface.ITALIC) != 0) FontStyle.Italic else FontStyle.Normal,
            fontWeight = when {
                textFontWeight in 0..149 -> FontWeight.W100
                textFontWeight in 150..249 -> FontWeight.W200
                textFontWeight in 250..349 -> FontWeight.W300
                textFontWeight in 350..449 -> FontWeight.W400
                textFontWeight in 450..549 -> FontWeight.W500
                textFontWeight in 550..649 -> FontWeight.W600
                textFontWeight in 650..749 -> FontWeight.W700
                textFontWeight in 750..849 -> FontWeight.W800
                textFontWeight in 850..999 -> FontWeight.W900
                // else, check the text style
                (textStyle and Typeface.BOLD) != 0 -> FontWeight.Bold
                else -> null
            },
            fontFeatureSettings = a.getString(R.styleable.AccompanistMdcTextAppearance_android_fontFeatureSettings),
            shadow = run {
                val shadowColor = a.getComposeColor(R.styleable.AccompanistMdcTextAppearance_android_shadowColor)
                if (shadowColor != Color.Unset) {
                    val dx = a.getFloat(R.styleable.AccompanistMdcTextAppearance_android_shadowDx, 0f)
                    val dy = a.getFloat(R.styleable.AccompanistMdcTextAppearance_android_shadowDy, 0f)
                    val rad = a.getFloat(R.styleable.AccompanistMdcTextAppearance_android_shadowRadius, 0f)
                    Shadow(color = shadowColor, offset = Offset(dx, dy), blurRadius = rad.px)
                } else null
            },
            letterSpacing = when {
                a.hasValue(R.styleable.AccompanistMdcTextAppearance_android_letterSpacing) -> {
                    a.getFloat(R.styleable.AccompanistMdcTextAppearance_android_letterSpacing, 0f).em
                }
                else -> TextUnit.Inherit
            }
        )
    }
}

@Composable
private fun readShapeAppearance(
    context: Context,
    @StyleRes id: Int,
    fallbackSize: CornerSize
): CornerBasedShape {
    return context.obtainStyledAttributes(id, R.styleable.AccompanistMdcShapeAppearance).use { a ->
        val defaultCornerSize = a.getCornerSize(
            R.styleable.AccompanistMdcShapeAppearance_cornerSize, fallbackSize)
        val cornerSizeTL = a.getCornerSizeOrNull(
            R.styleable.AccompanistMdcShapeAppearance_cornerSizeTopLeft)
        val cornerSizeTR = a.getCornerSizeOrNull(
            R.styleable.AccompanistMdcShapeAppearance_cornerSizeTopRight)
        val cornerSizeBL = a.getCornerSizeOrNull(
            R.styleable.AccompanistMdcShapeAppearance_cornerSizeBottomLeft)
        val cornerSizeBR = a.getCornerSizeOrNull(
            R.styleable.AccompanistMdcShapeAppearance_cornerSizeBottomRight)

        /**
         * We do not support the individual `cornerFamilyTopLeft`, etc, since Compose only supports
         * one corner type per shape. Therefore we only read the `cornerFamily` attribute.
         */
        when (a.getInt(R.styleable.AccompanistMdcShapeAppearance_cornerFamily, 0)) {
            0 -> {
                RoundedCornerShape(
                        topLeft = cornerSizeTL ?: defaultCornerSize,
                        topRight = cornerSizeTR ?: defaultCornerSize,
                        bottomRight = cornerSizeBR ?: defaultCornerSize,
                        bottomLeft = cornerSizeBL ?: defaultCornerSize
                )
            }
            1 -> {
                CutCornerShape(
                        topLeft = cornerSizeTL ?: defaultCornerSize,
                        topRight = cornerSizeTR ?: defaultCornerSize,
                        bottomRight = cornerSizeBR ?: defaultCornerSize,
                        bottomLeft = cornerSizeBL ?: defaultCornerSize
                )
            }
            else -> throw IllegalArgumentException("Unknown cornerFamily set in ShapeAppearance")
        }
    }
}

private fun Typography.merge(
    h1: TextStyle = TextStyle(),
    h2: TextStyle = TextStyle(),
    h3: TextStyle = TextStyle(),
    h4: TextStyle = TextStyle(),
    h5: TextStyle = TextStyle(),
    h6: TextStyle = TextStyle(),
    subtitle1: TextStyle = TextStyle(),
    subtitle2: TextStyle = TextStyle(),
    body1: TextStyle = TextStyle(),
    body2: TextStyle = TextStyle(),
    button: TextStyle = TextStyle(),
    caption: TextStyle = TextStyle(),
    overline: TextStyle = TextStyle()
) = copy(
    h1 = h1.merge(h1),
    h2 = h2.merge(h2),
    h3 = h3.merge(h3),
    h4 = h4.merge(h4),
    h5 = h5.merge(h5),
    h6 = h6.merge(h6),
    subtitle1 = subtitle1.merge(subtitle1),
    subtitle2 = subtitle2.merge(subtitle2),
    body1 = body1.merge(body1),
    body2 = body2.merge(body2),
    button = button.merge(button),
    caption = caption.merge(caption),
    overline = overline.merge(overline)
)

private val tempTypedValue = ThreadLocal<TypedValue>()

fun TypedArray.getComposeColor(
    index: Int,
    fallbackColor: Color = Color.Unset
): Color = if (hasValue(index)) Color(getColorOrThrow(index)) else fallbackColor

/**
 * Returns the given index as a [FontFamily], or [fallback] if the value can not be coerced to a [FontFamily].
 *
 * @param index index of attribute to retrieve.
 * @param fallback Value to return if the attribute is not defined or cannot be coerced to an [FontFamily].
 */
fun TypedArray.getFontFamily(index: Int, fallback: FontFamily): FontFamily {
    return getFontFamilyOrNull(index) ?: fallback
}

/**
 * Returns the given index as a [FontFamily], or `null` if the value can not be coerced to a [FontFamily].
 *
 * @param index index of attribute to retrieve.
 */
fun TypedArray.getFontFamilyOrNull(index: Int): FontFamily? {
    val tv = tempTypedValue.getOrSet { TypedValue() }
    if (getValue(index, tv) && tv.type == TypedValue.TYPE_STRING) {
        return font(tv.resourceId).asFontFamily()
    }
    return null
}

/**
 * Returns the given index as a [TextUnit], or [fallback] if the value can not be coerced to a [TextUnit].
 *
 * @param index index of attribute to retrieve.
 * @param density the current display density.
 * @param fallback Value to return if the attribute is not defined or cannot be coerced to an [TextUnit].
 */
fun TypedArray.getTextUnit(
    index: Int,
    density: Density,
    fallback: TextUnit = TextUnit.Inherit
): TextUnit = getTextUnitOrNull(index, density) ?: fallback

/**
 * Returns the given index as a [TextUnit], or `null` if the value can not be coerced to a [TextUnit].
 *
 * @param index index of attribute to retrieve.
 * @param density the current display density.
 */
fun TypedArray.getTextUnitOrNull(
    index: Int,
    density: Density
): TextUnit? {
    val tv = tempTypedValue.getOrSet { TypedValue() }
    if (getValue(index, tv) && tv.type == TypedValue.TYPE_DIMENSION) {
        return when (tv.complexUnitCompat) {
            // For SP values, we convert the value directly to an TextUnit.Sp
            TypedValue.COMPLEX_UNIT_SP -> TypedValue.complexToFloat(tv.data).sp
            // For DIP values, we convert the value to an TextUnit.Em (roughly equivalent)
            TypedValue.COMPLEX_UNIT_DIP -> TypedValue.complexToFloat(tv.data).em
            // For another other types, we let the TypedArray flatten to a px value, and
            // we convert it to an Sp based on the current density
            else -> with(density) { getDimension(index, 0f).toSp() }
        }
    }
    return null
}

/**
 * Returns the given index as a [CornerSize], or `null` if the value can not be coerced to a [CornerSize].
 *
 * @param index index of attribute to retrieve.
 */
fun TypedArray.getCornerSizeOrNull(index: Int): CornerSize? {
    val tv = tempTypedValue.getOrSet { TypedValue() }
    if (getValue(index, tv)) {
        return when (tv.type) {
            TypedValue.TYPE_DIMENSION -> {
                when (tv.complexUnitCompat) {
                    // For DIP and PX values, we convert the value to the equivalent
                    TypedValue.COMPLEX_UNIT_DIP -> CornerSize(TypedValue.complexToFloat(tv.data).dp)
                    TypedValue.COMPLEX_UNIT_PX -> CornerSize(TypedValue.complexToFloat(tv.data).px)
                    // For another other dim types, we let the TypedArray flatten to a px value
                    else -> CornerSize(getDimensionPixelSize(index, 0).px)
                }
            }
            TypedValue.TYPE_FRACTION -> CornerSize(tv.getFraction(1f, 1f))
            else -> null
        }
    }
    return null
}

/**
 * Returns the given index as a [CornerSize], or [fallback] if the value can not be coerced to a [CornerSize].
 *
 * @param index index of attribute to retrieve.
 * @param fallback Value to return if the attribute is not defined or cannot be coerced to an [CornerSize].
 */
fun TypedArray.getCornerSize(index: Int, fallback: CornerSize): CornerSize {
    return getCornerSizeOrNull(index) ?: fallback
}

/**
 * A workaround since [TypedValue.getComplexUnit] is API 22+
 */
private inline val TypedValue.complexUnitCompat
    get() = when {
        Build.VERSION.SDK_INT > 22 -> complexUnit
        else -> TypedValue.COMPLEX_UNIT_MASK and (data shr TypedValue.COMPLEX_UNIT_SHIFT)
    }

/**
 * This is gross, but we need a way to check for theme equality. Theme does not implement
 * `equals()` or `hashCode()`, but it does have a hidden method called `getKey()`.
 *
 * The cost of this reflective invoke is a lot cheaper than the full theme read which currently
 * happens on every re-composition.
 */
private inline val Resources.Theme.key: Any?
    get() = try {
        sThemeGetKeyMethod.invoke(this)
    } catch (e: ReflectiveOperationException) {
        Log.i("MaterialThemeFromMdcTheme", "Failed to retrieve theme key", e)
    }

private val sThemeGetKeyMethod: Method by lazy {
    Resources.Theme::class.java.getDeclaredMethod("getKey").apply {
        isAccessible = true
    }
}
