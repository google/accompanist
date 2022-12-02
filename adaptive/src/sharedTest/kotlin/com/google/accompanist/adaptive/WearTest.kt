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

package com.google.accompanist.adaptive

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.testharness.TestHarness
import com.google.common.truth.Truth.assertThat
import org.junit.Assume
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WearTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun is_round_for_wear() {
        assumeTrue(Build.VERSION.SDK_INT >= 23)

        var defaultRound: Boolean? = null
        var forcedRound: Boolean? = null
        var forcedNotRound: Boolean? = null

        composeTestRule.setContent {
            defaultRound = LocalConfiguration.current.isScreenRound
            TestHarness(isScreenRound = true) {
                forcedRound = LocalConfiguration.current.isScreenRound
                TestHarness(isScreenRound = false) {
                    forcedNotRound = LocalConfiguration.current.isScreenRound
                }
            }
        }

        assertThat(defaultRound).isEqualTo(composeTestRule.activity.resources.configuration.isScreenRound)
        assertThat(forcedRound).isTrue()
        assertThat(forcedNotRound).isFalse()
    }
}
