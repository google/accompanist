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

package com.google.accompanist.appcompattheme

import android.view.ContextThemeWrapper
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import com.google.accompanist.appcompattheme.test.R
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
            // AppCompatTheme updates the LocalContentColor to match the calculated onBackground
            assertEquals(Color.Black, LocalContentColor.current)
        }
    }

    @Test
    fun colors_textColorPrimary() = composeTestRule.setContent {
        WithThemeOverlay(R.style.ThemeOverlay_TextColorPrimary) {
            AppCompatTheme {
                val color = MaterialTheme.colors

                assertEquals(colorResource(R.color.aquamarine), color.primary)
                assertEquals(Color.Black, color.onPrimary)
                assertEquals(colorResource(R.color.royal_blue), color.primaryVariant)
                assertEquals(colorResource(R.color.dark_golden_rod), color.secondary)
                assertEquals(Color.Black, color.onSecondary)
                assertEquals(colorResource(R.color.dark_golden_rod), color.secondaryVariant)
                assertEquals(colorResource(R.color.dark_salmon), color.error)
                assertEquals(Color.Black, color.onError)

                assertEquals(colorResource(R.color.light_coral), color.background)
                // Our textColorPrimary (midnight_blue) contains provides enough contrast vs
                // the background color, so it should be used.
                assertEquals(colorResource(R.color.midnight_blue), color.onBackground)
                // AppCompatTheme updates the LocalContentColor to match the calculated onBackground
                assertEquals(colorResource(R.color.midnight_blue), LocalContentColor.current)

                if (!isSystemInDarkTheme()) {
                    // Our textColorPrimary (midnight_blue) provides enough contrast vs
                    // the light surface color, so it should be used.
                    assertEquals(colorResource(R.color.midnight_blue), color.onSurface)
                } else {
                    // In dark theme, textColorPrimary (midnight_blue) does not provide
                    // enough contrast vs  the light surface color,
                    // so we use a computed value of white
                    assertEquals(Color.White, color.onSurface)
                }
            }
        }
    }

    @Test
    @SdkSuppress(maxSdkVersion = 22) // On API 21-22, the family is loaded with only the 400 font
    fun type_rubik_family_api21() = composeTestRule.setContent {
        val rubik = Font(R.font.rubik, FontWeight.W400).toFontFamily()

        WithThemeOverlay(R.style.ThemeOverlay_RubikFontFamily) {
            AppCompatTheme {
                MaterialTheme.typography.assertFontFamily(expected = rubik)
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

        WithThemeOverlay(R.style.ThemeOverlay_RubikFontFamily) {
            AppCompatTheme {
                MaterialTheme.typography.assertFontFamily(expected = rubik)
            }
        }
    }

    @Test
    fun type_rubik_fixed400() = composeTestRule.setContent {
        val rubik400 = Font(R.font.rubik_400, FontWeight.W400).toFontFamily()
        WithThemeOverlay(R.style.ThemeOverlay_Rubik400) {
            AppCompatTheme {
                MaterialTheme.typography.assertFontFamily(expected = rubik400)
            }
        }
    }
}

private fun Typography.assertFontFamily(expected: FontFamily) {
    assertEquals(expected, h1.fontFamily)
    assertEquals(expected, h2.fontFamily)
    assertEquals(expected, h3.fontFamily)
    assertEquals(expected, h4.fontFamily)
    assertEquals(expected, h5.fontFamily)
    assertEquals(expected, h5.fontFamily)
    assertEquals(expected, h6.fontFamily)
    assertEquals(expected, body1.fontFamily)
    assertEquals(expected, body2.fontFamily)
    assertEquals(expected, button.fontFamily)
    assertEquals(expected, caption.fontFamily)
    assertEquals(expected, overline.fontFamily)
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
