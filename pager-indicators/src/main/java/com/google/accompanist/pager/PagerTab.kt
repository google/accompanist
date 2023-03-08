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

@file:Suppress("DEPRECATION")
package com.google.accompanist.pager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.lerp
/**
 * This indicator syncs up a [TabRow] or [ScrollableTabRow] tab indicator with a
 * [HorizontalPager] or [VerticalPager]. See the sample for a full demonstration.
 *
 * @sample com.google.accompanist.sample.pager.PagerWithTabs
 */
@Deprecated(
    """
   pagerTabIndicatorOffset for accompanist Pagers are deprecated, please use the version that takes 
   androidx.compose.foundation.pager.PagerState instead
For more migration information, please visit https://google.github.io/accompanist/pager/#migration
"""
)
@ExperimentalPagerApi
fun Modifier.pagerTabIndicatorOffset(
    pagerState: PagerState,
    tabPositions: List<TabPosition>,
    pageIndexMapping: (Int) -> Int = { it },
): Modifier {
    val stateBridge = object : PagerStateBridge {
        override val currentPage: Int
            get() = pagerState.currentPage
        override val currentPageOffset: Float
            get() = pagerState.currentPageOffset
    }

    return pagerTabIndicatorOffset(stateBridge, tabPositions, pageIndexMapping)
}

/**
 * This indicator syncs up a [TabRow] or [ScrollableTabRow] tab indicator with a
 * [androidx.compose.foundation.pager.HorizontalPager] or
 * [androidx.compose.foundation.pager.VerticalPager].
 */
@OptIn(ExperimentalFoundationApi::class)
@ExperimentalPagerApi
fun Modifier.pagerTabIndicatorOffset(
    pagerState: androidx.compose.foundation.pager.PagerState,
    tabPositions: List<TabPosition>,
    pageIndexMapping: (Int) -> Int = { it },
): Modifier {
    val stateBridge = object : PagerStateBridge {
        override val currentPage: Int
            get() = pagerState.currentPage
        override val currentPageOffset: Float
            get() = pagerState.currentPageOffsetFraction
    }

    return pagerTabIndicatorOffset(stateBridge, tabPositions, pageIndexMapping)
}

private fun Modifier.pagerTabIndicatorOffset(
    pagerState: PagerStateBridge,
    tabPositions: List<TabPosition>,
    pageIndexMapping: (Int) -> Int = { it },
): Modifier = layout { measurable, constraints ->
    if (tabPositions.isEmpty()) {
        // If there are no pages, nothing to show
        layout(constraints.maxWidth, 0) {}
    } else {
        val currentPage = minOf(tabPositions.lastIndex, pageIndexMapping(pagerState.currentPage))
        val currentTab = tabPositions[currentPage]
        val previousTab = tabPositions.getOrNull(currentPage - 1)
        val nextTab = tabPositions.getOrNull(currentPage + 1)
        val fraction = pagerState.currentPageOffset
        val indicatorWidth = if (fraction > 0 && nextTab != null) {
            lerp(currentTab.width, nextTab.width, fraction).roundToPx()
        } else if (fraction < 0 && previousTab != null) {
            lerp(currentTab.width, previousTab.width, -fraction).roundToPx()
        } else {
            currentTab.width.roundToPx()
        }
        val indicatorOffset = if (fraction > 0 && nextTab != null) {
            lerp(currentTab.left, nextTab.left, fraction).roundToPx()
        } else if (fraction < 0 && previousTab != null) {
            lerp(currentTab.left, previousTab.left, -fraction).roundToPx()
        } else {
            currentTab.left.roundToPx()
        }
        val placeable = measurable.measure(
            Constraints(
                minWidth = indicatorWidth,
                maxWidth = indicatorWidth,
                minHeight = 0,
                maxHeight = constraints.maxHeight
            )
        )
        layout(constraints.maxWidth, maxOf(placeable.height, constraints.minHeight)) {
            placeable.placeRelative(
                indicatorOffset,
                maxOf(constraints.minHeight - placeable.height, 0)
            )
        }
    }
}

internal interface PagerStateBridge {
    val currentPage: Int
    val currentPageOffset: Float
}
