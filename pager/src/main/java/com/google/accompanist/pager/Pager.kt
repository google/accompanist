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

@file:JvmName("Pager")

package com.google.accompanist.pager

import androidx.annotation.Px
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.google.accompanist.lazysnap.ExperimentalLazySnapApi
import com.google.accompanist.lazysnap.SnapOffsets
import com.google.accompanist.lazysnap.SnappingFlingBehavior
import com.google.accompanist.lazysnap.SnappingFlingBehaviorDefaults
import com.google.accompanist.lazysnap.rememberSnappingFlingBehavior
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter

/**
 * Library-wide switch to turn on debug logging.
 */
internal const val DebugLog = false

@RequiresOptIn(message = "Accompanist Pager is experimental. The API may be changed in the future.")
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalPagerApi

/**
 * Contains the default values used by [HorizontalPager] and [VerticalPager].
 */
@ExperimentalPagerApi
object PagerDefaults {
    /**
     * The default implementation for the `maximumFlingDistance` parameter of
     * [flingBehavior] which does limits the fling distance to 1 page.
     *
     * If you wish to expand the fling distance to be a multiple of the page size, you can use
     * something like this:
     *
     * @sample com.google.accompanist.sample.lazysnap.MultipleFlingBehavior
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val singlePageFlingDistance: (LazyListLayoutInfo) -> Int = { layoutInfo ->
        // We can scroll up to the scrollable size of the lazy layout
        layoutInfo.layoutSize + layoutInfo.itemSpacing
    }

    /**
     * Remember the default [FlingBehavior] that represents the scroll curve.
     *
     * Please remember to provide the correct [endContentPadding] if supplying your own
     * [FlingBehavior] to [VerticalPager] or [HorizontalPager]. See those functions for how they
     * calculate the value.
     *
     * @param state The [PagerState] to update.
     * @param decayAnimationSpec The decay animation spec to use for decayed flings.
     * @param snapAnimationSpec The animation spec to use when snapping.
     * @param maximumFlingDistance Block which returns the maximum fling distance in pixels.
     * @param endContentPadding The amount of content padding on the end edge of the lazy list
     * in pixels (end/bottom depending on the scrolling direction).
     */
    @Composable
    @ExperimentalLazySnapApi
    fun flingBehavior(
        state: PagerState,
        decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
        snapAnimationSpec: AnimationSpec<Float> = SnappingFlingBehaviorDefaults.SnapAnimationSpec,
        maximumFlingDistance: (LazyListLayoutInfo) -> Int = singlePageFlingDistance,
        @Px endContentPadding: Int = 0,
    ): FlingBehavior = rememberSnappingFlingBehavior(
        lazyListState = state.lazyListState,
        snapOffsetForItem = SnapOffsets.Start, // pages are full width, so we use the simplest
        decayAnimationSpec = decayAnimationSpec,
        snapAnimationSpec = snapAnimationSpec,
        maximumFlingDistance = maximumFlingDistance,
        endContentPadding = endContentPadding,
    )

    @Deprecated(
        "Replaced with PagerDefaults.flingBehavior()",
        ReplaceWith("PagerDefaults.flingBehavior(state, decayAnimationSpec, snapAnimationSpec)")
    )
    @ExperimentalLazySnapApi
    @Composable
    fun rememberPagerFlingConfig(
        state: PagerState,
        decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
        snapAnimationSpec: AnimationSpec<Float> = SnappingFlingBehaviorDefaults.SnapAnimationSpec,
    ): FlingBehavior = flingBehavior(state, decayAnimationSpec, snapAnimationSpec)
}

/**
 * Ideally this would exist on [LazyListLayoutInfo] but it doesn't right now.
 * Raised https://issuetracker.google.com/issues/200920410 to track.
 */
private val LazyListLayoutInfo.layoutSize: Int
    get() {
        // Instead we look at the first item with a non-zero size
        return visibleItemsInfo.firstOrNull { it.size > 0 }?.size
            // Or the viewport end offset (but this value contains the end content padding)
            ?: viewportEndOffset
    }

/**
 * This attempts to calculate the item spacing for the layout, by looking at the distance
 * between the visible items. If there's only 1 visible item available, it returns 0.
 */
private val LazyListLayoutInfo.itemSpacing: Int
    get() = if (visibleItemsInfo.size >= 2) {
        val first = visibleItemsInfo[0]
        val second = visibleItemsInfo[1]
        second.offset - (first.size + first.offset)
    } else 0

/**
 * A horizontally scrolling layout that allows users to flip between items to the left and right.
 *
 * @sample com.google.accompanist.sample.pager.HorizontalPagerSample
 *
 * @param count the number of pages.
 * @param modifier the modifier to apply to this layout.
 * @param state the state object to be used to control or observe the pager's state.
 * @param reverseLayout reverse the direction of scrolling and layout, when `true` items will be
 * composed from the end to the start and [PagerState.currentPage] == 0 will mean
 * the first item is located at the end.
 * @param itemSpacing horizontal spacing to add between items.
 * @param flingBehavior logic describing fling behavior.
 * @param key the scroll position will be maintained based on the key, which means if you
 * add/remove items before the current visible item the item with the given key will be kept as the
 * first visible one.
 * @param content a block which describes the content. Inside this block you can reference
 * [PagerScope.currentPage] and other properties in [PagerScope].
 */
@OptIn(ExperimentalLazySnapApi::class)
@ExperimentalPagerApi
@Composable
fun HorizontalPager(
    count: Int,
    modifier: Modifier = Modifier,
    state: PagerState = rememberPagerState(),
    reverseLayout: Boolean = false,
    itemSpacing: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    flingBehavior: FlingBehavior = with(LocalDensity.current) {
        PagerDefaults.flingBehavior(
            state = state,
            endContentPadding = contentPadding.calculateEndPadding(LayoutDirection.Ltr).roundToPx(),
        )
    },
    key: ((page: Int) -> Any)? = null,
    content: @Composable PagerScope.(page: Int) -> Unit,
) {
    Pager(
        count = count,
        state = state,
        modifier = modifier,
        isVertical = false,
        reverseLayout = reverseLayout,
        itemSpacing = itemSpacing,
        verticalAlignment = verticalAlignment,
        flingBehavior = flingBehavior,
        key = key,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * A vertically scrolling layout that allows users to flip between items to the top and bottom.
 *
 * @sample com.google.accompanist.sample.pager.VerticalPagerSample
 *
 * @param count the number of pages.
 * @param modifier the modifier to apply to this layout.
 * @param state the state object to be used to control or observe the pager's state.
 * @param reverseLayout reverse the direction of scrolling and layout, when `true` items will be
 * composed from the bottom to the top and [PagerState.currentPage] == 0 will mean
 * the first item is located at the bottom.
 * @param itemSpacing vertical spacing to add between items.
 * @param flingBehavior logic describing fling behavior.
 * @param key the scroll position will be maintained based on the key, which means if you
 * add/remove items before the current visible item the item with the given key will be kept as the
 * first visible one.
 * @param content a block which describes the content. Inside this block you can reference
 * [PagerScope.currentPage] and other properties in [PagerScope].
 */
@OptIn(ExperimentalLazySnapApi::class)
@ExperimentalPagerApi
@Composable
fun VerticalPager(
    count: Int,
    modifier: Modifier = Modifier,
    state: PagerState = rememberPagerState(),
    reverseLayout: Boolean = false,
    itemSpacing: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    flingBehavior: FlingBehavior = with(LocalDensity.current) {
        PagerDefaults.flingBehavior(
            state = state,
            endContentPadding = contentPadding.calculateBottomPadding().roundToPx(),
        )
    },
    key: ((page: Int) -> Any)? = null,
    content: @Composable PagerScope.(page: Int) -> Unit,
) {
    Pager(
        count = count,
        state = state,
        modifier = modifier,
        isVertical = true,
        reverseLayout = reverseLayout,
        itemSpacing = itemSpacing,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = flingBehavior,
        key = key,
        contentPadding = contentPadding,
        content = content
    )
}

@OptIn(ExperimentalLazySnapApi::class)
@ExperimentalPagerApi
@Composable
internal fun Pager(
    count: Int,
    modifier: Modifier,
    state: PagerState,
    reverseLayout: Boolean,
    itemSpacing: Dp,
    isVertical: Boolean,
    flingBehavior: FlingBehavior,
    key: ((page: Int) -> Any)?,
    contentPadding: PaddingValues,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable PagerScope.(page: Int) -> Unit,
) {
    require(count >= 0) { "pageCount must be >= 0" }

    // Provide our PagerState with access to the SnappingFlingBehavior animation target
    // TODO: can this be done in a better way?
    state.flingAnimationTarget = {
        (flingBehavior as? SnappingFlingBehavior)?.animationTarget
    }

    // Once a fling (scroll) has finished, notify the state
    LaunchedEffect(state) {
        // When a 'scroll' has finished, notify the state
        snapshotFlow { state.isScrollInProgress }
            .filter { !it }
            .collect { state.onScrollFinished() }
    }

    val pagerScope = remember(state) { PagerScopeImpl(state) }

    if (isVertical) {
        LazyColumn(
            state = state.lazyListState,
            verticalArrangement = Arrangement.spacedBy(itemSpacing, verticalAlignment),
            horizontalAlignment = horizontalAlignment,
            flingBehavior = flingBehavior,
            reverseLayout = reverseLayout,
            contentPadding = contentPadding,
            modifier = modifier,
        ) {
            items(
                count = count,
                key = key,
            ) { page ->
                Box(
                    Modifier
                        // We don't any nested flings to continue in the pager, so we add a
                        // connection which consumes them.
                        // See: https://github.com/google/accompanist/issues/347
                        .nestedScroll(connection = ConsumeFlingNestedScrollConnection)
                        // Constraint the content to be <= than the size of the pager.
                        .fillParentMaxSize()
                        .wrapContentSize()
                ) {
                    pagerScope.content(page)
                }
            }
        }
    } else {
        LazyRow(
            state = state.lazyListState,
            verticalAlignment = verticalAlignment,
            horizontalArrangement = Arrangement.spacedBy(itemSpacing, horizontalAlignment),
            flingBehavior = flingBehavior,
            reverseLayout = reverseLayout,
            contentPadding = contentPadding,
            modifier = modifier,
        ) {
            items(
                count = count,
                key = key,
            ) { page ->
                Box(
                    Modifier
                        // We don't any nested flings to continue in the pager, so we add a
                        // connection which consumes them.
                        // See: https://github.com/google/accompanist/issues/347
                        .nestedScroll(connection = ConsumeFlingNestedScrollConnection)
                        // Constraint the content to be <= than the size of the pager.
                        .fillParentMaxSize()
                        .wrapContentSize()
                ) {
                    pagerScope.content(page)
                }
            }
        }
    }
}

private object ConsumeFlingNestedScrollConnection : NestedScrollConnection {
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset = when (source) {
        // We can consume all resting fling scrolls so that they don't propagate up to the
        // Pager
        NestedScrollSource.Fling -> available
        else -> Offset.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        // We can consume all post fling velocity so that it doesn't propagate up to the Pager
        return available
    }
}

/**
 * Scope for [HorizontalPager] content.
 */
@ExperimentalPagerApi
@Stable
interface PagerScope {
    /**
     * Returns the current selected page
     */
    val currentPage: Int

    /**
     * The current offset from the start of [currentPage], as a ratio of the page width.
     */
    val currentPageOffset: Float
}

@ExperimentalPagerApi
private class PagerScopeImpl(
    private val state: PagerState,
) : PagerScope {
    override val currentPage: Int get() = state.currentPage
    override val currentPageOffset: Float get() = state.currentPageOffset
}

/**
 * Calculate the offset for the given [page] from the current scroll position. This is useful
 * when using the scroll position to apply effects or animations to items.
 *
 * The returned offset can positive or negative, depending on whether which direction the [page] is
 * compared to the current scroll position.
 *
 * @sample com.google.accompanist.sample.pager.HorizontalPagerWithOffsetTransition
 */
@ExperimentalPagerApi
fun PagerScope.calculateCurrentOffsetForPage(page: Int): Float {
    return (currentPage + currentPageOffset) - page
}
