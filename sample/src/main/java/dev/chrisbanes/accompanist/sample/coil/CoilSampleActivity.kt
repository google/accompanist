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
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.Recomposer
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.FlowRow
import androidx.ui.layout.padding
import androidx.ui.layout.preferredSize
import androidx.ui.material.Scaffold
import androidx.ui.material.TopAppBar
import androidx.ui.res.stringResource
import androidx.ui.unit.dp
import coil.request.GetRequest
import coil.transform.CircleCropTransformation
import dev.chrisbanes.accompanist.coil.CoilImage
import dev.chrisbanes.accompanist.coil.CoilImageWithCrossfade
import dev.chrisbanes.accompanist.mdctheme.MaterialThemeFromMdcTheme
import dev.chrisbanes.accompanist.sample.R

class CoilSampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contentView = FrameLayout(this)
        setContentView(contentView)

        contentView.setContent(Recomposer.current()) {
            MaterialThemeFromMdcTheme {
                CoilSample()
            }
        }
    }
}

@Composable
fun CoilSample() {
    Scaffold(
        topAppBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.coil_title)) }
            )
        }
    ) {
        VerticalScroller(modifier = Modifier.padding(16.dp)) {
            FlowRow(
                mainAxisSpacing = 4.dp,
                crossAxisSpacing = 4.dp
            ) {
                // CoilImage with data parameter
                CoilImage(
                    data = "https://loremflickr.com/300/300",
                    modifier = Modifier.preferredSize(128.dp, 128.dp)
                )

                // CoilImage with GetRequest parameter
                CoilImage(
                    request = GetRequest.Builder(ContextAmbient.current)
                        .data("https://loremflickr.com/300/300")
                        .transformations(CircleCropTransformation())
                        .build(),
                    modifier = Modifier.preferredSize(128.dp, 128.dp)
                )

                // CoilImageWithCrossfade with data parameter
                CoilImageWithCrossfade(
                    data = "https://loremflickr.com/300/300",
                    modifier = Modifier.preferredSize(128.dp, 128.dp)
                )

                // CoilImageWithCrossfade with GetRequest parameter
                CoilImageWithCrossfade(
                    request = GetRequest.Builder(ContextAmbient.current)
                        .data("https://loremflickr.com/300/300")
                        .transformations(CircleCropTransformation())
                        .build(),
                    modifier = Modifier.preferredSize(128.dp, 128.dp)
                )
            }
        }
    }
}
