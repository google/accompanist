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
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import com.google.accompanist.internal.test.combineWithParameters
import com.google.accompanist.internal.test.parameterizedParams

@OptIn(ExperimentalPagerApi::class) // Pager is currently experimental
abstract class BaseHorizontalPagerTest(
    private val itemWidthFraction: Float,
    private val horizontalAlignment: Alignment.Horizontal,
    // We don't use the Dp type due to https://youtrack.jetbrains.com/issue/KT-35523
    private val itemSpacingDp: Int,
    override val offscreenLimit: Int,
    private val layoutDirection: LayoutDirection,
    private val reverseLayout: Boolean,
    override val infiniteLoop: Boolean,
) : PagerTest() {
    protected companion object {
        @JvmStatic
        protected fun params(): Collection<Array<Any>> = parameterizedParams()
            // itemWidthFraction
            .combineWithParameters(1f, 0.8f)
            // horizontalAlignment
            .combineWithParameters(Alignment.CenterHorizontally, Alignment.Start, Alignment.End)
            // itemSpacing
            .combineWithParameters(0, 4)
            // offscreenLimit
            .combineWithParameters(1, 2)
            // layoutDirection
            .combineWithParameters(LayoutDirection.Ltr, LayoutDirection.Rtl)
            // reverseLayout
            .combineWithParameters(true, false)
            // looping
            .combineWithParameters(true, false)
    }

    /**
     * Returns the expected resolved layout direction for pages
     */
    private val reverseDirection: Boolean
        get() = if (layoutDirection == LayoutDirection.Rtl) !reverseLayout else reverseLayout

    override fun SemanticsNodeInteraction.swipeAcrossCenter(
        distancePercentage: Float,
        velocity: Float,
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
            infiniteLoop = infiniteLoop,
        ).apply { testing = true }
        composeTestRule.setContent(layoutDirection) {
            applierScope = rememberCoroutineScope()

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
