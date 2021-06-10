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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    val permissionState = rememberPermissionGrantedState(permission)
    val (shouldShowRationaleState, refreshShouldShowRationaleState) =
        rememberShouldShowRationaleState(permission)

    return remember(permission) {
        MutablePermissionState(
            permission = permission,
            refreshShouldShowRationaleState = refreshShouldShowRationaleState,
            hasPermissionState = permissionState,
            shouldShowRationaleState = shouldShowRationaleState
        )
    }
}

/**
 * A mutable state object that can be used to control and observe permission status changes.
 *
 * In most cases, this will be created via [rememberMutablePermissionState].
 *
 * @param permission the permission to control and observe.
 * @param hasPermissionState [State] that represents if the permission is granted.
 * @param shouldShowRationaleState [State] that represents if the user should be presented with a
 * rationale.
 * @param refreshShouldShowRationaleState action to refresh whether or not a rationale should be
 * presented to the user to explain why the permission should be granted.
 */
@Stable
internal data class MutablePermissionState(
    val permission: String,
    val hasPermissionState: MutableState<Boolean>,
    val shouldShowRationaleState: State<Boolean>,
    val refreshShouldShowRationaleState: () -> Unit,
)

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

    fun checkPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

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

    fun shouldShow(): Boolean =
        ActivityCompat.shouldShowRequestPermissionRationale(currentActivity, permission)

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
 * @param result the user should be presented with a rationale.
 * @param onRefresh refresh and update the [result] state.
 */
private data class ShouldShowRationaleState(
    val result: State<Boolean>,
    val onRefresh: () -> Unit
)
