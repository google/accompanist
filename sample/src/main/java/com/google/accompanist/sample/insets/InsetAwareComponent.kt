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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationDefaults
import androidx.compose.material.FabPosition
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.primarySurface
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.toPaddingValues

class InsetAwareState {

    var topBarSize: IntSize by mutableStateOf(IntSize.Zero)

    var bottomBarSize: IntSize by mutableStateOf(IntSize.Zero)
}

val LocalInsetAwareState = staticCompositionLocalOf { InsetAwareState() }

@Composable
fun InsetAwareState.toPaddingValues(
    additionalTop: Dp = 0.dp,
    additionalBottom: Dp = 0.dp,
): PaddingValues = with(LocalDensity.current) {
    LocalWindowInsets.current.systemBars.toPaddingValues(
        top = false,
        bottom = false,
        additionalTop = topBarSize.height.toDp() + additionalTop,
        additionalBottom = bottomBarSize.height.toDp() + additionalBottom,
    )
}

@Composable
fun edgeToEdgePaddingValues(
    additionalTop: Dp = 0.dp,
    additionalBottom: Dp = 0.dp,
): PaddingValues = LocalInsetAwareState.current.toPaddingValues(
    additionalTop = additionalTop,
    additionalBottom = additionalBottom
)

@Composable
fun edgeToEdgePaddingValues(
    additionalVertical: Dp = 0.dp,
): PaddingValues = edgeToEdgePaddingValues(
    additionalTop = additionalVertical,
    additionalBottom = additionalVertical
)

fun Modifier.edgeToEdgePadding(
    additionalTop: Dp = 0.dp,
    additionalBottom: Dp = 0.dp,
) = composed {
    padding(
        edgeToEdgePaddingValues(
            additionalTop = additionalTop,
            additionalBottom = additionalBottom
        )
    )
}

fun Modifier.edgeToEdgePadding(
    additionalVertical: Dp = 0.dp,
) = composed {
    padding(
        edgeToEdgePaddingValues(
            additionalVertical = additionalVertical
        )
    )
}

private fun Modifier.measureTopBarSize() = composed {
    onSizeChanged { LocalInsetAwareState.current.topBarSize = it }
}

private fun Modifier.measureBottomBarSize() = composed {
    onSizeChanged { LocalInsetAwareState.current.bottomBarSize = it }
}

@Composable
fun InsetAwareScaffold(
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    content: @Composable (PaddingValues) -> Unit = {},
) {
    val insetAwareState = InsetAwareState()
    CompositionLocalProvider(
        LocalInsetAwareState provides insetAwareState
    ) {
        Scaffold(
            modifier = modifier,
            scaffoldState = scaffoldState,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            bottomBar = {
                Box(
                    modifier = Modifier
                        .measureBottomBarSize()
                        .animateContentSize(),
                    content = { bottomBar() }
                )
            },
            content = { innerPadding ->
                content(innerPadding)
                Box(
                    modifier = Modifier
                        .measureTopBarSize()
                        .animateContentSize(),
                    content = { topBar() }
                )
            }
        )
    }
}

/**
 * A wrapper around [TopAppBar] which uses [Modifier.statusBarsPadding] to shift the app bar's
 * contents down, but still draws the background behind the status bar too.
 */
@Composable
fun InsetAwareTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
) {
    Surface(
        color = backgroundColor,
        elevation = elevation,
        modifier = modifier,
    ) {
        TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            backgroundColor = Color.Transparent,
            contentColor = contentColor,
            elevation = 0.dp,
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding(bottom = false)
        )
    }
}

@Composable
fun InsetAwareBottomNavigation(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    elevation: Dp = BottomNavigationDefaults.Elevation,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        elevation = elevation,
    ) {
        BottomNavigation(
            modifier = Modifier
                .navigationBarsPadding(),
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
            content = content,
        )
    }
}
