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

@file:Suppress("MemberVisibilityCanBePrivate")

package dev.chrisbanes.accompanist.pager

import android.util.Log
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.spring
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ScrollAxisRange
import androidx.compose.ui.semantics.horizontalScrollAxisRange
import androidx.compose.ui.semantics.scrollBy
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.roundToInt

private const val ScrollThreshold = 0.4f

private const val DebugLog = false

/**
 * A state object that can be hoisted to control and observe scrolling for [Pager].
 *
 * @param currentPage the initial value for [PagerState.currentPage]
 * @param pageCount the initial value for [PagerState.pageCount]
 */
class PagerState(
    currentPage: Int = 0,
    pageCount: Int = 0,
) {
    private var _pageCount by mutableStateOf(pageCount)

    /**
     * The index of the currently selected page.
     */
    var pageCount: Int
        get() = _pageCount
        set(value) {
            _pageCount = value.coerceAtLeast(0)
            currentPage = currentPage.coerceIn(0, pageCount)
        }

    private var _currentPage by mutableStateOf(currentPage.coerceIn(0, pageCount))

    /**
     * The index of the currently selected page.
     *
     * To update the scroll position, use [scrollToPage] or [animateScrollToPage].
     */
    var currentPage: Int
        get() = _currentPage
        private set(value) {
            _currentPage = value.coerceIn(0, _pageCount)
        }

    private val _currentPageOffset = mutableStateOf(0f)

    /**
     * The current offset from the start of [currentPage], as a fraction of the page width.
     *
     * To update the scroll position, use [scrollToPage] or [animateScrollToPage].
     */
    @get:FloatRange(from = 0.0, to = 1.0)
    var currentPageOffset: Float
        get() = _currentPageOffset.value
        private set(value) {
            _currentPageOffset.value = value.coerceIn(
                minimumValue = if (currentPage == pageCount) 0f else -1f,
                maximumValue = if (currentPage == 0) 0f else 1f,
            )
        }

    internal var pageSize by mutableStateOf(0)

    /**
     * Represents the current selection state of a [Pager].
     * Usually read from [PagerState.selectionState].
     */
    enum class SelectionState {
        Selected,
        Undecided
    }

    /**
     * The current selection state.
     */
    var selectionState by mutableStateOf(SelectionState.Selected)
        private set

    private val mutatorMutex = MutatorMutex()

    /**
     * Animate (smooth scroll) to the given page.
     *
     * Cancels the currently running scroll, if any, and suspends until the cancellation is
     * complete.
     *
     * @param page the page to snap to. Must be between 0 and [pageCount] (inclusive).
     * @param pageOffset the percentage of the page width to offset, from the start of [page]
     * @param initialVelocity Initial velocity in pixels per second, or `0f` to not use a start velocity.
     */
    suspend fun animateScrollToPage(
        page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
        initialVelocity: Float = 0f,
    ) {
        if (page == currentPage) return

        try {
            mutatorMutex.mutate {
                selectionState = SelectionState.Undecided
                animateToPage(
                    page = page.coerceIn(0, pageCount),
                    pageOffset = pageOffset.coerceIn(0f, 1f),
                    initialVelocity = initialVelocity,
                )
                selectionState = SelectionState.Selected
            }
        } catch (e: CancellationException) {
            // If we're cancelled, snap to the target page
            currentPage = page
            currentPageOffset = pageOffset
            selectionState = SelectionState.Selected

            throw e
        }
    }

    /**
     * Instantly brings the item at [page] to the middle of the viewport, offset by [pageOffset]
     * percentage of page width.
     *
     * Cancels the currently running scroll, if any, and suspends until the cancellation is
     * complete.
     *
     * @param page the page to snap to. Must be between 0 and [pageCount] (inclusive).
     * @param pageOffset the percentage of the page width to offset, from the start of [page]
     */
    suspend fun scrollToPage(
        page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) {
        mutatorMutex.mutate {
            currentPage = page
            currentPageOffset = pageOffset
            selectionState = SelectionState.Selected
        }
    }

    private fun snapToNearestPage() {
        if (DebugLog) {
            Log.d("Pager", "snapToNearestPage. currentPage:$currentPage, offset:$currentPageOffset")
        }
        currentPage -= currentPageOffset.roundToInt()
        currentPageOffset = 0f
        selectionState = SelectionState.Selected
    }

    private suspend fun animateToPage(
        page: Int,
        pageOffset: Float = 0f,
        animationSpec: AnimationSpec<Float> = spring(),
        initialVelocity: Float = 0f,
    ) {
        animate(
            initialValue = currentPage + currentPageOffset,
            targetValue = page + pageOffset,
            initialVelocity = initialVelocity,
            animationSpec = animationSpec
        ) { value, _ ->
            currentPage = floor(value).toInt()
            currentPageOffset = currentPage - value
        }
    }

    private fun scrollByInternal(x: Float) {
        currentPageOffset += x / pageSize.coerceAtLeast(1)
    }

    internal suspend fun scrollBy(x: Float) {
        mutatorMutex.mutate(MutatePriority.UserInput) {
            scrollByInternal(x)
        }
    }

    private fun determineSpringBackOffset(
        offset: Float = currentPageOffset
    ): Float = when {
        // If the offset exceeds the scroll threshold (in either direction), we want to
        // move to the next/previous item
        offset >= ScrollThreshold -> 1f
        offset <= -ScrollThreshold -> -1f
        // Otherwise we snap-back to 0
        else -> 0f
    }.coerceIn(
        minimumValue = if (currentPage == pageCount) 0f else -1f,
        maximumValue = if (currentPage == 0) 0f else 1f
    )

    /**
     * TODO make this public?
     */
    private suspend fun fling(
        initialVelocity: Float,
        animationSpec: DecayAnimationSpec<Float>,
    ) {
        // We calculate the target offset using pixels, rather than using the offset
        val targetOffset = animationSpec.calculateTargetValue(
            initialValue = currentPageOffset * pageSize,
            initialVelocity = initialVelocity
        ) / pageSize

        if (DebugLog) {
            Log.d(
                "Pager",
                "fling. velocity:$initialVelocity, " +
                    "page: $currentPage, " +
                    "offset:$currentPageOffset, " +
                    "targetOffset: $targetOffset"
            )
        }

        // If the animation can naturally end outside of current page bounds, we will
        // animate with decay.
        if (targetOffset.absoluteValue >= 1) {
            // Animate with the decay animation spec using the fling velocity
            AnimationState(
                initialValue = currentPageOffset * pageSize,
                initialVelocity = initialVelocity
            ).animateDecay(animationSpec) {
                // The property will coerce the value to the correct range
                currentPageOffset = value / pageSize

                if (currentPageOffset.absoluteValue >= 1) {
                    // If we reach the bounds of the allowed offset, cancel the animation
                    cancelAnimation()
                }
            }
            snapToNearestPage()
        } else {
            // Otherwise we animate to the next item, or spring-back depending on the offset
            animate(
                initialValue = currentPageOffset,
                targetValue = determineSpringBackOffset(targetOffset.coerceIn(-1f, 1f)),
                initialVelocity = initialVelocity / pageSize.coerceAtLeast(1),
                animationSpec = spring()
            ) { value, _ ->
                currentPageOffset = value
            }
            snapToNearestPage()
        }
    }

    internal suspend fun PointerInputScope.detectPageTouch(
        reverseScroll: Boolean,
    ) = coroutineScope {
        val velocityTracker = VelocityTracker()
        val decay = splineBasedDecay<Float>(this@detectPageTouch)

        forEachGesture {
            try {
                val down = awaitPointerEventScope { awaitFirstDown() }

                if (DebugLog) {
                    Log.d("Pager", "detectPageTouch: DOWN")
                }

                // Reset the velocity tracker and add our initial down event
                velocityTracker.resetTracking()
                velocityTracker.addPosition(down)

                mutatorMutex.mutate(MutatePriority.UserInput) {
                    selectionState = SelectionState.Undecided

                    awaitPointerEventScope {
                        horizontalDrag(down.id) { change ->
                            // Snap the value by the amount of finger movement
                            if (reverseScroll) {
                                scrollByInternal(-change.positionChange().x)
                            } else {
                                scrollByInternal(change.positionChange().x)
                            }
                            // Add the movement to the velocity tracker
                            velocityTracker.addPosition(change)
                        }
                    }
                }

                // The drag has finished, now calculate the velocity and fling
                val velX = velocityTracker.calculateVelocity().x

                if (DebugLog) {
                    Log.d("Pager", "detectPageTouch: UP. Velocity:$velX")
                }

                launch {
                    mutatorMutex.mutate {
                        fling(
                            initialVelocity = (if (reverseScroll) -velX else velX),
                            animationSpec = decay
                        )
                    }
                }
            } catch (e: CancellationException) {
                // If we're cancelled, no-op
                // TODO:
            }
        }
    }

    override fun toString(): String = "PagerState(" +
        "pageCount=$pageCount, " +
        "currentPage=$currentPage, " +
        "selectionState=$selectionState, " +
        "currentPageOffset=$currentPageOffset" +
        ")"
}

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
@Composable
fun Pager(
    state: PagerState,
    modifier: Modifier = Modifier,
    @IntRange(from = 1) offscreenLimit: Int = 1,
    content: @Composable PagerScope.(page: Int) -> Unit
) {
    require(offscreenLimit >= 1) { "offscreenLimit is required to be >= 1" }

    val reverseScroll = LocalLayoutDirection.current == LayoutDirection.Rtl

    val m = Modifier
        .composed {
            val coroutineScope = rememberCoroutineScope()
            semantics {
                if (state.pageCount > 0) {
                    horizontalScrollAxisRange = ScrollAxisRange(
                        value = {
                            (state.currentPage + state.currentPageOffset) * state.pageSize
                        },
                        maxValue = { state.pageCount.toFloat() * state.pageSize },
                        reverseScrolling = reverseScroll
                    )
                    // Hook up scroll actions to our state
                    scrollBy { x: Float, _ ->
                        coroutineScope.launch { state.scrollBy(x) }
                        true
                    }
                    // Treat this as a selectable group
                    selectableGroup()
                }
            }
        }
        .pointerInput(Unit) {
            with(state) {
                detectPageTouch(reverseScroll)
            }
        }
        .then(modifier)

    Layout(
        modifier = m,
        content = {
            val minPage = (state.currentPage - offscreenLimit).coerceAtLeast(0)
            val maxPage = (state.currentPage + offscreenLimit).coerceAtMost(state.pageCount)

            if (DebugLog) {
                Log.d(
                    "Pager",
                    "Layout content: minPage:$minPage, current:${state.currentPage}, maxPage:$maxPage"
                )
            }

            for (page in minPage..maxPage) {
                val pageData = PageData(page)
                key(pageData) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .composed {
                                semantics(mergeDescendants = true) {
                                    this.selected = page == state.currentPage
                                }
                            }.then(pageData)
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

                val xItemOffset = ((page + offset - currentPage) * placeable.width).roundToInt()
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

private fun VelocityTracker.addPosition(change: PointerInputChange) {
    addPosition(change.uptimeMillis, change.position)
}
