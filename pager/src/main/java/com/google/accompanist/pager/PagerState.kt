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
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.aakira.napier.Napier
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.roundToInt

private const val LogTag = "PagerState"

/**
 * Creates a [PagerState] that is remembered across compositions.
 *
 * Changes to the provided values for [initialPage], [initialPageOffset] & [initialOffscreenLimit]
 * will **not** result in the state being recreated or changed in any way if it has already
 * been created. Changes to [pageCount] will result in the [PagerState] being updated.
 *
 * @param pageCount the value for [PagerState.pageCount]
 * @param initialPage the initial value for [PagerState.currentPage]
 * @param initialPageOffset the initial value for [PagerState.currentPageOffset]
 * @param initialOffscreenLimit the number of pages that should be retained on either side of the
 * current page. This value is required to be `1` or greater.
 */
@ExperimentalPagerApi
@Composable
fun rememberPagerState(
    @IntRange(from = 0) pageCount: Int,
    @IntRange(from = 0) initialPage: Int = 0,
    @FloatRange(from = 0.0, to = 1.0) initialPageOffset: Float = 0f,
    @IntRange(from = 1) initialOffscreenLimit: Int = 1,
): PagerState = rememberSaveable(saver = PagerState.Saver) {
    PagerState(
        pageCount = pageCount,
        currentPage = initialPage,
        currentPageOffset = initialPageOffset,
        offscreenLimit = initialOffscreenLimit,
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
 */
@ExperimentalPagerApi
@Stable
class PagerState(
    @IntRange(from = 0) pageCount: Int,
    @IntRange(from = 0) currentPage: Int = 0,
    @FloatRange(from = 0.0, to = 1.0) currentPageOffset: Float = 0f,
    private val offscreenLimit: Int = 1,
) : ScrollableState {
    private var _pageCount by mutableStateOf(pageCount)
    private var _currentPage by mutableStateOf(currentPage)
    private var _currentLayoutPageOffset by mutableStateOf(currentPageOffset)

    /**
     * This is the array of all the pages to be laid out. In effect, this contains the
     * 'current' page, plus the `offscreenLimit` on either side. Each PageLayoutInfo holds the page
     * index it should be displaying, and it's current layout size. Pager reads these values
     * to layout the pages as appropriate.
     *
     * The 'current layout page' is in the center of the array. The index is available at
     * [currentLayoutPageIndex].
     */
    internal val layoutPages: Array<PageLayoutInfo> =
        Array((offscreenLimit * 2) + 1) { PageLayoutInfo() }

    /**
     * The index for the 'current layout page' in [layoutPages].
     */
    private val currentLayoutPageIndex: Int = (layoutPages.size - 1) / 2

    internal var currentLayoutPageOffset: Float
        get() = _currentLayoutPageOffset
        private set(value) {
            _currentLayoutPageOffset = value.coerceIn(
                minimumValue = 0f,
                maximumValue = if (currentLayoutPage == lastPageIndex) 0f else 1f,
            )
        }

    /**
     * The width/height of the current layout page (depending on the layout).
     */
    private inline val currentLayoutPageSize: Int
        get() = currentLayoutPageInfo.layoutSize

    /**
     * The page which is currently laid out.
     */
    internal inline val currentLayoutPage: Int
        get() = currentLayoutPageInfo.page!!

    internal inline val currentLayoutPageInfo: PageLayoutInfo
        get() = layoutPages[currentLayoutPageIndex]

    /**
     * The current scroll position, as a float value between `0 until pageSize`
     */
    private inline val absolutePosition: Float
        get() = currentLayoutPage + currentLayoutPageOffset

    internal inline val lastPageIndex: Int
        get() = (pageCount - 1).coerceAtLeast(0)

    /**
     * The ScrollableController instance. We keep it as we need to call stopAnimation on it once
     * we reached the end of the list.
     */
    private val scrollableState = ScrollableState { deltaPixels ->
        // scrollByOffset expects values in an opposite sign to what we're passed, so we need
        // to negate the value passed in, and the value returned.
        val size = currentLayoutPageSize
        require(size > 0) { "Layout size for current item is 0" }
        -scrollByOffset(-deltaPixels / size) * size
    }

    init {
        require(offscreenLimit >= 1) { "offscreenLimit is required to be >= 1" }
        require(pageCount >= 0) { "pageCount must be >= 0" }
        requireCurrentPage(currentPage, "currentPage")
        requireCurrentPageOffset(currentPageOffset, "currentPageOffset")

        updateLayoutPages(currentPage)
    }

    /**
     * The number of pages to display.
     */
    @get:IntRange(from = 0)
    var pageCount: Int
        get() = _pageCount
        set(@IntRange(from = 0) value) {
            require(value >= 0) { "pageCount must be >= 0" }
            _pageCount = value
            currentPage = currentPage.coerceIn(0, lastPageIndex)
            updateLayoutPages(currentPage)
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
            _currentPage = value.coerceIn(0, lastPageIndex)
            if (DebugLog) {
                Napier.d(tag = LogTag, message = "Current page changed: $_currentPage")
            }
            // If the current page is changed, update the layout page too
            updateLayoutPages(_currentPage)
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
     * Returns null if a scroll or animation is not currently in progress.
     */
    val targetPage: Int?
        get() = _animationTargetPage ?: when {
            // If a scroll isn't in progress, return null
            !isScrollInProgress -> null
            // If we're offset towards the start, guess the previous page
            currentPageOffset < 0 -> (currentPage - 1).coerceAtLeast(0)
            // If we're offset towards the end, guess the next page
            else -> (currentPage + 1).coerceAtMost(lastPageIndex)
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
     * @param initialVelocity Initial velocity in pixels per second, or `0f` to not use a start velocity.
     * Must be in the range 0f..1f.
     * @param skipPages Whether to skip most intermediate pages. This allows the layout to skip
     * creating pages which are only displayed for a *very* short amount of time. Visually users
     * should see no difference. Pass `false` to animate over all pages between [currentPage]
     * and [page]. Defaults to `true`.
     */
    suspend fun animateScrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
        animationSpec: AnimationSpec<Float> = spring(),
        initialVelocity: Float = 0f,
        skipPages: Boolean = true,
    ) {
        requireCurrentPage(page, "page")
        requireCurrentPageOffset(pageOffset, "pageOffset")
        if (page == currentPage && pageOffset == currentLayoutPageOffset) return

        // We don't specifically use the ScrollScope's scrollBy, but
        // we do want to use it's mutex
        scroll {
            val target = page.coerceIn(0, lastPageIndex)

            val currentIndex = currentLayoutPage
            val distance = (target - currentIndex).absoluteValue

            /**
             * The distance of 4 may seem like a magic number, but it's not.
             * It's: current page, current page + 1, target page - 1, target page.
             * This provides the illusion of movement, but allows us to lay out as few pages
             * as possible. 🧙‍♂️
             */
            if (skipPages && distance > 4) {
                animateToPageSkip(target, pageOffset, animationSpec, initialVelocity)
            } else {
                animateToPageLinear(target, pageOffset, animationSpec, initialVelocity)
            }
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
     * @param pageOffset the percentage of the page width to offset, from the start of [page].
     * Must be in the range 0f..1f.
     */
    suspend fun scrollToPage(
        @IntRange(from = 0) page: Int,
        @FloatRange(from = 0.0, to = 1.0) pageOffset: Float = 0f,
    ) {
        requireCurrentPage(page, "page")
        requireCurrentPageOffset(pageOffset, "pageOffset")
        if (page == currentPage && pageOffset == currentLayoutPageOffset) return

        // We don't specifically use the ScrollScope's scrollBy(), but
        // we do want to use it's mutex
        scroll {
            snapToPage(page, pageOffset)
        }
    }

    /**
     * Snap the layout the given [page] and [offset].
     */
    private fun snapToPage(page: Int, offset: Float = 0f) {
        if (DebugLog) {
            Napier.d(
                tag = LogTag,
                message = "snapToPage. page:$currentLayoutPage, offset:$currentLayoutPageOffset"
            )
        }
        // Snap the layout
        updateLayoutPages(page)
        currentLayoutPageOffset = offset
        // Then update the current page to match
        currentPage = page
        // Clear the target page
        _animationTargetPage = null
    }

    private fun snapToNearestPage() {
        snapToPage(currentLayoutPage + currentLayoutPageOffset.roundToInt())
    }

    /**
     * Animates to the given [page] and [pageOffset] linearly, by animating through all pages
     * in-between [currentPage] and [page]. As an example, if we're currently displaying item 0,
     * and we want to animate to page 9, this function will lay out and animate over:
     * [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]. This is different to [animateToPageSkip] which skips the
     * intermediate pages.
     */
    private suspend fun animateToPageLinear(
        page: Int,
        pageOffset: Float,
        animationSpec: AnimationSpec<Float>,
        initialVelocity: Float,
    ) {
        // Set our target page
        _animationTargetPage = page

        animate(
            initialValue = absolutePosition,
            targetValue = page + pageOffset,
            initialVelocity = initialVelocity,
            animationSpec = animationSpec
        ) { value, _ ->
            updateLayoutForScrollPosition(value)
        }

        // At the end of the animate, snap to the page + offset. This isn't strictly necessary,
        // but ensures that all our state to consistent.
        snapToPage(page = page, offset = pageOffset)
    }

    /**
     * Animates to the given [page] and [pageOffset], but unlike [animateToPageLinear]
     * it skips intermediate pages. As an example, if we're currently displaying item 0, and we
     * want to animate to page 9, this function will only lay out and animate over: [0, 1, 8, 9].
     */
    private suspend fun animateToPageSkip(
        page: Int,
        pageOffset: Float,
        animationSpec: AnimationSpec<Float>,
        initialVelocity: Float,
    ) {
        // Set our target page
        _animationTargetPage = page

        val initialIndex = currentLayoutPage
        // These are the pages which we'll iterate through to display the 'effect' of scrolling.
        val pages: IntArray = when {
            page > initialIndex -> intArrayOf(initialIndex, initialIndex + 1, page - 1, page)
            else -> intArrayOf(initialIndex, initialIndex - 1, page + 1, page)
        }

        // We animate over the length of the `pages` array (including the offset). Pages includes
        // the current page (to allow us to animate over the offset) so we need to minus 1
        animate(
            initialValue = currentPageOffset,
            targetValue = pages.size + pageOffset - 1,
            initialVelocity = initialVelocity,
            animationSpec = animationSpec
        ) { value, _ ->
            // Value here is the [index of page in pages] + offset. We floor the value to get
            // the pages index
            val flooredIndex = floor(value).toInt()
            // We then go through each layout page and set it to the correct page from [pages]
            layoutPages.forEachIndexed { index, layoutInfo ->
                layoutInfo.page = pages.getOrNull(flooredIndex + (index - currentLayoutPageIndex))
            }
            if (DebugLog) {
                Napier.d(
                    tag = LogTag,
                    message = "animateToPageSkip: $layoutPages"
                )
            }

            // Then derive the remaining offset from the index
            currentLayoutPageOffset = value - flooredIndex
        }

        // At the end of the animate, snap to the page + offset. This isn't strictly necessary,
        // but ensures that all our state to consistent.
        snapToPage(page = page, offset = pageOffset)
    }

    private fun determineSpringBackOffset(
        velocity: Float,
        offset: Float = currentLayoutPageOffset,
    ): Int = when {
        // If the velocity is greater than 1 page per second (velocity is px/s), spring
        // in the relevant direction
        velocity >= currentLayoutPageSize -> 1
        velocity <= -currentLayoutPageSize -> 0
        // If the offset exceeds the scroll threshold (in either direction), we want to
        // move to the next/previous item
        offset < 0.5f -> 0
        else -> 1
    }

    /**
     * Updates the [layoutPages] so that for the given [position].
     */
    private fun updateLayoutForScrollPosition(position: Float) {
        val newIndex = floor(position).toInt()
        updateLayoutPages(newIndex)
        currentLayoutPageOffset = position - newIndex
    }

    /**
     * Updates the [layoutPages] so that [page] is the current laid out page.
     */
    private fun updateLayoutPages(page: Int) {
        layoutPages.forEachIndexed { index, layoutPage ->
            val pg = page + index - offscreenLimit
            layoutPage.page = if (pg < 0 || pg > lastPageIndex) null else pg
        }
    }

    /**
     * Scroll by the pager with the given [deltaOffset].
     *
     * @param deltaOffset delta in offset values (0f..1f). Values > 0 signify scrolls
     * towards the end of the pager, and values < 0 towards the start.
     * @return the amount of [deltaOffset] consumed
     */
    private fun scrollByOffset(deltaOffset: Float): Float {
        val current = absolutePosition
        val target = (current + deltaOffset).coerceIn(0f, lastPageIndex.toFloat())

        if (DebugLog) {
            Napier.d(
                tag = LogTag,
                message = "scrollByOffset [before]. delta:%.4f, current:%.4f, target:%.4f"
                    .format(deltaOffset, current, target),
            )
        }

        updateLayoutForScrollPosition(target)

        if (DebugLog) {
            Napier.d(
                tag = LogTag,
                message = "scrollByOffset [after]. delta:%.4f, new-page:%d, new-offset:%.4f"
                    .format(deltaOffset, currentLayoutPage, currentLayoutPageOffset),
            )
        }

        return target - current
    }

    /**
     * Fling the pager with the given [initialVelocity]. [scrollBy] will called whenever a
     * scroll change is required by the fling.
     *
     * @param initialVelocity velocity in pixels per second. Values > 0 signify flings
     * towards the end of the pager, and values < 0 sign flings towards the start.
     * @param decayAnimationSpec The decay animation spec to use for decayed flings.
     * @param snapAnimationSpec The animation spec to use when snapping.
     * @param scrollBy block which is called when a scroll is required. Positive values passed in
     * signify scrolls towards the end of the pager, and values < 0 towards the start.
     * @return any remaining velocity after the scroll has finished.
     */
    internal suspend fun fling(
        initialVelocity: Float,
        decayAnimationSpec: DecayAnimationSpec<Float> = exponentialDecay(),
        snapAnimationSpec: AnimationSpec<Float> = spring(),
        scrollBy: (Float) -> Float,
    ): Float {
        // We calculate the target offset using pixels, rather than using the offset
        val targetOffset = decayAnimationSpec.calculateTargetValue(
            initialValue = currentLayoutPageOffset * currentLayoutPageSize,
            initialVelocity = initialVelocity
        ) / currentLayoutPageSize

        if (DebugLog) {
            Napier.d(
                tag = LogTag,
                message = "fling. velocity:%.4f, page: %d, offset:%.4f, targetOffset:%.4f"
                    .format(
                        initialVelocity,
                        currentLayoutPage,
                        currentLayoutPageOffset,
                        targetOffset
                    )
            )
        }

        var lastVelocity: Float = initialVelocity

        // If the animation can naturally end outside of current page bounds, we will
        // animate with decay.
        if (targetOffset.absoluteValue >= 1) {
            // Animate with the decay animation spec using the fling velocity

            val target = when {
                targetOffset > 0 -> (currentLayoutPage + 1).coerceAtMost(lastPageIndex)
                else -> currentLayoutPage
            }
            // Update the external state too
            _animationTargetPage = target

            AnimationState(
                initialValue = currentLayoutPageOffset * currentLayoutPageSize,
                initialVelocity = initialVelocity
            ).animateDecay(decayAnimationSpec) {
                if (DebugLog) {
                    Napier.d(
                        tag = LogTag,
                        message = "fling. decay. value:%.4f, page: %d, offset:%.4f"
                            .format(value, currentPage, currentPageOffset)
                    )
                }

                // Keep track of velocity
                lastVelocity = velocity

                // Now scroll..
                val coerced = value.coerceIn(0f, currentLayoutPageSize.toFloat())
                scrollBy(coerced - (currentLayoutPageOffset * currentLayoutPageSize))

                // If we've scroll our target page (or beyond it), cancel the animation
                if ((initialVelocity < 0 && absolutePosition <= target) ||
                    (initialVelocity > 0 && absolutePosition >= target)
                ) {
                    // If we reach the bounds of the allowed offset, cancel the animation
                    cancelAnimation()
                }
            }
            snapToPage(target)
        } else {
            // Otherwise we animate to the next item, or spring-back depending on the offset
            val target = currentLayoutPage + determineSpringBackOffset(
                velocity = initialVelocity,
                offset = targetOffset
            )
            // Update the external state too
            _animationTargetPage = target

            animate(
                initialValue = absolutePosition * currentLayoutPageSize,
                targetValue = target.toFloat() * currentLayoutPageSize,
                initialVelocity = initialVelocity,
                animationSpec = snapAnimationSpec,
            ) { value, velocity ->
                scrollBy(value - (absolutePosition * currentLayoutPageSize))
                // Keep track of velocity
                lastVelocity = velocity
            }
            snapToNearestPage()
        }

        return lastVelocity
    }

    override val isScrollInProgress: Boolean
        get() = scrollableState.isScrollInProgress

    override fun dispatchRawDelta(delta: Float): Float {
        return scrollableState.dispatchRawDelta(delta)
    }

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit
    ) {
        scrollableState.scroll(scrollPriority, block)
    }

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
                "$name must be >= 0 and < pageCount"
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

    companion object {
        /**
         * The default [Saver] implementation for [PagerState].
         */
        val Saver: Saver<PagerState, *> = listSaver(
            save = { listOf(it.pageCount, it.currentPage) },
            restore = {
                PagerState(
                    pageCount = it[0],
                    currentPage = it[1],
                )
            }
        )
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
