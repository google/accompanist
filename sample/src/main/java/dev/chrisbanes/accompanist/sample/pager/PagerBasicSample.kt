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

package dev.chrisbanes.accompanist.sample.pager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.chrisbanes.accompanist.sample.AccompanistSampleTheme
import dev.chrisbanes.accompanist.sample.R

class PagerBasicSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AccompanistSampleTheme {
                // We need to use ProvideWindowInsets to setup the necessary listeners which
                // power the library
                Surface {
                    Sample()
                }
            }
        }
    }
}

@Composable
private fun Sample() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pager_title_basics)) },
                backgroundColor = MaterialTheme.colors.surface,
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
    }
}
