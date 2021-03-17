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
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.filters.LargeTest
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@OptIn(ExperimentalPagerApi::class) // Pager is currently experimental
@LargeTest
@RunWith(Parameterized::class)
class VerticalPagerTest(
    private val itemWidthFraction: Float,
    offscreenLimit: Int,
) : PagerTest(
    offscreenLimit = offscreenLimit,
    layoutDirection = LayoutDirection.Ltr, // Stick to LTR for vertical tests
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> = listOf(
            // itemWidthFraction, offscreenLimit

            // Test typical full-width items
            arrayOf(1f, 2),
            // Test an increased offscreenLimit
            arrayOf(1f, 4),
        )
    }

    override fun SemanticsNodeInteraction.swipeAcrossCenter(
        velocity: Float,
        distancePercentage: Float
    ): SemanticsNodeInteraction = swipeAcrossCenterWithVelocity(
        distancePercentageY = distancePercentage,
        velocity = velocity,
    )

    override fun setPagerContent(
        layoutDirection: LayoutDirection,
        pageCount: Int,
        offscreenLimit: Int,
    ): PagerState {
        val pagerState = PagerState(pageCount = pageCount)
        composeTestRule.setContent(layoutDirection) {
            HorizontalPager(
                state = pagerState,
                offscreenLimit = offscreenLimit,
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
