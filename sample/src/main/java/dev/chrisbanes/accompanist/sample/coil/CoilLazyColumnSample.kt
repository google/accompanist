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

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.chrisbanes.accompanist.coil.CoilImage
import dev.chrisbanes.accompanist.sample.AccompanistSampleActivity
import dev.chrisbanes.accompanist.sample.R
import dev.chrisbanes.accompanist.sample.randomSampleImageUrl

class CoilLazyColumnSample : AccompanistSampleActivity(content = { Sample() })

private const val NumberItems = 60

@OptIn(ExperimentalLayout::class, ExperimentalStdlibApi::class)
@Composable
private fun Sample() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.coil_title_grid)) }
            )
        }
    ) {
        val items = buildList {
            repeat(NumberItems) { add(randomSampleImageUrl(it)) }
        }
        LazyColumnFor(items, modifier = Modifier.padding(16.dp)) { imageUrl ->
            Row(Modifier.padding(16.dp)) {
                CoilImage(
                    data = imageUrl,
                    modifier = Modifier.preferredSize(64.dp)
                )

                Spacer(Modifier.preferredWidth(8.dp))

                Text(
                    text = "Text",
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier.weight(1f).align(Alignment.CenterVertically)
                )
            }
        }
    }
}
