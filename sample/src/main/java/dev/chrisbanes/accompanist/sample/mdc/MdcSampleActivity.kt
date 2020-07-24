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

package dev.chrisbanes.accompanist.sample.mdc

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.Recomposer
import androidx.compose.foundation.Icon
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.unit.dp
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.res.stringResource
import dev.chrisbanes.accompanist.mdctheme.MaterialThemeFromMdcTheme
import dev.chrisbanes.accompanist.sample.R

class MdcSampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contentView = FrameLayout(this)
        setContentView(contentView)

        contentView.setContent(Recomposer.current()) {
            MaterialThemeFromMdcTheme {
                MdcSample()
            }
        }
    }
}

@Composable
fun MdcSample() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.mdc_title)) }
            )
        }
    ) {
        ScrollableColumn {
            Column(Modifier.padding(16.dp)) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.preferredHeight(8.dp))

                Button(onClick = {}) {
                    Text(text = "Button")
                }
                Spacer(modifier = Modifier.preferredHeight(8.dp))

                OutlinedButton(onClick = {}) {
                    Text(text = "Outlined Button")
                }
                Spacer(modifier = Modifier.preferredHeight(8.dp))

                TextButton(onClick = {}) {
                    Text(text = "Text Button")
                }
                Spacer(modifier = Modifier.preferredHeight(8.dp))

                FloatingActionButton(
                    onClick = {},
                    icon = { Icon(Icons.Default.Favorite) }
                )
                Spacer(modifier = Modifier.preferredHeight(8.dp))

                ExtendedFloatingActionButton(
                    onClick = {},
                    text = { Text(text = "Extended FAB") },
                    icon = { Icon(Icons.Default.Favorite) }
                )
                Spacer(modifier = Modifier.preferredHeight(8.dp))

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
}
