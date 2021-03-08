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
import androidx.compose.ui.test.performGesture
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.width
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@LargeTest
@RunWith(Parameterized::class)
class PagerTest(
    private val itemWidthFraction: Float,
    private val offscreenLimit: Int,
    private val layoutDirection: LayoutDirection,
) {
    @get:Rule
    val composeTestRule = createComposeRule()

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> = listOf(
            // itemWidthFraction, offscreenLimit, layoutDirection

            // Test typical full-width items
            arrayOf(1f, 2, LayoutDirection.Ltr),
            arrayOf(1f, 2, LayoutDirection.Rtl),

            // Test an increased offscreenLimit
            arrayOf(1f, 4, LayoutDirection.Ltr),
            arrayOf(1f, 4, LayoutDirection.Rtl),

            // Test items with 60% widths
            arrayOf(0.6f, 2, LayoutDirection.Ltr),
            arrayOf(0.6f, 2, LayoutDirection.Rtl),
        )
    }

    @Test
    fun layout() {
        setPagerContent(
            layoutDirection = layoutDirection,
            pageModifier = Modifier.fillMaxWidth(itemWidthFraction),
            maxPage = 10,
            offscreenLimit = offscreenLimit,
        )

        val rootBounds = composeTestRule.onRoot().getUnclippedBoundsInRoot()

        assertPagerLayout(
            currentPage = 0,
            maxPage = 10,
            offscreenLimit = offscreenLimit,
            expectedItemWidth = rootBounds.width * itemWidthFraction,
            layoutDirection = layoutDirection,
        )
    }

    @Test
    fun swipe() {
        setPagerContent(
            layoutDirection = layoutDirection,
            pageModifier = Modifier.fillMaxWidth(itemWidthFraction),
            maxPage = 10,
            offscreenLimit = offscreenLimit,
        )

        val rootBounds = composeTestRule.onRoot().getUnclippedBoundsInRoot()

        assertPagerLayout(
            currentPage = 0,
            maxPage = 10,
            offscreenLimit = offscreenLimit,
            expectedItemWidth = rootBounds.width * itemWidthFraction,
            layoutDirection = layoutDirection,
        )

        // First test swiping from 0 to -1, which should no-op
        composeTestRule.onRoot()
            .performGesture {
                when (layoutDirection) {
                    LayoutDirection.Ltr -> swipeRight()
                    else -> swipeLeft()
                }
            }
        assertPagerLayout(
            currentPage = 0,
            maxPage = 10,
            offscreenLimit = offscreenLimit,
            expectedItemWidth = rootBounds.width * itemWidthFraction,
            layoutDirection = layoutDirection,
        )

        // Now swipe from 0 to 1
        composeTestRule.onRoot()
            .performGesture {
                when (layoutDirection) {
                    LayoutDirection.Ltr -> swipeLeft()
                    else -> swipeRight()
                }
            }
        assertPagerLayout(
            currentPage = 1,
            maxPage = 10,
            offscreenLimit = offscreenLimit,
            expectedItemWidth = rootBounds.width * itemWidthFraction,
            layoutDirection = layoutDirection,
        )
    }

    private fun assertPagerLayout(
        currentPage: Int,
        maxPage: Int,
        expectedItemWidth: Dp,
        offscreenLimit: Int,
        layoutDirection: LayoutDirection,
    ) {
        val rootBounds = composeTestRule.onRoot().getUnclippedBoundsInRoot()
        val firstItemLeft = (rootBounds.width - expectedItemWidth) / 2

        val laidOutRange = (currentPage - offscreenLimit)..(currentPage + offscreenLimit)
            .coerceIn(0, maxPage)

        (0..maxPage).forEach { page ->
            if (page in laidOutRange) {
                composeTestRule.onNodeWithText(page.toString())
                    .assertExists()
                    .assertWidthIsEqualTo(expectedItemWidth)
                    .run {
                        if (layoutDirection == LayoutDirection.Ltr) {
                            assertLeftPositionInRootIsEqualTo(
                                firstItemLeft + (expectedItemWidth * (page - currentPage))
                            )
                        } else {
                            assertLeftPositionInRootIsEqualTo(
                                firstItemLeft - (expectedItemWidth * (page - currentPage))
                            )
                        }
                    }
            } else {
                composeTestRule.onNodeWithText(page.toString()).assertDoesNotExist()
            }
        }
    }

    private fun setPagerContent(
        layoutDirection: LayoutDirection,
        pageModifier: Modifier,
        maxPage: Int,
        offscreenLimit: Int,
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
