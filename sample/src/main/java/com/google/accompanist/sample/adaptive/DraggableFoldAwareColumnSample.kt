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
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.google.accompanist.adaptive.FoldAwareColumn
import com.google.accompanist.adaptive.calculateDisplayFeatures
import com.google.accompanist.sample.AccompanistSample
import com.google.accompanist.sample.R
import kotlin.math.roundToInt

class DraggableFoldAwareColumnSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccompanistSample {
                DraggableExample(this@DraggableFoldAwareColumnSample)
            }
        }
    }
}

@Composable
fun DraggableExample(activity: Activity) {
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
    FoldAwareColumn(
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offset = Offset(offset.x + dragAmount.x, offset.y + dragAmount.y)
                }
            }
            .width(400.dp)
            .border(5.dp, MaterialTheme.colorScheme.secondary),
        displayFeatures = calculateDisplayFeatures(activity),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier
                .border(2.dp, MaterialTheme.colorScheme.primary)
                .padding(20.dp)
                .align(Alignment.Start),
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = null
        )
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .border(2.dp, MaterialTheme.colorScheme.primary),
            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        )
        Image(
            modifier = Modifier
                .ignoreFold()
                .align(Alignment.End)
                .border(2.dp, MaterialTheme.colorScheme.primary),
            painter = painterResource(id = R.drawable.placeholder),
            contentDescription = null
        )
    }
}
