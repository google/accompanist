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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.produceIn
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

private const val LongSwipeDistance = 0.95f
private const val MediumSwipeDistance = 0.8f
private const val ShortSwipeDistance = 0.45f

private const val FastVelocity = 4000f
private const val MediumVelocity = 1700f
private const val SlowVelocity = 600f

@OptIn(ExperimentalPagerApi::class) // Pager is currently experimental
abstract class PagerTest {

    protected abstract val offscreenLimit: Int
    protected abstract val pageCount: Int

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun layout() {
        setPagerContent(pageCount = 10)
        assertPagerLayout(currentPage = 0)
    }

    @Test
    fun swipe() {
        setPagerContent(pageCount = 10)

        // First test swiping towards end, from 0 to -1, which should no-op
        composeTestRule.onNodeWithTag("0")
            .swipeAcrossCenter(
                distancePercentage = LongSwipeDistance,
                velocity = MediumVelocity,
            )
        // ...and assert that nothing happened
        assertPagerLayout(currentPage = 0)

        // Now swipe towards start, from page 0 to page 1
        composeTestRule.onNodeWithTag("0")
            .swipeAcrossCenter(
                distancePercentage = -LongSwipeDistance,
                velocity = MediumVelocity,
            )
        // ...and assert that we now laid out from page 1
        assertPagerLayout(currentPage = 1)
    }

    @Test
    fun mediumDistance_fastSwipe_toFling() {
        setPagerContent(pageCount = 10)

        // Now swipe towards start, from page 0 to page 1, over a medium distance of the item width.
        // This should trigger a fling()
        composeTestRule.onNodeWithTag("0")
            .swipeAcrossCenter(
                distancePercentage = -MediumSwipeDistance,
                velocity = FastVelocity,
            )
        // ...and assert that we now laid out from page 1
        assertPagerLayout(currentPage = 1)
    }

    @Test
    fun mediumDistance_slowSwipe_toSnapForward() {
        setPagerContent(pageCount = 10)

        // Now swipe towards start, from page 0 to page 1, over a medium distance of the item width.
        // This should trigger a spring to position 1
        composeTestRule.onNodeWithTag("0")
            .swipeAcrossCenter(
                distancePercentage = -MediumSwipeDistance,
                velocity = SlowVelocity,
            )
        // ...and assert that we now laid out from page 1
        assertPagerLayout(currentPage = 1)
    }

    @Test
    fun shortDistance_fastSwipe_toFling() {
        setPagerContent(pageCount = 10)

        // Now swipe towards start, from page 0 to page 1, over a short distance of the item width.
        // This should trigger a fling to page 1
        composeTestRule.onNodeWithTag("0")
            .swipeAcrossCenter(
                distancePercentage = -ShortSwipeDistance,
                velocity = FastVelocity,
            )
        // ...and assert that we now laid out from page 1
        assertPagerLayout(currentPage = 1)
    }

    @Test
    fun shortDistance_slowSwipe_toSnapBack() {
        setPagerContent(pageCount = 10)

        // Now swipe towards start, from page 0 to page 1, over a short distance of the item width.
        // This should trigger a spring back to the original position
        composeTestRule.onNodeWithTag("0")
            .swipeAcrossCenter(
                distancePercentage = -ShortSwipeDistance,
                velocity = SlowVelocity,
            )
        // ...and assert that we 'sprang back' to page 0
        assertPagerLayout(currentPage = 0)
    }

    @OptIn(FlowPreview::class)
    @Test
    fun pageChangesSample() = suspendTest {
        val pagerState = setPagerContent(pageCount = 10)

        // Collect the pageChanges flow into a Channel, allowing us to poll values
        val pageChangedChannel = pagerState.pageChanges.produceIn(this)

        // Assert that the first emission is 0
        assertThat(pageChangedChannel.receive()).isEqualTo(0)

        // Now swipe to page 1..
        composeTestRule.onNodeWithTag("0").swipeAcrossCenter(
            distancePercentage = -MediumSwipeDistance,
            velocity = MediumVelocity,
        )
        // ...and assert that the page 2 is emitted
        assertThat(pageChangedChannel.receive()).isEqualTo(1)

        // Now swipe to page 2...
        composeTestRule.onNodeWithTag("1").swipeAcrossCenter(
            distancePercentage = -MediumSwipeDistance,
            velocity = MediumVelocity,
        )
        // ...and assert that the page 2 is emitted
        assertThat(pageChangedChannel.receive()).isEqualTo(2)

        // Now swipe back to page 1...
        composeTestRule.onNodeWithTag("2").swipeAcrossCenter(
            distancePercentage = MediumSwipeDistance,
            velocity = MediumVelocity,
        )
        // ...and assert that the page 1 is emitted
        assertThat(pageChangedChannel.receive()).isEqualTo(1)

        // Assert that there are no more events
        assertThat(pageChangedChannel.poll()).isNull()

        // Cancel the channel
        pageChangedChannel.cancel()
    }

    @Test
    @Ignore("Currently broken") // TODO: Will fix this once we move to Modifier.scrollable()
    fun a11yScroll() {
        setPagerContent(pageCount = 10)

        // Perform a scroll to item 1
        composeTestRule.onNodeWithTag("1").performScrollTo()

        // ...and assert that we scrolled to page 1
        assertPagerLayout(currentPage = 1)
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
        velocity: Float,
        distancePercentage: Float = 0f,
    ): SemanticsNodeInteraction

    // TODO: add test for state restoration?

    private fun assertPagerLayout(currentPage: Int) {
        // The pages which are expected to be laid out, using the given current page,
        // offscreenLimit and page limit
        val expectedLaidOutPages = (currentPage - offscreenLimit)..(currentPage + offscreenLimit)
            .coerceIn(0, pageCount)

        // Go through all of the pages, and assert the expected layout state
        (0 until pageCount).forEach { page ->
            if (page in expectedLaidOutPages) {
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
                composeTestRule.onNodeWithTag(page.toString()).assertDoesNotExist()
            }
        }
    }

    protected abstract fun SemanticsNodeInteraction.assertLaidOutItemPosition(
        page: Int,
        currentPage: Int,
    ): SemanticsNodeInteraction

    protected abstract fun setPagerContent(pageCount: Int): PagerState
}
