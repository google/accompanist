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
import androidx.ui.test.android.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@MediumTest
@RunWith(JUnit4::class)
class NotMaterialThemeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<NotMdcActivity>()

    @Test(expected = IllegalArgumentException::class)
    fun isNotMaterialTheme() = composeTestRule.setContent {
        MaterialThemeFromMdcTheme {
            // Nothing to do here, exception should be thrown
        }
    }
}
