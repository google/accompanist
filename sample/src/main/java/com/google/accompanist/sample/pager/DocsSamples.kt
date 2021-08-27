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

@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.google.accompanist.sample.pager

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.VerticalPagerIndicator
import com.google.accompanist.pager.pagerTabIndicatorOffset
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
        // Collect from the a snapshotFlow reading the currentPage
        snapshotFlow { pagerState.currentPage }.collect { page ->
            AnalyticsService.sendPageSelectedEvent(page)
        }
    }

    VerticalPager(state = pagerState) { page ->
        Text(text = "Page: $page")
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PagerWithTabs(pages: List<String>) {
    val pagerState = rememberPagerState(pageCount = pages.size)

    TabRow(
        // Our selected tab is our current page
        selectedTabIndex = pagerState.currentPage,
        // Override the indicator, using the provided pagerTabIndicatorOffset modifier
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        }
    ) {
        // Add tabs for all of our pages
        pages.forEachIndexed { index, title ->
            Tab(
                text = { Text(title) },
                selected = pagerState.currentPage == index,
                onClick = { /* TODO */ },
            )
        }
    }

    HorizontalPager(state = pagerState) { page ->
        // TODO: page content
    }
}
