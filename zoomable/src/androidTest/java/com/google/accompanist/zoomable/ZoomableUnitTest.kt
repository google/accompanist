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

package com.google.accompanist.zoomable

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ZoomableUnitTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test(expected = IllegalArgumentException::class)
    fun rememberZoomableState_minScale_negative() {
        composeTestRule.setContent {
            rememberZoomableState(minScale = -1f)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rememberZoomableState_minScale_greater_than_maxScale() {
        composeTestRule.setContent {
            rememberZoomableState(minScale = 4f, maxScale = 1f)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rememberZoomableState_minScale_equals_maxScale() {
        composeTestRule.setContent {
            rememberZoomableState(minScale = 1f, maxScale = 1f)
        }
    }
}
