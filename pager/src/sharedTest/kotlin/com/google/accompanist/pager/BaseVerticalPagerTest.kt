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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
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

/**
 * Contains the [VerticalPager] tests. This class is extended
 * in both the `androidTest` and `test` source sets for setup of the relevant
 * test runner.
 */
@OptIn(ExperimentalPagerApi::class) // Pager is currently experimental
abstract class BaseVerticalPagerTest(
    private val contentPadding: PaddingValues,
    // We don't use the Dp type due to https://youtrack.jetbrains.com/issue/KT-35523
    private val itemSpacingDp: Int,
    private val reverseLayout: Boolean,
) : PagerTest() {

    override fun SemanticsNodeInteraction.swipeAcrossCenter(
        distancePercentage: Float,
        velocityPerSec: Dp,
    ): SemanticsNodeInteraction = swipeAcrossCenterWithVelocity(
        distancePercentageY = if (reverseLayout) -distancePercentage else distancePercentage,
        velocityPerSec = velocityPerSec,
    )

    override fun SemanticsNodeInteraction.assertLaidOutItemPosition(
        page: Int,
        currentPage: Int,
        offset: Float,
    ): SemanticsNodeInteraction {
        val rootBounds = composeTestRule.onRoot().getUnclippedBoundsInRoot()
        val expectedItemHeight = rootBounds.height -
            contentPadding.calculateTopPadding() -
            contentPadding.calculateBottomPadding()
        val expectedItemWidth = rootBounds.width

        val expectedLeft = (rootBounds.width - expectedItemWidth) / 2
        val expectedFirstItemTop = when (reverseLayout) {
            true -> (rootBounds.height - contentPadding.calculateBottomPadding() - expectedItemHeight) + (expectedItemHeight * offset)
            false -> contentPadding.calculateTopPadding() - (expectedItemHeight * offset)
        }

        return assertWidthIsEqualTo(expectedItemWidth)
            .assertHeightIsAtLeast(expectedItemHeight)
            .assertLeftPositionInRootIsEqualTo(expectedLeft)
            .run {
                val pageDelta = ((expectedItemHeight + itemSpacingDp.dp) * (page - currentPage))
                if (reverseLayout) {
                    assertTopPositionInRootIsEqualTo(expectedFirstItemTop - pageDelta)
                } else {
                    assertTopPositionInRootIsEqualTo(expectedFirstItemTop + pageDelta)
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
        onPageComposed: (Int) -> Unit
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            applierScope = rememberCoroutineScope()

            Box {
                if (observeStateInContent) {
                    BasicText(text = "${pagerState.isScrollInProgress}")
                }

                VerticalPager(
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
                    }
                ) { page ->
                    onPageComposed(page)
                    val item = pageToItem(page)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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
