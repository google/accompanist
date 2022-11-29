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

package com.google.accompanist.sample.themeadapter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.sample.R
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.google.android.material.color.DynamicColors

class Mdc3ThemeSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        val contentView = ComposeView(this)
        setContentView(contentView)
        contentView.setContent {
            Mdc3Theme {
                Material3Sample()
            }
        }
    }
}

@Preview
@Composable
fun Material3SamplePreview() {
    Mdc3Theme {
        Material3Sample()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3Sample() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.themeadapter_title_material3)) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp)
        ) {
            Button(onClick = {}) {
                Text(text = "Filled button")
            }
            VerticalSpacer()

            ElevatedButton(onClick = {}) {
                Text(text = "Elevated button")
            }
            VerticalSpacer()

            FilledTonalButton(onClick = {}) {
                Text(text = "Filled tonal button")
            }
            VerticalSpacer()

            OutlinedButton(onClick = {}) {
                Text(text = "Outlined button")
            }
            VerticalSpacer()

            TextButton(onClick = {}) {
                Text(text = "Text button")
            }
            VerticalSpacer()

            SmallFloatingActionButton(
                onClick = {},
                content = { Icon(Icons.Default.Favorite, null) }
            )
            VerticalSpacer()

            FloatingActionButton(
                onClick = {},
                content = { Icon(Icons.Default.Favorite, null) }
            )
            VerticalSpacer()

            LargeFloatingActionButton(
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

            Card(modifier = Modifier.size(width = 180.dp, height = 100.dp)) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Card")
                }
            }
            VerticalSpacer()

            var checkboxChecked by remember { mutableStateOf(true) }
            Checkbox(
                checked = checkboxChecked,
                onCheckedChange = { checkboxChecked = it }
            )
            VerticalSpacer()

            var radioButtonChecked by remember { mutableStateOf(true) }
            Row(Modifier.selectableGroup()) {
                RadioButton(
                    selected = radioButtonChecked,
                    onClick = { radioButtonChecked = true }
                )
                RadioButton(
                    selected = !radioButtonChecked,
                    onClick = { radioButtonChecked = false }
                )
            }
            VerticalSpacer()

            var switchChecked by remember { mutableStateOf(true) }
            Switch(
                checked = switchChecked,
                onCheckedChange = { switchChecked = it }
            )
            VerticalSpacer()

            var linearProgress by remember { mutableStateOf(0.1f) }
            val animatedLinearProgress by animateFloatAsState(
                targetValue = linearProgress,
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(progress = animatedLinearProgress)
                HorizontalSpacer()
                TextButton(
                    onClick = {
                        if (linearProgress < 1f) linearProgress += 0.1f
                    }
                ) {
                    Text("Increase")
                }
            }
            VerticalSpacer()

            var circularProgress by remember { mutableStateOf(0.1f) }
            val animatedCircularProgress by animateFloatAsState(
                targetValue = circularProgress,
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(progress = animatedCircularProgress)
                HorizontalSpacer()
                TextButton(
                    onClick = {
                        if (circularProgress < 1f) circularProgress += 0.1f
                    }
                ) {
                    Text("Increase")
                }
            }
            VerticalSpacer()

            var sliderValue by remember { mutableStateOf(0f) }
            Column {
                Text(text = sliderValue.toString())
                Slider(value = sliderValue, onValueChange = { sliderValue = it })
            }
            VerticalSpacer()

            var text by rememberSaveable { mutableStateOf("") }
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Text field") },
                singleLine = true
            )
            VerticalSpacer()

            var outlinedText by rememberSaveable { mutableStateOf("") }
            OutlinedTextField(
                value = outlinedText,
                onValueChange = { outlinedText = it },
                label = { Text("Outlined text field") },
                singleLine = true
            )
            VerticalSpacer()

            Text(
                text = "Display Large",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "Display Medium",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = "Display Small",
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                text = "Headline Large",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Headline Medium",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Headline Small",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Title Large",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Title Medium",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Title Small",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "Body Large",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Body Medium",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Body Small",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Label Large",
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "Label Medium",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = "Label Small",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun VerticalSpacer() {
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun HorizontalSpacer() {
    Spacer(Modifier.width(8.dp))
}
