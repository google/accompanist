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

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.layout.preferredWidth
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
import dev.chrisbanes.accompanist.picasso.PicassoImage
import dev.chrisbanes.accompanist.sample.AccompanistSampleActivity
import dev.chrisbanes.accompanist.sample.R
import dev.chrisbanes.accompanist.sample.randomSampleImageUrl

class PicassoBasicSample : AccompanistSampleActivity(content = { Sample() })

@OptIn(ExperimentalLayout::class)
@Composable
private fun Sample() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.picasso_title_basic)) }
            )
        }
    ) {
        ScrollableColumn(modifier = Modifier.padding(16.dp)) {
            // PicassoImage with data parameter
            PicassoImage(
                data = randomSampleImageUrl(),
                modifier = Modifier.preferredSize(128.dp)
            )

            // PicassoImage with ImageRequest builder parameter
            PicassoImage(
                data = randomSampleImageUrl(),
                requestBuilder = {
                    rotate(90f)
                },
                modifier = Modifier.preferredSize(128.dp)
            )

            // PicassoImage with loading slot
            PicassoImage(
                data = randomSampleImageUrl(),
                loading = {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                },
                modifier = Modifier.preferredSize(128.dp)
            )

            // PicassoImage with crossfade and data parameter
            PicassoImage(
                data = randomSampleImageUrl(),
                fadeIn = true,
                modifier = Modifier.preferredSize(128.dp)
            )

            // PicassoImage with crossfade and loading slot
            PicassoImage(
                data = randomSampleImageUrl(),
                fadeIn = true,
                loading = {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                },
                modifier = Modifier.preferredSize(128.dp)
            )

            // PicassoImage with an implicit size and loading slot
            PicassoImage(
                data = randomSampleImageUrl(),
                loading = {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
            )

            // PicassoImage with an aspect ratio
            PicassoImage(
                data = randomSampleImageUrl(),
                contentScale = ContentScale.Crop,
                modifier = Modifier.preferredWidth(256.dp)
                    .aspectRatio(16 / 9f)
            )
        }
    }
}
