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

package com.google.accompanist.pager

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

/**
 * Creates a [PagerState] that is remembered across compositions.
 *
 * Changes to the provided values for [initialPage], [initialPageOffset]
 * will **not** result in the state being recreated or changed in any way if it has already
 * been created. Changes to [pageCount] will result in the [PagerState] being updated.
 *
 * @param pageCount the value for [PagerState.pageCount]
 * @param initialPage the initial value for [PagerState.currentPage]
 * @param initialPageOffset the initial value for [PagerState.currentPageOffset]
 * @param infiniteLoop Whether to support infinite looping effect.
 */
@ExperimentalPagerApi
@Composable
fun rememberPagerState(
    @IntRange(from = 0) pageCount: Int,
    @IntRange(from = 0) initialPage: Int = 0,
    @FloatRange(from = 0.0, to = 1.0) initialPageOffset: Float = 0f,
    @IntRange(from = 1) initialOffscreenLimit: Int = 1,
    infiniteLoop: Boolean = false,
    lazyListState: LazyListState = rememberLazyListState(),
): PagerState = rememberSaveable(saver = PagerState.Saver) {
    PagerState(
        pageCount = pageCount,
        currentPage = initialPage,
        currentPageOffset = initialPageOffset,
        offscreenLimit = initialOffscreenLimit,
        infiniteLoop = infiniteLoop,
        lazyListState = lazyListState,
    )
}.apply {
    this.pageCount = pageCount
}

/**
 * A state object that can be hoisted to control and observe scrolling for [HorizontalPager].
 *
 * In most cases, this will be created via [rememberPagerState].
 *
 * The `offscreenLimit` param defines the number of pages that
 * should be retained on either side of the current page. Pages beyond this limit will be
 * recreated as needed. This value defaults to `1`, but can be increased to enable pre-loading
 * of more content.
 *
 * @param pageCount the initial value for [PagerState.pageCount]
 * @param currentPage the initial value for [PagerState.currentPage]
 * @param currentPageOffset the initial value for [PagerState.currentPageOffset]
 * @param offscreenLimit the number of pages that should be retained on either side of the
 * current page. This value is required to be `1` or greater.
 * @param infiniteLoop Whether to support infinite looping effect.
 */
@ExperimentalPagerApi
@Stable
class PagerState(
    @IntRange(from = 0) pageCount: Int,
    @IntRange(from = 0) currentPage: Int = 0,
    @FloatRange(from = 0.0, to = 1.0) currentPageOffset: Float = 0f,
    private val offscreenLimit: Int = 1,
    private val infiniteLoop: Boolean = false,
    val lazyListState: LazyListState,
) : ScrollableState by lazyListState {
    private var _pageCount by mutableStateOf(pageCount)
    private var _currentPage by mutableStateOf(currentPage)

    internal var isVertical by mutableStateOf(false)
    internal var viewportHeight by mutableStateOf(0)
    internal var viewportWidth by mutableStateOf(0)
    internal var verticalAlignment: Alignment.Vertical by mutableStateOf(Alignment.CenterVertically)
    internal var horizontalAlignment: Alignment.Horizontal by mutableStateOf(Alignment.CenterHorizontally)

    val currentLayoutPage: LazyListItemInfo? by derivedStateOf {
        val layoutInfo = lazyListState.layoutInfo

        if (isVertical) {
            null
        } else {
            when (horizontalAlignment) {
                Alignment.Start -> {
                    layoutInfo.visibleItemsInfo.firstOrNull {
                        it.offset < layoutInfo.viewportStartOffset &&
                            (it.offset + it.size) <= layoutInfo.viewportEndOffset
                    }
                }
                Alignment.End -> {
                    layoutInfo.visibleItemsInfo.firstOrNull {
                        it.offset < layoutInfo.viewportStartOffset &&
                            (it.offset + it.size) <= layoutInfo.viewportEndOffset
                    }
                }
                else -> { // CenterHorizontally
                    val center = if (isVertical) viewportHeight / 2 else viewportWidth / 2
                    layoutInfo.visibleItemsInfo.firstOrNull {
                        it.offset < center && (it.offset + it.size) <= center
                    }
                }
            }
        }
    }

    val currentLayoutPageOffset: Float by derivedStateOf {
        0f
    }

    /**
     * When set to true, `page` of [Pager] content can be different in [infiniteLoop] mode.
     */
    internal var testing = false

    /**
     * The current scroll position, as a float value between `firstPageIndex until lastPageIndex`
     */
    private inline val absolutePosition: Float
        get() = 0f // FIXME _currentLayoutPage + _currentLayoutPageOffset

    /**
     * [InteractionSource] that will be used to dispatch drag events when this
     * list is being dragged. If you want to know whether the fling (or animated scroll) is in
     * progress, use [isScrollInProgress].
     */
    val interactionSource: InteractionSource
        get() = lazyListState.interactionSource

    init {
        require(pageCount >= 0) { "pageCount must be >= 0" }
        requireCurrentPage(currentPage, "currentPage")
        requireCurrentPageOffset(currentPageOffset, "currentPageOffset")
    }

    /**
     * The number of pages to display.
     */
    @get:IntRange(from = 0)
    var pageCount: Int
        get() = _pageCount
        set(@IntRange(from = 0) value) {
            require(value >= 0) { "pageCount must be >= 0" }
            if (value != _pageCount) {
                _pageCount = value
                if (DebugLog) {
                    Napier.d(message = "Page count changed: $value")
                }
                currentPage = currentPage.coerceIn(0, pageCount)
            }
        }

    /**
     * The index of the currently selected page. This may not be the page which is
     * currently displayed on screen.
     *
     * To update the scroll position, use [scrollToPage] or [animateScrollToPage].
     */
    @get:IntRange(from = 0)
    var currentPage: Int
        get() = _currentPage
        private set(value) {
            val moddedValue = value.floorMod(pageCount)
            if (moddedValue != _currentPage) {
                _currentPage = moddedValue
                if (DebugLog) {
                    Napier.d(message = "Current page changed: $_currentPage")
                }
            }
        }

    /**
     * The current offset from the start of [currentPage], as a fraction of the page width.
     *
     * To update the scroll position, use [scrollToPage] or [animateScrollToPage].
     */
    val currentPageOffset: Float
        get() = absolutePosition - currentPage

    /**
     * The target page for any on-going animations.
     */
    private var _animationTargetPage: Int? by mutableStateOf(null)

    /**
     * The target page for any on-going animations or scrolls by the user.
     * Returns the current page if a scroll or animation is not currently in progress.
     */
    val targetPage: Int
        get() = _animationTargetPage ?: when {
            // If a scroll isn't in progress, return the current page
            !isScrollInProgress -> currentPage
            // If the offset is 0f (or very close), return the current page
            currentPageOffset < 0.001f -> currentPage
            // If we're offset towards the start, guess the previous page
            currentPageOffset < 0 -> (currentPage - 1).coerceAtLeast(0)
            // If we're offset towards the end, guess the next page
            else -> (currentPage + 1).coerceAtMost(pageCount)
        }

    /**
     * Animate (smooth scroll) to the given page to the middle of the viewport, offset
     * by [pageOffset] percentage of page width.
     *
     * Cancels the currently running scroll, if any, and suspends until the cancellation is
     * complete.
     *
     * @param page the page to animate to. Must be between 0 and [pageCount] (inclusive).
     * @param pageOffset the percentage of the page width to offset, from the start of [page].
     * Must be in the range 0f..1f.
     */
    suspend fun animateScrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) {
        requireCurrentPage(page, "page")
        requireCurrentPageOffset(pageOffset, "pageOffset")

        lazyListState.animateScrollToItem(index = page)
        // FIXME: use pageOffset
    }

    /**
     * Instantly brings the item at [page] to the middle of the viewport, offset by [pageOffset]
     * percentage of page width.
     *
     * Cancels the currently running scroll, if any, and suspends until the cancellation is
     * complete.
     *
     * @param page the page to snap to. Must be between 0 and [pageCount] (inclusive).
     * @param pageOffset the percentage of the page width to offset, from the start of [page].
     * Must be in the range 0f..1f.
     */
    suspend fun scrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) {
        requireCurrentPage(page, "page")
        requireCurrentPageOffset(pageOffset, "pageOffset")

        lazyListState.scrollToItem(index = page)
        // FIXME: use pageOffset
    }

//    private fun determineSpringBackOffset(
//        velocity: Float,
//        offset: Float = currentLayoutPageOffset,
//    ): Int = when {
//        // If the velocity is greater than 1 page per second (velocity is px/s), spring
//        // in the relevant direction
//        velocity >= currentLayoutPageSize -> 1
//        velocity <= -currentLayoutPageSize -> 0
//        // If the offset exceeds the scroll threshold (in either direction), we want to
//        // move to the next/previous item
//        offset < 0.5f -> 0
//        else -> 1
//    }

    override fun toString(): String = "PagerState(" +
        "pageCount=$pageCount, " +
        "currentPage=$currentPage, " +
        "currentPageOffset=$currentPageOffset" +
        ")"

    private fun requireCurrentPage(value: Int, name: String) {
        if (pageCount == 0) {
            require(value == 0) { "$name must be 0 when pageCount is 0" }
        } else {
            require(value in 0 until pageCount) {
                "$name[$value] must be >= firstPageIndex[0] and < lastPageIndex[pageCount]"
            }
        }
    }

    private fun requireCurrentPageOffset(value: Float, name: String) {
        if (pageCount == 0) {
            require(value == 0f) { "$name must be 0f when pageCount is 0" }
        } else {
            require(value in 0f..1f) { "$name must be >= 0 and <= 1" }
        }
    }

    /**
     * Considering infinite loop, returns page between 0 until [pageCount].
     */
    internal fun pageOf(rawPage: Int): Int {
        if (testing) {
            return rawPage
        }
        return rawPage.floorMod(pageCount)
    }

    companion object {
        /**
         * The default [Saver] implementation for [PagerState].
         */
        val Saver: Saver<PagerState, *> = listSaver(
            save = {
                listOf<Any>(
                    it.pageCount,
                    it.currentPage,
                )
            },
            restore = {
                PagerState(
                    pageCount = it[0] as Int,
                    currentPage = it[1] as Int,
                    lazyListState = LazyListState(), // FIXME
                )
            }
        )

        init {
            if (DebugLog) {
                Napier.base(DebugAntilog(defaultTag = "Pager"))
            }
        }

        /**
         * Calculates the floor modulus in the range of -abs([other]) < r < +abs([other]).
         */
        private fun Int.floorMod(other: Int): Int {
            return when (other) {
                0 -> this
                else -> this - this.floorDiv(other) * other
            }
        }
    }
}

@Stable
internal class PageLayoutInfo {
    var page: Int? by mutableStateOf(null)
    var layoutSize: Int by mutableStateOf(0)

    override fun toString(): String {
        return "PageLayoutInfo(page = $page, layoutSize=$layoutSize)"
    }
}
