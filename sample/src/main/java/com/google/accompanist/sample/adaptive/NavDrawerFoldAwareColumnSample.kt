/*
 * Copyright 2023 The Android Open Source Project
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

package com.google.accompanist.sample.adaptive

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.adaptive.FoldAwareColumn
import com.google.accompanist.adaptive.calculateDisplayFeatures
import com.google.accompanist.sample.AccompanistSample

class NavDrawerFoldAwareColumnSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccompanistSample(contentPadding = PaddingValues(all = 0.dp)) {
                NavDrawerExample(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavDrawerExample(activity: Activity) {
    val icons = listOf(
        Icons.Default.Done,
        Icons.Default.Face,
        Icons.Default.Lock,
        Icons.Default.Search,
        Icons.Default.ThumbUp,
        Icons.Default.Warning,
        Icons.Default.Star
    )

    var selectedIcon by remember { mutableStateOf(icons[0]) }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                FoldAwareColumn(
                    displayFeatures = calculateDisplayFeatures(activity),
                    foldPadding = PaddingValues(vertical = 10.dp)
                ) {
                    icons.forEach {
                        NavigationDrawerItem(
                            modifier = Modifier
                                .padding(5.dp)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            icon = { Icon(imageVector = it, contentDescription = it.name) },
                            label = { Text(it.name.substringAfter('.')) },
                            selected = it == selectedIcon,
                            onClick = { selectedIcon = it }
                        )
                    }
                }
            }
        },
        content = { Surface(modifier = Modifier.fillMaxSize()) {} }
    )
}
