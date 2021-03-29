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

package com.google.accompanist.sample.pager

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.VerticalPagerIndicator
import com.google.accompanist.pager.pageChanges
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HorizontalPagerSample() {
    // Display 10 items
    val pagerState = rememberPagerState(pageCount = 10)

    HorizontalPager(state = pagerState) { page ->
        // Our page content
        Text(
            text = "Page: $page",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun VerticalPagerSample() {
    // Display 10 items
    val pagerState = rememberPagerState(pageCount = 10)

    VerticalPager(state = pagerState) { page ->
        // Our page content
        Text(
            text = "Page: $page",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HorizontalPagerIndicatorSample() {
    // Display 10 items
    val pagerState = rememberPagerState(pageCount = 10)
    Column {
        HorizontalPager(state = pagerState) { page ->
            // Our page content
            Text(
                text = "Page: $page",
                modifier = Modifier.fillMaxWidth()
            )
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun VerticalPagerIndicatorSample() {
    // Display 10 items
    val pagerState = rememberPagerState(pageCount = 10)
    Row {
        VerticalPager(state = pagerState) { page ->
            // Our page content
            Text(
                text = "Page: $page",
                modifier = Modifier.fillMaxWidth()
            )
        }

        VerticalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Suppress("UNUSED_PARAMETER")
object AnalyticsService {
    fun sendPageSelectedEvent(page: Int) = Unit
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PageChangesSample() {
    val pagerState = rememberPagerState(pageCount = 10)

    LaunchedEffect(pagerState) {
        // Collect from the PageState's pageChanges flow, which emits when the
        // current page has changed
        pagerState.pageChanges.collect { page ->
            AnalyticsService.sendPageSelectedEvent(page)
        }
    }

    VerticalPager(state = pagerState) { page ->
        Text(text = "Page: $page")
    }
}
