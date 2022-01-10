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

package com.google.accompanist.navigation.material

import androidx.compose.material.BackdropScaffoldDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp



@ExperimentalMaterialNavigationApi
@OptIn(ExperimentalMaterialApi::class)
@Composable public fun BackdropScaffold(
    backdropNavigator: BackdropNavigator,
    appBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    gesturesEnabled: Boolean = true,
    peekHeight: Dp = BackdropScaffoldDefaults.PeekHeight,
    headerHeight: Dp = BackdropScaffoldDefaults.HeaderHeight,
    persistentAppBar: Boolean = true,
    stickyFrontLayer: Boolean = true,
    backLayerBackgroundColor: Color = MaterialTheme.colors.primary,
    backLayerContentColor: Color = contentColorFor(backLayerBackgroundColor),
    frontLayerShape: Shape = BackdropScaffoldDefaults.frontLayerShape,
    frontLayerElevation: Dp = BackdropScaffoldDefaults.FrontLayerElevation,
    frontLayerBackgroundColor: Color = MaterialTheme.colors.surface,
    frontLayerContentColor: Color = contentColorFor(frontLayerBackgroundColor),
    frontLayerScrimColor: Color = BackdropScaffoldDefaults.frontLayerScrimColor,
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    frontLayerContent: @Composable () -> Unit,
) {
    androidx.compose.material.BackdropScaffold(
        appBar,
        backdropNavigator.backLayerContent,
        frontLayerContent,
        modifier,
        backdropNavigator.backdropScaffoldState,
        gesturesEnabled,
        peekHeight,
        headerHeight,
        persistentAppBar,
        stickyFrontLayer,
        backLayerBackgroundColor,
        backLayerContentColor,
        frontLayerShape,
        frontLayerElevation,
        frontLayerBackgroundColor,
        frontLayerContentColor,
        frontLayerScrimColor,
        snackbarHost,
    )
}

