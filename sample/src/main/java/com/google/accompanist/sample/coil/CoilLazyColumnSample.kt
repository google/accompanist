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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.coil.rememberCoilImageLoadRequest
import com.google.accompanist.imageloading.ImageLoad
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import com.google.accompanist.sample.randomSampleImageUrl

class CoilLazyColumnSample : ComponentActivity() {
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

@Suppress("DEPRECATION")
@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun Sample() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.coil_title_lazy_row)) }
            )
        }
    ) {
        val items = buildList {
            repeat(NumberItems) { add(randomSampleImageUrl(it)) }
        }
        LazyColumn(Modifier.padding(16.dp)) {
            items(items) { imageUrl ->
                Row(Modifier.padding(16.dp)) {
                    ImageLoad(
                        request = rememberCoilImageLoadRequest(imageUrl),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Text",
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier.weight(1f).align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}
