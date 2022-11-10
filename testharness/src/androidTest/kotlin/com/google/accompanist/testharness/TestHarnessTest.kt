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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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

    }

    @Test
    fun locales() {

    }

    @Test
    fun localesAPI23() {

    }
    @Test
    fun localesAPI24() {

    }

    @Test
    fun layoutDirection() {

    }

    @Test
    fun fontScale() {

    }

    @Test
    fun fontWeightAdjustment() {

    }


    @Composable
    private fun BoxOfSize(size: Dp, onSize:(Dp) -> Unit) {
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
