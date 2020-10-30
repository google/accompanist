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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.chrisbanes.accompanist.coil.CoilImage
import dev.chrisbanes.accompanist.sample.R
import dev.chrisbanes.accompanist.sample.randomSampleImageUrl

class CoilGridSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Sample()
            }
        }
    }
}

private const val NumberItems = 60

@OptIn(ExperimentalLayout::class)
@Composable
private fun Sample() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.coil_title_grid)) }
            )
        }
    ) {
        ScrollableColumn(modifier = Modifier.padding(16.dp)) {
            FlowRow(
                mainAxisSpacing = 4.dp,
                crossAxisSpacing = 4.dp
            ) {
                for (i in 0 until NumberItems) {
                    CoilImage(
                        data = randomSampleImageUrl(i),
                        modifier = Modifier.preferredSize(112.dp)
                    )
                }
            }
        }
    }
}
