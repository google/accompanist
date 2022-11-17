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

import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class TestHarnessTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val contentTag = "TestHarnessTestTag"

    @Test
    fun sizeSmallerThanContent_measuredWidthSmaller() {
        var afterWidth = 0.dp
        var preWidth = 0.dp
        composeTestRule.setContent {
            BoxOfSize(200.dp, onSize = { preWidth = it })
            TestHarness(size = DpSize(100.dp, 100.dp)) {
                BoxOfSize(200.dp, onSize = { afterWidth = it })
            }
        }
        composeTestRule.waitForIdle()

        // Widths are approximate because of rounding in BoxWithConstraints
        assertEquals(afterWidth / preWidth, 0.5f, 0.01f)
    }

    @Test
    fun sizeBiggerThanContent_noChangeInWidth() {
        var afterWidth = 0.dp
        var preWidth = 0.dp
        composeTestRule.setContent {
            BoxOfSize(200.dp, onSize = { preWidth = it })
            TestHarness(size = DpSize(400.dp, 400.dp)) {
                BoxOfSize(200.dp, onSize = { afterWidth = it })
            }
        }
        composeTestRule.waitForIdle()

        // Widths are approximate because of rounding in BoxWithConstraints
        assertEquals(afterWidth / preWidth, 1f, 0.001f)
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
    @SdkSuppress(maxSdkVersion = 23)
    fun locales_api23_onlyfirstLocaleApplied() {
        val expectedLocales = LocaleListCompat.create(Locale.CANADA, Locale.ITALY)
        lateinit var locales: LocaleListCompat
        composeTestRule.setContent {
            TestHarness(locales = expectedLocales) {
                locales = LocaleListCompat.wrap(LocalConfiguration.current.locales)
            }
        }

        composeTestRule.waitForIdle()

        // Only one of the locales is used in Sdk<24
        assertEquals(expectedLocales[0], locales)
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
    fun layoutDirection_default() {
        lateinit var direction: LayoutDirection
        val initialLayoutDirection = LayoutDirection.Rtl
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalLayoutDirection provides initialLayoutDirection
            ) {
                TestHarness(layoutDirection = null) {
                    direction = LocalLayoutDirection.current
                }
            }
        }
        composeTestRule.waitForIdle()

        // The default should be the one provided by the CompositionLocal
        assertEquals(initialLayoutDirection, direction)
    }

    @Test
    fun layoutDirection_setLtr() {
        lateinit var direction: LayoutDirection
        val initialLayoutDirection = LayoutDirection.Rtl
        val expected = LayoutDirection.Ltr
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

        // The default should be the one provided by the CompositionLocal
        assertEquals(expected, direction)
    }

    @Test
    fun layoutDirection_setRtl() {
        lateinit var direction: LayoutDirection
        val initialLayoutDirection = LayoutDirection.Ltr
        val expected = LayoutDirection.Rtl
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

        // The default should be the one provided by the CompositionLocal
        assertEquals(expected, direction)
    }

    @Test
    fun fontScale() {
    }

    @Test
    fun fontWeightAdjustment() {
    }


    @Composable
    private fun BoxOfSize(size: Dp, onSize: (Dp) -> Unit) {
        val localDensity = LocalDensity.current
        Box(
            Modifier
                .size(size)
                .background(color = Color.Black)
                .testTag(contentTag)
                .onGloballyPositioned { it: LayoutCoordinates ->
                    onSize(with(localDensity) { it.size.width.toDp() })
                }
        )
    }
}
