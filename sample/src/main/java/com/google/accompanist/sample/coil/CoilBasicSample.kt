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

package com.google.accompanist.sample.coil

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.transform.CircleCropTransformation
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import com.google.accompanist.sample.rememberRandomSampleImageUrl

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
                // Data parameter
                Image(
                    painter = rememberCoilPainter(rememberRandomSampleImageUrl()),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp),
                )
            }

            item {
                // Data parameter with placeholder
                Image(
                    painter = rememberCoilPainter(
                        request = rememberRandomSampleImageUrl(),
                        requestBuilder = {
                            placeholder(R.drawable.placeholder)
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp),
                )
            }

            item {
                // Load GIF
                Image(
                    painter = rememberCoilPainter(
                        request = "https://cataas.com/cat/gif",
                        imageLoader = gifImageLoader(LocalContext.current),
                    ),
                    contentDescription = "Cat animation",
                    modifier = Modifier.size(128.dp),
                )
            }

            item {
                // Request builder parameter
                Image(
                    painter = rememberCoilPainter(
                        request = rememberRandomSampleImageUrl(),
                        requestBuilder = {
                            transformations(CircleCropTransformation())
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp),
                )
            }

            item {
                // Loading content
                Box {
                    val coilPainter = rememberCoilPainter(rememberRandomSampleImageUrl())

                    Image(
                        painter = coilPainter,
                        contentDescription = null,
                        modifier = Modifier.size(128.dp),
                    )

                    Crossfade(coilPainter.loadState) { state ->
                        if (state is ImageLoadState.Loading) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                    }
                }
            }

            item {
                // Fade in
                Image(
                    painter = rememberCoilPainter(
                        request = rememberRandomSampleImageUrl(),
                        fadeIn = true,
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp),
                )
            }

            item {
                // Implicit size
                Image(
                    painter = rememberCoilPainter(rememberRandomSampleImageUrl()),
                    contentDescription = null,
                )
            }

            item {
                // Aspect ratio and crop scale
                Image(
                    painter = rememberCoilPainter(rememberRandomSampleImageUrl()),
                    contentDescription = null,
                    modifier = Modifier
                        .width(256.dp)
                        .aspectRatio(16 / 9f),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewSample() {
    AccompanistSampleTheme {
        Sample()
    }
}

fun gifImageLoader(context: Context): ImageLoader = ImageLoader.Builder(context)
    .componentRegistry {
        if (SDK_INT >= 28) add(ImageDecoderDecoder(context)) else add(GifDecoder())
    }
    .build()
