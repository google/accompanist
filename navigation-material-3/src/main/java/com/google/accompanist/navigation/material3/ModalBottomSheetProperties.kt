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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.google.accompanist.navigation.material3

import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

@ExperimentalMaterial3NavigationApi
class ModalBottomSheetProperties(
    private val _shape: Shape? = null,
    private val _containerColor: Color? = null,
    private val _contentColor: Color? = null,
    private val _tonalElevation: Dp? = null,
    private val _scrimColor: Color? = null
) {
    val shape: Shape
        @Composable
        get() = _shape ?: BottomSheetDefaults.ExpandedShape
    val containerColor: Color
        @Composable
        get() = _containerColor ?: BottomSheetDefaults.ContainerColor
    val contentColor: Color
        @Composable
        get() = _contentColor ?: contentColorFor(containerColor)
    val tonalElevation: Dp
        @Composable
        get() = _tonalElevation ?: BottomSheetDefaults.Elevation
    val scrimColor: Color
        @Composable
        get() = _scrimColor ?: BottomSheetDefaults.ScrimColor
}