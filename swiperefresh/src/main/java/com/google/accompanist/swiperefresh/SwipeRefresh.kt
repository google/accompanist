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

package com.google.accompanist.swiperefresh

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

typealias SwipeRefreshIndicator = @Composable (state: SwipeRefreshState, refreshTrigger: Dp) -> Unit

private const val DragMultiplier = 0.5f

/**
 * Creates a [SwipeRefreshState] that is remembered across compositions.
 *
 * Changes to [isRefreshing] will result in the [SwipeRefreshState] being updated.
 *
 * @param isRefreshing the value for [SwipeRefreshState.isRefreshing]
 */
@Composable
fun rememberSwipeRefreshState(
    isRefreshing: Boolean,
): SwipeRefreshState {
    return remember {
        SwipeRefreshState(
            isRefreshing = isRefreshing,
        )
    }.apply {
        this.isRefreshing = isRefreshing
    }
}

/**
 * A state object that can be hoisted to control and observe changes for [SwipeRefresh].
 *
 * In most cases, this will be created via [rememberSwipeRefreshState].
 *
 * @param isRefreshing the initial value for [SwipeRefreshState.isRefreshing]
 */
@Stable
class SwipeRefreshState(
    isRefreshing: Boolean,
) {
    private val _indicatorOffset = Animatable(0f)
    private val mutatorMutex = MutatorMutex()

    /**
     * Indicate the position of the "swipe to refresh", that own this state, Whether
     * [SwipeRefreshPosition.TOP] or [SwipeRefreshPosition.BOTTOM], its default value is
     * [SwipeRefreshPosition.TOP].
     */
    internal var swipeRefreshPosition = SwipeRefreshPosition.TOP

    /**
     * Whether this [SwipeRefreshState] is currently refreshing or not.
     */
    var isRefreshing: Boolean by mutableStateOf(isRefreshing)

    /**
     * Whether a swipe/drag is currently in progress.
     */
    var isSwipeInProgress: Boolean by mutableStateOf(false)
        internal set

    /**
     * The current offset for the indicator, in pixels.
     */
    val indicatorOffset: Float get() = _indicatorOffset.value

    internal suspend fun animateOffsetTo(offset: Float) {
        mutatorMutex.mutate {
            _indicatorOffset.animateTo(offset)
        }
    }

    /**
     * Dispatch scroll delta in pixels from touch events.
     */
    internal suspend fun dispatchScrollDelta(delta: Float) {
        mutatorMutex.mutate(MutatePriority.UserInput) {
            if (swipeRefreshPosition == SwipeRefreshPosition.TOP) {
                _indicatorOffset.snapTo(_indicatorOffset.value + delta)
            } else {
                _indicatorOffset.snapTo(_indicatorOffset.value - delta)

            }
        }
    }
}

/**
 * The position for which the "swipe to refresh" must be enabled:
 */
enum class SwipeRefreshPosition {
    TOP,
    BOTTOM,
}

private class SwipeRefreshNestedScrollConnection(
    private val topSwipeRefreshState: SwipeRefreshState? = null,
    private val coroutineScope: CoroutineScope,
    private val onRefresh: (SwipeRefreshPosition) -> Unit,
    private val bottomSwipeRefreshState: SwipeRefreshState? = null,
) : NestedScrollConnection {
    var enabled: Boolean = false
    var refreshTrigger: Float = 0f

    init {
        if (topSwipeRefreshState == null && bottomSwipeRefreshState == null) {
            throw IllegalArgumentException(
                """`topSwipeRefreshState` and `bottomSwipeRefreshState` ar both null.
                | At least one [SwipeRefreshState] most be provided.""".trimMargin(),
            )
        }
    }

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        return when {
            // If swiping isn't enabled, return zero
            !enabled -> Offset.Zero
            // If we're refreshing, return zero
            topSwipeRefreshState?.isRefreshing ?: false ||
                bottomSwipeRefreshState?.isRefreshing ?: false -> Offset.Zero
            // If the user is swiping up, handle it
            source == NestedScrollSource.Drag && available.y < 0 -> {
                if (topSwipeRefreshState == null) {
                    Offset.Zero
                } else {
                    onScrollToTop(available)
                }
            }
            else -> Offset.Zero
        }
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        // If swiping isn't enabled, return zero
        !enabled -> Offset.Zero
        // If we're refreshing, return zero
        topSwipeRefreshState?.isRefreshing ?: false ||
            bottomSwipeRefreshState?.isRefreshing ?: false -> Offset.Zero
        // If the user is swiping down and there's y remaining, handle it
        source == NestedScrollSource.Drag && available.y > 0 -> {
            if (topSwipeRefreshState == null) {
                Offset.Zero
            } else {
                onScrollToTop(available)
            }
        }
        // If the user is swiping up and there's y remaining, handle it
        source == NestedScrollSource.Drag && available.y < 0 -> {
            if (bottomSwipeRefreshState == null) {
                Offset.Zero
            } else {
                onScrollToBottom(available)
            }
        }
        else -> Offset.Zero
    }

    private fun onScrollToBottom(available: Offset): Offset {
        bottomSwipeRefreshState!!.isSwipeInProgress = true

        val newOffset = (available.y * DragMultiplier - bottomSwipeRefreshState.indicatorOffset)
            .coerceAtMost(0f)
        val dragConsumed = newOffset + bottomSwipeRefreshState.indicatorOffset

        return if (dragConsumed.absoluteValue >= 0.5f) {
            coroutineScope.launch {
                bottomSwipeRefreshState.dispatchScrollDelta(dragConsumed)
            }
            // Return the consumed Y
            Offset(x = 0f, y = dragConsumed * DragMultiplier)
        } else {
            Offset.Zero
        }
    }

    private fun onScrollToTop(available: Offset): Offset {
        topSwipeRefreshState!!.isSwipeInProgress = true

        val newOffset = (available.y * DragMultiplier + topSwipeRefreshState.indicatorOffset)
            .coerceAtLeast(0f)
        val dragConsumed = newOffset - topSwipeRefreshState.indicatorOffset

        return if (dragConsumed.absoluteValue >= 0.5f) {
            coroutineScope.launch {
                topSwipeRefreshState.dispatchScrollDelta(dragConsumed)
            }
            // Return the consumed Y
            Offset(x = 0f, y = dragConsumed / DragMultiplier)
        } else {
            Offset.Zero
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        // If we're dragging, not currently refreshing and scrolled
        // past the trigger point, refresh!
        //TODO Refactoring && improve readability
        if (topSwipeRefreshState != null && !topSwipeRefreshState.isRefreshing &&
            topSwipeRefreshState.indicatorOffset >= refreshTrigger) {
            onRefresh(topSwipeRefreshState.swipeRefreshPosition)
            // Reset the drag in progress state
        } else if (bottomSwipeRefreshState != null && !bottomSwipeRefreshState.isRefreshing &&
            bottomSwipeRefreshState.indicatorOffset >= refreshTrigger) {
            onRefresh(bottomSwipeRefreshState.swipeRefreshPosition)
            // Reset the drag in progress state
        }

        topSwipeRefreshState?.isSwipeInProgress = false
        bottomSwipeRefreshState?.isSwipeInProgress = false

        // Don't consume any velocity, to allow the scrolling layout to fling
        return Velocity.Zero
    }
}

/**
 * A layout which implements the swipe-to-refresh pattern, allowing the user to refresh content via
 * a vertical swipe gesture.
 *
 * This layout requires its content to be scrollable so that it receives vertical swipe events.
 * The scrollable content does not need to be a direct descendant though. Layouts such as
 * [androidx.compose.foundation.lazy.LazyColumn] are automatically scrollable, but others such as
 * [androidx.compose.foundation.layout.Column] require you to provide the
 * [androidx.compose.foundation.verticalScroll] modifier to that content.
 *
 * Apps should provide a [onRefresh] block to be notified each time a swipe to refresh gesture
 * is completed. That block is responsible for updating the [topSwipeRefreshState] as appropriately,
 * typically by setting [SwipeRefreshState.isRefreshing] to `true` once a 'refresh' has been
 * started. Once a refresh has completed, the app should then set
 * [SwipeRefreshState.isRefreshing] to `false`.
 *
 * If an app wishes to show the progress animation outside of a swipe gesture, it can
 * set [SwipeRefreshState.isRefreshing] as required.
 *
 * This layout does not clip any of it's contents, including the indicator. If clipping
 * is required, apps can provide the [androidx.compose.ui.draw.clipToBounds] modifier.
 *
 * @sample com.google.accompanist.sample.swiperefresh.SwipeRefreshSample
 *
 * @param topSwipeRefreshState the state object to be used to control or observe the [SwipeRefresh] state.
 * @param onRefresh Lambda which is invoked when a swipe to refresh gesture is completed.
 * @param modifier the modifier to apply to this layout.
 * @param swipeEnabled Whether the the layout should react to swipe gestures or not.
 * @param refreshTriggerDistance The minimum swipe distance which would trigger a refresh.
 * @param topIndicatorAlignment The alignment of the indicator. Defaults to [Alignment.TopCenter].
 * @param indicatorPadding Content padding for the indicator, to inset the indicator in if required.
 * @param indicator the indicator that represents the current state. By default this
 * will use a [SwipeRefreshIndicator].
 * @param clipIndicatorToPadding Whether to clip the indicator to [indicatorPadding]. If false is
 * provided the indicator will be clipped to the [content] bounds. Defaults to true.
 * @param content The content containing a scroll composable.
 */
@Composable
fun SwipeRefresh(
    onRefresh: (SwipeRefreshPosition) -> Unit,
    modifier: Modifier = Modifier,
    topSwipeRefreshState: SwipeRefreshState? = null,
    bottomSwipeRefreshState: SwipeRefreshState? = null,
    swipeEnabled: Boolean = true,
    refreshTriggerDistance: Dp = 80.dp,
    topIndicatorAlignment: Alignment = Alignment.TopCenter,
    bottomIndicatorAlignment: Alignment = Alignment.BottomCenter,
    indicatorPadding: PaddingValues = PaddingValues(0.dp),
    topIndicator: SwipeRefreshIndicator = { s, trigger ->
        SwipeRefreshIndicator(s, trigger)
    },
    bottomIndicator: SwipeRefreshIndicator = { s, trigger ->
        SwipeRefreshIndicator(s, trigger)
    },
    clipIndicatorToPadding: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (topSwipeRefreshState == null && bottomSwipeRefreshState == null) {
        throw IllegalArgumentException(
            """`topSwipeRefreshState` and `bottomSwipeRefreshState` ar both null.
            | At least one [SwipeRefreshState] most be provided.""".trimMargin(),
        )
    }

    topSwipeRefreshState?.swipeRefreshPosition = SwipeRefreshPosition.TOP
    bottomSwipeRefreshState?.swipeRefreshPosition = SwipeRefreshPosition.BOTTOM

    val coroutineScope = rememberCoroutineScope()
    val updatedOnRefresh = rememberUpdatedState(onRefresh)

    // Our LaunchedEffect, which animates the indicator to its resting position
    LaunchedEffect(topSwipeRefreshState?.isSwipeInProgress) {
        if (topSwipeRefreshState != null) {
            if (!topSwipeRefreshState.isSwipeInProgress) {
                // If there's not a swipe in progress, rest the indicator at 0f
                topSwipeRefreshState.animateOffsetTo(0f)
            }
        }
    }

    LaunchedEffect(bottomSwipeRefreshState?.isSwipeInProgress) {
        if (bottomSwipeRefreshState != null && !bottomSwipeRefreshState.isSwipeInProgress) {
            bottomSwipeRefreshState.animateOffsetTo(0f)
        }
    }


    val refreshTriggerPx = with(LocalDensity.current) { refreshTriggerDistance.toPx() }

    // Our nested scroll connection, which updates our state.
    val nestedScrollConnection = remember(topSwipeRefreshState, coroutineScope) {
        SwipeRefreshNestedScrollConnection(
            topSwipeRefreshState,
            coroutineScope,
            onRefresh = updatedOnRefresh.value,
            bottomSwipeRefreshState = bottomSwipeRefreshState,
        )
    }.apply {
        this.enabled = swipeEnabled
        this.refreshTrigger = refreshTriggerPx
    }

    Box(modifier.nestedScroll(connection = nestedScrollConnection)) {
        content()

        Box(
            Modifier
                // If we're not clipping to the padding, we use clipToBounds() before the padding()
                // modifier.
                .let { if (!clipIndicatorToPadding) it.clipToBounds() else it }
                .padding(indicatorPadding)
                .matchParentSize()
                // Else, if we're are clipping to the padding, we use clipToBounds() after
                // the padding() modifier.
                .let { if (clipIndicatorToPadding) it.clipToBounds() else it }
        ) {
            Box(Modifier.align(topIndicatorAlignment)) {
                if (topSwipeRefreshState != null) {
                    topIndicator(topSwipeRefreshState, refreshTriggerDistance)
                }
            }

            if (bottomSwipeRefreshState != null) {
                Box(Modifier.align(bottomIndicatorAlignment)) {
                    bottomIndicator(bottomSwipeRefreshState, refreshTriggerDistance)
                }
            }
        }
    }
}
