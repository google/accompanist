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

package dev.chrisbanes.accompanist.insets

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.ui.test.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@LargeTest
@RunWith(JUnit4::class)
class InsetsTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule(InsetsTestActivity::class.java)

    @Test
    @SdkSuppress(minSdkVersion = 23) // ViewCompat.getRootWindowInsets
    fun assertValuesMatchViewInsets() {
        lateinit var composeWindowInsets: WindowInsets
        composeTestRule.setContent {
            ProvideWindowInsets {
                composeWindowInsets = AmbientWindowInsets.current
            }
        }

        lateinit var rootWindowInsets: WindowInsetsCompat
        composeTestRule.activityRule.scenario.onActivity {
            rootWindowInsets = ViewCompat.getRootWindowInsets(it.window.decorView)!!
        }

        composeWindowInsets.assertEqualTo(rootWindowInsets)
    }
}
