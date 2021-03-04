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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.chrisbanes.accompanist.glide.GlideImage

/**
 * Simple list item row which displays an image and text.
 */
@Composable
fun ListItem(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    Row(modifier.padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 8.dp)) {
        GlideImage(
            data = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(Modifier.width(16.dp))

        Text(
            text = "Text",
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        )

        Box {
            var showMenu by remember { mutableStateOf(false) }

            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Open menu"
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(onClick = { /* no-op */ }) {
                    Text(text = "Menu item 1")
                }
                DropdownMenuItem(onClick = { /* no-op */ }) {
                    Text(text = "Menu item 2")
                }
                DropdownMenuItem(onClick = { /* no-op */ }) {
                    Text(text = "Menu item 3")
                }
            }
        }
    }
}
