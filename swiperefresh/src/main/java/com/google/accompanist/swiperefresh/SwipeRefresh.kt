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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

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
    isRefreshing: Boolean
): SwipeRefreshState {
    return remember {
        SwipeRefreshState(
            isRefreshing = isRefreshing
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

    /**
     * The [indicatorOffset] value (or greater) which would trigger a refresh when the use
     * releases from a swipe.
     */
    var indicatorRefreshOffset by mutableStateOf(0f)
        internal set

    internal suspend fun animateOffsetTo(offset: Float) {
        mutatorMutex.mutate {
            _indicatorOffset.animateTo(offset)
        }
    }

    internal suspend fun animateBackToRest() {
        mutatorMutex.mutate {
            _indicatorOffset.animateTo(if (isRefreshing) indicatorRefreshOffset else 0f)
        }
    }

    /**
     * Dispatch scroll delta in pixels from touch events.
     */
    suspend fun dispatchRawDelta(delta: Float) {
        mutatorMutex.mutate(MutatePriority.UserInput) {
            _indicatorOffset.snapTo(_indicatorOffset.value + delta)
        }
    }
}

private class SwipeRefreshNestedScrollConnection(
    private val state: SwipeRefreshState,
    private val coroutineScope: CoroutineScope,
    private val onRefresh: () -> Unit,
) : NestedScrollConnection {
    var enabled: Boolean = false

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        // If swiping isn't enabled, return zero
        !enabled -> Offset.Zero
        // If the user is swiping up, handle it
        source == NestedScrollSource.Drag && available.y < 0 -> onScroll(available)
        else -> Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        // If swiping isn't enabled, return zero
        !enabled -> Offset.Zero
        // If the user is swiping down and there's y remaining, handle it
        source == NestedScrollSource.Drag && available.y > 0 -> onScroll(available)
        else -> Offset.Zero
    }

    private fun onScroll(available: Offset): Offset {
        state.isSwipeInProgress = true

        val minOffset = when {
            // If we're refreshing, we don't want the indicator to scroll below the refresh
            // trigger
            state.isRefreshing -> state.indicatorRefreshOffset
            else -> 0f
        }
        val newOffset = (available.y * DragMultiplier + state.indicatorOffset)
            .coerceAtLeast(minOffset)
        val dragConsumed = newOffset - state.indicatorOffset

        return if (dragConsumed.absoluteValue >= 0.5f) {
            coroutineScope.launch {
                state.dispatchRawDelta(dragConsumed)
            }
            // Return the consumed Y
            Offset(x = 0f, y = dragConsumed / DragMultiplier)
        } else {
            Offset.Zero
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity = when {
        // If we're currently refreshing, just animate back to the resting position
        state.isRefreshing -> {
            coroutineScope.launch {
                state.animateOffsetTo(state.indicatorRefreshOffset)
            }
            // Don't consume any velocity, to allow the scrolling layout to fling
            Velocity.Zero
        }
        // If we're dragging and scrolled past the trigger point, refresh!
        state.isSwipeInProgress && state.indicatorOffset >= state.indicatorRefreshOffset -> {
            onRefresh()
            // Don't consume any velocity, to allow the scrolling layout to fling
            Velocity.Zero
        }
        else -> Velocity.Zero
    }.also {
        // Reset the drag in progress state
        state.isSwipeInProgress = false
    }
}

/**
 * A layout which implements the swipe-to-refresh pattern.
 *
 * The layout can be used whenever the user has the ability to refresh content via a vertical
 * swipe gesture.
 *
 * Apps should provide a [onRefresh] block to be notified each time a swipe to refresh gesture
 * is completed. That block is responsible for updating the [state] as appropriately,
 * typically by setting [SwipeRefreshState.isRefreshing] to `true` once a 'refresh' has been
 * started. Once a refresh has completed, the app should then set
 * [SwipeRefreshState.isRefreshing] to `false`.
 *
 * If an app wishes to show just the progress animation, outside of a swipe refresh, it can
 * set [SwipeRefreshState.isRefreshing] as required.
 *
 * This layout does not clip any of it's contents, including the indicator. If clipping
 * is required, apps can provide the [androidx.compose.ui.draw.clipToBounds] modifier.
 *
 * @sample com.google.accompanist.sample.swiperefresh.SwipeRefreshSample
 *
 * @param state the state object to be used to control or observe the [SwipeRefresh] state.
 * @param onRefresh Lambda which is invoked when a swipe to refresh gesture is completed.
 * @param modifier the modifier to apply to this layout.
 * @param swipeEnabled Whether the the layout should react to swipe gestures or not.
 * @param indicator the indicator that represents the current state. By default this
 * will use a [SwipeRefreshIndicator].
 * @param content The content containing a scroll composable.
 */
@Composable
fun SwipeRefresh(
    state: SwipeRefreshState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    indicator: @Composable (SwipeRefreshState) -> Unit = { SwipeRefreshIndicator(it) },
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val updatedOnRefresh = rememberUpdatedState(onRefresh)

    // Our LaunchedEffect, which animates the indicator to an appropriate resting position
    LaunchedEffect(state.isSwipeInProgress, state.isRefreshing) {
        if (!state.isSwipeInProgress) {
            // If there's not a swipe in progress, rest the indicator at an appropriate position
            state.animateBackToRest()
        }
    }

    // Our nested scroll connection, which updates our state.
    val nestedScrollConnection = remember(state, coroutineScope) {
        SwipeRefreshNestedScrollConnection(state, coroutineScope) {
            // On refresh, re-dispatch to the update onRefresh block
            updatedOnRefresh.value.invoke()
        }
    }.apply {
        this.enabled = swipeEnabled
    }

    Layout(
        content = {
            Box(Modifier.layoutId(LayoutContentTag)) { content() }
            Box(Modifier.layoutId(LayoutIndicatorTag)) { indicator(state) }
        },
        modifier = modifier.nestedScroll(connection = nestedScrollConnection)
    ) { measurables, constraints ->
        val noMinConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        // Measure both the content and the indicator
        val contentPlaceable = measurables.first { it.layoutId == LayoutContentTag }
            .measure(noMinConstraints)
        val indicatorPlaceable = measurables.first { it.layoutId == LayoutIndicatorTag }
            .measure(noMinConstraints)

        // TODO: make this configurable?
        val trigger = indicatorPlaceable.height * 1.5f
        state.indicatorRefreshOffset = trigger

        // Our layout is the size of the content, coerced by the min constraints
        val layoutWidth = contentPlaceable.width.coerceAtLeast(constraints.minWidth)
        val layoutHeight = contentPlaceable.height.coerceAtLeast(constraints.minHeight)

        layout(layoutWidth, layoutHeight) {
            // Place our content at x = center, y = top
            contentPlaceable.place(x = (layoutWidth - contentPlaceable.width) / 2, y = 0)

            val slingshot = calculateSlingshot(
                offsetY = state.indicatorOffset,
                maxOffsetY = trigger,
                height = indicatorPlaceable.height
            )
            indicatorPlaceable.place(
                x = (layoutWidth - indicatorPlaceable.width) / 2,
                y = -indicatorPlaceable.height + slingshot.offset,
            )
        }
    }
}

private const val LayoutContentTag = "swiperefresh_content"
private const val LayoutIndicatorTag = "swiperefresh_indicator"
