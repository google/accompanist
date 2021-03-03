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

package dev.chrisbanes.accompanist.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@LargeTest
@RunWith(JUnit4::class)
class PagerTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun layout_fullWidthItems() {
        val pagerState = PagerState().apply { maxPage = 3 }

        composeTestRule.setContent {
            Pager(
                state = pagerState,
                offscreenLimit = 1,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                BasicText(
                    text = page.toString(),
                    Modifier.background(Color.Blue).fillMaxWidth()
                )
            }
        }

        composeTestRule.onNodeWithText("1")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("2")
            .assertExists()
            .assertIsNotDisplayed()

        // Offscreen limit is 1, so this shouldn't exist
        composeTestRule.onNodeWithText("3")
            .assertDoesNotExist()
    }
}
