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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.filters.LargeTest
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@OptIn(ExperimentalPagerApi::class) // Pager is currently experimental
@LargeTest
@RunWith(Parameterized::class)
class HorizontalPagerTest(
    private val itemWidthFraction: Float,
    private val horizontalAlignment: Alignment.Horizontal,
    // We don't use the Dp type due to https://youtrack.jetbrains.com/issue/KT-35523
    private val itemSpacingDp: Int,
    override val offscreenLimit: Int,
    private val layoutDirection: LayoutDirection,
    private val reverseLayout: Boolean,
) : PagerTest() {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> = listOf(
            // itemWidthFraction, horizontalAlignment, itemSpacing,
            // offscreenLimit, layoutDirection, reverseLayout

            // Test typical full-width items
            arrayOf(1f, Alignment.Start, 0, 2, LayoutDirection.Ltr, false),
            arrayOf(1f, Alignment.CenterHorizontally, 0, 2, LayoutDirection.Ltr, false),
            arrayOf(1f, Alignment.End, 0, 2, LayoutDirection.Ltr, false),
            arrayOf(1f, Alignment.Start, 0, 2, LayoutDirection.Rtl, false),
            arrayOf(1f, Alignment.CenterHorizontally, 0, 2, LayoutDirection.Rtl, false),
            arrayOf(1f, Alignment.End, 0, 2, LayoutDirection.Rtl, false),

            // Full-width items with item spacing
            arrayOf(1f, Alignment.Start, 4, 2, LayoutDirection.Ltr, false),
            arrayOf(1f, Alignment.CenterHorizontally, 4, 2, LayoutDirection.Ltr, false),
            arrayOf(1f, Alignment.End, 4, 2, LayoutDirection.Ltr, false),
            arrayOf(1f, Alignment.Start, 4, 2, LayoutDirection.Rtl, false),
            arrayOf(1f, Alignment.CenterHorizontally, 4, 2, LayoutDirection.Rtl, false),
            arrayOf(1f, Alignment.End, 4, 2, LayoutDirection.Rtl, false),

            // Full-width items with reverseLayout = true
            arrayOf(1f, Alignment.Start, 0, 2, LayoutDirection.Ltr, true),
            arrayOf(1f, Alignment.CenterHorizontally, 0, 2, LayoutDirection.Ltr, true),
            arrayOf(1f, Alignment.End, 0, 2, LayoutDirection.Ltr, true),
            arrayOf(1f, Alignment.Start, 0, 2, LayoutDirection.Rtl, true),
            arrayOf(1f, Alignment.CenterHorizontally, 0, 2, LayoutDirection.Rtl, true),
            arrayOf(1f, Alignment.End, 0, 2, LayoutDirection.Rtl, true),

            // Test an increased offscreenLimit
            arrayOf(1f, Alignment.CenterHorizontally, 0, 4, LayoutDirection.Ltr, false),
            arrayOf(1f, Alignment.CenterHorizontally, 0, 4, LayoutDirection.Rtl, false),

            // Test items with 80% widths
            arrayOf(0.8f, Alignment.Start, 0, 2, LayoutDirection.Ltr, false),
            arrayOf(0.8f, Alignment.CenterHorizontally, 0, 2, LayoutDirection.Ltr, false),
            arrayOf(0.8f, Alignment.End, 0, 2, LayoutDirection.Ltr, false),
            arrayOf(0.8f, Alignment.Start, 0, 2, LayoutDirection.Rtl, false),
            arrayOf(0.8f, Alignment.CenterHorizontally, 0, 2, LayoutDirection.Rtl, false),
            arrayOf(0.8f, Alignment.End, 0, 2, LayoutDirection.Rtl, false),
        )
    }

    /**
     * Returns the expected resolved layout direction for pages
     */
    private val reverseDirection: Boolean
        get() = if (layoutDirection == LayoutDirection.Rtl) !reverseLayout else reverseLayout

    override val pageCount: Int
        get() = 10

    override fun SemanticsNodeInteraction.swipeAcrossCenter(
        velocity: Float,
        distancePercentage: Float
    ): SemanticsNodeInteraction = swipeAcrossCenterWithVelocity(
        distancePercentageX = if (reverseDirection) -distancePercentage else distancePercentage,
        velocity = velocity,
    )

    override fun SemanticsNodeInteraction.assertLaidOutItemPosition(
        page: Int,
        currentPage: Int,
    ): SemanticsNodeInteraction {
        val rootBounds = composeTestRule.onRoot().getUnclippedBoundsInRoot()
        val expectedItemSize = rootBounds.width * itemWidthFraction

        // The expected coordinates. This uses the implicit fact that VerticalPager by
        // use Alignment.CenterVertically by default, and that we're using items
        // with an aspect ratio of 1:1
        val expectedTop = (rootBounds.height - expectedItemSize) / 2
        val expectedFirstItemLeft = when (horizontalAlignment) {
            Alignment.Start -> {
                when (layoutDirection) {
                    LayoutDirection.Ltr -> 0.dp
                    else -> rootBounds.width - expectedItemSize
                }
            }
            Alignment.End -> {
                when (layoutDirection) {
                    LayoutDirection.Ltr -> rootBounds.width - expectedItemSize
                    else -> 0.dp
                }
            }
            else /* Alignment.CenterVertically */ -> (rootBounds.width - expectedItemSize) / 2
        }

        return assertWidthIsEqualTo(expectedItemSize)
            .assertHeightIsAtLeast(expectedItemSize)
            .assertTopPositionInRootIsEqualTo(expectedTop)
            .run {
                val pageDelta = ((expectedItemSize + itemSpacingDp.dp) * (page - currentPage))
                if (reverseDirection) {
                    assertLeftPositionInRootIsEqualTo(expectedFirstItemLeft - pageDelta)
                } else {
                    assertLeftPositionInRootIsEqualTo(expectedFirstItemLeft + pageDelta)
                }
            }
    }

    override fun setPagerContent(
        pageCount: Int,
    ): PagerState {
        val pagerState = PagerState(
            pageCount = pageCount,
            offscreenLimit = offscreenLimit,
        )
        composeTestRule.setContent(layoutDirection) {
            HorizontalPager(
                state = pagerState,
                itemSpacing = itemSpacingDp.dp,
                reverseLayout = reverseLayout,
                horizontalAlignment = horizontalAlignment,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(itemWidthFraction)
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
