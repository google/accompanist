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
import androidx.annotation.IntRange
import androidx.compose.animation.defaultDecayAnimationSpec
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.roundToInt

/**
 * The scroll threshold for moving to the next page. The value is used in both directions
 * (so both negative and positive).
 */
internal const val ScrollThreshold = 0.35f

/**
 * Library-wide switch to turn on debug logging.
 */
internal const val DebugLog = true

private const val LogTag = "Pager"

@RequiresOptIn(message = "Accompanist Pager is experimental. The API may be changed in the future.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ExperimentalPagerApi

@Immutable
private data class PageData(val page: Int) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any = this@PageData
}

private val Measurable.page: Int
    get() = (parentData as? PageData)?.page ?: error("No PageData for measurable $this")

/**
 * A horizontally scrolling layout that allows users to flip between items to the left and right.
 *
 * This layout allows the setting of the [offscreenLimit], which defines the number of pages that
 * should be retained on either side of the current page. Pages beyond this limit will be
 * recreated as needed. This value defaults to `1`, but can be increased to enable pre-loading
 * of more content.
 *
 * @sample com.google.accompanist.sample.pager.PagerSample
 *
 * @param state the state object to be used to control or observe the list's state.
 * @param modifier the modifier to apply to this layout.
 * @param reverseLayout reverse the direction of scrolling and layout, when `true` items will be
 * composed from the end to the start and [PagerState.currentPage] == 0 will mean
 * the first item is located at the end.
 * @param offscreenLimit the number of pages that should be retained on either side of the
 * current page. This value is required to be `1` or greater.
 * @param content a block which describes the content. Inside this block you can reference
 * [PagerScope.currentPage] and other properties in [PagerScope].
 */
@ExperimentalPagerApi
@Composable
fun HorizontalPager(
    state: PagerState,
    modifier: Modifier = Modifier,
    reverseLayout: Boolean = false,
    @IntRange(from = 1) offscreenLimit: Int = 1,
    content: @Composable PagerScope.(page: Int) -> Unit
) {
    require(offscreenLimit >= 1) { "offscreenLimit is required to be >= 1" }

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val reverseDirection = if (isRtl) !reverseLayout else reverseLayout

    val semantics = Modifier.semantics {
        if (state.pageCount > 0) {
            horizontalScrollAxisRange = ScrollAxisRange(
                value = { (state.currentPage + state.currentPageOffset) * state.pageSize },
                maxValue = { state.lastPageIndex.toFloat() * state.pageSize },
            )
            // Hook up scroll actions to our state
            scrollBy { x: Float, _ ->
                state.dispatchRawDelta(x) != 0f
            }
            // Treat this as a selectable group
            selectableGroup()
        }
    }

    val decay = defaultDecayAnimationSpec()
    val flingBehavior = remember(state, reverseDirection, decay) {
        WorkaroundFlingBehavior(reverseDirection) { initialVelocity ->
            state.fling(-initialVelocity, decay) { deltaPixels ->
                -scrollBy(-deltaPixels)
            }
        }
    }

    val scrollable = Modifier.scrollable(
        orientation = Orientation.Horizontal,
        flingBehavior = flingBehavior,
        reverseDirection = reverseDirection,
        state = state,
    )

    Layout(
        modifier = modifier
            .then(semantics)
            .then(scrollable)
            .clipToBounds(),
        content = {
            val firstPage = (state.currentPage - offscreenLimit).coerceAtLeast(0)
            val lastPage = (state.currentPage + offscreenLimit).coerceAtMost(state.lastPageIndex)

            if (DebugLog) {
                Log.d(
                    LogTag,
                    "Content: firstPage:$firstPage, " +
                        "current:${state.currentPage}, " +
                        "lastPage:$lastPage"
                )
            }

            for (page in firstPage..lastPage) {
                key(page) {
                    val itemSemantics = Modifier.semantics(mergeDescendants = true) {
                        this.selected = page == state.currentPage
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = itemSemantics.then(PageData(page))
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
        layout(constraints.maxWidth, constraints.maxHeight) {
            val currentPage = state.currentPage
            val offset = state.currentPageOffset
            val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)

            measurables.forEach {
                val placeable = it.measure(childConstraints)
                val page = it.page

                // TODO: current this centers each page. We should investigate reading
                //  gravity modifiers on the child, or maybe as a param to Pager.
                val xCenterOffset = (constraints.maxWidth - placeable.width) / 2
                val yCenterOffset = (constraints.maxHeight - placeable.height) / 2

                if (currentPage == page) {
                    state.pageSize = placeable.width
                }

                val xItemOffset = ((page - currentPage - offset) * placeable.width).roundToInt()
                placeable.placeRelative(
                    x = xCenterOffset + xItemOffset,
                    y = yCenterOffset
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
    override val currentPage: Int
        get() = state.currentPage

    override val currentPageOffset: Float
        get() = state.currentPageOffset
}
