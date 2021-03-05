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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
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
    fun layout_fullWidthItems_ltr() {
        setPagerContent(
            layoutDirection = LayoutDirection.Ltr,
            pageModifier = Modifier.fillMaxWidth(),
            offscreenLimit = 2,
        )

        val rootBounds = composeTestRule.onRoot().getUnclippedBoundsInRoot()

        composeTestRule.onNodeWithText("0")
            .assertExists()
            .assertWidthIsEqualTo(rootBounds.width)
            .assertLeftPositionInRootIsEqualTo(0.dp)

        composeTestRule.onNodeWithText("1")
            .assertExists()
            .assertWidthIsEqualTo(rootBounds.width)
            .assertLeftPositionInRootIsEqualTo(rootBounds.width)

        composeTestRule.onNodeWithText("2")
            .assertExists()
            .assertWidthIsEqualTo(rootBounds.width)
            .assertLeftPositionInRootIsEqualTo(rootBounds.width * 2)

        // Offscreen limit is 2, so this shouldn't exist
        composeTestRule.onNodeWithText("3")
            .assertDoesNotExist()
    }

    @Test
    fun layout_fullWidthItems_rtl() {
        setPagerContent(
            layoutDirection = LayoutDirection.Rtl,
            pageModifier = Modifier.fillMaxWidth(),
            offscreenLimit = 2,
        )

        val rootBounds = composeTestRule.onRoot().getUnclippedBoundsInRoot()

        composeTestRule.onNodeWithText("0")
            .assertExists()
            .assertWidthIsEqualTo(rootBounds.width)
            .assertLeftPositionInRootIsEqualTo(0.dp)

        composeTestRule.onNodeWithText("1")
            .assertExists()
            .assertWidthIsEqualTo(rootBounds.width)
            .assertLeftPositionInRootIsEqualTo(-rootBounds.width)

        composeTestRule.onNodeWithText("2")
            .assertExists()
            .assertWidthIsEqualTo(rootBounds.width)
            .assertLeftPositionInRootIsEqualTo(-rootBounds.width * 2)

        // Offscreen limit is 2, so this shouldn't exist
        composeTestRule.onNodeWithText("3")
            .assertDoesNotExist()
    }

    private fun setPagerContent(
        layoutDirection: LayoutDirection,
        pageModifier: Modifier,
        maxPage: Int = 3,
        offscreenLimit: Int = 1,
    ): PagerState {
        val pagerState = PagerState().apply {
            this.maxPage = maxPage
        }
        composeTestRule.setContent(layoutDirection) {
            Pager(
                state = pagerState,
                offscreenLimit = offscreenLimit,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                BasicText(page.toString(), pageModifier)
            }
        }
        return pagerState
    }
}
