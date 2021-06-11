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

package com.google.accompanist.permissions

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.channels.Channel

/**
 * Creates a [MutablePermissionState] that is remembered across compositions.
 *
 * This automatically updates the `hasPermission` state every time the `lifecycle` of the
 * current [LocalLifecycleOwner] receives the `ON_START` lifecycle event.
 *
 * @param permission the permission to control and observe.
 */
@Composable
internal fun rememberMutablePermissionState(
    permission: String
): MutablePermissionState {
    val context = LocalContext.current
    val currentActivity by rememberUpdatedState(context.findActivity())

    // FIXME: This should probably be rememberSaveable with a saver for the
    //  permissionRequested value
    val state = remember(permission) { MutablePermissionState(permission = permission) }

    // Check if the permission was granted when the lifecycle is resumed.
    // The user might've gone to the Settings screen and granted the permission
    // We don't check if the permission was denied as that triggers a process restart
    val permissionCheckerObserver = remember(permission) {
        LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (!state.hasPermission) {
                    state.hasPermission = context.checkPermission(permission)
                }
            }
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, permissionCheckerObserver) {
        lifecycle.addObserver(permissionCheckerObserver)
        onDispose { lifecycle.removeObserver(permissionCheckerObserver) }
    }

    // Observe the state refresh channel, to trigger a refresh when requested.
    LaunchedEffect(state) {
        for (refreshEvent in state.refreshChannel) {
            state.shouldShowRationale = currentActivity.shouldShowRationale(permission)
        }
    }

    return state
}

/**
 * A mutable state object that can be used to control and observe permission status changes.
 *
 * In most cases, this will be created via [rememberMutablePermissionState].
 *
 * @param permission the permission to control and observe.
 */
@Stable
internal class MutablePermissionState(
    override val permission: String,
) : PermissionState {
    override var hasPermission: Boolean by mutableStateOf(false)
    override var shouldShowRationale: Boolean by mutableStateOf(false)
    override var permissionRequested: Boolean by mutableStateOf(false)

    // This feels a bit memory leaky
    internal lateinit var launcher: ActivityResultLauncher<String>

    internal val refreshChannel = Channel<Unit>(Channel.CONFLATED)

    internal fun refreshShouldShowRationaleState() {
        refreshChannel.trySend(Unit)
    }

    override fun launchPermissionRequest() {
        launcher.launch(permission)
    }
}
