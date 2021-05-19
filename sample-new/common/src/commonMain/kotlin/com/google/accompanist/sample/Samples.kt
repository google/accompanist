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

package com.google.accompanist.sample

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.accompanist.sample.flowlayout.FlowColumnSample
import com.google.accompanist.sample.flowlayout.FlowRowSample

data class Sample(
    val name: String,
    val content: @Composable () -> Unit,
)

val Samples = listOf(
    Sample("FlowRow") { FlowRowSample() },
    Sample("FlowColumn") { FlowColumnSample() },
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Samples() {
    var currentSample by remember { mutableStateOf<Sample?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (currentSample != null) {
                        IconButton(onClick = { currentSample = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Navigate back")
                        }
                    }
                },
                title = {
                    Text(currentSample?.name ?: "Accompanist Samples")
                }
            )
        }
    ) {
        Crossfade(currentSample) { sample ->
            if (sample != null) {
                sample.content()
            } else {
                SampleList(samples = Samples) {
                    currentSample = it
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SampleList(
    samples: List<Sample>,
    onSampleSelected: (Sample) -> Unit,
) {
    LazyColumn {
        items(samples) { sample ->
            ListItem(
                Modifier
                    .fillParentMaxWidth()
                    .clickable { onSampleSelected(sample) }
            ) {
                Text(sample.name)
            }
        }
    }
}
