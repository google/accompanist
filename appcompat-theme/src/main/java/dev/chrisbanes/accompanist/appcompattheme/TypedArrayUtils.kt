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

package dev.chrisbanes.accompanist.appcompattheme

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import androidx.annotation.StyleRes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.asFontFamily
import androidx.compose.ui.text.font.font
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.use
import kotlin.concurrent.getOrSet

internal fun textStyleFromTextAppearance(
    context: Context,
    density: Density,
    @StyleRes id: Int,
    setTextColors: Boolean
): TextStyle {
    return context.obtainStyledAttributes(id, R.styleable.AppCompatThemeAdapterTextAppearance).use { a ->
        val textStyle = a.getInt(R.styleable.AppCompatThemeAdapterTextAppearance_android_textStyle, -1)
        val textFontWeight = a.getInt(R.styleable.AppCompatThemeAdapterTextAppearance_android_textFontWeight, -1)
        val typeface = a.getInt(R.styleable.AppCompatThemeAdapterTextAppearance_android_typeface, -1)

        // TODO read and expand android:fontVariationSettings.
        // Variable fonts are not supported in Compose yet

        // FYI, this only works with static font files in assets
        val fontFamily: FontFamilyWithWeight? = a.getFontFamilyOrNull(
            R.styleable.AppCompatThemeAdapterTextAppearance_fontFamily
        ) ?: a.getFontFamilyOrNull(R.styleable.AppCompatThemeAdapterTextAppearance_android_fontFamily)

        TextStyle(
            color = when {
                setTextColors -> {
                    a.getComposeColor(R.styleable.AppCompatThemeAdapterTextAppearance_android_textColor)
                }
                else -> Color.Unspecified
            },
            fontSize = a.getTextUnit(R.styleable.AppCompatThemeAdapterTextAppearance_android_textSize, density),
            lineHeight = run {
                a.getTextUnitOrNull(R.styleable.AppCompatThemeAdapterTextAppearance_lineHeight, density)
                    ?: a.getTextUnitOrNull(R.styleable.AppCompatThemeAdapterTextAppearance_android_lineHeight, density)
                    ?: TextUnit.Unspecified
            },
            fontFamily = when {
                fontFamily != null -> fontFamily.fontFamily
                // Values below are from frameworks/base attrs.xml
                typeface == 1 -> FontFamily.SansSerif
                typeface == 2 -> FontFamily.Serif
                typeface == 3 -> FontFamily.Monospace
                else -> null
            },
            fontStyle = when {
                (textStyle and Typeface.ITALIC) != 0 -> FontStyle.Italic
                else -> FontStyle.Normal
            },
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
                // Else, check the text style for bold
                (textStyle and Typeface.BOLD) != 0 -> FontWeight.Bold
                // Else, the font family might have an implicit weight (san-serif-light, etc)
                fontFamily != null -> fontFamily.weight
                else -> null
            },
            fontFeatureSettings = a.getString(R.styleable.AppCompatThemeAdapterTextAppearance_android_fontFeatureSettings),
            shadow = run {
                val shadowColor = a.getComposeColor(R.styleable.AppCompatThemeAdapterTextAppearance_android_shadowColor)
                if (shadowColor != Color.Unspecified) {
                    val dx = a.getFloat(R.styleable.AppCompatThemeAdapterTextAppearance_android_shadowDx, 0f)
                    val dy = a.getFloat(R.styleable.AppCompatThemeAdapterTextAppearance_android_shadowDy, 0f)
                    val rad = a.getFloat(R.styleable.AppCompatThemeAdapterTextAppearance_android_shadowRadius, 0f)
                    Shadow(color = shadowColor, offset = Offset(dx, dy), blurRadius = rad)
                } else null
            },
            letterSpacing = when {
                a.hasValue(R.styleable.AppCompatThemeAdapterTextAppearance_android_letterSpacing) -> {
                    a.getFloat(R.styleable.AppCompatThemeAdapterTextAppearance_android_letterSpacing, 0f).em
                }
                else -> TextUnit.Unspecified
            }
        )
    }
}

private val tempTypedValue = ThreadLocal<TypedValue>()

internal fun TypedArray.getComposeColor(
    index: Int,
    fallbackColor: Color = Color.Unspecified
): Color = if (hasValue(index)) Color(getColorOrThrow(index)) else fallbackColor

/**
 * Returns the given index as a [FontFamily] and [FontWeight],
 * or [fallback] if the value can not be coerced to a [FontFamily].
 *
 * @param index index of attribute to retrieve.
 * @param fallback Value to return if the attribute is not defined or cannot be coerced to a
 * [FontFamily].
 */
internal fun TypedArray.getFontFamily(index: Int, fallback: FontFamily): FontFamilyWithWeight {
    return getFontFamilyOrNull(index) ?: FontFamilyWithWeight(fallback)
}

/**
 * Returns the given index as a [FontFamily] and [FontWeight],
 * or `null` if the value can not be coerced to a [FontFamily].
 *
 * @param index index of attribute to retrieve.
 */
internal fun TypedArray.getFontFamilyOrNull(index: Int): FontFamilyWithWeight? {
    val tv = tempTypedValue.getOrSet { TypedValue() }
    if (getValue(index, tv) && tv.type == TypedValue.TYPE_STRING) {
        return when (tv.string) {
            "sans-serif" -> FontFamilyWithWeight(FontFamily.SansSerif)
            "sans-serif-thin" -> FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Thin)
            "sans-serif-light" -> FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Light)
            "sans-serif-medium" -> FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Medium)
            "sans-serif-black" -> FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Black)
            "serif" -> FontFamilyWithWeight(FontFamily.Serif)
            "cursive" -> FontFamilyWithWeight(FontFamily.Cursive)
            "monospace" -> FontFamilyWithWeight(FontFamily.Monospace)
            // TODO: Compose does not expose a FontFamily for all strings yet
            else -> {
                if (tv.resourceId != 0 && tv.string.startsWith("res/font/")) {
                    // If there's a resource ID and the string starts with res/font, it's probably a @font resource
                    FontFamilyWithWeight(font(tv.resourceId).asFontFamily())
                } else {
                    null
                }
            }
        }
    }
    return null
}

internal data class FontFamilyWithWeight(
    val fontFamily: FontFamily,
    val weight: FontWeight = FontWeight.Normal
)

/**
 * Returns the given index as a [TextUnit], or [fallback] if the value can not be coerced to
 * a [TextUnit].
 *
 * @param index index of attribute to retrieve.
 * @param density the current display density.
 * @param fallback Value to return if the attribute is not defined or cannot be coerced to
 * a [TextUnit].
 */
internal fun TypedArray.getTextUnit(
    index: Int,
    density: Density,
    fallback: TextUnit = TextUnit.Unspecified
): TextUnit = getTextUnitOrNull(index, density) ?: fallback

/**
 * Returns the given index as a [TextUnit], or `null` if the value can not be coerced to
 * a [TextUnit].
 *
 * @param index index of attribute to retrieve.
 * @param density the current display density.
 */
internal fun TypedArray.getTextUnitOrNull(
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
 * A workaround since [TypedValue.getComplexUnit] is API 22+
 */
private inline val TypedValue.complexUnitCompat
    get() = when {
        Build.VERSION.SDK_INT > 22 -> complexUnit
        else -> TypedValue.COMPLEX_UNIT_MASK and (data shr TypedValue.COMPLEX_UNIT_SHIFT)
    }
