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

package dev.chrisbanes.accompanist.pager

import android.util.Log
import androidx.annotation.IntRange
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ScrollAxisRange
import androidx.compose.ui.semantics.horizontalScrollAxisRange
import androidx.compose.ui.semantics.scrollBy
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.launch
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
 * @sample dev.chrisbanes.accompanist.sample.pager.PagerSample
 *
 * @param state the state object to be used to control or observe the list's state.
 * @param modifier the modifier to apply to this layout.
 * @param offscreenLimit the number of pages that should be retained on either side of the
 * current page. This value is required to be `1` or greater.
 * @param content a block which describes the content. Inside this block you can reference
 * [PagerScope.currentPage] and other properties in [PagerScope].
 */
@ExperimentalPagerApi
@Composable
fun Pager(
    state: PagerState,
    modifier: Modifier = Modifier,
    @IntRange(from = 1) offscreenLimit: Int = 1,
    content: @Composable PagerScope.(page: Int) -> Unit
) {
    require(offscreenLimit >= 1) { "offscreenLimit is required to be >= 1" }

    val reverseScroll = LocalLayoutDirection.current == LayoutDirection.Rtl

    val density = LocalDensity.current
    val decay = remember(density) { splineBasedDecay<Float>(density) }

    val semantics = Modifier.composed {
        val coroutineScope = rememberCoroutineScope()
        semantics {
            if (state.pageCount > 0) {
                horizontalScrollAxisRange = ScrollAxisRange(
                    value = { (state.currentPage + state.currentPageOffset) * state.pageSize },
                    maxValue = { state.lastPageIndex.toFloat() * state.pageSize },
                    reverseScrolling = reverseScroll
                )
                // Hook up scroll actions to our state
                scrollBy { x: Float, _ ->
                    coroutineScope.launch {
                        state.draggableState.drag { dragBy(x) }
                    }
                    true
                }
                // Treat this as a selectable group
                selectableGroup()
            }
        }
    }

    val draggable = Modifier.draggable(
        state = state.draggableState,
        startDragImmediately = true,
        onDragStarted = {
            state.selectionState = PagerState.SelectionState.Dragging
        },
        onDragStopped = { velocity ->
            launch { state.performFling(velocity, decay) }
        },
        orientation = Orientation.Horizontal,
        reverseDirection = reverseScroll,
    )

    Layout(
        modifier = modifier.then(semantics).then(draggable),
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
                val pageData = PageData(page)
                key(pageData) {
                    val itemSemantics = Modifier.composed {
                        semantics(mergeDescendants = true) {
                            this.selected = page == state.currentPage
                        }
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = itemSemantics.then(pageData)
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
 * Scope for [Pager] content.
 */
@ExperimentalPagerApi
interface PagerScope : BoxScope {
    /**
     * Returns the current selected page
     */
    val currentPage: Int

    /**
     * Returns the current selected page offset
     */
    val currentPageOffset: Float

    /**
     * Returns the current selection state
     */
    val selectionState: PagerState.SelectionState
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

    override val selectionState: PagerState.SelectionState
        get() = state.selectionState
}
