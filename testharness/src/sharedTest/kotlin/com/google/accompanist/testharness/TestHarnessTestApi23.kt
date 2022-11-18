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

import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.os.LocaleListCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(AndroidJUnit4::class)
@Config(sdk = [23])
class TestHarnessTestApi23 {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    @SdkSuppress(maxSdkVersion = 23)
    fun locales_api23_onlyfirstLocaleApplied() {
        val expectedLocales = LocaleListCompat.create(Locale.CANADA, Locale.ITALY)
        lateinit var locales: LocaleListCompat
        composeTestRule.setContent {
            TestHarness(locales = expectedLocales) {
                @Suppress("DEPRECATION")
                locales = LocaleListCompat.create(LocalConfiguration.current.locale)
            }
        }

        composeTestRule.waitForIdle()

        // Only one of the locales is used in Sdk<24
        assertEquals(LocaleListCompat.create(Locale.CANADA), locales)
    }
}
