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

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue
import kotlin.math.sign
import androidx.compose.foundation.pager.PagerState as FoundationPagerState

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
 * @param pageCount the size of indicators should be displayed, defaults to [PagerState.pageCount].
 * If you are implementing a looping pager with a much larger [PagerState.pageCount]
 * than indicators should displayed, e.g. [Int.MAX_VALUE], specify you real size in this param.
 * @param pageIndexMapping describe how to get the position of active indicator by the giving page
 * from [PagerState.currentPage], if [pageCount] is not equals to [PagerState.pageCount].
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
    pageCount: Int = pagerState.pageCount,
    pageIndexMapping: (Int) -> Int = { it },
    activeColor: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
    inactiveColor: Color = activeColor.copy(ContentAlpha.disabled),
    indicatorWidth: Dp = 8.dp,
    indicatorHeight: Dp = indicatorWidth,
    spacing: Dp = indicatorWidth,
    indicatorShape: Shape = CircleShape,
) {
    val stateBridge = remember(pagerState) {
        object : PagerStateBridge {
            override val currentPage: Int
                get() = pagerState.currentPage
            override val currentPageOffset: Float
                get() = pagerState.currentPageOffset
        }
    }

    HorizontalPagerIndicator(
        pagerState = stateBridge,
        pageCount = pageCount,
        modifier = modifier,
        pageIndexMapping = pageIndexMapping,
        activeColor = activeColor,
        inactiveColor = inactiveColor,
        indicatorHeight = indicatorHeight,
        indicatorWidth = indicatorWidth,
        spacing = spacing,
        indicatorShape = indicatorShape
    )
}

/**
 * An horizontally laid out indicator for a [HorizontalPager] or [VerticalPager], representing
 * the currently active page and total pages drawn using a [Shape].
 *
 * This element allows the setting of the [indicatorShape], which defines how the
 * indicator is visually represented.
 *
 * @sample com.google.accompanist.sample.pager.HorizontalPagerIndicatorSample
 *
 * @param pagerState The Compose Foundation PagerState object of your [Pager] to be used to observe
 * the list's state.
 * @param modifier the modifier to apply to this layout.
 * @param pageCount the size of indicators should be displayed.
 * If you are implementing a looping pager with a much larger [pageCount]
 * than indicators should displayed, e.g. [Int.MAX_VALUE], specify you real size in this param.
 * @param pageIndexMapping describe how to get the position of active indicator by the giving page
 * from [PagerState.currentPage].
 * @param activeColor the color of the active Page indicator
 * @param inactiveColor the color of page indicators that are inactive. This defaults to
 * [activeColor] with the alpha component set to the [ContentAlpha.disabled].
 * @param indicatorWidth the width of each indicator in [Dp].
 * @param indicatorHeight the height of each indicator in [Dp]. Defaults to [indicatorWidth].
 * @param spacing the spacing between each indicator in [Dp].
 * @param indicatorShape the shape representing each indicator. This defaults to [CircleShape].
 */
@OptIn(ExperimentalFoundationApi::class)
@ExperimentalPagerApi
@Composable
fun HorizontalPagerIndicator(
    pagerState: FoundationPagerState,
    pageCount: Int,
    modifier: Modifier = Modifier,
    pageIndexMapping: (Int) -> Int = { it },
    activeColor: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
    inactiveColor: Color = activeColor.copy(ContentAlpha.disabled),
    indicatorWidth: Dp = 8.dp,
    indicatorHeight: Dp = indicatorWidth,
    spacing: Dp = indicatorWidth,
    indicatorShape: Shape = CircleShape,
) {
    val stateBridge = remember(pagerState) {
        object : PagerStateBridge {
            override val currentPage: Int
                get() = pagerState.currentPage
            override val currentPageOffset: Float
                get() = pagerState.currentPageOffsetFraction
        }
    }

    HorizontalPagerIndicator(
        pagerState = stateBridge,
        pageCount = pageCount,
        modifier = modifier,
        pageIndexMapping = pageIndexMapping,
        activeColor = activeColor,
        inactiveColor = inactiveColor,
        indicatorHeight = indicatorHeight,
        indicatorWidth = indicatorWidth,
        spacing = spacing,
        indicatorShape = indicatorShape
    )
}

@Composable
private fun HorizontalPagerIndicator(
    pagerState: PagerStateBridge,
    pageCount: Int,
    modifier: Modifier = Modifier,
    pageIndexMapping: (Int) -> Int = { it },
    activeColor: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
    inactiveColor: Color = activeColor.copy(ContentAlpha.disabled),
    indicatorWidth: Dp = 8.dp,
    indicatorHeight: Dp = indicatorWidth,
    spacing: Dp = indicatorWidth,
    indicatorShape: Shape = CircleShape,
) {

    val indicatorWidthPx = LocalDensity.current.run { indicatorWidth.roundToPx() }
    val spacingPx = LocalDensity.current.run { spacing.roundToPx() }

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

            repeat(pageCount) {
                Box(indicatorModifier)
            }
        }

        Box(
            Modifier
                .offset {
                    val position = pageIndexMapping(pagerState.currentPage)
                    val offset = pagerState.currentPageOffset
                    val next = pageIndexMapping(pagerState.currentPage + offset.sign.toInt())
                    val scrollPosition = ((next - position) * offset.absoluteValue + position)
                        .coerceIn(
                            0f,
                            (pageCount - 1)
                                .coerceAtLeast(0)
                                .toFloat()
                        )

                    IntOffset(
                        x = ((spacingPx + indicatorWidthPx) * scrollPosition).toInt(),
                        y = 0
                    )
                }
                .size(width = indicatorWidth, height = indicatorHeight)
                .then(
                    if (pageCount > 0) Modifier.background(
                        color = activeColor,
                        shape = indicatorShape,
                    )
                    else Modifier
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
 * @param pageCount the size of indicators should be displayed, defaults to [PagerState.pageCount].
 * If you are implementing a looping pager with a much larger [PagerState.pageCount]
 * than indicators should displayed, e.g. [Int.MAX_VALUE], specify you real size in this param.
 * @param pageIndexMapping describe how to get the position of active indicator by the giving page
 * from [PagerState.currentPage], if [pageCount] is not equals to [PagerState.pageCount].
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
    pageCount: Int = pagerState.pageCount,
    pageIndexMapping: (Int) -> Int = { it },
    activeColor: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
    inactiveColor: Color = activeColor.copy(ContentAlpha.disabled),
    indicatorHeight: Dp = 8.dp,
    indicatorWidth: Dp = indicatorHeight,
    spacing: Dp = indicatorHeight,
    indicatorShape: Shape = CircleShape,
) {
    val stateBridge = remember(pagerState) {
        object : PagerStateBridge {
            override val currentPage: Int
                get() = pagerState.currentPage
            override val currentPageOffset: Float
                get() = pagerState.currentPageOffset
        }
    }

    VerticalPagerIndicator(
        pagerState = stateBridge,
        pageCount = pageCount,
        modifier = modifier,
        pageIndexMapping = pageIndexMapping,
        activeColor = activeColor,
        inactiveColor = inactiveColor,
        indicatorHeight = indicatorHeight,
        indicatorWidth = indicatorWidth,
        spacing = spacing,
        indicatorShape = indicatorShape
    )
}

/**
 * An vertically laid out indicator for a [VerticalPager] or [HorizontalPager], representing
 * the currently active page and total pages drawn using a [Shape].
 *
 * This element allows the setting of the [indicatorShape], which defines how the
 * indicator is visually represented.
 *
 * @param pagerState The Compose Foundation PagerState object of your [Pager] to be used to observe
 * the list's state.
 * @param modifier the modifier to apply to this layout.
 * @param pageCount the size of indicators should be displayed. If you are implementing a looping
 * pager with a much larger [pageCount] than indicators should displayed, e.g. [Int.MAX_VALUE],
 * specify you real size in this param.
 * @param pageIndexMapping describe how to get the position of active indicator by the giving page
 * from [PagerState.currentPage].
 * @param activeColor the color of the active Page indicator
 * @param inactiveColor the color of page indicators that are inactive. This defaults to
 * [activeColor] with the alpha component set to the [ContentAlpha.disabled].
 * @param indicatorHeight the height of each indicator in [Dp].
 * @param indicatorWidth the width of each indicator in [Dp]. Defaults to [indicatorHeight].
 * @param spacing the spacing between each indicator in [Dp].
 * @param indicatorShape the shape representing each indicator. This defaults to [CircleShape].
 */
@OptIn(ExperimentalFoundationApi::class)
@ExperimentalPagerApi
@Composable
fun VerticalPagerIndicator(
    pagerState: FoundationPagerState,
    pageCount: Int,
    modifier: Modifier = Modifier,
    pageIndexMapping: (Int) -> Int = { it },
    activeColor: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
    inactiveColor: Color = activeColor.copy(ContentAlpha.disabled),
    indicatorHeight: Dp = 8.dp,
    indicatorWidth: Dp = indicatorHeight,
    spacing: Dp = indicatorHeight,
    indicatorShape: Shape = CircleShape,
) {
    val stateBridge = remember(pagerState) {
        object : PagerStateBridge {
            override val currentPage: Int
                get() = pagerState.currentPage
            override val currentPageOffset: Float
                get() = pagerState.currentPageOffsetFraction
        }
    }

    VerticalPagerIndicator(
        pagerState = stateBridge,
        pageCount = pageCount,
        modifier = modifier,
        pageIndexMapping = pageIndexMapping,
        activeColor = activeColor,
        inactiveColor = inactiveColor,
        indicatorHeight = indicatorHeight,
        indicatorWidth = indicatorWidth,
        spacing = spacing,
        indicatorShape = indicatorShape
    )
}

@Composable
private fun VerticalPagerIndicator(
    pagerState: PagerStateBridge,
    pageCount: Int,
    modifier: Modifier = Modifier,
    pageIndexMapping: (Int) -> Int = { it },
    activeColor: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
    inactiveColor: Color = activeColor.copy(ContentAlpha.disabled),
    indicatorHeight: Dp = 8.dp,
    indicatorWidth: Dp = indicatorHeight,
    spacing: Dp = indicatorHeight,
    indicatorShape: Shape = CircleShape,
) {

    val indicatorHeightPx = LocalDensity.current.run { indicatorHeight.roundToPx() }
    val spacingPx = LocalDensity.current.run { spacing.roundToPx() }

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

            repeat(pageCount) {
                Box(indicatorModifier)
            }
        }

        Box(
            Modifier
                .offset {
                    val position = pageIndexMapping(pagerState.currentPage)
                    val offset = pagerState.currentPageOffset
                    val next = pageIndexMapping(pagerState.currentPage + offset.sign.toInt())
                    val scrollPosition = ((next - position) * offset.absoluteValue + position)
                        .coerceIn(
                            0f,
                            (pageCount - 1)
                                .coerceAtLeast(0)
                                .toFloat()
                        )

                    IntOffset(
                        x = 0,
                        y = ((spacingPx + indicatorHeightPx) * scrollPosition).toInt(),
                    )
                }
                .size(width = indicatorWidth, height = indicatorHeight)
                .then(
                    if (pageCount > 0) Modifier.background(
                        color = activeColor,
                        shape = indicatorShape,
                    )
                    else Modifier
                )
        )
    }
}
