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
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.google.accompanist.testharness.test.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class TestHarnessTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun size_SmallerThanOuterBox_measuredWidthIsCorrect() {
        var width = 0.dp
        composeTestRule.setContent {
            Box(Modifier.requiredSize(300.dp)) {
                TestHarness(size = DpSize(200.dp, 200.dp)) {
                    BoxOfSize(200.dp, onWidth = { width = it })
                }
            }
        }
        composeTestRule.waitForIdle()

        val ratio = width / 200.dp
        assertEquals(ratio, 1f, 0.01f)
    }

    @Test
    fun size_BiggerThanOuterBox_measuredWidthIsCorrect() {
        var width = 0.dp
        composeTestRule.setContent {
            Box(Modifier.requiredSize(100.dp)) {
                TestHarness(size = DpSize(200.dp, 200.dp)) {
                    BoxOfSize(200.dp, onWidth = { width = it })
                }
            }
        }
        composeTestRule.waitForIdle()

        val ratio = width / 200.dp
        assertEquals(ratio, 1f, 0.01f)
    }

    @Test
    fun size_ExtremelyBig_measuredWidthIsCorrect() {
        var width = 0.dp
        composeTestRule.setContent {
            TestHarness(size = DpSize(10000.dp, 10000.dp)) {
                BoxOfSize(10000.dp, onWidth = { width = it })
            }
        }
        composeTestRule.waitForIdle()

        val ratio = width / 10000.dp
        assertEquals(ratio, 1f, 0.01f)
    }

    @Test
    fun darkMode_enabled() {
        var darkMode: Int = -1
        composeTestRule.setContent {
            TestHarness(darkMode = true) {
                darkMode = LocalConfiguration.current.uiMode
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(darkMode and UI_MODE_NIGHT_MASK, UI_MODE_NIGHT_YES)
    }

    @Test
    fun darkMode_disabled() {
        var darkMode: Int = -1
        composeTestRule.setContent {
            TestHarness(darkMode = false) {
                darkMode = LocalConfiguration.current.uiMode
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(darkMode and UI_MODE_NIGHT_MASK, UI_MODE_NIGHT_NO)
    }

    @Test
    @SdkSuppress(minSdkVersion = 24)
    fun locales_api24_allLocalesApplied() {
        val expectedLocales = LocaleListCompat.create(Locale.CANADA, Locale.ITALY)
        lateinit var locales: LocaleListCompat
        composeTestRule.setContent {
            TestHarness(locales = expectedLocales) {
                locales = LocaleListCompat.wrap(LocalConfiguration.current.locales)
            }
        }

        composeTestRule.waitForIdle()

        // All locales are expected in Sdk>=24
        assertEquals(expectedLocales, locales)
    }

    @Test
    fun usLocale_usesCorrectResource() {
        composeTestRule.setContent {
            TestHarness(locales = LocaleListCompat.forLanguageTags("us")) {
                BasicText(text = stringResource(R.string.this_is_content, "abc"))
            }
        }
        composeTestRule.onNodeWithText("This is content\nabc").assertExists()
    }

    @Test
    fun arLocale_usesCorrectResource() {
        composeTestRule.setContent {
            TestHarness(locales = LocaleListCompat.forLanguageTags("ar")) {
                BasicText(text = stringResource(R.string.this_is_content, "abc"))
            }
        }
        composeTestRule.onNodeWithText("هذا مضمون \nabc").assertExists()
    }

    @Test
    fun layoutDirection_RtlLocale_usesOverride() {
        lateinit var direction: LayoutDirection
        val initialLocale = LocaleListCompat.create(Locale("ar")) // Arabic
        val initialLayoutDirection = LayoutDirection.Ltr

        // Given test harness setting an RTL Locale but it also forcing the opposite
        // layout direction
        composeTestRule.setContent {
            TestHarness(
                layoutDirection = initialLayoutDirection,
                locales = initialLocale
            ) {
                direction = LocalLayoutDirection.current
            }
        }
        composeTestRule.waitForIdle()

        // The used locale should be the one overriden with the test harness, ignoring the Locale's.
        assertEquals(initialLayoutDirection, direction)
    }

    @Test
    fun layoutDirection_default_RtlLocale() {
        lateinit var direction: LayoutDirection
        val initialLocale = LocaleListCompat.create(Locale("ar")) // Arabic

        // Given an initial layout direction, when the test harness sets an RTL Locale and doesn't
        // force the layout direction
        composeTestRule.setContent {
            TestHarness(
                layoutDirection = null,
                locales = initialLocale
            ) {
                direction = LocalLayoutDirection.current
            }
        }
        composeTestRule.waitForIdle()

        // The used locale should be the Locale's.
        assertEquals(LayoutDirection.Rtl, direction)
    }

    @Test
    fun layoutDirection_default_usesLocales() {
        lateinit var direction: LayoutDirection
        val initialLocale = LocaleListCompat.create(Locale("ar")) // Arabic
        val initialLayoutDirection = LayoutDirection.Ltr

        // Given no layout direction, when the test harness sets an RTL Locale with an initial
        // LTR direction
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalLayoutDirection provides initialLayoutDirection
            ) {
                TestHarness(
                    layoutDirection = null,
                    locales = initialLocale
                ) {
                    direction = LocalLayoutDirection.current
                }
            }
        }
        composeTestRule.waitForIdle()

        // The default should be the one provided by the Locale
        assertNotEquals(initialLayoutDirection, direction)
    }

    @Test
    fun layoutDirection_setLtr() {
        lateinit var direction: LayoutDirection
        val initialLayoutDirection = LayoutDirection.Rtl
        val expected = LayoutDirection.Ltr

        // Given a content with an initial RTL layout direction, when the test harness overrides it
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalLayoutDirection provides initialLayoutDirection
            ) {
                TestHarness(layoutDirection = expected) {
                    direction = LocalLayoutDirection.current
                }
            }
        }
        composeTestRule.waitForIdle()

        // The direction should be the one forced by the test harness
        assertEquals(expected, direction)
    }

    @Test
    fun layoutDirection_setRtl() {
        lateinit var direction: LayoutDirection
        val initialLayoutDirection = LayoutDirection.Ltr
        val expected = LayoutDirection.Rtl

        // Given a content with an initial RTL layout direction, when the test harness overrides it
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalLayoutDirection provides initialLayoutDirection
            ) {
                TestHarness(layoutDirection = expected) {
                    direction = LocalLayoutDirection.current
                }
            }
        }
        composeTestRule.waitForIdle()

        // The direction should be the one forced by the test harness
        assertEquals(expected, direction)
    }

    @Test
    fun layoutDirection_default_followsLocaleLtr() {
        lateinit var direction: LayoutDirection

        // Given an initial layout direction and no overrides
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalConfiguration provides Configuration().apply {
                    setLocale(Locale.ENGLISH)
                }
            ) {
                TestHarness(layoutDirection = null) {
                    direction = LocalLayoutDirection.current
                }
            }
        }
        composeTestRule.waitForIdle()

        // The direction should be set by the Locale
        assertEquals(LayoutDirection.Ltr, direction)
    }

    @Test
    fun layoutDirection_default_followsLocaleRtl() {
        lateinit var direction: LayoutDirection

        // Given an initial layout direction and no overrides
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalConfiguration provides Configuration().apply {
                    setLocale(Locale("ar"))
                }
            ) {
                TestHarness(layoutDirection = null) {
                    direction = LocalLayoutDirection.current
                }
            }
        }
        composeTestRule.waitForIdle()

        // The direction should be set by the Locale
        assertEquals(LayoutDirection.Rtl, direction)
    }

    @Test
    fun fontScale() {
        val expectedFontScale = 5f
        var fontScale = 0f
        // Given
        composeTestRule.setContent {
            TestHarness(fontScale = expectedFontScale) {
                fontScale = LocalConfiguration.current.fontScale
            }
        }

        composeTestRule.waitForIdle()

        assertEquals(expectedFontScale, fontScale)
    }

    @Test
    @SdkSuppress(minSdkVersion = 31)
    fun fontWeightAdjustment() {
        val expectedFontWeightAdjustment = 10
        var fontWeightAdjustment = 0
        composeTestRule.setContent {
            TestHarness(fontWeightAdjustment = expectedFontWeightAdjustment) {
                fontWeightAdjustment = LocalConfiguration.current.fontWeightAdjustment
            }
        }

        composeTestRule.waitForIdle()

        assertEquals(expectedFontWeightAdjustment, fontWeightAdjustment)
    }

    @Composable
    private fun BoxOfSize(size: Dp, onWidth: (Dp) -> Unit) {
        val localDensity = LocalDensity.current
        Box(
            Modifier
                .size(size)
                .background(color = Color.Black)
                .onGloballyPositioned { it: LayoutCoordinates ->
                    onWidth(with(localDensity) { it.size.width.toDp() })
                }
        )
    }
}
