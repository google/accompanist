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
import androidx.compose.runtime.Stable
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

/**
 * Creates a [PermissionState] that is remembered across compositions.
 *
 * It's recommended that apps exercise the permissions workflow as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 *
 * @param permission the permission to control and observe.
 */
@Composable
fun ActivityResultRegistry.rememberPermissionState(
    permission: String
): PermissionState {
    val permissionRequested = rememberSaveable { mutableStateOf(false) }
    val permissionState = rememberPermissionGrantedState(permission)
    val (shouldShowRationaleState, refreshShouldShowRationaleState) =
        rememberShouldShowRationaleState(permission)

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

/**
 * A state object that can be hoisted to control and observe permission status changes.
 *
 * In most cases, this will be created via [rememberPermissionState].
 *
 * @param permission the permission to control and observe.
 * @param refreshShouldShowRationaleState action to refresh whether or not a rationale should be
 * presented to the user to explain why the permission should be granted.
 * @param launcher [ActivityResultLauncher] to ask for this permission to the user.
 * @param hasPermissionState [State] that represents if the permission is granted.
 * @param shouldShowRationaleState [State] that represents if the user should be presented with a
 * rationale.
 * @param permissionRequestedState [State] that represents if the permission has been requested
 * previously.
 */
@Stable
class PermissionState(
    val permission: String,
    private val refreshShouldShowRationaleState: () -> Unit,
    private val launcher: ActivityResultLauncher<String>,
    hasPermissionState: State<Boolean>,
    shouldShowRationaleState: State<Boolean>,
    permissionRequestedState: State<Boolean>
) {
    /**
     * When `true`, the user has granted the permission.
     */
    val hasPermission by hasPermissionState

    /**
     * When `true`, the user should be presented with a rationale.
     */
    val shouldShowRationale by shouldShowRationaleState

    /**
     * When `true`, the permission request has been done previously.
     */
    val permissionRequested by permissionRequestedState

    /**
     * Request the permission to the user.
     *
     * This triggers a system dialog that asks the user to grant or revoke the permission.
     * Note that this dialog might not appear on the screen if the user doesn't want to be asked
     * again or has denied the permission multiple times.
     * This behavior varies depending on the Android level API.
     */
    fun launchPermissionRequest() = launcher.launch(permission)

    /**
     * Check if the rationale should be presented to the user.
     *
     * This triggers a state update in the [shouldShowRationale] property.
     */
    fun refreshShouldShowRationale() = refreshShouldShowRationaleState()
}

/**
 * Creates a [MutableState] that represents if the user has granted the permission.
 *
 * This state is remembered across compositions and is updated every time the `lifecycle` of the
 * current [LocalLifecycleOwner] receives the `ON_START` lifecycle event. This check is crucial
 * when the user manually grants the permission on the Settings screen while the app is in the
 * background. There's no need to check when the permission is revoked from the Settings screen
 * as that triggers a process restart.
 */
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

/**
 * Creates a [ShouldShowRationaleState] that is remembered across recompositions.
 */
@Composable
private fun rememberShouldShowRationaleState(permission: String): ShouldShowRationaleState {
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
    return remember {
        ShouldShowRationaleState(result) { refreshChannel.trySend(Unit) }
    }
}

/**
 * A state object that is used to check if the user should be presented with a rationale for a
 * certain permission and a lambda to refresh and update the state on demand.
 *
 * @param
 */
private data class ShouldShowRationaleState(
    val result: State<Boolean>,
    val onRefresh: () -> Unit
)
