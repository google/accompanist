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

@file:Suppress("DEPRECATION")
package com.google.accompanist.placeholder

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaceholderHighlightTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun fadeBrush() {
        composeTestRule.setContent {
            PlaceholderHighlight.fade(highlightColor = Color.Blue)
        }
    }

    @Test
    fun shimmerBrush() {
        composeTestRule.setContent {
            PlaceholderHighlight.shimmer(highlightColor = Color.Blue)
        }
    }

    @Test
    fun fadeBrush_equals() {
        assertThat(PlaceholderHighlight.fade(highlightColor = Color.Blue))
            .isEqualTo(PlaceholderHighlight.fade(highlightColor = Color.Blue))
    }

    @Test
    fun shimmerBrush_equals() {
        assertThat(PlaceholderHighlight.shimmer(highlightColor = Color.Blue))
            .isEqualTo(PlaceholderHighlight.shimmer(highlightColor = Color.Blue))
    }
}
