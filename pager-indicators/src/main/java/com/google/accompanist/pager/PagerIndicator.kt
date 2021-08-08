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
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * An horizontally laid out indicator for a [HorizontalPager] or [VerticalPager], representing
 * the currently active page and total pages drawn using a [Shape].
 *
 * This element allows the setting of the [indicatorShape], which defines how the
 * indicator is visually represented.
 *
 * @sample com.google.accompanist.sample.pager.HorizontalPagerIndicatorSample
 *
 * @param pagerState the state object of your [Pager] to be used to observe the list's state.
 * @param modifier the modifier to apply to this layout.
 * @param activeColor the color of the active Page indicator
 * @param inactiveColor the color of page indicators that are inactive. This defaults to
 * [activeColor] with the alpha component set to the [ContentAlpha.disabled].
 * @param indicatorWidth the width of each indicator in [Dp].
 * @param indicatorHeight the height of each indicator in [Dp]. Defaults to [indicatorWidth].
 * @param spacing the spacing between each indicator in [Dp].
 * @param indicatorShape the shape representing each indicator. This defaults to [CircleShape].
 */
@ExperimentalPagerApi
@Composable
fun HorizontalPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    activeColor: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
    inactiveColor: Color = activeColor.copy(ContentAlpha.disabled),
    indicatorWidth: Dp = 8.dp,
    indicatorHeight: Dp = indicatorWidth,
    spacing: Dp = indicatorWidth,
    indicatorShape: Shape = CircleShape,
) {

    val indicatorWidthPx = LocalDensity.current.run { indicatorWidth.roundToPx() }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val indicatorModifier = Modifier
                .size(width = indicatorWidth, height = indicatorHeight)
                .background(color = inactiveColor, shape = indicatorShape)

            repeat(pagerState.pageCount) {
                Box(indicatorModifier)
            }
        }

        Box(
            Modifier
                .offset {
                    val scrollPosition = (pagerState.currentPage + pagerState.currentPageOffset)
                        .coerceIn(0f, (pagerState.pageCount - 1).toFloat())
                    IntOffset(
                        x = ((spacing.roundToPx() + indicatorWidthPx) * scrollPosition).toInt(),
                        y = 0
                    )
                }
                .size(width = indicatorWidth, height = indicatorHeight)
                .background(
                    color = activeColor,
                    shape = indicatorShape,
                )
        )
    }
}

/**
 * An vertically laid out indicator for a [VerticalPager] or [HorizontalPager], representing
 * the currently active page and total pages drawn using a [Shape].
 *
 * This element allows the setting of the [indicatorShape], which defines how the
 * indicator is visually represented.
 *
 * @sample com.google.accompanist.sample.pager.VerticalPagerIndicatorSample
 *
 * @param pagerState the state object of your [Pager] to be used to observe the list's state.
 * @param modifier the modifier to apply to this layout.
 * @param activeColor the color of the active Page indicator
 * @param inactiveColor the color of page indicators that are inactive. This defaults to
 * [activeColor] with the alpha component set to the [ContentAlpha.disabled].
 * @param indicatorHeight the height of each indicator in [Dp].
 * @param indicatorWidth the width of each indicator in [Dp]. Defaults to [indicatorHeight].
 * @param spacing the spacing between each indicator in [Dp].
 * @param indicatorShape the shape representing each indicator. This defaults to [CircleShape].
 */
@ExperimentalPagerApi
@Composable
fun VerticalPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    activeColor: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
    inactiveColor: Color = activeColor.copy(ContentAlpha.disabled),
    indicatorHeight: Dp = 8.dp,
    indicatorWidth: Dp = indicatorHeight,
    spacing: Dp = indicatorHeight,
    indicatorShape: Shape = CircleShape,
) {

    val indicatorHeightPx = LocalDensity.current.run { indicatorHeight.roundToPx() }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val indicatorModifier = Modifier
                .size(width = indicatorWidth, height = indicatorHeight)
                .background(color = inactiveColor, shape = indicatorShape)

            repeat(pagerState.pageCount) {
                Box(indicatorModifier)
            }
        }

        Box(
            Modifier
                .offset {
                    val scrollPosition = (pagerState.currentPage + pagerState.currentPageOffset)
                        .coerceIn(0f, (pagerState.pageCount - 1).toFloat())
                    IntOffset(
                        x = 0,
                        y = ((spacing.roundToPx() + indicatorHeightPx) * scrollPosition).toInt(),
                    )
                }
                .size(width = indicatorWidth, height = indicatorHeight)
                .background(
                    color = activeColor,
                    shape = indicatorShape,
                )
        )
    }
}
