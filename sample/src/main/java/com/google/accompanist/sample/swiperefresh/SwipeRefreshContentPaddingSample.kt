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

package com.google.accompanist.sample.swiperefresh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberWindowInsetsTypePaddingValues
import com.google.accompanist.insets.toPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import com.google.accompanist.sample.insets.ListItem
import com.google.accompanist.sample.randomSampleImageUrl
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay

class SwipeRefreshContentPaddingSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AccompanistSampleTheme {
                Sample()
            }
        }
    }
}

private val listItems = List(40) { randomSampleImageUrl(it) }

@Composable
private fun Sample() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colors.isLight
    SideEffect {
        systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
    }

    Surface {
        Box(Modifier.fillMaxSize()) {
            // A state instance which allows us to track the size of the top app bar
            var topAppBarSize by remember { mutableStateOf(0) }

            // Simulate a fake 2-second 'load'. Ideally this 'refreshing' value would
            // come from a ViewModel or similar
            var refreshing by remember { mutableStateOf(false) }
            LaunchedEffect(refreshing) {
                if (refreshing) {
                    delay(2000)
                    refreshing = false
                }
            }

            val contentPadding = LocalWindowInsets.current.systemBars.toPaddingValues(
                top = false,
                additionalTop = with(LocalDensity.current) { topAppBarSize.toDp() }
            )

            SwipeRefresh(
                state = rememberSwipeRefreshState(refreshing),
                onRefresh = { refreshing = true },
                // Shift the indicator to match the list content padding
                indicatorPadding = contentPadding,
                // We want the indicator to draw within the padding
                clipIndicatorToPadding = false,
                // Tweak the indicator to scale up/down
                indicator = { state, refreshTriggerDistance ->
                    SwipeRefreshIndicator(
                        state = state,
                        refreshTriggerDistance = refreshTriggerDistance,
                        scale = true
                    )
                }
            ) {
                LazyColumn(contentPadding = contentPadding) {
                    items(items = listItems) { imageUrl ->
                        ListItem(imageUrl, Modifier.fillMaxWidth())
                    }
                }
            }

            /**
             * We show a translucent app bar above which floats about the content.
             * We use [TopAppBar] from accompanist-insets-ui which allows us to provide
             * content padding matching the system bars insets.
             */
            TopAppBar(
                title = { Text(stringResource(R.string.swiperefresh_title_content_padding)) },
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.9f),
                contentPadding = rememberWindowInsetsTypePaddingValues(
                    LocalWindowInsets.current.systemBars,
                    applyBottom = false,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    // We use onSizeChanged to track the app bar height, and update
                    // our state above
                    .onSizeChanged { topAppBarSize = it.height }
            )
        }
    }
}
