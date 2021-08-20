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

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

/**
 * Library-wide switch to turn on debug logging.
 */
internal const val DebugLog = false

private const val LogTag = "Pager"

/**
 * This attempts to mimic ViewPager's custom scroll interpolator. It's not a perfect match
 * (and we may not want it to be), but this seem to match in terms of scroll duration and 'feel'
 */
private const val SnapSpringStiffness = 2750f

@RequiresOptIn(message = "Accompanist Pager is experimental. The API may be changed in the future.")
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalPagerApi

@Immutable
private data class PageData(val page: Int) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any = this@PageData
}

private val Measurable.page: Int
    get() = (parentData as? PageData)?.page ?: error("No PageData for measurable $this")

/**
 * Contains the default values used by [HorizontalPager] and [VerticalPager].
 */
@ExperimentalPagerApi
object PagerDefaults {
    /**
     * Create and remember the default [FlingBehavior] that represents the scroll curve.
     *
     * @param state The [PagerState] to update.
     * @param decayAnimationSpec The decay animation spec to use for decayed flings.
     * @param snapAnimationSpec The animation spec to use when snapping.
     */
    @Composable
    fun rememberPagerFlingConfig(
        state: PagerState,
        decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
        snapAnimationSpec: AnimationSpec<Float> = spring(stiffness = SnapSpringStiffness),
    ): FlingBehavior = remember(state, decayAnimationSpec, snapAnimationSpec) {
        object : FlingBehavior {
            override suspend fun ScrollScope.performFling(
                initialVelocity: Float
            ): Float = 0f
//                state.fling(
//                initialVelocity = -initialVelocity,
//                decayAnimationSpec = decayAnimationSpec,
//                snapAnimationSpec = snapAnimationSpec,
//                scrollBy = { deltaPixels -> -scrollBy(-deltaPixels) },
//            )
        }
    }

    @Deprecated(
        "Replaced with PagerDefaults.rememberPagerFlingConfig()",
        ReplaceWith("PagerDefaults.rememberPagerFlingConfig(state, decayAnimationSpec, snapAnimationSpec)")
    )
    @Composable
    fun defaultPagerFlingConfig(
        state: PagerState,
        decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
        snapAnimationSpec: AnimationSpec<Float> = spring(stiffness = SnapSpringStiffness),
    ): FlingBehavior = rememberPagerFlingConfig(state, decayAnimationSpec, snapAnimationSpec)
}

/**
 * A horizontally scrolling layout that allows users to flip between items to the left and right.
 *
 * @sample com.google.accompanist.sample.pager.HorizontalPagerSample
 *
 * @param state the state object to be used to control or observe the pager's state.
 * @param modifier the modifier to apply to this layout.
 * @param reverseLayout reverse the direction of scrolling and layout, when `true` items will be
 * composed from the end to the start and [PagerState.currentPage] == 0 will mean
 * the first item is located at the end.
 * @param itemSpacing horizontal spacing to add between items.
 * @param dragEnabled toggle manual scrolling, when `false` the user can not drag the view to a
 * different page.
 * @param flingBehavior logic describing fling behavior.
 * @param content a block which describes the content. Inside this block you can reference
 * [PagerScope.currentPage] and other properties in [PagerScope].
 */
@ExperimentalPagerApi
@Composable
fun HorizontalPager(
    state: PagerState,
    modifier: Modifier = Modifier,
    reverseLayout: Boolean = false,
    itemSpacing: Dp = 0.dp,
    dragEnabled: Boolean = true,
    flingBehavior: FlingBehavior = PagerDefaults.rememberPagerFlingConfig(state),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable PagerScope.(page: Int) -> Unit,
) {
    Pager(
        state = state,
        modifier = modifier,
        isVertical = false,
        reverseLayout = reverseLayout,
        itemSpacing = itemSpacing,
        verticalAlignment = verticalAlignment,
        horizontalAlignment = horizontalAlignment,
        dragEnabled = dragEnabled,
        flingBehavior = flingBehavior,
        content = content
    )
}

/**
 * A vertically scrolling layout that allows users to flip between items to the top and bottom.
 *
 * @sample com.google.accompanist.sample.pager.VerticalPagerSample
 *
 * @param state the state object to be used to control or observe the pager's state.
 * @param modifier the modifier to apply to this layout.
 * @param reverseLayout reverse the direction of scrolling and layout, when `true` items will be
 * composed from the bottom to the top and [PagerState.currentPage] == 0 will mean
 * the first item is located at the bottom.
 * @param itemSpacing vertical spacing to add between items.
 * @param dragEnabled toggle manual scrolling, when `false` the user can not drag the view to a
 * different page.
 * @param flingBehavior logic describing fling behavior.
 * @param content a block which describes the content. Inside this block you can reference
 * [PagerScope.currentPage] and other properties in [PagerScope].
 */
@ExperimentalPagerApi
@Composable
fun VerticalPager(
    state: PagerState,
    modifier: Modifier = Modifier,
    reverseLayout: Boolean = false,
    itemSpacing: Dp = 0.dp,
    dragEnabled: Boolean = true,
    flingBehavior: FlingBehavior = PagerDefaults.rememberPagerFlingConfig(state),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable PagerScope.(page: Int) -> Unit,
) {
    Pager(
        state = state,
        modifier = modifier,
        isVertical = true,
        reverseLayout = reverseLayout,
        itemSpacing = itemSpacing,
        verticalAlignment = verticalAlignment,
        horizontalAlignment = horizontalAlignment,
        dragEnabled = dragEnabled,
        flingBehavior = flingBehavior,
        content = content
    )
}

@ExperimentalPagerApi
@Composable
@Suppress("UNUSED_PARAMETER")
internal fun Pager(
    state: PagerState,
    modifier: Modifier,
    reverseLayout: Boolean,
    itemSpacing: Dp,
    isVertical: Boolean,
    verticalAlignment: Alignment.Vertical,
    horizontalAlignment: Alignment.Horizontal,
    dragEnabled: Boolean,
    flingBehavior: FlingBehavior,
    content: @Composable PagerScope.(page: Int) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier,
        propagateMinConstraints = true
    ) {
        // TODO: handle infinite constraints
        state.viewportHeight = constraints.maxHeight
        state.viewportWidth = constraints.maxWidth

        val scope = remember(state) { PagerScopeImpl(state) }

        // TODO: add consuming touch handler if dragEnabled = false

        if (isVertical) {
            LazyColumn(
                state = state.lazyListState,
                verticalArrangement = Arrangement.spacedBy(itemSpacing, verticalAlignment),
                horizontalAlignment = horizontalAlignment,
                flingBehavior = flingBehavior,
                reverseLayout = reverseLayout,
                modifier = modifier
                    .nestedScroll(connection = ConsumeFlingNestedScrollConnection),
            ) {
                items(
                    count = state.pageCount,
                    // TODO: expose key
                ) { page ->
                    PagerItem(
                        page = page,
                        itemCount = state.pageCount,
                        pagerState = state,
                        isVertical = true,
                        horizontalAlignment = horizontalAlignment,
                        verticalAlignment = verticalAlignment,
                        modifier = Modifier.sizeIn(maxWidth = maxWidth, maxHeight = maxHeight),
                        content = { scope.content(page) },
                    )
                }
            }
        } else {
            LazyRow(
                state = state.lazyListState,
                verticalAlignment = verticalAlignment,
                horizontalArrangement = Arrangement.spacedBy(itemSpacing, horizontalAlignment),
                flingBehavior = flingBehavior,
                reverseLayout = reverseLayout,
                modifier = modifier
                    .nestedScroll(connection = ConsumeFlingNestedScrollConnection),
            ) {
                items(
                    count = state.pageCount,
                    // TODO: expose key
                ) { page ->
                    PagerItem(
                        page = page,
                        itemCount = state.pageCount,
                        pagerState = state,
                        isVertical = false,
                        horizontalAlignment = horizontalAlignment,
                        verticalAlignment = verticalAlignment,
                        modifier = Modifier.sizeIn(maxWidth = maxWidth, maxHeight = maxHeight),
                        content = { scope.content(page) },
                    )
                }
            }
        }
    }
}

@ExperimentalPagerApi
@Composable
private fun PagerItem(
    page: Int,
    itemCount: Int,
    pagerState: PagerState,
    isVertical: Boolean,
    verticalAlignment: Alignment.Vertical,
    horizontalAlignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        content = content,
        modifier = modifier.then(PageData(page)), // TODO: Need this?
    ) { measurables, constraints ->
        require(measurables.size == 1) { "PagerItem should have only one measurable" }

        val placeable = measurables[0].measure(constraints)

        val startSpacing = if (page == 0 && !isVertical) {
            when (horizontalAlignment) {
                Alignment.Start -> 0
                Alignment.End -> constraints.maxWidth - placeable.width
                else /* Center */ -> (constraints.maxWidth - placeable.width) / 2
            }
        } else 0
        val topSpacing = if (page == 0 && isVertical) {
            when (verticalAlignment) {
                Alignment.Top -> 0
                Alignment.Bottom -> constraints.maxHeight - placeable.height
                else /* Center */ -> (constraints.maxHeight - placeable.height) / 2
            }
        } else 0
        val endSpacing = if (page == itemCount - 1 && !isVertical) {
            when (horizontalAlignment) {
                Alignment.Start -> constraints.maxWidth - placeable.width
                Alignment.End -> 0
                else /* Center */ -> (constraints.maxWidth - placeable.width) / 2
            }
        } else 0
        val bottomSpacing = if (page == itemCount - 1 && isVertical) {
            when (verticalAlignment) {
                Alignment.Top -> constraints.maxHeight - placeable.height
                Alignment.Bottom -> 0
                else /* Center */ -> (constraints.maxHeight - placeable.height) / 2
            }
        } else 0

        if (page == 0) {
            pagerState.leadSpacing = if (isVertical) topSpacing else startSpacing
        }

        layout(
            width = placeable.width + startSpacing + endSpacing,
            height = placeable.height + topSpacing + bottomSpacing
        ) {
            placeable.placeRelative(startSpacing, topSpacing)
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
     * Returns the current selected page offset
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
