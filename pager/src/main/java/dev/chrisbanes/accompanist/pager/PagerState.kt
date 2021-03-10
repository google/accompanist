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
 * @param currentPage the initial value for [PagerState.currentPage]
 * @param currentPageOffset the initial value for [PagerState.currentPageOffset]
 * @param pageCount the initial value for [PagerState.pageCount]
 */
@Composable
fun rememberPagerState(
    currentPage: Int = 0,
    @FloatRange(from = 0.0, to = 1.0) currentPageOffset: Float = 0f,
    pageCount: Int = 0,
): PagerState = rememberSaveable(saver = PagerState.Saver) {
    PagerState(
        currentPage = currentPage,
        currentPageOffset = currentPageOffset,
        pageCount = pageCount
    )
}

/**
 * A state object that can be hoisted to control and observe scrolling for [Pager].
 *
 * In most cases, this will be created via [rememberPagerState].
 *
 * @param currentPage the initial value for [PagerState.currentPage]
 * @param currentPageOffset the initial value for [PagerState.currentPageOffset]
 * @param pageCount the initial value for [PagerState.pageCount]
 */
class PagerState(
    currentPage: Int = 0,
    @FloatRange(from = 0.0, to = 1.0) currentPageOffset: Float = 0f,
    pageCount: Int = 0,
) {
    private var _pageCount by mutableStateOf(pageCount)
    private var _currentPage by mutableStateOf(currentPage)
    private val _currentPageOffset = mutableStateOf(currentPageOffset)
    internal var pageSize by mutableStateOf(0)

    /**
     * The index of the currently selected page.
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
                minimumValue = if (currentPage == lastPageIndex) 0f else -1f,
                maximumValue = if (currentPage == 0) 0f else 1f,
            )
        }

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
            selectionState = SelectionState.Undecided
            animateToPage(
                page = page.coerceIn(0, lastPageIndex),
                pageOffset = pageOffset.coerceIn(0f, 1f),
                initialVelocity = initialVelocity,
            )
            selectionState = SelectionState.Selected
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
            selectionState = SelectionState.Selected
        }
    }

    private fun snapToNearestPage() {
        if (DebugLog) {
            Log.d(LogTag, "snapToNearestPage. currentPage:$currentPage, offset:$currentPageOffset")
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
        minimumValue = if (currentPage == lastPageIndex) 0f else -1f,
        maximumValue = if (currentPage == 0) 0f else 1f
    )

    internal val draggableState = DraggableState { delta ->
        if (DebugLog) Log.d(LogTag, "DraggableState.onDrag: $delta")

        this@PagerState.currentPageOffset += delta / pageSize.coerceAtLeast(1)
    }

    /**
     * TODO make this public?
     */
    internal suspend fun performFling(
        initialVelocity: Float,
        animationSpec: DecayAnimationSpec<Float>,
    ) = draggableState.drag {
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
                dragBy(value - (currentPageOffset * pageSize))

                if (currentPageOffset.absoluteValue >= 1) {
                    // If we reach the bounds of the allowed offset, cancel the animation
                    cancelAnimation()
                }
            }
            snapToNearestPage()
        } else {
            // Otherwise we animate to the next item, or spring-back depending on the offset
            animate(
                initialValue = currentPageOffset * pageSize,
                targetValue = determineSpringBackOffset(targetOffset.coerceIn(-1f, 1f)) * pageSize,
                initialVelocity = initialVelocity,
                animationSpec = spring()
            ) { value, _ ->
                dragBy(value - (currentPageOffset * pageSize))
            }
            snapToNearestPage()
        }
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
            save = { listOf<Any>(it.currentPage, it.currentPageOffset) },
            restore = {
                PagerState(
                    currentPage = it[0] as Int,
                    currentPageOffset = it[1] as Float
                )
            }
        )
    }
}
