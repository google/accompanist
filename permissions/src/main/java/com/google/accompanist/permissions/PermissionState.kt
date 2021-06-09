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

import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.channels.Channel

@Composable
fun ActivityResultRegistry.rememberPermissionState(
    permission: String
): PermissionState {
    val permissionRequested = rememberSaveable { mutableStateOf(false) }
    val permissionState = rememberPermissionGrantedState(permission)
    val (shouldShowRationaleState, refreshShouldShowRationaleState) =
        produceShouldShowRationaleState(permission)

    val launcher = rememberActivityResultLauncher(ActivityResultContracts.RequestPermission()) {
        permissionRequested.value = true
        permissionState.value = it
        refreshShouldShowRationaleState()
    }

    return remember(launcher) {
        PermissionState(
            permission = permission,
            refreshShouldShowRationaleState = refreshShouldShowRationaleState,
            launcher = launcher,
            hasPermissionState = permissionState,
            shouldShowRationaleState = shouldShowRationaleState,
            permissionRequestedState = permissionRequested,
        )
    }
}

class PermissionState(
    val permission: String,
    private val refreshShouldShowRationaleState: () -> Unit,
    private val launcher: ActivityResultLauncher<String>,
    hasPermissionState: State<Boolean>,
    shouldShowRationaleState: State<Boolean>,
    permissionRequestedState: State<Boolean>
) {
    val hasPermission by hasPermissionState
    val shouldShowRationale by shouldShowRationaleState
    val permissionRequested by permissionRequestedState

    fun launchPermissionRequest() = launcher.launch(permission)
    fun refreshShouldShowRationale() = refreshShouldShowRationaleState()
}

@Composable
private fun rememberPermissionGrantedState(permission: String): MutableState<Boolean> {
    val context = LocalContext.current
    val checkPermission = {
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    val permissionState = remember { mutableStateOf(checkPermission()) }

    // Check if the permission was granted when the app comes from the background
    // The user might've gone to the Settings screen and granted the permission
    // We don't check if the permission was denied as that triggers a process restart
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val permissionCheckerObserver = remember(permission) {
        LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (!permissionState.value) { permissionState.value = checkPermission() }
            }
        }
    }
    DisposableEffect(lifecycle, permissionCheckerObserver) {
        lifecycle.addObserver(permissionCheckerObserver)
        onDispose {
            lifecycle.removeObserver(permissionCheckerObserver)
        }
    }

    return permissionState
}

@Composable
private fun produceShouldShowRationaleState(permission: String): ShouldShowRationaleState {
    val currentActivity by rememberUpdatedState(LocalContext.current.findActivity())
    val shouldShow: () -> Boolean = {
        ActivityCompat.shouldShowRequestPermissionRationale(currentActivity, permission)
    }

    // Posting to this channel will trigger a single refresh. The channel conflates multiple
    // refresh events that could be sent while the state is being produced
    val refreshChannel = remember { Channel<Unit>(Channel.CONFLATED) }

    val result = produceState(initialValue = shouldShow()) {
        // This for-loop will loop until the [produceState] coroutine is cancelled.
        for (refreshEvent in refreshChannel) {
            value = shouldShow()
        }
    }
    return ShouldShowRationaleState(result) { refreshChannel.trySend(Unit) }
}

private data class ShouldShowRationaleState(
    val result: State<Boolean>,
    val onRefresh: () -> Unit
)
