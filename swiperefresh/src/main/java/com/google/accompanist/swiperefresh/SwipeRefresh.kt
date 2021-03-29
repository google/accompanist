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
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.max

private const val DRAG_MULTIPLIER = 0.5f

@Stable
class SwipeRefreshState {
    private val _indicatorOffset = Animatable(0f)
    private val mutatorMutex = MutatorMutex()

    var refreshState by mutableStateOf(SwipeRefreshState2.Idle)
        internal set

    val indicatorOffset: Float
        get() = _indicatorOffset.value

    suspend fun animateOffsetTo(offset: Float) {
        mutatorMutex.mutate {
            _indicatorOffset.animateTo(offset)
        }
    }

    suspend fun dragOffsetBy(delta: Float) {
        mutatorMutex.mutate {
            _indicatorOffset.snapTo(_indicatorOffset.value + delta)
        }
    }
}

enum class SwipeRefreshState2 {
    Idle,
    Dragging,
    Refreshing,
}

@Composable
fun SwipeRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: SwipeRefreshState = remember { SwipeRefreshState() },
    indicatorSize: IndicatorSize = IndicatorSize.DEFAULT,
    indicator: @Composable SwipeRefreshIndicatorScope.() -> Unit = {
        SwipeRefreshIndicator(
            isRefreshing = isRefreshing,
            offset = indicatorOffset,
            size = indicatorSize
        )
    },
    content: @Composable () -> Unit,
) {
    if (isRefreshing) {
        state.refreshState = SwipeRefreshState2.Refreshing
    } else if (!isRefreshing && state.refreshState == SwipeRefreshState2.Refreshing) {
        state.refreshState = SwipeRefreshState2.Idle
    }

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val indicatorSizePx = with(LocalDensity.current) { indicatorSize.size.roundToPx() }
    val refreshTriggerPx = with(LocalDensity.current) { indicatorSize.refreshTrigger.toPx() }

    LaunchedEffect(state.refreshState, density) {
        if (state.refreshState == SwipeRefreshState2.Refreshing) {
            state.animateOffsetTo(refreshTriggerPx)
        } else if (state.refreshState == SwipeRefreshState2.Idle) {
            state.animateOffsetTo(0f)
        }
    }

    val nestedScrollConnection: NestedScrollConnection = remember(state, density) {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset = when {
                source == NestedScrollSource.Drag && available.y < 0 -> {
                    val drag = available.y * DRAG_MULTIPLIER
                    val distanceAvailable = max(drag, -state.indicatorOffset)
                    if (distanceAvailable.absoluteValue > 0.5f) {
                        coroutineScope.launch {
                            state.dragOffsetBy(distanceAvailable)
                        }
                    }
                    // Consume the consumed Y
                    Offset(x = 0f, y = distanceAvailable / DRAG_MULTIPLIER)
                }
                else -> Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset = when {
                source == NestedScrollSource.Drag && available.y > 0 -> {
                    state.refreshState = SwipeRefreshState2.Dragging

                    coroutineScope.launch {
                        state.dragOffsetBy(available.y * DRAG_MULTIPLIER)
                    }
                    // Consume the entire Y delta
                    Offset(x = 0f, y = available.y)
                }
                else -> Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                return when {
                    state.refreshState == SwipeRefreshState2.Refreshing -> {
                        // If we're currently refreshing, just animate back to the resting position
                        coroutineScope.launch {
                            state.animateOffsetTo(refreshTriggerPx)
                        }
                        // Consume the entire velocity
                        available
                    }
                    state.refreshState == SwipeRefreshState2.Dragging &&
                        state.indicatorOffset >= refreshTriggerPx -> {
                        // If we're dragging and scrolled past the trigger point, refresh!
                        onRefresh()
                        // Consume the entire velocity
                        available
                    }
                    else -> {
                        // Otherwise animate back to the idle resting position
                        state.refreshState = SwipeRefreshState2.Idle
                        Velocity.Zero
                    }
                }
            }
        }
    }

    val indicatorScope = remember(state) { SwipeRefreshIndicatorScopeImpl(state) }

    Box(
        modifier
            .nestedScroll(connection = nestedScrollConnection)
            .clipToBounds()
    ) {
        content()

        Box(
            Modifier
                .offset {
                    val slingshot = calculateSlingshot(
                        offsetY = state.indicatorOffset,
                        maxOffsetY = refreshTriggerPx,
                        height = indicatorSizePx
                    )
                    IntOffset(x = 0, y = slingshot.offset)
                }
                .offset { IntOffset(x = 0, y = -indicatorSizePx) }
                .align(Alignment.TopCenter)
        ) {
            indicatorScope.indicator()
        }
    }
}

private class SwipeRefreshIndicatorScopeImpl(
    val state: SwipeRefreshState
) : SwipeRefreshIndicatorScope {
    override val isRefreshing: Boolean
        get() = state.refreshState == SwipeRefreshState2.Refreshing
    override val indicatorOffset: Float
        get() = state.indicatorOffset
    override val isSwipeInProgress: Boolean
        get() = state.refreshState == SwipeRefreshState2.Dragging
}

/**
 * TODO
 */
interface SwipeRefreshIndicatorScope {
    val isRefreshing: Boolean
    val indicatorOffset: Float
    val isSwipeInProgress: Boolean
}
