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

package com.google.accompanist.placeholder

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.accompanist.placeholder.PlaceholderAnimatedBrush.Companion.fade
import com.google.accompanist.placeholder.PlaceholderAnimatedBrush.Companion.shimmer
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PlaceholderAnimatedBrushTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun fadeBrush_colorsDefault() {
        composeTestRule.setContent {
            fade()
        }
    }

    @Test
    fun shimmerBrush_colorsDefault() {
        composeTestRule.setContent {
            shimmer()
        }
    }

    @Test
    fun fadeBrush_colorsCustom() {
        composeTestRule.setContent {
            fade(initialColor = Color.Red, targetColor = Color.Blue)
        }
    }

    @Test
    fun shimmerBrush_colorsCustom() {
        composeTestRule.setContent {
            shimmer(color = Color.Red, highlightColor = Color.Blue)
        }
    }

    @Test
    fun fadeBrush_equalsDefault() {
        assertThat(fade()).isEqualTo(fade())
    }

    @Test
    fun shimmerBrush_equalsDefault() {
        assertThat(shimmer()).isEqualTo(shimmer())
    }

    @Test
    fun fadeBrush_equalsCustom() {
        assertThat(fade(initialColor = Color.Red, targetColor = Color.Blue))
            .isEqualTo(fade(initialColor = Color.Red, targetColor = Color.Blue))
    }

    @Test
    fun shimmerBrush_equalsCustom() {
        assertThat(shimmer(color = Color.Red, highlightColor = Color.Blue))
            .isEqualTo(shimmer(color = Color.Red, highlightColor = Color.Blue))
    }
}
