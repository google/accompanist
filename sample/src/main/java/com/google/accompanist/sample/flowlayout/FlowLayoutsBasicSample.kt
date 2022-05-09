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

package com.google.accompanist.sample.flowlayout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowColumn
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R

class FlowLayoutsBasicSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccompanistSampleTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(text = stringResource(R.string.flowlayouts_title_basic))
                            }
                        )
                    }
                ) { padding ->
                    Column(modifier = Modifier.padding(padding)) {
                        var settings by remember { mutableStateOf(Settings()) }

                        SettingsLayout(settings, onChanged = { settings = it })
                        FlowLayouts(settings)
                    }
                }
            }
        }
    }

    @Composable
    private fun FlowLayouts(settings: Settings) {
        val maxLines = if (settings.withMaxLines) 2 else Int.MAX_VALUE
        if (settings.isHorizontal) {
            FlowRow(maxLines = maxLines) {
                SampleContent()
            }
        } else {
            FlowColumn(maxLines = maxLines) {
                SampleContent()
            }
        }
    }

    @Composable
    private fun SettingsLayout(
        settings: Settings,
        onChanged: (Settings) -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OrientationSetting(
                isHorizontal = settings.isHorizontal,
                onOrientationChanged = { onChanged(settings.copy(isHorizontal = it)) }
            )
            MaxLinesSetting(
                withMaxLines = settings.withMaxLines,
                onWithMaxLinesChanged = { onChanged(settings.copy(withMaxLines = it)) }
            )
        }
    }

    @Composable
    private fun OrientationSetting(
        isHorizontal: Boolean,
        onOrientationChanged: (isHorizontal: Boolean) -> Unit,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OrientationButton(
                text = "FlowRow",
                isSelected = isHorizontal,
                onClick = { onOrientationChanged(true) }
            )
            OrientationButton(
                text = "FlowColumn",
                isSelected = !isHorizontal,
                onClick = { onOrientationChanged(false) }
            )
        }
    }

    @Composable
    private fun MaxLinesSetting(
        withMaxLines: Boolean,
        onWithMaxLinesChanged: (withMaxLines: Boolean) -> Unit,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = withMaxLines,
                onCheckedChange = onWithMaxLinesChanged,
            )
            Text(text = "With max lines (2)")
        }
    }

    @Composable
    private fun OrientationButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
        Text(
            text = text,
            color = MaterialTheme.colors.onSecondary,
            modifier = Modifier
                .alpha(if (isSelected) 1f else 0.6f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colors.secondary)
                .clickable(onClick = onClick)
                .padding(8.dp)
        )
    }

    data class Settings(
        val isHorizontal: Boolean = true,
        val withMaxLines: Boolean = false,
    )
}
