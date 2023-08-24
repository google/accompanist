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

package com.google.accompanist.themeadapter.material3

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.Size
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
import com.google.accompanist.themeadapter.core.FontFamilyWithWeight
import com.google.accompanist.themeadapter.material3.test.R
import org.junit.Assert.assertEquals
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
abstract class BaseMdc3ThemeTest<T : AppCompatActivity>(
    activityClass: Class<T>
) {
    @get:Rule
    val composeTestRule = createAndroidComposeRule(activityClass)

    @Test
    fun colors() = composeTestRule.setContent {
        Mdc3Theme {
            val colorScheme = MaterialTheme.colorScheme

            assertEquals(colorResource(R.color.aquamarine), colorScheme.primary)
            assertEquals(colorResource(R.color.pale_turquoise), colorScheme.onPrimary)
            assertEquals(colorResource(R.color.midnight_blue), colorScheme.inversePrimary)
            assertEquals(colorResource(R.color.royal_blue), colorScheme.primaryContainer)
            assertEquals(colorResource(R.color.steel_blue), colorScheme.onPrimaryContainer)

            assertEquals(colorResource(R.color.dodger_blue), colorScheme.secondary)
            assertEquals(colorResource(R.color.dark_golden_rod), colorScheme.onSecondary)
            assertEquals(colorResource(R.color.peru), colorScheme.secondaryContainer)
            assertEquals(colorResource(R.color.blue_violet), colorScheme.onSecondaryContainer)

            assertEquals(colorResource(R.color.dark_orchid), colorScheme.tertiary)
            assertEquals(colorResource(R.color.slate_gray), colorScheme.onTertiary)
            assertEquals(colorResource(R.color.gray), colorScheme.tertiaryContainer)
            assertEquals(colorResource(R.color.spring_green), colorScheme.onTertiaryContainer)

            assertEquals(colorResource(R.color.medium_spring_green), colorScheme.background)
            assertEquals(colorResource(R.color.navy), colorScheme.onBackground)

            assertEquals(colorResource(R.color.dark_blue), colorScheme.surface)
            assertEquals(colorResource(R.color.light_coral), colorScheme.onSurface)
            assertEquals(colorResource(R.color.salmon), colorScheme.surfaceVariant)
            assertEquals(colorResource(R.color.dark_salmon), colorScheme.onSurfaceVariant)
            assertEquals(colorResource(R.color.indian_red), colorScheme.surfaceTint)
            assertEquals(colorResource(R.color.light_salmon), colorScheme.inverseSurface)
            assertEquals(colorResource(R.color.orchid), colorScheme.inverseOnSurface)

            assertEquals(colorResource(R.color.violet), colorScheme.outline)
            // TODO: MDC-Android doesn't include outlineVariant yet, add when available

            assertEquals(colorResource(R.color.beige), colorScheme.error)
            assertEquals(colorResource(R.color.white_smoke), colorScheme.onError)
            assertEquals(colorResource(R.color.olive), colorScheme.errorContainer)
            assertEquals(colorResource(R.color.olive_drab), colorScheme.onErrorContainer)

            assertEquals(colorResource(R.color.crimson), colorScheme.scrim)

            // Mdc3Theme updates the LocalContentColor to match the calculated onBackground
            assertEquals(colorResource(R.color.navy), LocalContentColor.current)
        }
    }

    @Test
    fun shapes() = composeTestRule.setContent {
        Mdc3Theme {
            val shapes = MaterialTheme.shapes
            val density = LocalDensity.current

            shapes.extraSmall.run {
                assertTrue(this is RoundedCornerShape)
                assertEquals(4f, topStart.toPx(density))
                assertEquals(9.dp.scaleToPx(density), topEnd.toPx(density))
                assertEquals(5f, bottomEnd.toPx(density))
                assertEquals(3.dp.scaleToPx(density), bottomStart.toPx(density))
            }
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
                assertEquals(16.dp.scaleToPx(density), topStart.toPx(density))
                assertEquals(16.dp.scaleToPx(density), topEnd.toPx(density))
                assertEquals(16.dp.scaleToPx(density), bottomEnd.toPx(density))
                assertEquals(16.dp.scaleToPx(density), bottomStart.toPx(density))
            }
            shapes.extraLarge.run {
                assertTrue(this is RoundedCornerShape)
                assertEquals(28.dp.scaleToPx(density), topStart.toPx(density))
                assertEquals(28.dp.scaleToPx(density), topEnd.toPx(density))
                assertEquals(28.dp.scaleToPx(density), bottomEnd.toPx(density))
                assertEquals(28.dp.scaleToPx(density), bottomStart.toPx(density))
            }
        }
    }

    @Test
    fun type() = composeTestRule.setContent {
        Mdc3Theme {
            val typography = MaterialTheme.typography
            val density = LocalDensity.current

            val rubik300 = Font(R.font.rubik_300).toFontFamily()
            val rubik400 = Font(R.font.rubik_400).toFontFamily()
            val rubik500 = Font(R.font.rubik_500).toFontFamily()
            val rubik700 = Font(R.font.rubik_700).toFontFamily()
            val sansSerif = FontFamilyWithWeight(FontFamily.SansSerif)
            val sansSerifLight = FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Light)
            val sansSerifBlack = FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Black)
            val serif = FontFamilyWithWeight(FontFamily.Serif)
            val cursive = FontFamilyWithWeight(FontFamily.Cursive)
            val monospace = FontFamilyWithWeight(FontFamily.Monospace)

            typography.displayLarge.run {
                assertTextUnitEquals(97.54.sp, fontSize, density)
                assertTextUnitEquals((-0.0015).em, letterSpacing, density)
                assertEquals(rubik300, fontFamily)
            }

            assertNotNull(typography.displayMedium.shadow)
            typography.displayMedium.shadow!!.run {
                assertEquals(colorResource(R.color.olive_drab), color)
                assertEquals(4.43f, offset.x)
                assertEquals(8.19f, offset.y)
                assertEquals(2.13f, blurRadius)
            }

            typography.displaySmall.run {
                assertEquals(sansSerif.fontFamily, fontFamily)
                assertEquals(sansSerif.weight, fontWeight)
            }

            typography.headlineLarge.run {
                assertEquals(sansSerifLight.fontFamily, fontFamily)
                assertEquals(sansSerifLight.weight, fontWeight)
            }

            typography.headlineMedium.run {
                assertEquals(sansSerifLight.fontFamily, fontFamily)
                assertEquals(sansSerifLight.weight, fontWeight)
            }

            typography.headlineSmall.run {
                assertEquals(sansSerifBlack.fontFamily, fontFamily)
                assertEquals(sansSerifBlack.weight, fontWeight)
            }

            typography.titleLarge.run {
                assertEquals(serif.fontFamily, fontFamily)
                assertEquals(serif.weight, fontWeight)
            }

            typography.titleMedium.run {
                assertEquals(monospace.fontFamily, fontFamily)
                assertEquals(monospace.weight, fontWeight)
                assertTextUnitEquals(0.em, letterSpacing, density)
            }

            typography.titleSmall.run {
                assertEquals(FontFamily.SansSerif, fontFamily)
            }

            typography.bodyLarge.run {
                assertTextUnitEquals(16.26.sp, fontSize, density)
                assertTextUnitEquals(0.005.em, letterSpacing, density)
                assertEquals(rubik400, fontFamily)
                assertNull(shadow)
            }

            typography.bodyMedium.run {
                assertEquals(cursive.fontFamily, fontFamily)
                assertEquals(cursive.weight, fontWeight)
            }

            typography.bodySmall.run {
                assertEquals(FontFamily.SansSerif, fontFamily)
                assertTextUnitEquals(0.04.em, letterSpacing, density)
            }

            typography.labelLarge.run {
                assertEquals(rubik500, fontFamily)
            }

            typography.labelMedium.run {
                assertEquals(rubik700, fontFamily)
            }

            typography.labelSmall.run {
                assertEquals(FontFamily.SansSerif, fontFamily)
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
