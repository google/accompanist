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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.LocalOwnersProvider

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterial3NavigationApi
@Composable
fun ModalBottomSheet(
    navigator: BottomSheetNavigator,
    content: @Composable () -> Unit
) {
    content()

    val saveableStateHolder = rememberSaveableStateHolder()
    val sheetBackStack by navigator.backStack.collectAsState()
    val visibleBackStack = rememberVisibleList(sheetBackStack)
    visibleBackStack.PopulateVisibleList(sheetBackStack)

    visibleBackStack.forEach { backStackEntry ->
        val destination = backStackEntry.destination as BottomSheetNavigator.Destination
        // while in the scope of the composable, we provide the navBackStackEntry as the
        // ViewModelStoreOwner and LifecycleOwner. We provide this to the whole ModalBottomSheet
        // because its content has a ColumnScope receiver which we have to honor when composing it.
        backStackEntry.LocalOwnersProvider(saveableStateHolder) {
            ModalBottomSheet(
                onDismissRequest = { navigator.dismiss(backStackEntry) },
                shape = destination.sheetProperties.shape,
                containerColor = destination.sheetProperties.containerColor,
                contentColor = destination.sheetProperties.contentColor,
                tonalElevation = destination.sheetProperties.tonalElevation,
                scrimColor = destination.sheetProperties.scrimColor,
                dragHandle = destination.dragHandle ?: { BottomSheetDefaults.DragHandle() }
            ) {
                DisposableEffect(backStackEntry) {
                    onDispose {
                        navigator.onTransitionComplete(backStackEntry)
                    }
                }

                destination.content(this, backStackEntry)
            }
        }
    }
}

@Composable
internal fun MutableList<NavBackStackEntry>.PopulateVisibleList(
    transitionsInProgress: Collection<NavBackStackEntry>
) {
    val isInspecting = LocalInspectionMode.current
    transitionsInProgress.forEach { entry ->
        DisposableEffect(entry.getLifecycle()) {
            val observer = LifecycleEventObserver { _, event ->
                // show dialog in preview
                if (isInspecting && !contains(entry)) {
                    add(entry)
                }
                // ON_START -> add to visibleBackStack, ON_STOP -> remove from visibleBackStack
                if (event == Lifecycle.Event.ON_START) {
                    // We want to treat the visible lists as Sets but we want to keep
                    // the functionality of mutableStateListOf() so that we recompose in response
                    // to adds and removes.
                    if (!contains(entry)) {
                        add(entry)
                    }
                }
                if (event == Lifecycle.Event.ON_STOP) {
                    remove(entry)
                }
            }
            entry.getLifecycle().addObserver(observer)
            onDispose {
                entry.getLifecycle().removeObserver(observer)
            }
        }
    }
}

@Composable
internal fun rememberVisibleList(
    transitionsInProgress: Collection<NavBackStackEntry>
): SnapshotStateList<NavBackStackEntry> {
    // show sheet in preview
    val isInspecting = LocalInspectionMode.current
    return remember(transitionsInProgress) {
        mutableStateListOf<NavBackStackEntry>().also {
            it.addAll(
                transitionsInProgress.filter { entry ->
                    if (isInspecting) {
                        true
                    } else {
                        entry.getLifecycle().currentState.isAtLeast(Lifecycle.State.STARTED)
                    }
                }
            )
        }
    }
}