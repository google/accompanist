/*
 * Copyright 2022 The Android Open Source Project
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

@file:Suppress("DEPRECATION")

package com.google.accompanist.sample.themeadapter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.sample.R
import com.google.accompanist.themeadapter.material.MdcTheme

class MdcThemeSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentView = ComposeView(this)
        setContentView(contentView)
        contentView.setContent {
            MdcTheme {
                MaterialSample()
            }
        }
    }
}

@Preview
@Composable
fun MaterialSamplePreview() {
    MdcTheme {
        MaterialSample()
    }
}

@Composable
fun MaterialSample() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.themeadapter_title_material)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp)
        ) {
            CircularProgressIndicator()
            VerticalSpacer()

            Button(onClick = {}) {
                Text(text = "Button")
            }
            VerticalSpacer()

            OutlinedButton(onClick = {}) {
                Text(text = "Outlined Button")
            }
            VerticalSpacer()

            TextButton(onClick = {}) {
                Text(text = "Text Button")
            }
            VerticalSpacer()

            FloatingActionButton(
                onClick = {},
                content = { Icon(Icons.Default.Favorite, null) }
            )
            VerticalSpacer()

            ExtendedFloatingActionButton(
                onClick = {},
                text = { Text(text = "Extended FAB") },
                icon = { Icon(Icons.Default.Favorite, null) }
            )
            VerticalSpacer()

            TextField(
                value = "",
                onValueChange = {},
                label = { Text(text = "Text field") }
            )
            VerticalSpacer()

            Text(
                text = "H1",
                style = MaterialTheme.typography.h1
            )
            Text(
                text = "Headline 2",
                style = MaterialTheme.typography.h2
            )
            Text(
                text = "Headline 3",
                style = MaterialTheme.typography.h3
            )
            Text(
                text = "Headline 4",
                style = MaterialTheme.typography.h4
            )
            Text(
                text = "Headline 5",
                style = MaterialTheme.typography.h5
            )
            Text(
                text = "Headline 6",
                style = MaterialTheme.typography.h6
            )
            Text(
                text = "Subtitle 1",
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                text = "Subtitle 2",
                style = MaterialTheme.typography.subtitle2
            )
            Text(
                text = "Body 1",
                style = MaterialTheme.typography.body1
            )
            Text(
                text = "Body 2",
                style = MaterialTheme.typography.body2
            )
            Text(
                text = "Caption",
                style = MaterialTheme.typography.caption
            )
            Text(
                text = "Overline",
                style = MaterialTheme.typography.overline
            )
        }
    }
}

@Composable
private fun VerticalSpacer() {
    Spacer(Modifier.height(8.dp))
}
