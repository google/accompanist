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

package com.google.accompanist.sample.insets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.rememberWindowInsetsTypePaddingValues
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.insets.ui.BottomNavigation

@Composable
fun ImeAvoidingBox() {
    val insets = LocalWindowInsets.current

    val imeBottom = with(LocalDensity.current) { insets.ime.bottom.toDp() }
    Box(Modifier.padding(bottom = imeBottom))
}

@Composable
fun Sample_fab() = Box {
    FloatingActionButton(
        onClick = { /* TODO */ },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp) // normal 16dp of padding for FABs
            .navigationBarsPadding() // Move it out from under the nav bar
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
    }
}

@Composable
fun Sample_spacer() {
    Spacer(
        Modifier
            .background(Color.Black.copy(alpha = 0.7f))
            .statusBarsHeight() // Match the height of the status bar
            .fillMaxWidth()
    )
}

@Composable
fun Sample_lazycolumn() {
    LazyColumn(
        contentPadding = rememberWindowInsetsTypePaddingValues(
            type = LocalWindowInsets.current.systemBars,
            applyTop = true,
            applyBottom = true,
        )
    ) {
        // content
    }
}

@Composable
fun BottomNavigation_Insets() {
    BottomNavigation(
        contentPadding = rememberWindowInsetsTypePaddingValues(
            LocalWindowInsets.current.navigationBars,
            applyStart = true,
            applyEnd = true,
            applyBottom = true,
        )
    ) {
        // content
    }
}

@Composable
fun TopAppBar_Insets() {
    TopAppBar(
        contentPadding = rememberWindowInsetsTypePaddingValues(
            LocalWindowInsets.current.systemBars,
            applyStart = true,
            applyTop = true,
            applyEnd = true,
        )
    ) {
        // content
    }
}
