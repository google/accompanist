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

package dev.chrisbanes.accompanist.sample.coil

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import dev.chrisbanes.accompanist.coil.CoilImage
import dev.chrisbanes.accompanist.sample.AccompanistSampleTheme
import dev.chrisbanes.accompanist.sample.R
import dev.chrisbanes.accompanist.sample.randomSampleImageUrl

class CoilBasicSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccompanistSampleTheme {
                Sample()
            }
        }
    }
}

@OptIn(ExperimentalLayout::class)
@Composable
private fun Sample() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.coil_title_basic)) }
            )
        }
    ) {
        LazyColumn(Modifier.padding(16.dp)) {
            item {
                // CoilImage with data parameter
                CoilImage(
                    data = randomSampleImageUrl(),
                    contentDescription = null,
                    modifier = Modifier.preferredSize(128.dp)
                )
            }

            item {
                // CoilImage with GIF
                CoilImage(
                    data = "https://cataas.com/cat/gif",
                    contentDescription = "Cat animation",
                    imageLoader = GifImageLoader(AmbientContext.current),
                    modifier = Modifier.preferredSize(128.dp)
                )
            }

            item {
                // CoilImage with ImageRequest parameter
                CoilImage(
                    request = ImageRequest.Builder(AmbientContext.current)
                        .data(randomSampleImageUrl())
                        .transformations(CircleCropTransformation())
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.preferredSize(128.dp)
                )
            }

            item {
                // CoilImage with ImageRequest builder parameter
                CoilImage(
                    data = randomSampleImageUrl(),
                    requestBuilder = {
                        transformations(CircleCropTransformation())
                    },
                    contentDescription = null,
                    modifier = Modifier.preferredSize(128.dp)
                )
            }

            item {
                // CoilImage with loading slot
                CoilImage(
                    data = randomSampleImageUrl(),
                    contentDescription = null,
                    loading = {
                        Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                    },
                    modifier = Modifier.preferredSize(128.dp)
                )
            }

            item {
                // CoilImage with crossfade and data parameter
                CoilImage(
                    data = randomSampleImageUrl(),
                    fadeIn = true,
                    contentDescription = null,
                    modifier = Modifier.preferredSize(128.dp)
                )
            }

            item {
                // CoilImage with crossfade and ImageRequest parameter
                CoilImage(
                    request = ImageRequest.Builder(AmbientContext.current)
                        .data(randomSampleImageUrl())
                        .transformations(CircleCropTransformation())
                        .build(),
                    fadeIn = true,
                    contentDescription = null,
                    modifier = Modifier.preferredSize(128.dp)
                )
            }

            item {
                // CoilImage with crossfade and loading slot
                CoilImage(
                    data = randomSampleImageUrl(),
                    fadeIn = true,
                    loading = {
                        Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                    },
                    contentDescription = null,
                    modifier = Modifier.preferredSize(128.dp)
                )
            }

            item {
                // CoilImage with an implicit size
                CoilImage(
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
                // CoilImage with an aspect ratio and crop scale
                CoilImage(
                    data = randomSampleImageUrl(),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier
                        .preferredWidth(256.dp)
                        .aspectRatio(16 / 9f)
                )
            }
        }
    }
}

fun GifImageLoader(context: Context): ImageLoader = ImageLoader.Builder(context)
    .componentRegistry {
        if (SDK_INT >= 28) add(ImageDecoderDecoder()) else add(GifDecoder())
    }
    .build()
