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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.google.accompanist.coil.CoilImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.VerticalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import com.google.accompanist.sample.randomSampleImageUrl
import kotlinx.coroutines.launch

class VerticalPagerWithIndicatorSample : ComponentActivity() {
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
                title = { Text(stringResource(R.string.vertical_pager_with_indicator_title)) },
                backgroundColor = MaterialTheme.colors.surface,
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(Modifier.fillMaxSize()) {
            // Display 10 items
            val pagerState = rememberPagerState(pageCount = 10)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                VerticalPager(
                    state = pagerState,
                    // We increase the offscreen limit, to allow pre-loading of images
                    offscreenLimit = 2,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) { page ->
                    Box {
                        // Our page content, displaying a random image
                        CoilImage(
                            data = randomSampleImageUrl(width = 600),
                            contentDescription = null,
                            fadeIn = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )

                        // Displays the page index
                        Text(
                            text = page.toString(),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .background(MaterialTheme.colors.surface, RoundedCornerShape(4.dp))
                                .padding(4.dp)
                                .wrapContentSize(Alignment.Center)
                        )
                    }
                }

                VerticalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier.padding(16.dp)
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
private fun ActionsRow(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        val scope = rememberCoroutineScope()

        IconButton(
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(0)
                }
            }
        ) {
            Icon(Icons.Default.FirstPage, null)
        }

        IconButton(
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            }
        ) {
            Icon(Icons.Default.NavigateBefore, null)
        }

        IconButton(
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        ) {
            Icon(Icons.Default.NavigateNext, null)
        }

        IconButton(
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
