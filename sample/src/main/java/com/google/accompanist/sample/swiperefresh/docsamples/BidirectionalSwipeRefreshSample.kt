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

package com.google.accompanist.sample.swiperefresh.docsamples

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.Position
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SampleViewModel : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    private val _isLoadingNextPage = MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    val isLoadingNextPage: StateFlow<Boolean>
        get() = _isLoadingNextPage.asStateFlow()

    fun refreshList() {
        viewModelScope.launch {
            // Simulate refreshing time
            _isRefreshing.emit(true)
            delay(2000)
            _isRefreshing.emit(false)
        }
    }

    fun loadNextPage() {
        viewModelScope.launch {
            // Simulate loading time
            _isLoadingNextPage.emit(true)
            delay(2000)
            _isLoadingNextPage.emit(false)
        }
    }
}

@Composable
fun BidirectionalSwipeRefreshSample() {
    val viewModel = viewModel<SampleViewModel>()
    val isTopIndicatorRefreshing by viewModel.isRefreshing.collectAsState()
    val isBottomIndicatorRefreshing by viewModel.isLoadingNextPage.collectAsState()

    SwipeRefresh(
        state = rememberSwipeRefreshState(isTopIndicatorRefreshing),
        onRefresh = { viewModel.refreshList() },
    ) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isBottomIndicatorRefreshing),
            position = Position.BOTTOM,
            onRefresh = { viewModel.loadNextPage() },
        ) {
            LazyColumn {
                items(30) { index ->
                    TODO(" SHOW ITEM $index ")
                }
            }
        }
    }
}
