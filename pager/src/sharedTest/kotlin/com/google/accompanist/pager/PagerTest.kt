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

package com.google.accompanist.pager

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performScrollTo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

private const val LongSwipeDistance = 0.95f
private const val MediumSwipeDistance = 0.8f
private const val ShortSwipeDistance = 0.45f

private const val FastVelocity = 4000f
private const val MediumVelocity = 1500f
private const val SlowVelocity = 300f

@OptIn(ExperimentalPagerApi::class) // Pager is currently experimental
abstract class PagerTest {
    protected abstract val offscreenLimit: Int
    protected abstract val infiniteLoop: Boolean

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * This is a workaround for https://issuetracker.google.com/issues/179492185.
     * Ideally we would have a way to get the applier scope from the rule
     */
    protected lateinit var applierScope: CoroutineScope

    @Test
    fun layout() {
        val pagerState = setPagerContent(pageCount = 10)
        assertPagerLayout(0, pagerState.pageCount)
    }

    @Test
    fun swipe() {
        val pagerState = setPagerContent(pageCount = 10)

        if (infiniteLoop) {
            // First test swiping towards end, from 0 to -1
            composeTestRule.onNodeWithTag("0")
                .swipeAcrossCenter(
                    distancePercentage = LongSwipeDistance,
                    velocity = MediumVelocity,
                )
            // ...and assert that we now laid out from page 9
            assertPagerLayout(9, pagerState.pageCount)

            // Now swipe towards start, from page 9 to page 0
            composeTestRule.onNodeWithTag("9")
                .swipeAcrossCenter(
                    distancePercentage = -LongSwipeDistance,
                    velocity = MediumVelocity,
                )
            // ...and assert that we now laid out from page 0
            assertPagerLayout(0, pagerState.pageCount)
        } else {
            // First test swiping towards end, from 0 to -1, which should no-op
            composeTestRule.onNodeWithTag("0")
                .swipeAcrossCenter(
                    distancePercentage = LongSwipeDistance,
                    velocity = MediumVelocity,
                )
            // ...and assert that nothing happened
            assertPagerLayout(0, pagerState.pageCount)

            // Now swipe towards start, from page 0 to page 1
            composeTestRule.onNodeWithTag("0")
                .swipeAcrossCenter(
                    distancePercentage = -LongSwipeDistance,
                    velocity = MediumVelocity,
                )
            // ...and assert that we now laid out from page 1
            assertPagerLayout(1, pagerState.pageCount)
        }
    }

    @Test
    fun swipeToEndAndBack() {
        val pagerState = setPagerContent(pageCount = 4)

        // Now swipe towards start, from page 0 to page 1 and assert the layout
        composeTestRule.onNodeWithTag("0").swipeAcrossCenter(-LongSwipeDistance)
        composeTestRule.waitForIdle()
        assertThat(pagerState.currentPage).isEqualTo(1)
        assertPagerLayout(1, pagerState.pageCount)

        // Repeat for 1 -> 2
        composeTestRule.onNodeWithTag("1").swipeAcrossCenter(-LongSwipeDistance)
        composeTestRule.waitForIdle()
        assertThat(pagerState.currentPage).isEqualTo(2)
        assertPagerLayout(2, pagerState.pageCount)

        // Repeat for 2 -> 3
        composeTestRule.onNodeWithTag("2").swipeAcrossCenter(-LongSwipeDistance)
        composeTestRule.waitForIdle()
        assertThat(pagerState.currentPage).isEqualTo(3)
        assertPagerLayout(3, pagerState.pageCount)

        if (infiniteLoop) {
            // Swipe past the last item to first item.
            composeTestRule.onNodeWithTag("3").swipeAcrossCenter(-LongSwipeDistance)
            composeTestRule.waitForIdle()
            assertThat(pagerState.currentPage).isEqualTo(0)
            assertPagerLayout(0, pagerState.pageCount)

            // Swipe back to last item.
            composeTestRule.onNodeWithTag("0").swipeAcrossCenter(LongSwipeDistance)
            composeTestRule.waitForIdle()
            assertThat(pagerState.currentPage).isEqualTo(3)
            assertPagerLayout(3, pagerState.pageCount)
        } else {
            // Swipe past the last item. We shouldn't move
            composeTestRule.onNodeWithTag("3").swipeAcrossCenter(-LongSwipeDistance)
            composeTestRule.waitForIdle()
            assertThat(pagerState.currentPage).isEqualTo(3)
            assertPagerLayout(3, pagerState.pageCount)
        }

        // Swipe back from 3 -> 2
        composeTestRule.onNodeWithTag("3").swipeAcrossCenter(LongSwipeDistance)
        composeTestRule.waitForIdle()
        assertThat(pagerState.currentPage).isEqualTo(2)
        assertPagerLayout(2, pagerState.pageCount)

        // Swipe back from 2 -> 1
        composeTestRule.onNodeWithTag("2").swipeAcrossCenter(LongSwipeDistance)
        composeTestRule.waitForIdle()
        assertThat(pagerState.currentPage).isEqualTo(1)
        assertPagerLayout(1, pagerState.pageCount)

        // Swipe back from 1 -> 0
        composeTestRule.onNodeWithTag("1").swipeAcrossCenter(LongSwipeDistance)
        composeTestRule.waitForIdle()
        assertThat(pagerState.currentPage).isEqualTo(0)
        assertPagerLayout(0, pagerState.pageCount)

        if (infiniteLoop) {
            // Swipe past the first item to last item.
            composeTestRule.onNodeWithTag("0").swipeAcrossCenter(LongSwipeDistance)
            composeTestRule.waitForIdle()
            assertThat(pagerState.currentPage).isEqualTo(3)
            assertPagerLayout(3, pagerState.pageCount)

            // Swipe back to first item.
            composeTestRule.onNodeWithTag("3").swipeAcrossCenter(-LongSwipeDistance)
            composeTestRule.waitForIdle()
            assertThat(pagerState.currentPage).isEqualTo(0)
            assertPagerLayout(0, pagerState.pageCount)
        } else {
            // Swipe past the first item. We shouldn't move
            composeTestRule.onNodeWithTag("0").swipeAcrossCenter(LongSwipeDistance)
            composeTestRule.waitForIdle()
            assertThat(pagerState.currentPage).isEqualTo(0)
            assertPagerLayout(0, pagerState.pageCount)
        }
    }

    @Test
    fun mediumDistance_fastSwipe_toFling() {
        val pagerState = setPagerContent(pageCount = 10)

        // Now swipe towards start, from page 0 to page 1, over a medium distance of the item width.
        // This should trigger a fling()
        composeTestRule.onNodeWithTag("0")
            .swipeAcrossCenter(
                distancePercentage = -MediumSwipeDistance,
                velocity = FastVelocity,
            )
        // ...and assert that we now laid out from page 1
        assertPagerLayout(1, pagerState.pageCount)
    }

    @Test
    fun mediumDistance_slowSwipe_toSnapForward() {
        val pagerState = setPagerContent(pageCount = 10)

        // Now swipe towards start, from page 0 to page 1, over a medium distance of the item width.
        // This should trigger a spring to position 1
        composeTestRule.onNodeWithTag("0")
            .swipeAcrossCenter(
                distancePercentage = -MediumSwipeDistance,
                velocity = SlowVelocity,
            )
        // ...and assert that we now laid out from page 1
        assertPagerLayout(1, pagerState.pageCount)
    }

    @Test
    fun shortDistance_fastSwipe_toFling() {
        val pagerState = setPagerContent(pageCount = 10)

        // Now swipe towards start, from page 0 to page 1, over a short distance of the item width.
        // This should trigger a fling to page 1
        composeTestRule.onNodeWithTag("0")
            .swipeAcrossCenter(
                distancePercentage = -ShortSwipeDistance,
                velocity = FastVelocity,
            )
        // ...and assert that we now laid out from page 1
        assertPagerLayout(1, pagerState.pageCount)
    }

    @Test
    fun shortDistance_slowSwipe_toSnapBack() {
        val pagerState = setPagerContent(pageCount = 10)

        // Now swipe towards start, from page 0 to page 1, over a short distance of the item width.
        // This should trigger a spring back to the original position
        composeTestRule.onNodeWithTag("0")
            .swipeAcrossCenter(
                distancePercentage = -ShortSwipeDistance,
                velocity = SlowVelocity,
            )
        // ...and assert that we 'sprang back' to page 0
        assertPagerLayout(0, pagerState.pageCount)
    }

    @Test
    fun scrollToPage() = suspendTest {
        val pagerState = setPagerContent(pageCount = 10)

        pagerState.scrollToPage(3)
        assertThat(pagerState.currentPage).isEqualTo(3)
        assertPagerLayout(3, pagerState.pageCount)

        pagerState.scrollToPage(0)
        assertThat(pagerState.currentPage).isEqualTo(0)
        assertPagerLayout(0, pagerState.pageCount)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun animateScrollToPage() = suspendTest {
        val pagerState = setPagerContent(pageCount = 10)

        withContext(applierScope.coroutineContext) {
            pagerState.animateScrollToPage(3)
        }
        composeTestRule.awaitIdle()
        assertThat(pagerState.currentPage).isEqualTo(3)
        assertPagerLayout(3, pagerState.pageCount)

        withContext(applierScope.coroutineContext) {
            pagerState.animateScrollToPage(0)
        }
        composeTestRule.awaitIdle()
        assertThat(pagerState.currentPage).isEqualTo(0)
        assertPagerLayout(0, pagerState.pageCount)
    }

    @Test
    @Ignore("Currently broken") // TODO: Will fix this once we move to Modifier.scrollable()
    fun a11yScroll() {
        val pagerState = setPagerContent(pageCount = 10)

        // Perform a scroll to item 1
        composeTestRule.onNodeWithTag("1").performScrollTo()

        // ...and assert that we scrolled to page 1
        assertPagerLayout(1, pagerState.pageCount)
    }

    @Test
    fun scrollWhenStateObserved() {
        val pagerState = setPagerContent(pageCount = 4, observeStateInContent = true)

        // Now swipe towards start, from page 0 to page 1
        composeTestRule.onNodeWithTag("0")
            .swipeAcrossCenter(distancePercentage = -MediumSwipeDistance)
        // ...and assert that we now laid out from page 1
        assertPagerLayout(1, pagerState.pageCount)

        // Now swipe towards the end, from page 1 to page 0
        composeTestRule.onNodeWithTag("1")
            .swipeAcrossCenter(distancePercentage = MediumSwipeDistance)
        // ...and assert that we now laid out from page 0
        assertPagerLayout(0, pagerState.pageCount)
    }

    /**
     * Swipe across the center of the node. The major axis of the swipe is defined by the
     * overriding test.
     *
     * @param velocity Target end velocity for the swipe.
     * @param distancePercentage The swipe distance in percentage of the node's size.
     * Negative numbers mean swipe towards the start, positive towards the end.
     */
    abstract fun SemanticsNodeInteraction.swipeAcrossCenter(
        distancePercentage: Float,
        velocity: Float = MediumVelocity
    ): SemanticsNodeInteraction

    // TODO: add test for state restoration?

    private fun assertPagerLayout(currentPage: Int, pageCount: Int) {
        // The pages which are expected to be laid out, using the given current page,
        // offscreenLimit and page limit
        val pageRange = (currentPage - offscreenLimit)..(currentPage + offscreenLimit)
        val expectedLaidOutPages = if (infiniteLoop) {
            pageRange.toList()
        } else {
            pageRange.filter { it in 0 until pageCount }.toList()
        }

        // Go through all of the pages, and assert the expected layout state
        (0 until pageCount).forEach { _page ->
            val page = expectedLaidOutPages.find { it == _page }
            if (page != null) {
                // If this page is expected to be laid out, assert that it exists and is
                // laid out in the correct position
                composeTestRule.onNodeWithTag(page.toString())
                    .assertExists()
                    .assertLaidOutItemPosition(page, currentPage)
                    .onParent()
                    .assertIsSelectable()
                    .assertWhen(page == currentPage) { assertIsSelected() }
                    .assertWhen(page != currentPage) { assertIsNotSelected() }
            } else {
                // If this page is not expected to be laid out, assert that it doesn't exist
                composeTestRule.onNodeWithTag(_page.toString()).assertDoesNotExist()
            }
        }
    }

    protected abstract fun SemanticsNodeInteraction.assertLaidOutItemPosition(
        page: Int,
        currentPage: Int,
    ): SemanticsNodeInteraction

    protected abstract fun setPagerContent(
        pageCount: Int,
        observeStateInContent: Boolean = false,
    ): PagerState
}
