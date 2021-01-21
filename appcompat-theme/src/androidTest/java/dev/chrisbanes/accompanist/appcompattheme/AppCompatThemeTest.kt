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
import android.view.ContextThemeWrapper
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.asFontFamily
import androidx.compose.ui.text.font.font
import androidx.compose.ui.text.font.fontFamily
import androidx.test.filters.LargeTest
import dev.chrisbanes.accompanist.appcompattheme.test.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@LargeTest
@RunWith(Parameterized::class)
class AppCompatThemeTest<T : AppCompatActivity>(activityClass: Class<T>) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun activities() = listOf(
            DarkAppCompatActivity::class.java,
            LightAppCompatActivity::class.java
        )
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule(activityClass)

    @Test
    fun colors() = composeTestRule.setContent {
        AppCompatTheme {
            val color = MaterialTheme.colors

            assertEquals(colorResource(R.color.aquamarine), color.primary)
            // By default, onSecondary is calculated to the highest contrast of black/white
            // against primary
            assertEquals(Color.Black, color.onPrimary)
            // primaryVariant == colorPrimaryDark
            assertEquals(colorResource(R.color.royal_blue), color.primaryVariant)

            assertEquals(colorResource(R.color.dark_golden_rod), color.secondary)
            // By default, onSecondary is calculated to the highest contrast of black/white
            // against secondary
            assertEquals(Color.Black, color.onSecondary)
            // Assert that secondaryVariant == secondary
            assertEquals(colorResource(R.color.dark_golden_rod), color.secondaryVariant)

            assertEquals(colorResource(R.color.dark_salmon), color.error)
            // onError is calculated to the highest contrast of black/white against error
            assertEquals(Color.Black, color.onError)

            assertEquals(colorResource(R.color.light_coral), color.background)
            // By default, onBackground is calculated to the highest contrast of black/white
            // against background
            assertEquals(Color.Black, color.onBackground)
        }
    }

    @Test
    fun type_rubik_family() = composeTestRule.setContent {
        val rubik = fontFamily(
            font(R.font.rubik_300, FontWeight.W300),
            font(R.font.rubik_400, FontWeight.W400),
            font(R.font.rubik_500, FontWeight.W500)
        )

        testTypography(
            context = AmbientContext.current,
            themeOverlayId = R.style.ThemeOverlay_RubikFontFamily,
            expectedFontFamily = rubik
        )
    }

    @Test
    fun type_rubik_fixed400() = composeTestRule.setContent {
        testTypography(
            context = AmbientContext.current,
            themeOverlayId = R.style.ThemeOverlay_Rubik400,
            expectedFontFamily = font(R.font.rubik_400, FontWeight.W400).asFontFamily()
        )
    }

    private fun testTypography(
        context: Context,
        @StyleRes themeOverlayId: Int,
        expectedFontFamily: FontFamily
    ) {
        val themedContext = ContextThemeWrapper(context, themeOverlayId)

        val typography = themedContext.createAppCompatTheme(
            readColors = false,
            readTypography = true
        ).typography

        checkNotNull(typography)

        assertEquals(expectedFontFamily, typography.h1.fontFamily)
        assertEquals(expectedFontFamily, typography.h2.fontFamily)
        assertEquals(expectedFontFamily, typography.h3.fontFamily)
        assertEquals(expectedFontFamily, typography.h4.fontFamily)
        assertEquals(expectedFontFamily, typography.h5.fontFamily)
        assertEquals(expectedFontFamily, typography.h5.fontFamily)
        assertEquals(expectedFontFamily, typography.h6.fontFamily)
        assertEquals(expectedFontFamily, typography.body1.fontFamily)
        assertEquals(expectedFontFamily, typography.body2.fontFamily)
        assertEquals(expectedFontFamily, typography.button.fontFamily)
        assertEquals(expectedFontFamily, typography.caption.fontFamily)
        assertEquals(expectedFontFamily, typography.overline.fontFamily)
    }
}
