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

@file:Suppress("DEPRECATION")

package com.google.accompanist.themeadapter.material

import android.view.ContextThemeWrapper
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.test.filters.SdkSuppress
import com.google.accompanist.themeadapter.core.FontFamilyWithWeight
import com.google.accompanist.themeadapter.material.test.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Class which contains the majority of the tests. This class is extended
 * in both the `androidTest` and `test` source sets for setup of the relevant
 * test runner.
 */
abstract class BaseMdcThemeTest<T : AppCompatActivity>(
    activityClass: Class<T>
) {
    @get:Rule
    val composeTestRule = createAndroidComposeRule(activityClass)

    @Test
    fun colors() = composeTestRule.setContent {
        MdcTheme {
            val color = MaterialTheme.colors

            assertEquals(colorResource(R.color.aquamarine), color.primary)
            assertEquals(colorResource(R.color.royal_blue), color.primaryVariant)
            assertEquals(colorResource(R.color.midnight_blue), color.onPrimary)

            assertEquals(colorResource(R.color.dark_golden_rod), color.secondary)
            assertEquals(colorResource(R.color.slate_gray), color.onSecondary)
            assertEquals(colorResource(R.color.blue_violet), color.secondaryVariant)

            assertEquals(colorResource(R.color.spring_green), color.surface)
            assertEquals(colorResource(R.color.navy), color.onSurface)

            assertEquals(colorResource(R.color.dark_salmon), color.error)
            assertEquals(colorResource(R.color.beige), color.onError)

            assertEquals(colorResource(R.color.light_coral), color.background)
            assertEquals(colorResource(R.color.orchid), color.onBackground)

            // MdcTheme updates the LocalContentColor to match the calculated onBackground
            assertEquals(colorResource(R.color.orchid), LocalContentColor.current)
        }
    }

    @Test
    fun shapes() = composeTestRule.setContent {
        MdcTheme {
            val shapes = MaterialTheme.shapes
            val density = LocalDensity.current

            shapes.small.run {
                assertTrue(this is CutCornerShape)
                assertEquals(4f, topStart.toPx(density))
                assertEquals(9.dp.scaleToPx(density), topEnd.toPx(density))
                assertEquals(5f, bottomEnd.toPx(density))
                assertEquals(3.dp.scaleToPx(density), bottomStart.toPx(density))
            }
            shapes.medium.run {
                assertTrue(this is RoundedCornerShape)
                assertEquals(12.dp.scaleToPx(density), topStart.toPx(density))
                assertEquals(12.dp.scaleToPx(density), topEnd.toPx(density))
                assertEquals(12.dp.scaleToPx(density), bottomEnd.toPx(density))
                assertEquals(12.dp.scaleToPx(density), bottomStart.toPx(density))
            }
            shapes.large.run {
                assertTrue(this is CutCornerShape)
                assertEquals(0f, topStart.toPx(density))
                assertEquals(0f, topEnd.toPx(density))
                assertEquals(0f, bottomEnd.toPx(density))
                assertEquals(0f, bottomStart.toPx(density))
            }
        }
    }

    @Test
    fun type() = composeTestRule.setContent {
        MdcTheme {
            val typography = MaterialTheme.typography
            val density = LocalDensity.current

            val rubik300 = Font(R.font.rubik_300).toFontFamily()
            val rubik400 = Font(R.font.rubik_400).toFontFamily()
            val rubik500 = Font(R.font.rubik_500).toFontFamily()
            val sansSerif = FontFamilyWithWeight(FontFamily.SansSerif)
            val sansSerifLight = FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Light)
            val sansSerifBlack = FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Black)
            val serif = FontFamilyWithWeight(FontFamily.Serif)
            val cursive = FontFamilyWithWeight(FontFamily.Cursive)
            val monospace = FontFamilyWithWeight(FontFamily.Monospace)

            typography.h1.run {
                assertTextUnitEquals(97.54.sp, fontSize, density)
                assertTextUnitEquals((-0.0015).em, letterSpacing, density)
                assertEquals(rubik300, fontFamily)
            }

            assertNotNull(typography.h2.shadow)
            typography.h2.shadow!!.run {
                assertEquals(colorResource(R.color.olive_drab), color)
                assertEquals(4.43f, offset.x)
                assertEquals(8.19f, offset.y)
                assertEquals(2.13f, blurRadius)
            }

            typography.h3.run {
                assertEquals(sansSerif.fontFamily, fontFamily)
                assertEquals(sansSerif.weight, fontWeight)
            }

            typography.h4.run {
                assertEquals(sansSerifLight.fontFamily, fontFamily)
                assertEquals(sansSerifLight.weight, fontWeight)
            }

            typography.h5.run {
                assertEquals(sansSerifBlack.fontFamily, fontFamily)
                assertEquals(sansSerifBlack.weight, fontWeight)
            }

            typography.h6.run {
                assertEquals(serif.fontFamily, fontFamily)
                assertEquals(serif.weight, fontWeight)
            }

            typography.body1.run {
                assertTextUnitEquals(16.26.sp, fontSize, density)
                assertTextUnitEquals(0.005.em, letterSpacing, density)
                assertEquals(rubik400, fontFamily)
                assertNull(shadow)
            }

            typography.body2.run {
                assertEquals(cursive.fontFamily, fontFamily)
                assertEquals(cursive.weight, fontWeight)
            }

            typography.subtitle1.run {
                assertEquals(monospace.fontFamily, fontFamily)
                assertEquals(monospace.weight, fontWeight)
                assertTextUnitEquals(0.em, letterSpacing, density)
            }

            typography.subtitle2.run {
                assertEquals(FontFamily.SansSerif, fontFamily)
            }

            typography.button.run {
                assertEquals(rubik500, fontFamily)
            }

            typography.caption.run {
                assertEquals(FontFamily.SansSerif, fontFamily)
                assertTextUnitEquals(0.04.em, letterSpacing, density)
            }

            typography.overline.run {
                assertEquals(FontFamily.SansSerif, fontFamily)
            }
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 23) // XML font families with >1 fonts are only supported on API 23+
    fun type_rubik_family_api23() = composeTestRule.setContent {
        val rubik = FontFamily(
            Font(R.font.rubik_300, FontWeight.W300),
            Font(R.font.rubik_400, FontWeight.W400),
            Font(R.font.rubik_500, FontWeight.W500),
            Font(R.font.rubik_700, FontWeight.W700),
        )
        WithThemeOverlay(R.style.ThemeOverlay_MdcThemeTest_DefaultFontFamily_Rubik) {
            MdcTheme(setDefaultFontFamily = true) {
                MaterialTheme.typography.assertFontFamilies(expected = rubik)
            }
        }
        WithThemeOverlay(R.style.ThemeOverlay_MdcThemeTest_DefaultAndroidFontFamily_Rubik) {
            MdcTheme(setDefaultFontFamily = true) {
                MaterialTheme.typography.assertFontFamilies(expected = rubik)
            }
        }
    }

    @Test
    fun type_rubik_fixed400() = composeTestRule.setContent {
        val rubik400 = Font(R.font.rubik_400, FontWeight.W400).toFontFamily()
        WithThemeOverlay(R.style.ThemeOverlay_MdcThemeTest_DefaultFontFamily_Rubik400) {
            MdcTheme(setDefaultFontFamily = true) {
                MaterialTheme.typography.assertFontFamilies(expected = rubik400)
            }
        }
        WithThemeOverlay(R.style.ThemeOverlay_MdcThemeTest_DefaultAndroidFontFamily_Rubik400) {
            MdcTheme(setDefaultFontFamily = true) {
                MaterialTheme.typography.assertFontFamilies(expected = rubik400)
            }
        }
    }

    @Test
    fun type_rubik_fixed700_withTextAppearances() = composeTestRule.setContent {
        val rubik700 = Font(R.font.rubik_700, FontWeight.W700).toFontFamily()
        WithThemeOverlay(
            R.style.ThemeOverlay_MdcThemeTest_DefaultFontFamilies_Rubik700_WithTextAppearances
        ) {
            MdcTheme {
                MaterialTheme.typography.assertFontFamilies(
                    expected = rubik700,
                    notEquals = true
                )
            }
        }
    }
}

private fun Dp.scaleToPx(density: Density): Float {
    val dp = this
    return with(density) { dp.toPx() }
}

private fun assertTextUnitEquals(expected: TextUnit, actual: TextUnit, density: Density) {
    if (expected.javaClass == actual.javaClass) {
        // If the expected and actual are the same type, compare the raw values with a
        // delta to account for float inaccuracy
        assertEquals(expected.value, actual.value, 0.001f)
    } else {
        // Otherwise we need to flatten to a px to compare the values. Again using a
        // delta to account for float inaccuracy
        with(density) { assertEquals(expected.toPx(), actual.toPx(), 0.001f) }
    }
}

private fun CornerSize.toPx(density: Density) = toPx(Size.Unspecified, density)

internal fun Typography.assertFontFamilies(
    expected: FontFamily,
    notEquals: Boolean = false
) {
    if (notEquals) assertNotEquals(expected, h1.fontFamily) else assertEquals(expected, h1.fontFamily)
    if (notEquals) assertNotEquals(expected, h2.fontFamily) else assertEquals(expected, h2.fontFamily)
    if (notEquals) assertNotEquals(expected, h3.fontFamily) else assertEquals(expected, h3.fontFamily)
    if (notEquals) assertNotEquals(expected, h4.fontFamily) else assertEquals(expected, h4.fontFamily)
    if (notEquals) assertNotEquals(expected, h5.fontFamily) else assertEquals(expected, h5.fontFamily)
    if (notEquals) assertNotEquals(expected, h6.fontFamily) else assertEquals(expected, h6.fontFamily)
    if (notEquals) assertNotEquals(expected, subtitle1.fontFamily) else assertEquals(expected, subtitle1.fontFamily)
    if (notEquals) assertNotEquals(expected, subtitle2.fontFamily) else assertEquals(expected, subtitle2.fontFamily)
    if (notEquals) assertNotEquals(expected, body1.fontFamily) else assertEquals(expected, body1.fontFamily)
    if (notEquals) assertNotEquals(expected, body2.fontFamily) else assertEquals(expected, body2.fontFamily)
    if (notEquals) assertNotEquals(expected, button.fontFamily) else assertEquals(expected, button.fontFamily)
    if (notEquals) assertNotEquals(expected, caption.fontFamily) else assertEquals(expected, caption.fontFamily)
    if (notEquals) assertNotEquals(expected, overline.fontFamily) else assertEquals(expected, overline.fontFamily)
}

/**
 * Function which applies an Android theme overlay to the current context.
 */
@Composable
fun WithThemeOverlay(
    @StyleRes themeOverlayId: Int,
    content: @Composable () -> Unit,
) {
    val themedContext = ContextThemeWrapper(LocalContext.current, themeOverlayId)
    CompositionLocalProvider(LocalContext provides themedContext, content = content)
}
