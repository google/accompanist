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

package com.google.accompanist.sample.systemuicontroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class SystemBarsVisibilitySample : ComponentActivity() {
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

@Composable
private fun Sample() {
    // Get the current SystemUiController
    val systemUiController = rememberSystemUiController()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.system_ui_controller_title_visibility)) },
                backgroundColor = MaterialTheme.colors.surface,
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                var isShowingDropdownMenu by remember { mutableStateOf(false) }

                Button(
                    onClick = {
                        isShowingDropdownMenu = true
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "Change System Bars Behavior")
                }

                DropdownMenu(
                    expanded = isShowingDropdownMenu,
                    onDismissRequest = { isShowingDropdownMenu = false }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            systemUiController.systemBarsBehavior =
                                WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                            isShowingDropdownMenu = false
                        }
                    ) {
                        Text("BEHAVIOR_SHOW_BARS_BY_SWIPE")
                    }
                    DropdownMenuItem(
                        onClick = {
                            systemUiController.systemBarsBehavior =
                                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            isShowingDropdownMenu = false
                        }
                    ) {
                        Text("BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE")
                    }
                }
            }

            /** Status bar */

            Button(
                onClick = {
                    systemUiController.isStatusBarVisible = true
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "Show the status bar")
            }
            Button(
                onClick = {
                    systemUiController.isStatusBarVisible = false
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "Hide the status bar")
            }

            /** Navigation bar */

            Button(
                onClick = { systemUiController.isNavigationBarVisible = true },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "Show the navigation bar")
            }
            Button(
                onClick = {
                    systemUiController.isNavigationBarVisible = false
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "Hide the navigation bar")
            }

            /** System bars */

            Button(
                onClick = { systemUiController.isSystemBarsVisible = true },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "Show the system bars")
            }
            Button(
                onClick = { systemUiController.isSystemBarsVisible = false },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "Hide the system bars")
            }
        }
    }
}
