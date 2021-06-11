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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Creates a [PermissionState] that is remembered across compositions.
 *
 * It's recommended that apps exercise the permissions workflow as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 *
 * @param permission the permission to control and observe.
 */
@Composable
fun rememberPermissionState(
    permission: String
): PermissionState {
    val mutablePermissionState = rememberMutablePermissionState(permission)
    return rememberPermissionState(permission, mutablePermissionState)
}

/**
 * Creates a [PermissionState] that is remembered across compositions.
 *
 * It's recommended that apps exercise the permissions workflow as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 *
 * @param permission the permission to control and observe.
 * @param mutablePermissionState state to control the an individual permission.
 */
@Composable
internal fun rememberPermissionState(
    permission: String,
    mutablePermissionState: MutablePermissionState
): PermissionState {
    val permissionRequested = rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        permissionRequested.value = true
        mutablePermissionState.hasPermissionState.value = it
        mutablePermissionState.refreshShouldShowRationaleState()
    }

    return PermissionState(
        permission = permission,
        launcher = launcher,
        hasPermissionState = mutablePermissionState.hasPermissionState,
        shouldShowRationaleState = mutablePermissionState.shouldShowRationaleState,
        permissionRequestedState = permissionRequested,
    )
}

/**
 * A state object that can be hoisted to control and observe permission status changes.
 *
 * In most cases, this will be created via [rememberPermissionState].
 *
 * It's recommended that apps exercise the permissions workflow as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 *
 * @param permission the permission to control and observe.
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
    private val launcher: ActivityResultLauncher<String>,
    hasPermissionState: State<Boolean>,
    shouldShowRationaleState: State<Boolean>,
    permissionRequestedState: State<Boolean>
) {
    /**
     * When `true`, the user has granted the [permission].
     */
    val hasPermission by hasPermissionState

    /**
     * When `true`, the user should be presented with a rationale.
     */
    val shouldShowRationale by shouldShowRationaleState

    /**
     * When `true`, the [permission] request has been done previously.
     */
    val permissionRequested by permissionRequestedState

    /**
     * Request the [permission] to the user.
     *
     * This should always be triggered from a side-effect in Compose. Otherwise, this will
     * result in an IllegalStateException.
     *
     * This triggers a system dialog that asks the user to grant or revoke the permission.
     * Note that this dialog might not appear on the screen if the user doesn't want to be asked
     * again or has denied the permission multiple times.
     * This behavior varies depending on the Android level API.
     */
    fun launchPermissionRequest(): Unit = launcher.launch(permission)
}
