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
import androidx.compose.ui.draw.clipToBounds
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
import kotlin.math.max

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

    internal suspend fun animateOffsetTo(offset: Float) {
        mutatorMutex.mutate {
            _indicatorOffset.animateTo(offset)
        }
    }

    /**
     * Dispatch scroll delta in pixels.
     */
    suspend fun dispatchRawDelta(delta: Float) {
        mutatorMutex.mutate {
            _indicatorOffset.snapTo(_indicatorOffset.value + delta)
        }
    }
}

private class SwipeRefreshNestedScrollConnection(
    private val state: SwipeRefreshState,
    private val coroutineScope: CoroutineScope,
    private val onRefresh: () -> Unit,
) : NestedScrollConnection {
    var refreshTrigger: Float = 0f
    var enabled: Boolean = false

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        // If swiping isn't enabled, return zero
        !enabled -> Offset.Zero
        source == NestedScrollSource.Drag && available.y < 0 -> {
            state.isSwipeInProgress = true

            val drag = available.y * DragMultiplier
            val distanceAvailable = max(drag, -state.indicatorOffset)
            if (distanceAvailable.absoluteValue > 0.5f) {
                coroutineScope.launch {
                    state.dispatchRawDelta(distanceAvailable)
                }
            }
            // Consume the consumed Y
            Offset(x = 0f, y = distanceAvailable / DragMultiplier)
        }
        else -> Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        // If swiping isn't enabled, return zero
        !enabled -> Offset.Zero
        source == NestedScrollSource.Drag && available.y > 0 -> {
            state.isSwipeInProgress = true

            coroutineScope.launch {
                state.dispatchRawDelta(available.y * DragMultiplier)
            }
            // Consume the entire Y delta
            Offset(x = 0f, y = available.y)
        }
        else -> Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity = when {
        // If we're currently refreshing, just animate back to the resting position
        state.isRefreshing -> {
            coroutineScope.launch {
                state.animateOffsetTo(refreshTrigger)
            }
            // Consume the entire velocity
            available
        }
        // If we're dragging and scrolled past the trigger point, refresh!
        state.isSwipeInProgress && state.indicatorOffset >= refreshTrigger -> {
            onRefresh()
            // Consume the entire velocity
            available
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
 * @sample com.google.accompanist.sample.swiperefresh.SwipeRefreshSample
 *
 * @param state the state object to be used to control or observe the [SwipeRefresh] state.
 * @param onRefresh Lambda which is invoked when a swipe to refresh gesture is completed.
 * @param modifier the modifier to apply to this layout.
 * @param swipeEnabled Whether the the layout should react to swipe gestures or not.
 * @param indicator the indicator that represents the current state. By default this
 * will be a [SwipeRefreshIndicator].
 * @param content The content containing a scroll composable.
 */
@Composable
fun SwipeRefresh(
    state: SwipeRefreshState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    indicator: @Composable SwipeRefreshIndicatorScope.() -> Unit = {
        SwipeRefreshIndicator(
            isRefreshing = isRefreshing,
            offset = indicatorOffset,
            triggerOffset = indicatorTriggerOffset,
        )
    },
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val updatedOnRefresh = rememberUpdatedState(onRefresh)
    var refreshTrigger by remember { mutableStateOf(0f) }

    // Our LaunchedEffect, which animates the indicator to an appropriate resting position
    LaunchedEffect(state.isRefreshing, state.isSwipeInProgress, refreshTrigger) {
        if (!state.isSwipeInProgress) {
            // If there's not a swipe in progress, rest the indicator
            // at an appropriate position
            state.animateOffsetTo(if (state.isRefreshing) refreshTrigger else 0f)
        }
    }

    // Our nested scroll connection, which updates our state.
    val nestedScrollConnection = remember(state, coroutineScope) {
        SwipeRefreshNestedScrollConnection(state, coroutineScope) {
            // On refresh, re-dispatch to the update onRefresh block
            updatedOnRefresh.value.invoke()
        }
    }.apply {
        this.refreshTrigger = refreshTrigger
        this.enabled = swipeEnabled
    }

    val indicatorContentScope = remember(state) {
        SwipeRefreshIndicatorScopeImpl(state)
    }.apply {
        this.indicatorTriggerOffset = refreshTrigger
    }

    Layout(
        content = {
            Box(Modifier.layoutId(LayoutContentTag)) { content() }
            Box(Modifier.layoutId(LayoutIndicatorTag)) { indicatorContentScope.indicator() }
        },
        modifier = modifier
            .nestedScroll(connection = nestedScrollConnection)
            .clipToBounds()
    ) { measurables, constraints ->
        val noMinConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        // Measure both the content and the indicator
        val contentPlaceable = measurables.first { it.layoutId == LayoutContentTag }
            .measure(noMinConstraints)
        val indicatorPlaceable = measurables.first { it.layoutId == LayoutIndicatorTag }
            .measure(noMinConstraints)

        // TODO: make this configurable?
        val trigger = indicatorPlaceable.height * 1.5f
        refreshTrigger = trigger

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

@Stable
private class SwipeRefreshIndicatorScopeImpl(
    private val state: SwipeRefreshState
) : SwipeRefreshIndicatorScope {
    override val isRefreshing: Boolean get() = state.isRefreshing
    override val indicatorOffset: Float get() = state.indicatorOffset
    override var indicatorTriggerOffset: Float by mutableStateOf(0f)
    override val isSwipeInProgress: Boolean get() = state.isSwipeInProgress
}

/**
 * Scope for [SwipeRefresh] indicator content.
 */
@Stable
interface SwipeRefreshIndicatorScope {
    /**
     * Whether this [SwipeRefreshState] is currently refreshing or not.
     */
    val isRefreshing: Boolean

    /**
     * The current offset of the indicator.
     */
    val indicatorOffset: Float

    /**
     * The offset of the indicator which would trigger a refresh.
     */
    val indicatorTriggerOffset: Float

    /**
     * Whether this [SwipeRefreshState] is currently refreshing or not.
     */
    val isSwipeInProgress: Boolean
}
