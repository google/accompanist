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

package dev.chrisbanes.accompanist.sample.glide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.chrisbanes.accompanist.glide.GlideImage
import dev.chrisbanes.accompanist.sample.R
import dev.chrisbanes.accompanist.sample.randomSampleImageUrl

class GlideBasicSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
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
                title = { Text(text = stringResource(R.string.glide_title_basic)) }
            )
        }
    ) {
        ScrollableColumn(modifier = Modifier.padding(16.dp)) {
            FlowRow(
                mainAxisSpacing = 4.dp,
                crossAxisSpacing = 4.dp
            ) {
                // GlideImage with data parameter
                GlideImage(
                    data = randomSampleImageUrl(),
                    modifier = Modifier.preferredSize(128.dp)
                )

                // GlideImage with loading slot
                GlideImage(
                    data = randomSampleImageUrl(),
                    loading = {
                        Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                    },
                    modifier = Modifier.preferredSize(128.dp)
                )

                // GlideImage with crossfade and data parameter
                GlideImage(
                    data = randomSampleImageUrl(),
                    fadeIn = true,
                    modifier = Modifier.preferredSize(128.dp)
                )

                // GlideImage with crossfade and loading slot
                GlideImage(
                    data = randomSampleImageUrl(),
                    fadeIn = true,
                    loading = {
                        Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                    },
                    modifier = Modifier.preferredSize(128.dp)
                )

                // GlideImage with an implicit size
                GlideImage(
                    data = randomSampleImageUrl(),
                    loading = {
                        Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                    }
                )

                // GlideImage with an aspect ratio
                GlideImage(
                    data = randomSampleImageUrl(),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.preferredWidth(256.dp)
                        .aspectRatio(16 / 9f)
                )
            }
        }
    }
}
