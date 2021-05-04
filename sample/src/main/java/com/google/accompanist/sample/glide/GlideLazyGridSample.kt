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

package com.google.accompanist.sample.glide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberWindowInsetsTypePaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import com.google.accompanist.sample.rememberRandomSampleImageUrl

class GlideLazyGridSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ProvideWindowInsets {
                AccompanistSampleTheme {
                    Sample()
                }
            }
        }
    }
}

private const val NumberItems = 60

@OptIn(ExperimentalStdlibApi::class, ExperimentalFoundationApi::class)
@Composable
private fun Sample() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.glide_title_lazy_grid)) },
                backgroundColor = MaterialTheme.colors.surface,
                contentPadding = rememberWindowInsetsTypePaddingValues(
                    LocalWindowInsets.current.systemBars,
                    applyBottom = false,
                ),
            )
        }
    ) {
        LazyVerticalGrid(
            cells = GridCells.Adaptive(96.dp),
            contentPadding = rememberWindowInsetsTypePaddingValues(
                type = LocalWindowInsets.current.navigationBars,
                additionalStart = 16.dp,
                additionalTop = 16.dp,
                additionalEnd = 16.dp,
                additionalBottom = 16.dp
            ),
        ) {
            items(NumberItems) { index ->
                Image(
                    painter = rememberGlidePainter(
                        request = rememberRandomSampleImageUrl(index),
                        fadeIn = true,
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth(),
                )
            }
        }
    }
}
