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

@file:Suppress("UNUSED_PARAMETER", "unused", "RedundantOverride")

package com.google.accompanist.swiperefresh

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

private const val DRAG_RATE = 0.5f
private const val MAX_ALPHA = 1f
private const val STARTING_PROGRESS_ALPHA = 0.3f
private const val MAX_PROGRESS_ANGLE = .8f

private val IDLE_OFFSET = 16.dp
private val REFRESH_TRIGGER_OFFSET = 60.dp

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

@Composable
fun SwipeRefreshIndicatorScope.defaultSwipeRefreshIndicator() {
    SwipeRefreshIndicator(
        isRefreshing = isRefreshing,
        swipeFraction = indicatorOffset,
        isSwipeInProgress = isSwipeInProgress
    )
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
    indicator: @Composable SwipeRefreshIndicatorScope.() -> Unit = { defaultSwipeRefreshIndicator() },
    content: @Composable BoxScope.() -> Unit,
) {
    if (isRefreshing) {
        state.refreshState = SwipeRefreshState2.Refreshing
    } else if (!isRefreshing && state.refreshState == SwipeRefreshState2.Refreshing) {
        state.refreshState = SwipeRefreshState2.Idle
    }

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.refreshState) {
        if (state.refreshState == SwipeRefreshState2.Refreshing) {
            state.animateOffsetTo(with(density) { REFRESH_TRIGGER_OFFSET.toPx() })
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
                    val drag = available.y * DRAG_RATE
                    val distanceAvailable = maxOf(drag, -state.indicatorOffset)
                    if (distanceAvailable.absoluteValue > 0.5f) {
                        coroutineScope.launch {
                            state.dragOffsetBy(distanceAvailable)
                        }
                    }
                    // Consume the consumed Y
                    Offset(x = 0f, y = distanceAvailable)
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
                        state.dragOffsetBy(available.y * DRAG_RATE)
                    }
                    // Consume the entire Y delta
                    Offset(x = 0f, y = available.y)
                }
                else -> Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val trigger = with(density) { REFRESH_TRIGGER_OFFSET.toPx() }
                return when {
                    state.refreshState == SwipeRefreshState2.Refreshing -> {
                        // If we're currently refreshing, just animate back to the resting position
                        coroutineScope.launch {
                            state.animateOffsetTo(trigger)
                        }
                        // Consume the entire velocity
                        available
                    }
                    state.refreshState == SwipeRefreshState2.Dragging &&
                        state.indicatorOffset >= trigger -> {
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

        // FIXME: think of a better way to offset the indicator off-screen initially
        var indicatorHeight by remember { mutableStateOf(0) }

        Box(
            Modifier
                .offset { IntOffset(x = 0, y = state.indicatorOffset.roundToInt()) }
                .offset { IntOffset(x = 0, y = -indicatorHeight) }
                .onSizeChanged { indicatorHeight = it.height }
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

@Composable
fun SwipeRefreshIndicator(
    isRefreshing: Boolean,
    swipeFraction: Float,
    isSwipeInProgress: Boolean,
) {
    // TODO

    Surface(
        shape = CircleShape,
        elevation = 6.dp,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refreshing",
            modifier = Modifier
                .graphicsLayer {

                    val circumference = 20.dp.toPx() * Math.PI * 2f

                    rotationZ = (swipeFraction / circumference.toFloat()) * 360f
                }
                .fillMaxSize()
                .wrapContentSize()
        )
    }
}

@Preview
@Composable
fun PreviewSwipeRefreshIndicator() {
    MaterialTheme {
        SwipeRefreshIndicator(
            isRefreshing = false,
            swipeFraction = 0f,
            isSwipeInProgress = true
        )
    }
}
