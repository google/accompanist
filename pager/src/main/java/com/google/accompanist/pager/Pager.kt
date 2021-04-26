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

import android.util.Log
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.defaultDecayAnimationSpec
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ScrollAxisRange
import androidx.compose.ui.semantics.horizontalScrollAxisRange
import androidx.compose.ui.semantics.scrollBy
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Library-wide switch to turn on debug logging.
 */
internal const val DebugLog = true

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
     * Create and remember default [FlingBehavior] that will represent the scroll curve.
     *
     * @param state The [PagerState] to update.
     * @param decayAnimationSpec The decay animation spec to use for decayed flings.
     * @param snapAnimationSpec The animation spec to use when snapping.
     */
    @Composable
    fun defaultPagerFlingConfig(
        state: PagerState,
        decayAnimationSpec: DecayAnimationSpec<Float> = defaultDecayAnimationSpec(),
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
}

/**
 * A horizontally scrolling layout that allows users to flip between items to the left and right.
 *
 * This layout allows the setting of the [offscreenLimit], which defines the number of pages that
 * should be retained on either side of the current page. Pages beyond this limit will be
 * recreated as needed. This value defaults to `1`, but can be increased to enable pre-loading
 * of more content.
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
    flingBehavior: FlingBehavior = PagerDefaults.defaultPagerFlingConfig(state),
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
 * This layout allows the setting of the [offscreenLimit], which defines the number of pages that
 * should be retained on either side of the current page. Pages beyond this limit will be
 * recreated as needed. This value defaults to `1`, but can be increased to enable pre-loading
 * of more content.
 *
 * @sample com.google.accompanist.sample.pager.VerticalPagerSample
 *
 * @param state the state object to be used to control or observe the pager's state.
 * @param modifier the modifier to apply to this layout.
 * @param reverseLayout reverse the direction of scrolling and layout, when `true` items will be
 * composed from the bottom to the top and [PagerState.currentPage] == 0 will mean
 * the first item is located at the bottom.
 * @param itemSpacing vertical spacing to add between items.
 * @param offscreenLimit the number of pages that should be retained on either side of the
 * current page. This value is required to be `1` or greater.
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
    flingBehavior: FlingBehavior = PagerDefaults.defaultPagerFlingConfig(state),
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
        enabled = dragEnabled,
    )

    Layout(
        modifier = modifier
            .then(semantics)
            .then(scrollable)
            .clipToBounds(),
        content = {
            if (DebugLog) {
                val firstPage = state.layoutPages.firstOrNull { it.page != null }
                val lastPage = state.layoutPages.lastOrNull { it.page != null }
                Log.d(
                    LogTag,
                    "Content: firstPage:${firstPage?.page ?: "none"}, " +
                        "layoutPage:${state.currentLayoutPage}, " +
                        "currentPage:${state.currentPage}, " +
                        "lastPage:${lastPage?.page ?: "none"}"
                )
            }

            for (page in state.layoutPages) {
                val pageIndex = page.page ?: continue

                key(page) {
                    val itemSemantics = Modifier.semantics {
                        this.selected = pageIndex == state.currentPage
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = itemSemantics.then(PageData(pageIndex))
                    ) {
                        val scope = remember(this, state) {
                            PagerScopeImpl(this, state)
                        }
                        scope.content(pageIndex)
                    }
                }
            }
        },
    ) { measurables, constraints ->
        if (measurables.isEmpty()) {
            // If we have no measurables, no-op and return
            return@Layout layout(constraints.minWidth, constraints.minHeight) {}
        }

        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)

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

                val xCenterOffset = horizontalAlignment.align(
                    size = placeable.width,
                    space = pagerWidth,
                    layoutDirection = layoutDirection,
                )
                val yCenterOffset = verticalAlignment.align(
                    size = placeable.height,
                    space = pagerHeight,
                )

                var yItemOffset = 0
                var xItemOffset = 0
                val offsetForPage = page - layoutPage - offset

                if (isVertical) {
                    state.layoutPages[index].also {
                        if (it.page == page) it.layoutSize = placeable.height
                    }
                    yItemOffset = (offsetForPage * (placeable.height + itemSpacingPx)).roundToInt()
                } else {
                    state.layoutPages[index].also {
                        if (it.page == page) it.layoutSize = placeable.width
                    }
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
