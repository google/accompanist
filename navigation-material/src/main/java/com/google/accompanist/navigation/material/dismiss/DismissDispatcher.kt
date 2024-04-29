/*
 * Copyright 2024 The Android Open Source Project
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

package com.google.accompanist.navigation.material.dismiss

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavigatorState

internal val LocalDismissDispatcher = compositionLocalOf<OnDismissDispatcher?> {
    null
}

internal class OnDismissDispatcher {

    private val callbacks: MutableList<DismissHandlerCallback> = mutableListOf()

    fun tryToDismissSheet(
        state: NavigatorState,
        backStackEntry: NavBackStackEntry,
        showSheet: () -> Unit,
    ) {
        if (callbacks.any { it.isEnabled }) {
            callbacks.forEach { it.onDismiss() }
            // the sheet has been dismissed by the user
            // (for example by tapping on the scrim or through an accessibility action)
            // we need to show the sheet again to handle dismissal manually
            showSheet()
        } else {
            state.pop(popUpTo = backStackEntry, saveState = false)
        }
    }

    fun addCallback(callback: DismissHandlerCallback) = callbacks.add(callback)

    fun removeCallback(callback: DismissHandlerCallback) = callbacks.remove(callback)
}

internal interface DismissHandlerCallback {
    val isEnabled: Boolean
    fun onDismiss()
}
