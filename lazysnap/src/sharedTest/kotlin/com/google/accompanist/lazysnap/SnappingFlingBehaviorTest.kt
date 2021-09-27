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

package com.google.accompanist.lazysnap

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import org.junit.Rule
import org.junit.Test

private const val MediumSwipeDistance = 0.75f
private const val ShortSwipeDistance = 0.4f

private val FastVelocity = 2000.dp
private val MediumVelocity = 700.dp
private val SlowVelocity = 100.dp

internal val ItemSize = 200.dp

@OptIn(ExperimentalLazySnapApi::class) // Pager is currently experimental
abstract class SnappingFlingBehaviorTest(
    private val maxScrollDistanceDp: Float,
) {
    @get:Rule
    val rule = createComposeRule()

    /**
     * This is a workaround for https://issuetracker.google.com/issues/179492185.
     * Ideally we would have a way to get the applier scope from the rule
     */
    protected lateinit var applierScope: CoroutineScope

    @Test
    fun swipe() {
        val lazyListState = LazyListState()
        val snappingFlingBehavior = createSnappingFlingBehavior(lazyListState)
        setTestContent(
            flingBehavior = snappingFlingBehavior,
            lazyListState = lazyListState,
            count = 10,
        )

        // First test swiping towards end, from 0 to -1, which should no-op
        rule.onNodeWithTag("0").swipeAcrossCenter(MediumSwipeDistance)
        rule.waitForIdle()
        // ...and assert that nothing happened
        lazyListState.assertCurrentItem(index = 0, offset = 0)

        // Now swipe towards start, from page 0
        rule.onNodeWithTag("0").swipeAcrossCenter(-MediumSwipeDistance)
        rule.waitForIdle()

        // ...and assert that we now laid out from page 1
        lazyListState.assertCurrentItem(minIndex = 1, offset = 0)
    }

    @Test
    fun swipeToEndAndBack() {
        val lazyListState = LazyListState()
        val snappingFlingBehavior = createSnappingFlingBehavior(lazyListState)
        setTestContent(
            flingBehavior = snappingFlingBehavior,
            lazyListState = lazyListState,
            count = 4,
        )

        // Now swipe towards start, from page 0 to page 1 and assert the layout
        rule.onNodeWithTag("0").swipeAcrossCenter(-MediumSwipeDistance)
        rule.waitForIdle()
        lazyListState.assertCurrentItem(minIndex = 1, offset = 0)

        // Repeat for 1 -> 2
        rule.onNodeWithTag("1").swipeAcrossCenter(-MediumSwipeDistance)
        rule.waitForIdle()
        lazyListState.assertCurrentItem(minIndex = 2, offset = 0)

        // Repeat for 2 -> 3
        rule.onNodeWithTag("2").swipeAcrossCenter(-MediumSwipeDistance)
        rule.waitForIdle()
        lazyListState.assertCurrentItem(index = 3, offset = 0)

        // Swipe past the last item. We shouldn't move
        rule.onNodeWithTag("3").swipeAcrossCenter(-MediumSwipeDistance)
        rule.waitForIdle()
        lazyListState.assertCurrentItem(index = 3, offset = 0)

        // Swipe back from 3 -> 2
        rule.onNodeWithTag("3").swipeAcrossCenter(MediumSwipeDistance)
        rule.waitForIdle()
        lazyListState.assertCurrentItem(maxIndex = 2, offset = 0)

        // Swipe back from 2 -> 1
        rule.onNodeWithTag("2").swipeAcrossCenter(MediumSwipeDistance)
        rule.waitForIdle()
        lazyListState.assertCurrentItem(maxIndex = 1, offset = 0)

        // Swipe back from 1 -> 0
        rule.onNodeWithTag("1").swipeAcrossCenter(MediumSwipeDistance)
        rule.waitForIdle()
        lazyListState.assertCurrentItem(index = 0, offset = 0)

        // Swipe past the first item. We shouldn't move
        rule.onNodeWithTag("0").swipeAcrossCenter(MediumSwipeDistance)
        rule.waitForIdle()
        lazyListState.assertCurrentItem(index = 0, offset = 0)
    }

    @Test
    fun mediumDistance_fastSwipe_toFling() {
        rule.mainClock.autoAdvance = false

        val lazyListState = LazyListState()
        val snappingFlingBehavior = createSnappingFlingBehavior(lazyListState)
        setTestContent(
            flingBehavior = snappingFlingBehavior,
            lazyListState = lazyListState,
            count = 10,
        )

        assertThat(lazyListState.isScrollInProgress).isFalse()
        assertThat(snappingFlingBehavior.animationTarget).isNull()
        lazyListState.assertCurrentItem(index = 0, offset = 0)

        // Now swipe towards start, from page 0 to page 1, over a medium distance of the item width.
        // This should trigger a fling
        rule.onNodeWithTag("0").swipeAcrossCenter(
            distancePercentage = -MediumSwipeDistance,
            velocityPerSec = FastVelocity,
        )

        assertThat(lazyListState.isScrollInProgress).isTrue()
        assertThat(snappingFlingBehavior.animationTarget).isAtLeast(1)

        // Now re-enable the clock advancement and let the fling animation run
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()

        // ...and assert that we now laid out from page 1
        lazyListState.assertCurrentItem(minIndex = 1, offset = 0)
    }

    @Test
    fun mediumDistance_slowSwipe_toSnapForward() {
        rule.mainClock.autoAdvance = false

        val lazyListState = LazyListState()
        val snappingFlingBehavior = createSnappingFlingBehavior(lazyListState)
        setTestContent(
            flingBehavior = snappingFlingBehavior,
            lazyListState = lazyListState,
            count = 10,
        )

        assertThat(lazyListState.isScrollInProgress).isFalse()
        assertThat(snappingFlingBehavior.animationTarget).isNull()
        lazyListState.assertCurrentItem(index = 0, offset = 0)

        // Now swipe towards start, from page 0 to page 1, over a medium distance of the item width.
        // This should trigger a spring to position 1
        rule.onNodeWithTag("0").swipeAcrossCenter(
            distancePercentage = -MediumSwipeDistance,
            velocityPerSec = SlowVelocity,
        )

        assertThat(lazyListState.isScrollInProgress).isTrue()
        assertThat(snappingFlingBehavior.animationTarget).isEqualTo(1)

        // Now re-enable the clock advancement and let the snap animation run
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()

        // ...and assert that we now laid out from page 1
        lazyListState.assertCurrentItem(index = 1, offset = 0)
    }

    @Test
    fun shortDistance_fastSwipe_toFling() {
        rule.mainClock.autoAdvance = false

        val lazyListState = LazyListState()
        val snappingFlingBehavior = createSnappingFlingBehavior(lazyListState)
        setTestContent(
            flingBehavior = snappingFlingBehavior,
            lazyListState = lazyListState,
            count = 10,
        )

        assertThat(lazyListState.isScrollInProgress).isFalse()
        assertThat(snappingFlingBehavior.animationTarget).isNull()
        lazyListState.assertCurrentItem(index = 0, offset = 0)

        // Now swipe towards start, from page 0 to page 1, over a short distance of the item width.
        // This should trigger a spring back to the original position
        rule.onNodeWithTag("0").swipeAcrossCenter(
            distancePercentage = -ShortSwipeDistance,
            velocityPerSec = FastVelocity,
        )

        assertThat(lazyListState.isScrollInProgress).isTrue()
        assertThat(snappingFlingBehavior.animationTarget).isAtLeast(1)

        // Now re-enable the clock advancement and let the fling animation run
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()

        // ...and assert that we now laid out from page 1
        lazyListState.assertCurrentItem(minIndex = 1, offset = 0)
    }

    @Test
    fun shortDistance_slowSwipe_toSnapBack() {
        rule.mainClock.autoAdvance = false

        val lazyListState = LazyListState()
        val snappingFlingBehavior = createSnappingFlingBehavior(lazyListState)
        setTestContent(
            flingBehavior = snappingFlingBehavior,
            lazyListState = lazyListState,
            count = 10,
        )

        assertThat(lazyListState.isScrollInProgress).isFalse()
        assertThat(snappingFlingBehavior.animationTarget).isNull()
        lazyListState.assertCurrentItem(index = 0, offset = 0)

        // Now swipe towards start, from page 0 to page 1, over a short distance of the item width.
        // This should trigger a spring back to the original position
        rule.onNodeWithTag("0").swipeAcrossCenter(
            distancePercentage = -ShortSwipeDistance,
            velocityPerSec = SlowVelocity,
        )

        assertThat(lazyListState.isScrollInProgress).isTrue()
        assertThat(snappingFlingBehavior.animationTarget).isEqualTo(0)

        // Now re-enable the clock advancement and let the snap animation run
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()

        // ...and assert that we 'sprang back' to page 0
        lazyListState.assertCurrentItem(index = 0, offset = 0)
    }

    /**
     * Swipe across the center of the node. The major axis of the swipe is defined by the
     * overriding test.
     *
     * @param distancePercentage The swipe distance in percentage of the node's size.
     * Negative numbers mean swipe towards the start, positive towards the end.
     * @param velocityPerSec Target end velocity for the swipe in Dps per second
     */
    abstract fun SemanticsNodeInteraction.swipeAcrossCenter(
        distancePercentage: Float,
        velocityPerSec: Dp = MediumVelocity
    ): SemanticsNodeInteraction

    private fun setTestContent(
        count: Int,
        lazyListState: LazyListState = LazyListState(),
        flingBehavior: SnappingFlingBehavior = createSnappingFlingBehavior(lazyListState),
    ) {
        setTestContent(
            flingBehavior = flingBehavior,
            count = { count },
            lazyListState = lazyListState,
        )
    }

    protected abstract fun setTestContent(
        flingBehavior: SnappingFlingBehavior,
        count: () -> Int,
        lazyListState: LazyListState = LazyListState(),
    )

    private fun createSnappingFlingBehavior(
        lazyListState: LazyListState
    ): SnappingFlingBehavior {
        return SnappingFlingBehavior(
            lazyListState = lazyListState,
            snapOffsetForItem = SnapOffsets.Start,
            maximumFlingDistance = {
                with(rule.density) { maxScrollDistanceDp.dp.roundToPx() }
            }
        )
    }
}

/**
 * This doesn't handle the scroll range < lazy size, but that won't happen in these tests
 */
private fun LazyListState.isScrolledToEnd(): Boolean {
    val lastVisibleItem = layoutInfo.visibleItemsInfo.last()
    if (lastVisibleItem.index == layoutInfo.totalItemsCount - 1) {
        // This isn't perfect as it doesn't properly handle content padding, but good enough
        return (lastVisibleItem.offset + lastVisibleItem.size) <= layoutInfo.viewportEndOffset
    }
    return false
}

private fun LazyListState.logVisibleItems() = Napier.d(
    message = {
        "Visible Items. " + layoutInfo.visibleItemsInfo.joinToString { it.log() }
    }
)

private fun LazyListState.assertCurrentItem(
    index: Int,
    offset: Int = 0,
) = assertCurrentItem(minIndex = index, maxIndex = index, offset = offset)

private fun LazyListState.assertCurrentItem(
    minIndex: Int = 0,
    maxIndex: Int = Int.MAX_VALUE,
    offset: Int = 0,
) {
    if (isScrolledToEnd()) return

    currentItem.let {
        assertThat(it.index).isIn(minIndex..maxIndex)
        assertThat(it.offset).isEqualTo(offset)
    }
}

private val LazyListState.currentItem: LazyListItemInfo
    get() = layoutInfo.visibleItemsInfo.asSequence()
        .filter { it.offset <= 0 }
        .last()
