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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.rememberImeNestedScrollConnection
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import com.google.accompanist.sample.randomSampleImageUrl
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalAnimatedInsets::class)
class ImeAnimationSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn off the decor fitting system windows, which means we need to through handling
        // insets
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AccompanistSampleTheme {
                ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
                    Sample()
                }
            }
        }
    }
}

private val listItems = List(40) { randomSampleImageUrl(it) }

@OptIn(ExperimentalAnimatedInsets::class)
@Composable
private fun Sample() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colors.isLight
    SideEffect {
        systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
    }

    Scaffold(
        topBar = {
            /**
             * We use [TopAppBar] from accompanist-insets-ui which allows us to provide
             * content padding matching the system bars insets.
             */
            TopAppBar(
                title = {
                    Text(stringResource(R.string.insets_title_imeanim))
                },
                backgroundColor = MaterialTheme.colors.surface,
                contentPadding = rememberInsetsPaddingValues(
                    LocalWindowInsets.current.systemBars,
                    applyBottom = false,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            LazyColumn(
                reverseLayout = true,
                modifier = Modifier
                    .weight(1f)
                    .nestedScroll(connection = rememberImeNestedScrollConnection())
            ) {
                items(listItems) { imageUrl ->
                    ListItem(imageUrl, Modifier.fillMaxWidth())
                }
            }

            Surface(elevation = 1.dp) {
                val text = remember { mutableStateOf(TextFieldValue()) }
                OutlinedTextField(
                    value = text.value,
                    onValueChange = { text.value = it },
                    placeholder = { Text(text = "Watch me animate...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .navigationBarsWithImePadding()
                )
            }
        }
    }
}
