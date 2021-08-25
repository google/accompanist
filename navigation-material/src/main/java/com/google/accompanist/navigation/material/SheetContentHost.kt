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

package com.google.accompanist.navigation.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.LocalOwnersProvider
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * Hosts a [BottomSheetNavigator.Destination]'s [NavBackStackEntry] and its
 * [BottomSheetNavigator.Destination.content] and provides a [onSheetDismissed] callback. It also
 * shows and hides the [ModalBottomSheetLayout] through the [sheetState] when the sheet content
 * enters or leaves the composition.
 *
 * @param columnHost The [ColumnScope] the sheet content is hosted in, typically the instance
 * that is provided by [ModalBottomSheetLayout]
 * @param backStackEntry The [NavBackStackEntry] holding the [BottomSheetNavigator.Destination],
 * or null if there is no [NavBackStackEntry]
 * @param sheetState The [ModalBottomSheetState] used to observe and control the sheet visibility
 * @param onSheetDismissed Callback when the sheet has been dismissed. Typically, you'll want to
 * pop the back stack here.
 */
@ExperimentalMaterialNavigationApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun SheetContentHost(
    columnHost: ColumnScope,
    backStackEntry: NavBackStackEntry?,
    sheetState: ModalBottomSheetState,
    saveableStateHolder: SaveableStateHolder,
    onSheetShown: (entry: NavBackStackEntry) -> Unit,
    onSheetDismissed: (entry: NavBackStackEntry) -> Unit
) {
    val scope = rememberCoroutineScope()
    if (backStackEntry != null) {
        val currentOnSheetShown by rememberUpdatedState(onSheetShown)
        val currentOnSheetDismissed by rememberUpdatedState(onSheetDismissed)
        var hideCalled by remember(backStackEntry) { mutableStateOf(false) }
        LaunchedEffect(backStackEntry, hideCalled) {
            val sheetVisibility = snapshotFlow { sheetState.isVisible }
            sheetVisibility
                // We are only interested in changes in the sheet's visibility
                .distinctUntilChanged()
                // distinctUntilChanged emits the initial value which we don't need
                .drop(1)
                // We want to know when the sheet was visible but is not anymore
                .filter { isVisible -> !isVisible }
                // Finally, pop the back stack when the sheet has been hidden
                .collect { if (!hideCalled) currentOnSheetDismissed(backStackEntry) }
        }

        // Whenever the composable associated with the latestEntry enters the composition, we
        // want to show the sheet, and hide it when this composable leaves the composition
        DisposableEffect(backStackEntry) {
            scope.launch {
                // Our show call can get cancelled in which case Swipeable will move to the closest
                // anchor
                try {
                    sheetState.show()
                } finally {
                    // If the target state is a visible state, it's fairly safe to assume that
                    // Swipeable will end up settling in that state
                    if (sheetState.targetValue == ModalBottomSheetValue.Expanded ||
                        sheetState.targetValue == ModalBottomSheetValue.HalfExpanded
                    ) {
                        currentOnSheetShown(backStackEntry)
                    }
                }
            }
            onDispose {
                scope.launch {
                    hideCalled = true
                    try {
                        sheetState.internalHide()
                    } finally {
                        hideCalled = false
                    }
                }
            }
        }

        val content = (backStackEntry.destination as BottomSheetNavigator.Destination).content
        backStackEntry.LocalOwnersProvider(saveableStateHolder) {
            columnHost.content(backStackEntry)
        }
    } else {
        EmptySheet()
    }
}

@Composable
private fun EmptySheet() {
    // The swipeable modifier has a bug where it doesn't support having something with
    // height = 0
    // b/178529942
    // If there are no destinations on the back stack, we need to add something to work
    // around this
    Box(Modifier.height(1.dp))
}

// We have the same issue when we are hiding the sheet, but snapTo works better
@OptIn(ExperimentalMaterialApi::class)
private suspend fun ModalBottomSheetState.internalHide() {
    snapTo(ModalBottomSheetValue.Hidden)
}
