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
@file:JvmName("Pager")

package com.google.accompanist.pager

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
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.SnapOffsets
import dev.chrisbanes.snapper.SnapperFlingBehavior
import dev.chrisbanes.snapper.SnapperFlingBehaviorDefaults
import dev.chrisbanes.snapper.SnapperLayoutInfo
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter

/**
 * Library-wide switch to turn on debug logging.
 */
internal const val DebugLog = false

@Deprecated(
    """
accompanist/pager is deprecated.
The androidx.compose equivalent of Pager is androidx.compose.foundation.pager.Pager.
For more migration information, please visit https://google.github.io/accompanist/pager/#migration
"""
)
@RequiresOptIn(message = "Accompanist Pager is experimental. The API may be changed in the future.")
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalPagerApi

/**
 * Contains the default values used by [HorizontalPager] and [VerticalPager].
 */
@Deprecated(
    """
accompanist/pager is deprecated.
The androidx.compose equivalent of Pager is androidx.compose.foundation.pager.Pager.
For more migration information, please visit https://google.github.io/accompanist/pager/#migration
"""
)
public object PagerDefaults {
    /**
     * The default implementation for the `maximumFlingDistance` parameter of
     * [flingBehavior] which limits the fling distance to a single page.
     */
    @ExperimentalSnapperApi
    @Suppress("MemberVisibilityCanBePrivate")
    @Deprecated("MaximumFlingDistance has been deprecated in Snapper")
    public val singlePageFlingDistance: (SnapperLayoutInfo) -> Float = { layoutInfo ->
        // We can scroll up to the scrollable size of the lazy layout
        layoutInfo.endScrollOffset - layoutInfo.startScrollOffset.toFloat()
    }

    /**
     * The default implementation for the `snapIndex` parameter of
     * [flingBehavior] which limits the fling distance to a single page.
     */
    @ExperimentalSnapperApi
    public val singlePageSnapIndex: (SnapperLayoutInfo, startIndex: Int, targetIndex: Int) -> Int =
        { layoutInfo, startIndex, targetIndex ->
            targetIndex
                .coerceIn(startIndex - 1, startIndex + 1)
                .coerceIn(0, layoutInfo.totalItemsCount - 1)
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
    @Deprecated(
        """
            accompanist/pager is deprecated.
            The androidx.compose equivalent of Pager is androidx.compose.foundation.pager.Pager.
            For more migration information, please visit https://google.github.io/accompanist/pager/#migration
    """
    )
    @Composable
    @ExperimentalSnapperApi
    @Suppress("DEPRECATION")
    public fun flingBehavior(
        state: PagerState,
        decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
        snapAnimationSpec: AnimationSpec<Float> = SnapperFlingBehaviorDefaults.SpringAnimationSpec,
        maximumFlingDistance: (SnapperLayoutInfo) -> Float = singlePageFlingDistance,
        endContentPadding: Dp = 0.dp,
    ): FlingBehavior = rememberSnapperFlingBehavior(
        lazyListState = state.lazyListState,
        snapOffsetForItem = SnapOffsets.Start, // pages are full width, so we use the simplest
        decayAnimationSpec = decayAnimationSpec,
        springAnimationSpec = snapAnimationSpec,
        maximumFlingDistance = maximumFlingDistance,
        endContentPadding = endContentPadding,
    )

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
     * @param endContentPadding The amount of content padding on the end edge of the lazy list
     * in pixels (end/bottom depending on the scrolling direction).
     * @param snapIndex Block which returns the index to snap to. The block is provided with the
     * [SnapperLayoutInfo], the index where the fling started, and the index which Snapper has
     * determined is the correct target index. Callers can override this value to any valid index
     * for the layout. Some common use cases include limiting the fling distance, and rounding up/down
     * to achieve snapping to groups of items.
     */
    @Deprecated(
        """
accompanist/pager is deprecated.
The androidx.compose equivalent of Pager is androidx.compose.foundation.pager.Pager
For more migration information, please visit https://google.github.io/accompanist/pager/#migration
"""
    )
    @Composable
    @ExperimentalSnapperApi
    public fun flingBehavior(
        state: PagerState,
        decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
        snapAnimationSpec: AnimationSpec<Float> = SnapperFlingBehaviorDefaults.SpringAnimationSpec,
        endContentPadding: Dp = 0.dp,
        snapIndex: (SnapperLayoutInfo, startIndex: Int, targetIndex: Int) -> Int,
    ): FlingBehavior = rememberSnapperFlingBehavior(
        lazyListState = state.lazyListState,
        snapOffsetForItem = SnapOffsets.Start, // pages are full width, so we use the simplest
        decayAnimationSpec = decayAnimationSpec,
        springAnimationSpec = snapAnimationSpec,
        endContentPadding = endContentPadding,
        snapIndex = snapIndex,
    )

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
     * @param endContentPadding The amount of content padding on the end edge of the lazy list
     * in pixels (end/bottom depending on the scrolling direction).
     */
    @Deprecated(
        """
accompanist/pager is deprecated.
For more migration information, please visit https://google.github.io/accompanist/pager/#migration
""",
        replaceWith = ReplaceWith(
            "androidx.compose.foundation.pager.PagerDefaults.flingBehavior(state = state)",
            "androidx.compose.foundation.pager.PagerDefaults"
        )
    )
    @Composable
    @ExperimentalSnapperApi
    public fun flingBehavior(
        state: PagerState,
        decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
        snapAnimationSpec: AnimationSpec<Float> = SnapperFlingBehaviorDefaults.SpringAnimationSpec,
        endContentPadding: Dp = 0.dp,
    ): FlingBehavior {
        // You might be wondering this is function exists rather than a default value for snapIndex
        // above. It was done to remove overload ambiguity with the maximumFlingDistance overload
        // below. When that function is removed, we also remove this function and move to a default
        // param value.
        return flingBehavior(
            state = state,
            decayAnimationSpec = decayAnimationSpec,
            snapAnimationSpec = snapAnimationSpec,
            endContentPadding = endContentPadding,
            snapIndex = singlePageSnapIndex,
        )
    }
}

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
 * @param userScrollEnabled whether the scrolling via the user gestures or accessibility actions
 * is allowed. You can still scroll programmatically using the state even when it is disabled.
 * @param content a block which describes the content. Inside this block you can reference
 * [PagerScope.currentPage] and other properties in [PagerScope].
 */
@OptIn(ExperimentalSnapperApi::class)
@Composable
@Deprecated(
    """
accompanist/pager is deprecated.
The androidx.compose equivalent of HorizontalPager is androidx.compose.foundation.pager.HorizontalPager
For more migration information, please visit https://google.github.io/accompanist/pager/#migration
""",
    replaceWith = ReplaceWith(
        "HorizontalPager",
        "androidx.compose.foundation.pager.HorizontalPager"
    )
)
public fun HorizontalPager(
    count: Int,
    modifier: Modifier = Modifier,
    state: PagerState = rememberPagerState(),
    reverseLayout: Boolean = false,
    itemSpacing: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    flingBehavior: FlingBehavior = PagerDefaults.flingBehavior(
        state = state,
        endContentPadding = contentPadding.calculateEndPadding(LayoutDirection.Ltr),
    ),
    key: ((page: Int) -> Any)? = null,
    userScrollEnabled: Boolean = true,
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
        userScrollEnabled = userScrollEnabled,
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
 * @param userScrollEnabled whether the scrolling via the user gestures or accessibility actions
 * is allowed. You can still scroll programmatically using the state even when it is disabled.
 * @param content a block which describes the content. Inside this block you can reference
 * [PagerScope.currentPage] and other properties in [PagerScope].
 */
@OptIn(ExperimentalSnapperApi::class)
@Composable
@Deprecated(
    """
accompanist/pager is deprecated.
The androidx.compose equivalent of VerticalPager is androidx.compose.foundation.pager.VerticalPager.
For more migration information, please visit https://google.github.io/accompanist/pager/#migration
"""
)
public fun VerticalPager(
    count: Int,
    modifier: Modifier = Modifier,
    state: PagerState = rememberPagerState(),
    reverseLayout: Boolean = false,
    itemSpacing: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    flingBehavior: FlingBehavior = PagerDefaults.flingBehavior(
        state = state,
        endContentPadding = contentPadding.calculateBottomPadding(),
    ),
    key: ((page: Int) -> Any)? = null,
    userScrollEnabled: Boolean = true,
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
        userScrollEnabled = userScrollEnabled,
        content = content
    )
}

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
    userScrollEnabled: Boolean,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable PagerScope.(page: Int) -> Unit,
) {
    require(count >= 0) { "pageCount must be >= 0" }

    // Provide our PagerState with access to the SnappingFlingBehavior animation target
    // TODO: can this be done in a better way?
    state.flingAnimationTarget = {
        @OptIn(ExperimentalSnapperApi::class)
        (flingBehavior as? SnapperFlingBehavior)?.animationTarget
    }

    LaunchedEffect(count) {
        state.currentPage = minOf(count - 1, state.currentPage).coerceAtLeast(0)
    }

    // Once a fling (scroll) has finished, notify the state
    LaunchedEffect(state) {
        // When a 'scroll' has finished, notify the state
        snapshotFlow { state.isScrollInProgress }
            .filter { !it }
            // initially isScrollInProgress is false as well and we want to start receiving
            // the events only after the real scroll happens.
            .drop(1)
            .collect { state.onScrollFinished() }
    }
    LaunchedEffect(state) {
        snapshotFlow { state.mostVisiblePageLayoutInfo?.index }
            .distinctUntilChanged()
            .collect { state.updateCurrentPageBasedOnLazyListState() }
    }
    val density = LocalDensity.current
    LaunchedEffect(density, state, itemSpacing) {
        with(density) { state.itemSpacing = itemSpacing.roundToPx() }
    }

    val pagerScope = remember(state) { PagerScopeImpl(state) }

    // We only consume nested flings in the main-axis, allowing cross-axis flings to propagate
    // as normal
    val consumeFlingNestedScrollConnection = remember(isVertical) {
        ConsumeFlingNestedScrollConnection(
            consumeHorizontal = !isVertical,
            consumeVertical = isVertical,
            pagerState = state,
        )
    }

    if (isVertical) {
        LazyColumn(
            state = state.lazyListState,
            verticalArrangement = Arrangement.spacedBy(itemSpacing, verticalAlignment),
            horizontalAlignment = horizontalAlignment,
            flingBehavior = flingBehavior,
            reverseLayout = reverseLayout,
            contentPadding = contentPadding,
            userScrollEnabled = userScrollEnabled,
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
                        .nestedScroll(connection = consumeFlingNestedScrollConnection)
                        // Constraint the content height to be <= than the height of the pager.
                        .fillParentMaxHeight()
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
            userScrollEnabled = userScrollEnabled,
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
                        .nestedScroll(connection = consumeFlingNestedScrollConnection)
                        // Constraint the content width to be <= than the width of the pager.
                        .fillParentMaxWidth()
                        .wrapContentSize()
                ) {
                    pagerScope.content(page)
                }
            }
        }
    }
}

private class ConsumeFlingNestedScrollConnection(
    private val consumeHorizontal: Boolean,
    private val consumeVertical: Boolean,
    private val pagerState: PagerState,
) : NestedScrollConnection {
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset = when (source) {
        // We can consume all resting fling scrolls so that they don't propagate up to the
        // Pager
        NestedScrollSource.Fling -> available.consume(consumeHorizontal, consumeVertical)
        else -> Offset.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        return if (pagerState.currentPageOffset != 0f) {
            // The Pager is already scrolling. This means that a nested scroll child was
            // scrolled to end, and the Pager can use this fling
            Velocity.Zero
        } else {
            // A nested scroll child is still scrolling. We can consume all post fling
            // velocity on the main-axis so that it doesn't propagate up to the Pager
            available.consume(consumeHorizontal, consumeVertical)
        }
    }
}

private fun Offset.consume(
    consumeHorizontal: Boolean,
    consumeVertical: Boolean,
): Offset = Offset(
    x = if (consumeHorizontal) this.x else 0f,
    y = if (consumeVertical) this.y else 0f,
)

private fun Velocity.consume(
    consumeHorizontal: Boolean,
    consumeVertical: Boolean,
): Velocity = Velocity(
    x = if (consumeHorizontal) this.x else 0f,
    y = if (consumeVertical) this.y else 0f,
)

/**
 * Scope for [HorizontalPager] content.
 */
@Deprecated(
    """
accompanist/pager is deprecated.
The androidx.compose equivalent of Pager is androidx.compose.foundation.pager.Pager.
For more migration information, please visit https://google.github.io/accompanist/pager/#migration
"""
)
@Stable
public interface PagerScope {
    /**
     * Returns the current selected page
     */
    public val currentPage: Int

    /**
     * The current offset from the start of [currentPage], as a ratio of the page width.
     */
    public val currentPageOffset: Float
}

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
@Deprecated(
    """
accompanist/pager is deprecated.
The androidx.compose equivalent of Pager is androidx.compose.foundation.pager.Pager.
For more migration information, please visit https://google.github.io/accompanist/pager/#migration
"""
)
public fun PagerScope.calculateCurrentOffsetForPage(page: Int): Float {
    return (currentPage - page) + currentPageOffset
}
