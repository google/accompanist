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

@file:Suppress("DEPRECATION")
package com.google.accompanist.sample.pager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import kotlinx.coroutines.launch

class HorizontalPagerLoopingTabsSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AccompanistSampleTheme {
                Surface {
                    Sample()
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun Sample() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.horiz_pager_title_looping_tabs)) },
                backgroundColor = MaterialTheme.colors.surface,
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        val pages = remember {
            listOf("Home", "Shows", "Movies", "Books", "Really long movies", "Short audiobooks")
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val coroutineScope = rememberCoroutineScope()

            // We start the pager in the middle of the raw number of pages
            val loopingCount = Int.MAX_VALUE
            val startIndex = loopingCount / 2
            val pagerState = rememberPagerState(initialPage = startIndex)

            fun pageMapper(index: Int): Int {
                return (index - startIndex).floorMod(pages.count())
            }

            val currentIndex by remember {
                derivedStateOf { pageMapper(pagerState.currentPage) }
            }

            ScrollableTabRow(
                // Our selected tab is our current page
                selectedTabIndex = currentIndex,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.pagerTabIndicatorOffset(pagerState, tabPositions, ::pageMapper)
                    )
                }
            ) {
                // Add tabs for all of our pages
                pages.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = currentIndex == index,
                        onClick = {
                            // Animate to the selected page when clicked
                            coroutineScope.launch {
                                when {
                                    currentIndex > index -> {
                                        pagerState.animateScrollToPage(
                                            page = pagerState.currentPage - (currentIndex - index)
                                        )
                                    }
                                    currentIndex < index -> {
                                        pagerState.animateScrollToPage(
                                            page = pagerState.currentPage + (index - currentIndex)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            HorizontalPager(
                count = loopingCount,
                state = pagerState,
                // Add 16.dp padding to 'center' the pages
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { index ->
                val page = pageMapper(index)
                Card {
                    Box(Modifier.fillMaxSize()) {
                        Text(
                            text = "Page: ${pages[page]}",
                            style = MaterialTheme.typography.h4,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

private fun Int.floorMod(other: Int): Int = when (other) {
    0 -> this
    else -> this - floorDiv(other) * other
}
