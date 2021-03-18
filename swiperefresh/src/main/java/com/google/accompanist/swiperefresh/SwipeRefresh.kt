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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp



private const val DRAG_RATE = .5f
private const val MAX_ALPHA = 1f
private const val STARTING_PROGRESS_ALPHA = 0.3f
private const val MAX_PROGRESS_ANGLE = .8f

@Stable
class SwipeRefreshState {

}

private class SwipeRefreshNestedScrollConnection(
    state: SwipeRefreshState
) : NestedScrollConnection {
    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        return super.onPreScroll(available, source)
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        return super.onPostScroll(consumed, available, source)
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        return super.onPreFling(available)
    }
}

object SwipeRefreshDefaults {
    @Composable
    fun SwipeRefreshIndicatorScope.defaultSwipeRefreshIndicator() {
        SwipeRefreshIndicator(
            isRefreshing = isRefreshing,
            swipeFraction = swipeFraction,
            isSwipeInProgress = isSwipeInProgress
        )
    }

}

@Composable
fun SwipeRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: SwipeRefreshState = remember { SwipeRefreshState() },
    indicator: @Composable SwipeRefreshIndicatorScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val nestedScrollConnection = remember(state) { SwipeRefreshNestedScrollConnection(state) }
    val indicatorScope = remember(state) { SwipeRefreshIndicatorScopeImpl(state) }

    Box(
        modifier.nestedScroll(connection = nestedScrollConnection)
    ) {
        content()

        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset()
        ) {
            indicatorScope.indicator()
        }
    }
}

private class SwipeRefreshIndicatorScopeImpl(
    val state: SwipeRefreshState
) : SwipeRefreshIndicatorScope {
    override val isRefreshing: Boolean
        get() = TODO("Not yet implemented")
    override val swipeFraction: Float
        get() = TODO("Not yet implemented")
    override val isSwipeInProgress: Boolean
        get() = TODO("Not yet implemented")
}

interface SwipeRefreshIndicatorScope {
    val isRefreshing: Boolean
    val swipeFraction: Float
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
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refreshing"
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