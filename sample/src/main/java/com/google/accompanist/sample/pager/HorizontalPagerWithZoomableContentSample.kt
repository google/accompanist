/*
 * Copyright 2023 The Android Open Source Project
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
import androidx.annotation.FloatRange
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R

class HorizontalPagerWithZoomableContentSample : ComponentActivity() {
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
                title = { Text(stringResource(R.string.horiz_pager_title_zoomable_content)) },
                backgroundColor = MaterialTheme.colors.surface,
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        // Display 10 items
        val pageCount = 10
        val zoomableStates = remember(pageCount) {
            Array(pageCount) { ZoomableState() }
        }

        val pagerState = rememberPagerState()
        val zoomableStateOfCurrentPage by remember {
            derivedStateOf {
                zoomableStates[pagerState.currentPage]
            }
        }
        val isScaled by remember {
            derivedStateOf {
                zoomableStateOfCurrentPage.isScaled
            }
        }
        Box(Modifier.fillMaxSize().padding(padding)) {
            HorizontalPager(
                count = pageCount,
                state = pagerState,
                // Add 32.dp horizontal padding to 'center' the pages
                contentPadding = PaddingValues(horizontal = 32.dp),
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = isScaled.not(),
            ) { page ->
                val zoomableState = zoomableStates[page]
                PagerSampleItem(
                    page = page,
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (zoomableState.isScaled) {
                                        zoomableState.zoomOut()
                                    } else {
                                        zoomableState.zoomIn()
                                    }
                                },
                            )
                        }
                        .clipToBounds()
                        .graphicsLayer {
                            scaleX = zoomableState.scale
                            scaleY = zoomableState.scale
                        }
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }

            Row(modifier = Modifier.align(Alignment.BottomCenter)) {
                IconButton(
                    enabled = isScaled,
                    onClick = {
                        zoomableStateOfCurrentPage.zoomOut()
                    }
                ) {
                    Icon(Icons.Default.ZoomOut, null)
                }

                IconButton(
                    enabled = isScaled.not(),
                    onClick = {
                        zoomableStateOfCurrentPage.zoomIn()
                    }
                ) {
                    Icon(Icons.Default.ZoomIn, null)
                }
            }
        }
    }
}

@Stable
private class ZoomableState {
    private var _scale by mutableStateOf(MinimumScale)

    @get:FloatRange(from = 1.0)
    var scale: Float
        get() = _scale
        private set(value) {
            val coerceValue = value.coerceIn(MinimumScale, MaximumScale)
            if (coerceValue != _scale) {
                _scale = coerceValue
            }
        }

    val isScaled: Boolean
        get() = scale != 1f

    fun zoomIn() {
        scale = MaximumScale
    }

    fun zoomOut() {
        scale = MinimumScale
    }

    companion object {
        private const val MinimumScale = 1f
        private const val MaximumScale = 3f
    }
}
