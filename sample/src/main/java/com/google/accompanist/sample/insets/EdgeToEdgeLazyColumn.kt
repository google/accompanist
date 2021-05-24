/*
 * Copyright 2020 The Android Open Source Project
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

package com.google.accompanist.sample.insets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.rememberWindowInsetsTypePaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import com.google.accompanist.sample.randomSampleImageUrl
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class EdgeToEdgeLazyColumn : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn off the decor fitting system windows, which means we need to through handling
        // insets
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AccompanistSampleTheme {
                ProvideWindowInsets {
                    Sample()
                }
            }
        }
    }
}

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

            // We use the systemBar insets as the source of our content padding.
            // We add on the topAppBarSize, so that the content is displayed below
            // the app bar. Since the top inset is already contained within the app
            // bar height, we disable handling it in toPaddingValues().
            LazyColumn(
                contentPadding = rememberWindowInsetsTypePaddingValues(
                    type = LocalWindowInsets.current.systemBars,
                    applyTop = false,
                    additionalTop = with(LocalDensity.current) { topAppBarSize.toDp() }
                )
            ) {
                items(items = listItems) { imageUrl ->
                    ListItem(imageUrl, Modifier.fillMaxWidth())
                }
            }

            /**
             * We show a translucent app bar above which floats about the content.
             * We use [TopAppBar] from accompanist-insets-ui which allows us to provide
             * content padding matching the system bars insets.
             */
            TopAppBar(
                title = { Text(stringResource(R.string.insets_title_list)) },
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

            FloatingActionButton(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Face icon"
                )
            }
        }
    }
}

private val listItems = List(40) { randomSampleImageUrl(it) }
