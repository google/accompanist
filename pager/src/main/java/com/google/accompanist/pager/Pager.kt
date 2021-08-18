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
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ScrollAxisRange
import androidx.compose.ui.semantics.horizontalScrollAxisRange
import androidx.compose.ui.semantics.scrollBy
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
            ): Float = state.fling(
                initialVelocity = -initialVelocity,
                decayAnimationSpec = decayAnimationSpec,
                snapAnimationSpec = snapAnimationSpec,
                scrollBy = { deltaPixels -> -scrollBy(-deltaPixels) },
            )
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
    contentPadding: PaddingValues = PaddingValues(0.dp),
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
        contentPadding = contentPadding,
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
    contentPadding: PaddingValues = PaddingValues(0.dp),
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
        contentPadding = contentPadding,
        content = content
    )
}

@ExperimentalPagerApi
@Composable
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
    contentPadding: PaddingValues,
    content: @Composable PagerScope.(page: Int) -> Unit,
) {
    // True if the scroll direction is RTL, false for LTR
    val reverseDirection = when {
        // If we're vertical, just use reverseLayout as-is
        isVertical -> reverseLayout
        // If we're horizontal in RTL, flip reverseLayout
        LocalLayoutDirection.current == LayoutDirection.Rtl -> !reverseLayout
        // Else (horizontal in LTR), use reverseLayout as-is
        else -> reverseLayout
    }

    val coroutineScope = rememberCoroutineScope()
    val semanticsAxisRange = remember(state, reverseDirection) {
        ScrollAxisRange(
            value = { state.currentLayoutPage + state.currentLayoutPageOffset },
            maxValue = { state.lastPageIndex.toFloat() },
        )
    }
    val semantics = Modifier.semantics {
        horizontalScrollAxisRange = semanticsAxisRange
        // Hook up scroll actions to our state
        scrollBy { x, y ->
            coroutineScope.launch {
                if (isVertical) {
                    state.scrollBy(if (reverseDirection) y else -y)
                } else {
                    state.scrollBy(if (reverseDirection) x else -x)
                }
            }
            true
        }
        // Treat this as a selectable group
        selectableGroup()
    }

    val scrollable = Modifier.scrollable(
        orientation = if (isVertical) Orientation.Vertical else Orientation.Horizontal,
        flingBehavior = flingBehavior,
        reverseDirection = reverseDirection,
        state = state,
        interactionSource = state.internalInteractionSource,
        enabled = dragEnabled,
    )

    Layout(
        modifier = modifier
            .then(semantics)
            .then(scrollable)
            // Add a NestedScrollConnection which consumes all post fling/scrolls
            .nestedScroll(connection = ConsumeFlingNestedScrollConnection)
            .clipScrollableContainer(isVertical),
        content = {
            if (DebugLog) {
                val firstPage = state.layoutPages.firstOrNull { it.page != null }
                val lastPage = state.layoutPages.lastOrNull { it.page != null }
                Napier.d(
                    tag = LogTag,
                    message = "Content: firstPage:${firstPage?.page ?: "none"}, " +
                        "layoutPage:${state.currentLayoutPageInfo}, " +
                        "currentPage:${state.currentPage}, " +
                        "lastPage:${lastPage?.page ?: "none"}"
                )
            }

            // FYI: We need to filter out null/empty pages *outside* of the loop. Compose uses the
            // call stack as part of the key for state, so we need to ensure that the call stack
            // for page content is consistent as the user scrolls, otherwise content will
            // drop/recreate state.
            val pages = state.layoutPages.mapNotNull { it.page }
            for (_page in pages) {
                val page = state.pageOf(_page)
                key(page) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = PageData(_page)
                    ) {
                        val scope = remember(this, state) {
                            PagerScopeImpl(this, state)
                        }
                        scope.content(page)
                    }
                }
            }
        },
    ) { measurables, constraints ->
        if (measurables.isEmpty()) {
            // If we have no measurables, no-op and return
            return@Layout layout(constraints.minWidth, constraints.minHeight) {}
        }

        val horizContentPadding = (
            contentPadding.calculateLeftPadding(layoutDirection) +
                contentPadding.calculateRightPadding(layoutDirection)
            ).roundToPx()
        val vertContentPadding = (
            contentPadding.calculateTopPadding() +
                contentPadding.calculateBottomPadding()
            ).roundToPx()

        val childConstraints = Constraints(
            minWidth = 0,
            maxWidth = when {
                constraints.hasBoundedWidth -> constraints.maxWidth - horizContentPadding
                else -> constraints.maxWidth
            },
            minHeight = 0,
            maxHeight = when {
                constraints.hasBoundedHeight -> constraints.maxHeight - vertContentPadding
                else -> constraints.maxHeight
            },
        )

        val placeables = measurables.map { it.measure(childConstraints) }
        // Our pager width/height is the maximum pager content width/height, and coerce
        // each by our minimum constraints
        val pagerWidth = placeables.maxOf { it.width }.coerceAtLeast(constraints.minWidth)
        val pagerHeight = placeables.maxOf { it.height }.coerceAtLeast(constraints.minHeight)

        layout(width = pagerWidth, height = pagerHeight) {
            val layoutPage = state.currentLayoutPage
            val offset = state.currentLayoutPageOffset
            val itemSpacingPx = itemSpacing.roundToPx()

            placeables.forEachIndexed { index, placeable ->
                val page = measurables[index].page
                val layoutInfo = state.layoutPages.firstOrNull { it.page == page }

                val xCenterOffset = horizontalAlignment.align(
                    size = placeable.width,
                    space = pagerWidth - horizContentPadding,
                    layoutDirection = layoutDirection,
                ) + contentPadding.calculateLeftPadding(layoutDirection).roundToPx()
                val yCenterOffset = verticalAlignment.align(
                    size = placeable.height,
                    space = pagerHeight - vertContentPadding,
                ) + contentPadding.calculateTopPadding().roundToPx()

                var yItemOffset = 0
                var xItemOffset = 0
                val offsetForPage = page - layoutPage - offset

                if (isVertical) {
                    layoutInfo?.layoutSize = placeable.height
                    yItemOffset = (offsetForPage * (placeable.height + itemSpacingPx)).roundToInt()
                } else {
                    layoutInfo?.layoutSize = placeable.width
                    xItemOffset = (offsetForPage * (placeable.width + itemSpacingPx)).roundToInt()
                }

                // We can't rely on placeRelative() since that only uses the LayoutDirection, and
                // we need to cater for our reverseLayout param too. reverseDirection contains
                // the resolved direction, so we use that to flip the offset direction...
                placeable.place(
                    x = xCenterOffset + if (reverseDirection) -xItemOffset else xItemOffset,
                    y = yCenterOffset + if (reverseDirection) -yItemOffset else yItemOffset,
                )
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
interface PagerScope : BoxScope {
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
    private val boxScope: BoxScope,
    private val state: PagerState,
) : PagerScope, BoxScope by boxScope {
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
