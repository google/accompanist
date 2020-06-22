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

import androidx.test.filters.MediumTest
import androidx.ui.core.DensityAmbient
import androidx.ui.foundation.shape.corner.CornerSize
import androidx.ui.foundation.shape.corner.CutCornerShape
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.material.MaterialTheme
import androidx.ui.res.colorResource
import androidx.ui.test.android.AndroidComposeTestRule
import androidx.ui.text.font.asFontFamily
import androidx.ui.text.font.font
import androidx.ui.unit.Density
import androidx.ui.unit.Dp
import androidx.ui.unit.PxSize
import androidx.ui.unit.TextUnit
import androidx.ui.unit.dp
import androidx.ui.unit.em
import androidx.ui.unit.sp
import dev.chrisbanes.accompanist.mdctheme.test.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@MediumTest
@RunWith(JUnit4::class)
class MaterialThemeTest {
    @get:Rule
    val composeTestRule = AndroidComposeTestRule<MdcActivity>()

    @get:Rule
    val notMdcComposeTestRule = AndroidComposeTestRule<NotMdcActivity>()

    @Test(expected = IllegalArgumentException::class)
    fun isNotMaterialTheme() = notMdcComposeTestRule.setContent {
        MaterialThemeFromMdcTheme {
            // Nothing to do here, exception should be thrown
        }
    }

    @Test
    fun colors() = composeTestRule.setContent {
        MaterialThemeFromMdcTheme {
            val color = MaterialTheme.colors

            assertEquals(colorResource(R.color.Aquamarine), color.primary)
            assertEquals(colorResource(R.color.RoyalBlue), color.primaryVariant)
            assertEquals(colorResource(R.color.MidnightBlue), color.onPrimary)

            assertEquals(colorResource(R.color.DarkGoldenrod), color.secondary)
            assertEquals(colorResource(R.color.BlueViolet), color.secondaryVariant)
            assertEquals(colorResource(R.color.SlateGray), color.onSecondary)

            assertEquals(colorResource(R.color.MediumSpringGreen), color.surface)
            assertEquals(colorResource(R.color.Navy), color.onSurface)

            assertEquals(colorResource(R.color.DarkSalmon), color.error)
            assertEquals(colorResource(R.color.Beige), color.onError)

            assertEquals(colorResource(R.color.IndianRed), color.background)
            assertEquals(colorResource(R.color.Orchid), color.onBackground)
        }
    }

    @Test
    fun shapes() = composeTestRule.setContent {
        MaterialThemeFromMdcTheme {
            val shapes = MaterialTheme.shapes
            val density = DensityAmbient.current

            shapes.small.run {
                assertTrue(this is CutCornerShape)
                assertEquals(4f, topLeft.toPx(density))
                assertEquals(9.dp.scaleToPx(density), topRight.toPx(density))
                assertEquals(5f, bottomRight.toPx(density))
                assertEquals(3.dp.scaleToPx(density), bottomLeft.toPx(density))
            }
            shapes.medium.run {
                assertTrue(this is RoundedCornerShape)
                assertEquals(12.dp.scaleToPx(density), topLeft.toPx(density))
                assertEquals(12.dp.scaleToPx(density), topRight.toPx(density))
                assertEquals(12.dp.scaleToPx(density), bottomRight.toPx(density))
                assertEquals(12.dp.scaleToPx(density), bottomLeft.toPx(density))
            }
            shapes.large.run {
                assertTrue(this is CutCornerShape)
                assertEquals(0f, topLeft.toPx(density))
                assertEquals(0f, topRight.toPx(density))
                assertEquals(0f, bottomRight.toPx(density))
                assertEquals(0f, bottomLeft.toPx(density))
            }
        }
    }

    @Test
    fun type() = composeTestRule.setContent {
        MaterialThemeFromMdcTheme {
            val typography = MaterialTheme.typography
            val density = DensityAmbient.current

            val rubik300 = font(R.font.rubik_300).asFontFamily()
            val rubik400 = font(R.font.rubik_400).asFontFamily()
            val rubik500 = font(R.font.rubik_500).asFontFamily()

            typography.h1.run {
                assertTextUnitEquals(97.54.sp, fontSize, density)
                assertTextUnitEquals((-0.0015).em, letterSpacing, density)
                assertEquals(rubik300, fontFamily)
            }

            assertNotNull(typography.h2.shadow)
            typography.h2.shadow!!.run {
                assertEquals(colorResource(R.color.OliveDrab), color)
                assertEquals(4.43f, offset.dx)
                assertEquals(8.19f, offset.dy)
                assertEquals(2.13f, blurRadius)
            }

            typography.body1.run {
                assertTextUnitEquals(16.26.sp, fontSize, density)
                assertTextUnitEquals(0.005.em, letterSpacing, density)
                assertEquals(rubik400, fontFamily)
                assertNull(shadow)
            }

            typography.button.run {
                assertEquals(rubik500, fontFamily)
            }
        }
    }
}

private fun Dp.scaleToPx(density: Density): Float {
    val dp = this
    return with(density) { dp.toPx().value }
}

private fun assertTextUnitEquals(expected: TextUnit, actual: TextUnit, density: Density) {
    if (expected.javaClass == actual.javaClass) {
        // If the expected and actual are the same type, compare the raw values with a
        // delta to account for float inaccuracy
        assertEquals(expected.value, actual.value, 0.001f)
    } else {
        // Otherwise we need to flatten to a px to compare the values. Again using a
        // delta to account for float inaccuracy
        with(density) { assertEquals(expected.toPx().value, actual.toPx().value, 0.001f) }
    }
}

private fun CornerSize.toPx(density: Density) = toPx(PxSize.Companion.UnspecifiedSize, density)
