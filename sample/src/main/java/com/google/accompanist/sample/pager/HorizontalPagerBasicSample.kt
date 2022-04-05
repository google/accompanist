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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import kotlinx.coroutines.launch

class HorizontalPagerBasicSample : ComponentActivity() {
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
                title = { Text(stringResource(R.string.horiz_pager_title_basics)) },
                backgroundColor = MaterialTheme.colors.surface,
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            val pagerState = rememberPagerState()

            // Display 10 items
            HorizontalPager(
                count = 10,
                state = pagerState,
                // Add 32.dp horizontal padding to 'center' the pages
                contentPadding = PaddingValues(horizontal = 32.dp),
                // Add some horizontal spacing between items
                itemSpacing = 4.dp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                PagerSampleItem(
                    page = page,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }

            ActionsRow(
                pagerState = pagerState,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
internal fun ActionsRow(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    infiniteLoop: Boolean = false
) {
    Row(modifier) {
        val scope = rememberCoroutineScope()

        IconButton(
            enabled = infiniteLoop.not() && pagerState.currentPage > 0,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(0)
                }
            }
        ) {
            Icon(Icons.Default.FirstPage, null)
        }

        IconButton(
            enabled = infiniteLoop || pagerState.currentPage > 0,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            }
        ) {
            Icon(Icons.Default.NavigateBefore, null)
        }

        IconButton(
            enabled = infiniteLoop || pagerState.currentPage < pagerState.pageCount - 1,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        ) {
            Icon(Icons.Default.NavigateNext, null)
        }

        IconButton(
            enabled = infiniteLoop.not() && pagerState.currentPage < pagerState.pageCount - 1,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.pageCount - 1)
                }
            }
        ) {
            Icon(Icons.Default.LastPage, null)
        }
    }
}
