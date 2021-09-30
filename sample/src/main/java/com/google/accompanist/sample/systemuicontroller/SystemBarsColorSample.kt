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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import com.google.accompanist.sample.rememberRandomSampleImageUrl
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class SystemBarsColorSample : ComponentActivity() {
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

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun Sample() {
    // Get the current SystemUiController
    val systemUiController = rememberSystemUiController()
    var clickedColor by remember { mutableStateOf(Color.Unspecified) }
    var statusBarDarkIcons by remember { mutableStateOf(false) }
    var navigationBarDarkIcons by remember { mutableStateOf(false) }

    @Composable
    fun Color(color: Color) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clickable { clickedColor = color },
            contentAlignment = Alignment.Center
        ) {
            if (clickedColor == color) {
                Box(
                    Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.6f))
                )
            }
            Box(modifier = Modifier.size(44.dp)) {
                Image(
                    painter = painterResource(R.drawable.alpha),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(color)
                )
            }
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Displaying a random image
        Image(
            painter = rememberImagePainter(
                data = with(LocalDensity.current) {
                    rememberRandomSampleImageUrl(
                        seed = 16,
                        width = maxWidth.roundToPx(),
                        height = maxHeight.roundToPx()
                    )
                },
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            TopAppBar(
                title = { Text(stringResource(R.string.system_ui_controller_title_color)) },
                backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.5f),
                elevation = 0.dp,
            )
            Spacer(modifier = Modifier.weight(1f))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.surface.copy(alpha = 0.5f))
                    .padding(vertical = 16.dp),
            ) {
                Row {
                    Color(Color.Black)
                    Color(Color.DarkGray)
                    Color(Color.Gray)
                    Color(Color.LightGray)
                }
                Row {
                    Color(Color.White)
                    Color(Color.Red)
                    Color(Color.Green)
                    Color(Color.Blue)
                }
                Row {
                    Color(Color.Yellow)
                    Color(Color.Cyan)
                    Color(Color.Magenta)
                    Color(Color(0xFF673AB7))
                }
                Row {
                    Color(Color.Black.copy(alpha = 0.5f))
                    Color(Color.Blue.copy(alpha = 0.5f))
                    Color(Color.White.copy(alpha = 0.5f))
                    Color(Color.Transparent)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        systemUiController.setStatusBarColor(clickedColor)
                        statusBarDarkIcons = clickedColor.luminance() > 0.5f
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "Change status bar color")
                }
                Button(
                    onClick = {
                        systemUiController.setNavigationBarColor(clickedColor)
                        navigationBarDarkIcons = clickedColor.luminance() > 0.5f
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "Change navigation bar color")
                }
                Button(
                    onClick = {
                        systemUiController.setSystemBarsColor(clickedColor)
                        statusBarDarkIcons = clickedColor.luminance() > 0.5f
                        navigationBarDarkIcons = statusBarDarkIcons
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "Change system bars color")
                }

                Row(
                    modifier = Modifier
                        .clickable {
                            statusBarDarkIcons = !statusBarDarkIcons
                            systemUiController.statusBarDarkContentEnabled = statusBarDarkIcons
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Set status bar dark icons  ")
                    Checkbox(
                        checked = statusBarDarkIcons,
                        onCheckedChange = {
                            statusBarDarkIcons = it
                            systemUiController.statusBarDarkContentEnabled = it
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .clickable {
                            navigationBarDarkIcons = !navigationBarDarkIcons
                            systemUiController.navigationBarDarkContentEnabled = navigationBarDarkIcons
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Set navigation bar dark icons  ")
                    Checkbox(
                        checked = navigationBarDarkIcons,
                        onCheckedChange = {
                            navigationBarDarkIcons = it
                            systemUiController.navigationBarDarkContentEnabled = it
                        }
                    )
                }
            }
        }
    }
}
