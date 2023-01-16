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

package com.google.accompanist.themeadapter.material

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.test.filters.SdkSuppress
import com.google.accompanist.themeadapter.material.test.R
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Version of [BaseMdcThemeTest] which is designed to be run on device/emulators.
 */
@RunWith(Parameterized::class)
class InstrumentedMdcThemeTest<T : AppCompatActivity>(
    activityClass: Class<T>
) : BaseMdcThemeTest<T>(activityClass) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun activities() = listOf(
            DarkMdcActivity::class.java,
            LightMdcActivity::class.java
        )
    }

    /**
     * On API 21-22, the family is loaded with only the 400 font.
     *
     * This only works on device as Robolectric seems to always use the behavior from API 23+,
     * which is not what we want to test.
     */
    @Test
    @SdkSuppress(maxSdkVersion = 22)
    fun type_rubik_family_api21() = composeTestRule.setContent {
        val rubik = Font(R.font.rubik, FontWeight.W400).toFontFamily()
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
}
