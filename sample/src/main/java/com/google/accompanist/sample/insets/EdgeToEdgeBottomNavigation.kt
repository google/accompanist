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

@file:Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")

package com.google.accompanist.sample.insets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.Insets
import com.google.accompanist.insets.Insets.Companion.Insets
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.toPaddingValues
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import com.google.accompanist.sample.randomSampleImageUrl
import com.google.accompanist.systemuicontroller.LocalSystemUiController
import com.google.accompanist.systemuicontroller.rememberAndroidSystemUiController

class EdgeToEdgeBottomNavigation : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn off the decor fitting system windows, which means we need to through handling
        // insets
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AccompanistSampleTheme {
                val controller = rememberAndroidSystemUiController()
                CompositionLocalProvider(LocalSystemUiController provides controller) {
                    ProvideWindowInsets {
                        Sample()
                    }
                }
            }
        }
    }
}

@Composable
private fun Sample() {
    val systemUiController = LocalSystemUiController.current
    val useDarkIcons = MaterialTheme.colors.isLight
    SideEffect {
        systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
    }

    Surface {
        Box(Modifier.fillMaxSize()) {
            // A state instance which allows us to track the size of the top app bar
            var topAppBarSize by remember { mutableStateOf(0) }
            var bottomBarSize by remember { mutableStateOf(0) }

            var selectedBottomNavIndex by remember { mutableStateOf(0) }

            val currentWindowInsets = LocalWindowInsets.current
            val newWindowInsets = currentWindowInsets.copy(
                statusBars = currentWindowInsets.statusBars.copyWithAppend(
                    Insets(top = topAppBarSize)
                ),
                navigationBars = currentWindowInsets.navigationBars.copyWithAppend(
                    Insets(bottom = bottomBarSize)
                )
            )

            // We use the systemBar insets as the source of our content padding.
            // We add on the topAppBarSize, so that the content is displayed below
            // the app bar. Since the top inset is already contained within the app
            // bar height, we disable handling it in toPaddingValues().
            CompositionLocalProvider(LocalWindowInsets provides newWindowInsets) {
                AppPage(
                    pageTitle = "Page: $selectedBottomNavIndex",
                    modifier = Modifier.fillMaxSize()
                )
            }

            /**
             * We show a translucent app bar above which floats about the content. Our
             * [InsetAwareTopAppBar] below automatically draws behind the status bar too.
             */
            InsetAwareTopAppBar(
                title = { Text(stringResource(R.string.insets_title_bottomnav)) },
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.9f),
                modifier = Modifier
                    .fillMaxWidth()
                    // We use onSizeChanged to track the app bar height, and update
                    // our state above
                    .onSizeChanged { topAppBarSize = it.height }
            )

            /**
             * We show a translucent bottom navigation bar above which floats about the content. Our
             * [InsetAwareBottomNavigation] below automatically draws behind the status bar too.
             */
            InsetAwareBottomNavigation(
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    // We use onSizeChanged to track the bottom nav bar height, and update
                    // our state above
                    .onSizeChanged { bottomBarSize = it.height }
            ) {
                BottomNavigationItem(
                    selected = selectedBottomNavIndex == 0,
                    onClick = { selectedBottomNavIndex = 0 },
                    label = { Text("Home") },
                    icon = { Icon(Icons.Default.Home, null) },
                )

                BottomNavigationItem(
                    selected = selectedBottomNavIndex == 1,
                    onClick = { selectedBottomNavIndex = 1 },
                    label = { Text("Phone") },
                    icon = { Icon(Icons.Default.Phone, null) },
                )

                BottomNavigationItem(
                    selected = selectedBottomNavIndex == 2,
                    onClick = { selectedBottomNavIndex = 2 },
                    label = { Text("Contacts") },
                    icon = { Icon(Icons.Default.Contacts, null) },
                )

                BottomNavigationItem(
                    selected = selectedBottomNavIndex == 3,
                    onClick = { selectedBottomNavIndex = 3 },
                    label = { Text("Video") },
                    icon = { Icon(Icons.Default.VideoCall, null) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppPage(
    pageTitle: String,
    modifier: Modifier = Modifier,
) {
    // We use the systemBar insets as the source of our content padding.
    // We add on the topAppBarSize, so that the content is displayed below
    // the app bar. Since the top inset is already contained within the app
    // bar height, we disable handling it in toPaddingValues().
    LazyColumn(
        contentPadding = LocalWindowInsets.current.systemBars.toPaddingValues(),
        modifier = modifier,
    ) {
        item {
            Surface(Modifier.fillMaxWidth()) {
                Text(text = pageTitle, Modifier.padding(16.dp))
            }
        }
        items(items = listItems) { imageUrl ->
            ListItem(imageUrl, Modifier.fillMaxWidth())
        }
    }
}

private val listItems = List(40) { randomSampleImageUrl(it) }
