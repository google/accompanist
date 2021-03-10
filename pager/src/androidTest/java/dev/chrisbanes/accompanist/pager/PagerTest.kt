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
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performGesture
import androidx.compose.ui.test.performScrollTo
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

private const val MediumSwipeDistance = 0.6f
private const val ShortSwipeDistance = 0.3f

@OptIn(ExperimentalPagerApi::class) // Pager is currently experimental
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

        // First test swiping from 0 to -1, which should no-op
        composeTestRule.onRoot()
            .performGesture {
                when (layoutDirection) {
                    LayoutDirection.Ltr -> swipeRight()
                    else -> swipeLeft()
                }
            }
        // ...and assert that nothing happened
        assertPagerLayout(
            currentPage = 0,
            maxPage = 10,
            offscreenLimit = offscreenLimit,
            expectedItemWidth = rootBounds.width * itemWidthFraction,
            layoutDirection = layoutDirection,
        )

        // Now swipe from page 0 to page 1
        composeTestRule.onRoot()
            .performGesture {
                when (layoutDirection) {
                    LayoutDirection.Ltr -> swipeLeft()
                    else -> swipeRight()
                }
            }
        // ...and assert that we now laid out from page 1
        assertPagerLayout(
            currentPage = 1,
            maxPage = 10,
            offscreenLimit = offscreenLimit,
            expectedItemWidth = rootBounds.width * itemWidthFraction,
            layoutDirection = layoutDirection,
        )
    }

    @Test
    fun mediumFastSwipeToFling() {
        setPagerContent(
            layoutDirection = layoutDirection,
            pageModifier = Modifier.fillMaxWidth(itemWidthFraction),
            maxPage = 10,
            offscreenLimit = offscreenLimit,
        )

        val rootBounds = composeTestRule.onRoot().getUnclippedBoundsInRoot()

        // Now swipe from page 0 to page 1, over a medium distance of the item width.
        // This should trigger a fling()
        composeTestRule.onRoot().swipeAcrossCenter(
            distancePercentageX = itemWidthFraction * when (layoutDirection) {
                LayoutDirection.Rtl -> MediumSwipeDistance
                else -> -MediumSwipeDistance
            },
            durationMillis = 200,
        )
        // ...and assert that we now laid out from page 1
        assertPagerLayout(
            currentPage = 1,
            maxPage = 10,
            offscreenLimit = offscreenLimit,
            expectedItemWidth = rootBounds.width * itemWidthFraction,
            layoutDirection = layoutDirection,
        )
    }

    @Test
    fun mediumSlowSwipeToSnapForward() {
        setPagerContent(
            layoutDirection = layoutDirection,
            pageModifier = Modifier.fillMaxWidth(itemWidthFraction),
            maxPage = 10,
            offscreenLimit = offscreenLimit,
        )

        val rootBounds = composeTestRule.onRoot().getUnclippedBoundsInRoot()

        // Now slowly swipe from page 0 to page 1, over a medium distance of the item width.
        // This should trigger a spring to position 1
        composeTestRule.onRoot().swipeAcrossCenter(
            distancePercentageX = itemWidthFraction * when (layoutDirection) {
                LayoutDirection.Rtl -> MediumSwipeDistance
                else -> -MediumSwipeDistance
            },
            durationMillis = 4000,
        )
        // ...and assert that we now laid out from page 1
        assertPagerLayout(
            currentPage = 1,
            maxPage = 10,
            offscreenLimit = offscreenLimit,
            expectedItemWidth = rootBounds.width * itemWidthFraction,
            layoutDirection = layoutDirection,
        )
    }

    @Test
    fun shortFastSwipeToFling() {
        setPagerContent(
            layoutDirection = layoutDirection,
            pageModifier = Modifier.fillMaxWidth(itemWidthFraction),
            maxPage = 10,
            offscreenLimit = offscreenLimit,
        )

        val rootBounds = composeTestRule.onRoot().getUnclippedBoundsInRoot()

        // Now swipe from page 0 to page 1, over a short distance of the item width.
        // This should trigger a fling to page 1
        composeTestRule.onRoot().swipeAcrossCenter(
            distancePercentageX = itemWidthFraction * when (layoutDirection) {
                LayoutDirection.Rtl -> ShortSwipeDistance
                else -> -ShortSwipeDistance
            },
            durationMillis = 200,
        )
        // ...and assert that we now laid out from page 1
        assertPagerLayout(
            currentPage = 1,
            maxPage = 10,
            offscreenLimit = offscreenLimit,
            expectedItemWidth = rootBounds.width * itemWidthFraction,
            layoutDirection = layoutDirection,
        )
    }

    @Test
    fun shortSlowSwipeToSnapBack() {
        setPagerContent(
            layoutDirection = layoutDirection,
            pageModifier = Modifier.fillMaxWidth(itemWidthFraction),
            maxPage = 10,
            offscreenLimit = offscreenLimit,
        )

        val rootBounds = composeTestRule.onRoot().getUnclippedBoundsInRoot()

        // Now slowly swipe from page 0 to page 1, over a short distance of the item width.
        // This should trigger a spring back to the original position
        composeTestRule.onRoot().swipeAcrossCenter(
            distancePercentageX = itemWidthFraction * when (layoutDirection) {
                LayoutDirection.Rtl -> ShortSwipeDistance
                else -> -ShortSwipeDistance
            },
            durationMillis = 3000,
        )
        // ...and assert that we 'sprang back' to page 0
        assertPagerLayout(
            currentPage = 0,
            maxPage = 10,
            offscreenLimit = offscreenLimit,
            expectedItemWidth = rootBounds.width * itemWidthFraction,
            layoutDirection = layoutDirection,
        )
    }

    @Test
    fun a11yScroll() {
        setPagerContent(
            layoutDirection = layoutDirection,
            pageModifier = Modifier.fillMaxWidth(itemWidthFraction),
            maxPage = 10,
            offscreenLimit = offscreenLimit,
        )

        val rootBounds = composeTestRule.onRoot().getUnclippedBoundsInRoot()

        // Perform a scroll to item 1
        composeTestRule.onNodeWithText("1").performScrollTo()

        // ...and assert that we scrolled to page 1
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

        // The expected left of the first item. This uses the implicit fact that Pager
        // centers items horizontally.
        val firstItemLeft = (rootBounds.width - expectedItemWidth) / 2

        // The pages which are expected to be laid out, using the given current page,
        // offscreenLimit and page limit
        val expectedLaidOutPages = (currentPage - offscreenLimit)..(currentPage + offscreenLimit)
            .coerceIn(0, maxPage)

        // Go through all of the pages, and assert the expected layout state
        (0..maxPage).forEach { page ->
            if (page in expectedLaidOutPages) {
                // If this page is expected to be laid out, assert that it exists and is
                // laid out in the correct position
                composeTestRule.onNodeWithText(page.toString())
                    .assertExists()
                    .assertWidthIsEqualTo(expectedItemWidth)
                    .assertIsSelectable()
                    .run {
                        if (page == currentPage) {
                            assertIsSelected()
                        } else {
                            assertIsNotSelected()
                        }
                    }
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
                // If this page is not expected to be laid out, assert that it doesn't exist
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
            this.pageCount = maxPage
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
