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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.filters.LargeTest
import com.google.common.truth.Truth.assertThat
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@OptIn(ExperimentalPagerApi::class) // Pager is currently experimental
@LargeTest
@RunWith(Parameterized::class)
class VerticalPagerTest(
    private val verticalAlignment: Alignment.Vertical,
    // We don't use the Dp type due to https://youtrack.jetbrains.com/issue/KT-35523
    private val itemSpacingDp: Int,
    override val offscreenLimit: Int,
    private val reverseLayout: Boolean,
    override val infiniteLoop: Boolean,
) : PagerTest() {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> = dataset(false) + dataset(true)

        private fun dataset(looping: Boolean): Collection<Array<Any>> = listOf(
            // verticalAlignment, itemSpacingDp, offscreenLimit, reverseLayout, infiniteLoop

            // Test typical full-width items
            arrayOf(Alignment.CenterVertically, 0, 2, false, looping),
            arrayOf(Alignment.Top, 0, 2, false, looping),
            arrayOf(Alignment.Bottom, 0, 2, false, looping),

            // Full-width items with spacing
            arrayOf(Alignment.CenterVertically, 4, 2, false, looping),
            arrayOf(Alignment.Top, 4, 2, false, looping),
            arrayOf(Alignment.Bottom, 4, 2, false, looping),

            // Full-width items with reverseLayout = true
            arrayOf(Alignment.CenterVertically, 0, 2, true, looping),
            arrayOf(Alignment.Top, 0, 2, true, looping),
            arrayOf(Alignment.Bottom, 0, 2, true, looping),

            // Test an increased offscreenLimit
            arrayOf(Alignment.CenterVertically, 0, 4, false, looping),
        )
    }

    override fun SemanticsNodeInteraction.swipeAcrossCenter(
        distancePercentage: Float,
        velocity: Float,
    ): SemanticsNodeInteraction = swipeAcrossCenterWithVelocity(
        distancePercentageY = if (reverseLayout) -distancePercentage else distancePercentage,
        velocity = velocity,
    )

    override fun SemanticsNodeInteraction.assertLaidOutItemPosition(
        page: Int,
        currentPage: Int
    ): SemanticsNodeInteraction {
        val rootBounds = composeTestRule.onRoot().getUnclippedBoundsInRoot()
        val expectedItemSize = rootBounds.width

        // The expected coordinates. This uses the implicit fact that VerticalPager by
        // use Alignment.CenterVertically by default, and that we're using items
        // with an aspect ratio of 1:1
        val expectedLeft = (rootBounds.width - expectedItemSize) / 2
        val expectedFirstItemTop = when (verticalAlignment) {
            Alignment.Top -> 0.dp
            Alignment.Bottom -> rootBounds.height - expectedItemSize
            else /* Alignment.CenterVertically */ -> (rootBounds.height - expectedItemSize) / 2
        }

        return assertWidthIsEqualTo(expectedItemSize)
            .assertHeightIsAtLeast(expectedItemSize)
            .assertLeftPositionInRootIsEqualTo(expectedLeft)
            .run {
                val pageDelta = ((expectedItemSize + itemSpacingDp.dp) * (page - currentPage))
                // Not sure why, but there's a rounding error somewhere.
                // TODO: reduce this tolerance and work out where the issue is
                if (reverseLayout) {
                    assertTopPositionInRootIsEqualTo(
                        expectedTop = expectedFirstItemTop - pageDelta,
                        tolerance = 1.dp
                    )
                } else {
                    assertTopPositionInRootIsEqualTo(
                        expectedTop = expectedFirstItemTop + pageDelta,
                        tolerance = 1.dp
                    )
                }
            }
    }

    override fun setPagerContent(pageCount: Int): PagerState {
        val pagerState = PagerState(
            pageCount = pageCount,
            offscreenLimit = offscreenLimit,
            infiniteLoop = infiniteLoop,
        ).apply { testing = true }
        // Stick to LTR for vertical tests
        composeTestRule.setContent(LayoutDirection.Ltr) {
            applierScope = rememberCoroutineScope()

            VerticalPager(
                state = pagerState,
                itemSpacing = itemSpacingDp.dp,
                reverseLayout = reverseLayout,
                verticalAlignment = verticalAlignment,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(randomColor())
                        .testTag(page.toString())
                ) {
                    BasicText(
                        text = page.toString(),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
        return pagerState
    }
}

/**
 * A version of `assertTopPositionInRootIsEqualTo` which allows setting of the tolerance.
 */
private fun SemanticsNodeInteraction.assertTopPositionInRootIsEqualTo(
    expectedTop: Dp,
    tolerance: Dp = 0.5.dp
): SemanticsNodeInteraction {
    assertThat(getUnclippedBoundsInRoot().top.value)
        .isWithin(tolerance.value)
        .of(expectedTop.value)
    return this
}
