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
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.roundToInt

private const val LogTag = "PagerState"

/**
 * Creates a [PagerState] that is remembered across compositions.
 *
 * Changes to the provided initial values will **not** result in the state being recreated or
 * changed in any way if it has already been created.
 *
 * @param pageCount the initial value for [PagerState.pageCount]
 * @param currentPage the initial value for [PagerState.currentPage]
 * @param currentPageOffset the initial value for [PagerState.currentPageOffset]
 */
@ExperimentalPagerApi
@Composable
fun rememberPagerState(
    pageCount: Int,
    currentPage: Int = 0,
    @FloatRange(from = 0.0, to = 1.0) currentPageOffset: Float = 0f,
): PagerState = rememberSaveable(saver = PagerState.Saver) {
    PagerState(
        pageCount = pageCount,
        currentPage = currentPage,
        currentPageOffset = currentPageOffset,
    )
}

/**
 * A state object that can be hoisted to control and observe scrolling for [Pager].
 *
 * In most cases, this will be created via [rememberPagerState].
 *
 * @param pageCount the initial value for [PagerState.pageCount]
 * @param currentPage the initial value for [PagerState.currentPage]
 * @param currentPageOffset the initial value for [PagerState.currentPageOffset]
 */
@ExperimentalPagerApi
class PagerState(
    pageCount: Int,
    currentPage: Int = 0,
    @FloatRange(from = 0.0, to = 1.0) currentPageOffset: Float = 0f,
) {
    private var _pageCount by mutableStateOf(pageCount)
    private var _currentPage by mutableStateOf(currentPage)
    private val _currentPageOffset = mutableStateOf(currentPageOffset)
    internal var pageSize by mutableStateOf(0)

    /**
     * The number of pages to display.
     */
    @get:IntRange(from = 0)
    var pageCount: Int
        get() = _pageCount
        set(@IntRange(from = 0) value) {
            _pageCount = value.coerceAtLeast(0)
            currentPage = currentPage.coerceIn(0, lastPageIndex)
        }

    internal val lastPageIndex: Int
        get() = (pageCount - 1).coerceAtLeast(0)

    /**
     * The index of the currently selected page.
     *
     * To update the scroll position, use [scrollToPage] or [animateScrollToPage].
     */
    @get:IntRange(from = 0)
    var currentPage: Int
        get() = _currentPage
        private set(value) {
            _currentPage = value.coerceIn(0, _pageCount)
        }

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
                minimumValue = 0f,
                maximumValue = if (currentPage == lastPageIndex) 0f else 1f,
            )
        }

    /**
     * Represents the current selection state of a [Pager].
     * Usually read from [PagerState.selectionState].
     */
    enum class SelectionState {
        /**
         * Indicates that the pager is in an idle, settled state. The current page
         * is fully in view and no animation is in progress.
         */
        Idle,

        /**
         * Indicates that the pager is currently being dragged by the user.
         */
        Dragging,

        /**
         * Indicates that the pager is in the process of settling to a final position.
         */
        Settling
    }

    /**
     * The current selection state.
     */
    var selectionState by mutableStateOf(SelectionState.Idle)
        internal set

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
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
        initialVelocity: Float = 0f,
    ) {
        if (page == currentPage) return

        // We don't specifically use the DragScope's dragBy, but
        // we do want to use it's mutex
        draggableState.drag {
            selectionState = SelectionState.Settling
            animateToPage(
                page = page.coerceIn(0, lastPageIndex),
                pageOffset = pageOffset.coerceIn(0f, 1f),
                initialVelocity = initialVelocity,
            )
            selectionState = SelectionState.Idle
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
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) {
        // We don't specifically use the DragScope's dragBy, but
        // we do want to use it's mutex
        draggableState.drag {
            currentPage = page
            currentPageOffset = pageOffset
            selectionState = SelectionState.Idle
        }
    }

    private fun snapToNearestPage() {
        if (DebugLog) {
            Log.d(LogTag, "snapToNearestPage. currentPage:$currentPage, offset:$currentPageOffset")
        }
        currentPage += currentPageOffset.roundToInt()
        currentPageOffset = 0f
        selectionState = SelectionState.Idle
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
            currentPageOffset = value - currentPage
        }
        snapToNearestPage()
    }

    private fun determineSpringBackOffset(
        velocity: Float,
        offset: Float = currentPageOffset,
    ): Float = when {
        // If the offset exceeds the scroll threshold (in either direction), we want to
        // move to the next/previous item
        offset < ScrollThreshold -> 0f
        offset > 1 - ScrollThreshold -> 1f
        // Otherwise we look at the velocity for scroll direction
        velocity < 0 -> 1f
        else -> 0f
    }

    internal val draggableState = DraggableState { delta ->
        dragByOffset(delta / pageSize.coerceAtLeast(1))
    }

    private fun dragByOffset(deltaOffset: Float) {
        val targetedOffset = currentPageOffset - deltaOffset

        if (targetedOffset < 0) {
            // If the target offset is < 0, we're trying to cross the boundary to the previous page
            if (currentPage > 0) {
                // We can only move to the previous page if we're not at page 0
                currentPage--
                currentPageOffset = targetedOffset + 1
            } else {
                // If we're at page 0, pin to 0f offset
                currentPageOffset = 0f
            }
        } else if (targetedOffset >= 1) {
            // If the target offset is > 1, we're trying to cross the boundary to the next page
            if (currentPage < pageCount - 1) {
                // We can only move to the next page if we're not on the last page
                currentPage++
                currentPageOffset = targetedOffset - 1
            } else {
                // If we're on the last page, pin to 0f offset
                currentPageOffset = 0f
            }
        } else {
            // Otherwise, we can use the offset as-is
            currentPageOffset = targetedOffset
        }

        if (DebugLog) {
            Log.d(
                LogTag,
                "dragByOffset. delta:%.4f, targetOffset:%.4f, new-page:%d, new-offset:%.4f"
                    .format(deltaOffset, targetedOffset, currentPage, currentPageOffset),
            )
        }
    }

    /**
     * TODO make this public?
     */
    internal suspend fun performFling(
        initialVelocity: Float,
        animationSpec: DecayAnimationSpec<Float>,
    ) = draggableState.drag {
        selectionState = SelectionState.Settling

        // We calculate the target offset using pixels, rather than using the offset
        val targetOffset = animationSpec.calculateTargetValue(
            initialValue = currentPageOffset * pageSize,
            initialVelocity = initialVelocity
        ) / pageSize

        if (DebugLog) {
            Log.d(
                LogTag,
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
                dragBy((currentPageOffset * pageSize) - value)

                if (currentPageOffset.absoluteValue >= 1) {
                    // If we reach the bounds of the allowed offset, cancel the animation
                    cancelAnimation()
                }
            }
        } else {
            // Otherwise we animate to the next item, or spring-back depending on the offset
            animate(
                initialValue = currentPageOffset * pageSize,
                targetValue = pageSize * determineSpringBackOffset(
                    velocity = initialVelocity,
                    offset = targetOffset.coerceIn(-1f, 1f)
                ),
                initialVelocity = initialVelocity,
                animationSpec = spring()
            ) { value, _ ->
                dragBy((currentPageOffset * pageSize) - value)
            }
        }

        snapToNearestPage()
    }

    override fun toString(): String = "PagerState(" +
        "pageCount=$pageCount, " +
        "currentPage=$currentPage, " +
        "selectionState=$selectionState, " +
        "currentPageOffset=$currentPageOffset" +
        ")"

    companion object {
        /**
         * The default [Saver] implementation for [PagerState].
         */
        val Saver: Saver<PagerState, *> = listSaver(
            save = { listOf<Any>(it.pageCount, it.currentPage, it.currentPageOffset) },
            restore = {
                PagerState(
                    pageCount = it[0] as Int,
                    currentPage = it[1] as Int,
                    currentPageOffset = it[2] as Float
                )
            }
        )
    }
}
