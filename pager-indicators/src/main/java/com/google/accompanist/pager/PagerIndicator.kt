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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * Contains default values used for [HorizontalPagerIndicator].
 */
object PagerIndicatorDefaults {
    /**
     * Default spacing used for [HorizontalPagerIndicator].
     */
    val Spacing = 8.dp

    /**
     * Default indicator size used for [HorizontalPagerIndicator].
     */
    val Size = 8.dp

    /**
     * Default shape used for [HorizontalPagerIndicator].
     */
    val Shape = CircleShape

    /**
     * Default Indicator color value used for [HorizontalPagerIndicator].
     */
    val IndicatorColor = Color.White

    /**
     * Default alpha value used for generating the inactive indicators color for [HorizontalPagerIndicator].
     */
    const val InactiveIndicatorColorAlpha = 0.4f
}

/**
 * An indicator for a [HorizontalPager] representing the currently active page and total pages
 * drawn using a [Shape].
 *
 * This element allows the setting of both the [shape] and [indicatorShape], which defines how the
 * Indicator is visually represented. By default Indicators are represented as [CircleShape].
 * When changing the [shape] by default the [indicatorShape] adjusts accordingly. If you want the
 * active page indicator to have a different appearance override [indicatorShape].
 *
 * @sample com.google.accompanist.sample.pager.HorizontalPagerIndicatorSample
 *
 * @param pagerState the state object of your [Pager] to be used to observe the list's state.
 * @param modifier the modifier to apply to this layout.
 * @param indicatorColor the color of the active Page indicator
 * @param color the color of page indicators that are inactive. This defaults to [indicatorColor]
 * with an alpha value as defined in [PagerIndicatorDefaults.InactiveIndicatorColorAlpha].
 * @param size the size of both indicators in dp.
 * @param spacing the spacing added between each indicator in dp.
 * @param shape the shape representing inactive pages.
 * @param indicatorShape the shape representing the active page. This defaults to [shape]
 */
@ExperimentalPagerApi
@Composable
fun HorizontalPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    indicatorColor: Color = PagerIndicatorDefaults.IndicatorColor,
    color: Color = indicatorColor.copy(PagerIndicatorDefaults.InactiveIndicatorColorAlpha),
    size: Dp = PagerIndicatorDefaults.Size,
    spacing: Dp = PagerIndicatorDefaults.Spacing,
    shape: Shape = PagerIndicatorDefaults.Shape,
    indicatorShape: Shape = shape
) {
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(pagerState.pageCount) {
                Box(
                    modifier = Modifier
                        .size(size)
                        .background(
                            color = color,
                            shape = shape,
                        )
                )
            }
        }

        Box(
            modifier = Modifier
                .offset {
                    val scrollPosition = pagerState.currentPage + pagerState.currentPageOffset
                    IntOffset(x = ((spacing + size) * scrollPosition).roundToPx(), y = 0)
                }
                .size(size)
                .background(
                    color = indicatorColor,
                    shape = indicatorShape,
                )

        )
    }
}

/**
 * An indicator for a [VerticalPager] representing the currently active page and total pages
 * drawn using a [Shape].
 *
 * This element allows the setting of both the [shape] and [indicatorShape], which defines how the
 * Indicator is visually represented. By default Indicators are represented as [CircleShape].
 * When changing the [shape] by default the [indicatorShape] adjusts accordingly. If you want the
 * active page indicator to have a different appearance override [indicatorShape].
 *
 * @sample com.google.accompanist.sample.pager.HorizontalPagerIndicatorSample
 *
 * @param pagerState the state object of your [Pager] to be used to observe the list's state.
 * @param modifier the modifier to apply to this layout.
 * @param indicatorColor the color of the active Page indicator
 * @param color the color of page indicators that are inactive. This defaults to [indicatorColor]
 * with an alpha value as defined in [PagerIndicatorDefaults.InactiveIndicatorColorAlpha].
 * @param size the size of both indicators in dp.
 * @param spacing the spacing added between each indicator in dp.
 * @param shape the shape representing inactive pages.
 * @param indicatorShape the shape representing the active page. This defaults to [shape]
 */
@ExperimentalPagerApi
@Composable
fun VerticalPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    indicatorColor: Color = PagerIndicatorDefaults.IndicatorColor,
    color: Color = indicatorColor.copy(PagerIndicatorDefaults.InactiveIndicatorColorAlpha),
    size: Dp = PagerIndicatorDefaults.Size,
    spacing: Dp = PagerIndicatorDefaults.Spacing,
    shape: Shape = PagerIndicatorDefaults.Shape,
    indicatorShape: Shape = shape
) {
    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        Column(
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            repeat(pagerState.pageCount) {
                Box(
                    modifier = Modifier
                        .size(size)
                        .background(
                            color = color,
                            shape = shape,
                        )
                )
            }
        }

        Box(
            modifier = Modifier
                .offset {
                    val scrollPosition = pagerState.currentPage + pagerState.currentPageOffset
                    IntOffset(x = 0, y = ((spacing + size) * scrollPosition).roundToPx())
                }
                .size(size)
                .background(
                    color = indicatorColor,
                    shape = indicatorShape,
                )

        )
    }
}
