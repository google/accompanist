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

package dev.chrisbanes.accompanist.sample.insets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.onSizeChanged
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import dev.chrisbanes.accompanist.glide.GlideImage
import dev.chrisbanes.accompanist.insets.AmbientWindowInsets
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import dev.chrisbanes.accompanist.insets.add
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding
import dev.chrisbanes.accompanist.insets.toPaddingValues
import dev.chrisbanes.accompanist.sample.R
import dev.chrisbanes.accompanist.sample.randomSampleImageUrl

class EdgeToEdgeLazyColumn : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn off the decor fitting system windows, which means we need to through handling
        // insets
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MaterialTheme {
                Sample()
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun Sample() {
    ProvideWindowInsets {
        Surface {
            Box(Modifier.fillMaxSize()) {
                // A state instance which allows us to track the size of the top app bar
                var topAppBarSize by remember { mutableStateOf(0) }

                LazyColumnFor(
                    items = listItems,
                    // We use the systemBar insets as the source of our content padding.
                    // We add on the topAppBarSize, so that the content is displayed below
                    // the app bar. Since the top inset is already contained within the app
                    // bar height, we disable handling it in toPaddingValues().
                    contentPadding = AmbientWindowInsets.current.systemBars
                        .toPaddingValues(top = false)
                        .add(top = with(DensityAmbient.current) { topAppBarSize.toDp() })
                ) { imageUrl ->
                    ListItem(imageUrl, Modifier.fillMaxWidth())
                }

                InsetAwareTopAppBar(
                    title = { Text(stringResource(R.string.insets_title_list)) },
                    modifier = Modifier.fillMaxWidth()
                        // We use onSizeChanged to track the app bar height, and update
                        // our state above
                        .onSizeChanged { topAppBarSize = it.height }
                )

                FloatingActionButton(
                    onClick = { /* TODO */ },
                    icon = { Icon(Icons.Default.Face) },
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
private val listItems = buildList {
    repeat(40) {
        add(randomSampleImageUrl(it))
    }
}

/**
 * A wrapper around [TopAppBar] which uses [Modifier.statusBarsPadding] to shift the app bar's
 * contents down, but still draws the background behind the status bar too.
 */
@Composable
private fun InsetAwareTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = 4.dp
) {
    Surface(
        color = backgroundColor,
        elevation = elevation,
        modifier = modifier
    ) {
        TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            backgroundColor = Color.Transparent,
            contentColor = contentColor,
            elevation = 0.dp,
            modifier = Modifier.statusBarsPadding()
        )
    }
}

/**
 * Simple list item row which displays an image and text.
 */
@Composable
private fun ListItem(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    Row(modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        GlideImage(
            data = imageUrl,
            modifier = Modifier.preferredSize(64.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(Modifier.preferredWidth(16.dp))

        Text(
            text = "Text",
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier.weight(1f)
                .align(Alignment.CenterVertically)
        )
    }
}
