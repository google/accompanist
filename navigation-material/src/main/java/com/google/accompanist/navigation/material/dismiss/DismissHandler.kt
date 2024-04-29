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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
public fun DismissHandler(isEnabled: Boolean = true, onDismiss: () -> Unit) {
    // Safely update the current `onDismiss` lambda when a new one is provided
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    // Remember in Composition a dismiss callback that calls the `onDismiss` lambda
    val backCallback = remember {
        object : DismissHandlerCallback {
            override val isEnabled: Boolean = isEnabled

            override fun onDismiss() {
                return currentOnDismiss()
            }
        }
    }

    val dismissDispatcher = checkNotNull(LocalDismissDispatcher.current) {
        "No OnDismissDispatcher was provided via LocalDismissDispatcher"
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, dismissDispatcher) {
        // Add callback to the dismissDispatcher
        dismissDispatcher.addCallback(backCallback)
        // When the effect leaves the Composition, remove the callback
        onDispose {
            dismissDispatcher.removeCallback(backCallback)
        }
    }
}
