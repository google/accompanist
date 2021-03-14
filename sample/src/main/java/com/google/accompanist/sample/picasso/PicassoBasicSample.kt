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

package com.google.accompanist.sample.picasso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.picasso.PicassoImage
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import com.google.accompanist.sample.randomSampleImageUrl

class PicassoBasicSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccompanistSampleTheme {
                Sample()
            }
        }
    }
}

@Composable
private fun Sample() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.picasso_title_basic)) }
            )
        }
    ) {
        LazyColumn(Modifier.padding(16.dp)) {
            item {
                // PicassoImage with data parameter
                PicassoImage(
                    data = randomSampleImageUrl(),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp)
                )
            }

            item {
                // PicassoImage with ImageRequest builder parameter
                PicassoImage(
                    data = randomSampleImageUrl(),
                    requestBuilder = {
                        rotate(90f)
                    },
                    contentDescription = null,
                    modifier = Modifier.size(128.dp)
                )
            }

            item {
                // PicassoImage with loading slot
                PicassoImage(
                    data = randomSampleImageUrl(),
                    loading = {
                        Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                    },
                    contentDescription = null,
                    modifier = Modifier.size(128.dp)
                )
            }

            item {
                // PicassoImage with crossfade and data parameter
                PicassoImage(
                    data = randomSampleImageUrl(),
                    fadeIn = true,
                    contentDescription = null,
                    modifier = Modifier.size(128.dp)
                )
            }

            item {
                // PicassoImage with crossfade and loading slot
                PicassoImage(
                    data = randomSampleImageUrl(),
                    fadeIn = true,
                    loading = {
                        Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                    },
                    contentDescription = null,
                    modifier = Modifier.size(128.dp)
                )
            }

            item {
                // PicassoImage with an implicit size and loading slot
                PicassoImage(
                    data = randomSampleImageUrl(),
                    loading = {
                        Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                    },
                    contentDescription = null,
                )
            }

            item {
                // PicassoImage with an aspect ratio
                PicassoImage(
                    data = randomSampleImageUrl(),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier.width(256.dp)
                        .aspectRatio(16 / 9f)
                )
            }
        }
    }
}
