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

import android.annotation.SuppressLint
import android.content.res.Resources
import android.content.res.TypedArray
import android.os.Build
import android.util.TypedValue
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.core.content.res.FontResourcesParserCompat
import androidx.core.content.res.getColorOrThrow
import kotlin.concurrent.getOrSet

private val tempTypedValue = ThreadLocal<TypedValue>()

internal fun TypedArray.getComposeColor(
    index: Int,
    fallbackColor: Color = Color.Unspecified
): Color = if (hasValue(index)) Color(getColorOrThrow(index)) else fallbackColor

/**
 * Returns the given index as a [FontFamily] and [FontWeight],
 * or `null` if the value can not be coerced to a [FontFamily].
 *
 * @param index index of attribute to retrieve.
 */
internal fun TypedArray.getFontFamilyOrNull(index: Int): FontFamilyWithWeight? {
    val tv = tempTypedValue.getOrSet(::TypedValue)
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
                // If there's a resource ID and the string starts with res/font,
                // it's probably a @font resource
                if (tv.resourceId != 0 && tv.string.startsWith("res/font")) {
                    // If we're running on API 23+ and the resource is an XML, we can parse
                    // the fonts into a full FontFamily.
                    if (Build.VERSION.SDK_INT >= 23 && tv.string.endsWith(".xml")) {
                        resources.parseXmlFontFamily(tv.resourceId)?.let(::FontFamilyWithWeight)
                    } else {
                        // Otherwise we just load it as a single font
                        FontFamilyWithWeight(Font(tv.resourceId).toFontFamily())
                    }
                } else null
            }
        }
    }
    return null
}

@SuppressLint("RestrictedApi") // FontResourcesParserCompat.*
@RequiresApi(23) // XML font families with >1 fonts are only supported on API 23+
private fun Resources.parseXmlFontFamily(resourceId: Int): FontFamily? {
    val parser = getXml(resourceId)

    // Can't use {} since XmlResourceParser is AutoCloseable, not Closeable
    @Suppress("ConvertTryFinallyToUseCall")
    try {
        val result = FontResourcesParserCompat.parse(parser, this)
        if (result is FontResourcesParserCompat.FontFamilyFilesResourceEntry) {
            val fonts = result.entries.map { font ->
                Font(
                    resId = font.resourceId,
                    weight = fontWeightOf(font.weight),
                    style = if (font.isItalic) FontStyle.Italic else FontStyle.Normal
                )
            }
            return FontFamily(fonts)
        }
    } finally {
        parser.close()
    }
    return null
}

private fun fontWeightOf(weight: Int): FontWeight = when (weight) {
    in 0..149 -> FontWeight.W100
    in 150..249 -> FontWeight.W200
    in 250..349 -> FontWeight.W300
    in 350..449 -> FontWeight.W400
    in 450..549 -> FontWeight.W500
    in 550..649 -> FontWeight.W600
    in 650..749 -> FontWeight.W700
    in 750..849 -> FontWeight.W800
    in 850..999 -> FontWeight.W900
    // Else, we use the 'normal' weight
    else -> FontWeight.W400
}

internal data class FontFamilyWithWeight(
    val fontFamily: FontFamily,
    val weight: FontWeight = FontWeight.Normal
)
