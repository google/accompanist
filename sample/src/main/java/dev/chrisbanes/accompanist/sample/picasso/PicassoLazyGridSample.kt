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

package dev.chrisbanes.accompanist.sample.picasso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.chrisbanes.accompanist.picasso.PicassoImage
import dev.chrisbanes.accompanist.sample.AccompanistSampleTheme
import dev.chrisbanes.accompanist.sample.R
import dev.chrisbanes.accompanist.sample.randomSampleImageUrl

class PicassoLazyGridSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccompanistSampleTheme {
                Sample()
            }
        }
    }
}

private const val NumberItems = 60

@OptIn(
    ExperimentalLayout::class,
    ExperimentalStdlibApi::class,
    ExperimentalFoundationApi::class
)
@Composable
private fun Sample() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.glide_title_lazy_row)) }
            )
        }
    ) {
        LazyVerticalGrid(
            cells = GridCells.Adaptive(96.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            items(NumberItems) { index ->
                PicassoImage(
                    data = randomSampleImageUrl(index),
                    contentDescription = null,
                    fadeIn = true,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth()
                )
            }
        }
    }
}
