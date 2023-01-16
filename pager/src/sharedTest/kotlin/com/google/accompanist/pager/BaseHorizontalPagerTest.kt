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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import com.google.accompanist.testharness.TestHarness

/**
 * Contains [HorizontalPager] tests. This class is extended
 * in both the `androidTest` and `test` source sets for setup of the relevant
 * test runner.
 */
@OptIn(ExperimentalPagerApi::class) // Pager is currently experimental
abstract class BaseHorizontalPagerTest(
    private val itemWidthFraction: Float,
    private val contentPadding: PaddingValues,
    // We don't use the Dp type due to https://youtrack.jetbrains.com/issue/KT-35523
    private val itemSpacingDp: Int,
    private val layoutDirection: LayoutDirection,
    private val reverseLayout: Boolean,
) : PagerTest() {

    /**
     * Returns the expected resolved layout direction for pages
     */
    private val laidOutRtl: Boolean
        get() = if (layoutDirection == LayoutDirection.Rtl) !reverseLayout else reverseLayout

    override fun SemanticsNodeInteraction.swipeAcrossCenter(
        distancePercentage: Float,
        velocityPerSec: Dp,
    ): SemanticsNodeInteraction = swipeAcrossCenterWithVelocity(
        distancePercentageX = if (laidOutRtl) -distancePercentage else distancePercentage,
        velocityPerSec = velocityPerSec,
    )

    override fun SemanticsNodeInteraction.assertLaidOutItemPosition(
        page: Int,
        currentPage: Int,
        pageCount: Int,
        offset: Float
    ): SemanticsNodeInteraction {
        val rootBounds = composeTestRule.onRoot().getUnclippedBoundsInRoot()
        val expectedItemSize = (
            rootBounds.width -
                contentPadding.calculateLeftPadding(layoutDirection) -
                contentPadding.calculateRightPadding(layoutDirection)
            ) * itemWidthFraction
        val expectedItemSizeWithSpacing = expectedItemSize + itemSpacingDp.dp

        // The expected coordinates. This uses the implicit fact that VerticalPager by
        // use Alignment.CenterVertically by default, and that we're using items
        // with an aspect ratio of 1:1
        val expectedTop = (rootBounds.height - expectedItemSize) / 2
        val expectedFirstItemLeft = if (laidOutRtl) {
            (
                rootBounds.width -
                    expectedItemSize -
                    contentPadding.calculateRightPadding(layoutDirection)
                ) +
                (expectedItemSizeWithSpacing * offset)
        } else {
            contentPadding.calculateLeftPadding(layoutDirection) -
                (expectedItemSizeWithSpacing * offset)
        }

        return assertWidthIsEqualTo(expectedItemSize)
            .assertHeightIsAtLeast(expectedItemSize)
            .assertTopPositionInRootIsEqualTo(expectedTop)
            .run {
                val pageDelta = (expectedItemSizeWithSpacing * (page - currentPage))
                if (laidOutRtl) {
                    assertLeftPositionInRootIsEqualTo(expectedFirstItemLeft - pageDelta)
                } else {
                    assertLeftPositionInRootIsEqualTo(expectedFirstItemLeft + pageDelta)
                }
            }
    }

    @Composable
    override fun AbstractPagerContent(
        count: () -> Int,
        pagerState: PagerState,
        observeStateInContent: Boolean,
        pageToItem: (Int) -> String,
        useKeys: Boolean,
        userScrollEnabled: Boolean,
        onPageComposed: (Int) -> Unit
    ) {
        TestHarness(layoutDirection = layoutDirection) {
            applierScope = rememberCoroutineScope()

            Box {
                if (observeStateInContent) {
                    BasicText(text = "${pagerState.isScrollInProgress}")
                }

                HorizontalPager(
                    count = count(),
                    state = pagerState,
                    itemSpacing = itemSpacingDp.dp,
                    reverseLayout = reverseLayout,
                    contentPadding = contentPadding,
                    modifier = Modifier.fillMaxSize(),
                    key = if (useKeys) {
                        { pageToItem(it) }
                    } else {
                        null
                    },
                    userScrollEnabled = userScrollEnabled,
                ) { page ->
                    onPageComposed(page)
                    val item = pageToItem(page)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(itemWidthFraction)
                            .aspectRatio(1f)
                            .background(randomColor())
                            .testTag(item)
                    ) {
                        BasicText(
                            text = item,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}
