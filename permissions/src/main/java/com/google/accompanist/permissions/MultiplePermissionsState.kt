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
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Creates a [MultiplePermissionsState] that is remembered across compositions.
 *
 * It's recommended that apps exercise the permissions workflow as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 *
 * @param permissions the permissions to control and observe.
 */
@Composable
fun ActivityResultRegistry.rememberMultiplePermissionsState(
    vararg permissions: String
): MultiplePermissionsState {
    val permissionRequested = rememberSaveable { mutableStateOf(false) }
    val permissionsState = permissions.map {
        rememberPermissionState(permission = it)
    }
    val revokedPermissions = remember {
        mutableStateOf(permissionsState.filter { !it.hasPermission })
    }
    val shouldShowRationale = remember {
        mutableStateOf(permissionsState.firstOrNull { it.shouldShowRationale } != null)
    }

    val launcher = rememberActivityResultLauncher(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        // The permission has been requested
        permissionRequested.value = true
        // Keep track of those permissions that were revoked by the user
        val revokedPermissionsResult = mutableListOf<PermissionState>()
        for (permission in permissionsResult.keys) {
            if (permissionsResult[permission] == false) {
                revokedPermissionsResult.add(permissionsState.first { it.permission == permission })
            }
        }
        // Update shouldShowRationale with the revoked permissions
        shouldShowRationale.value = revokedPermissionsResult
            .onEach { it.refreshShouldShowRationale() }
            .firstOrNull { it.shouldShowRationale } != null
        // Update revokedPermissions state
        revokedPermissions.value = revokedPermissionsResult
    }
    return remember(launcher) {
        MultiplePermissionsState(
            permissions = permissionsState,
            launcher = launcher,
            revokedPermissionsState = revokedPermissions,
            shouldShowRationaleState = shouldShowRationale,
            permissionRequestedState = permissionRequested
        )
    }
}

/**
 * A state object that can be hoisted to control and observe multiple permission status changes.
 *
 * In most cases, this will be created via [rememberMultiplePermissionsState].
 *
 * @param permissions list of permissions to control and observe.
 * @param launcher [ActivityResultLauncher] to ask for all permissions to the user.
 * @param revokedPermissionsState [State] with all revoked permissions.
 * @param shouldShowRationaleState [State] that represents if the user should be presented with a
 * rationale.
 * @param permissionRequestedState [State] that represents if the permission has been requested
 * previously.
 */
@Stable
class MultiplePermissionsState(
    val permissions: List<PermissionState>,
    private val launcher: ActivityResultLauncher<Array<String>>,
    revokedPermissionsState: State<List<PermissionState>>,
    shouldShowRationaleState: State<Boolean>,
    permissionRequestedState: State<Boolean>
) {
    /**
     * When `true`, the permission request has been done previously.
     */
    val permissionRequested by permissionRequestedState

    /**
     * Subset of [permissions] that the user revoked.
     */
    val revokedPermissions by revokedPermissionsState

    /**
     * When `true`, the user should be presented with a rationale.
     */
    val shouldShowRationale by shouldShowRationaleState

    /**
     * When `true`, all [permissions] have been granted.
     */
    val allPermissionsGranted: Boolean
        get() = permissions.all { it.hasPermission } || // Up to date when the lifecycle is STARTED
            revokedPermissions.isEmpty() // Up to date when the user launches the action

    /**
     * Request all [permissions] to the user.
     *
     * This triggers a system dialog per permission that asks the user to grant or revoke one
     * permission at a time.
     * Note that this dialog might not appear on the screen if the user doesn't want to be asked
     * again or has denied the permission multiple times.
     * This behavior varies depending on the Android level API.
     */
    fun launchMultiplePermissionRequest() = launcher.launch(
        permissions.map { it.permission }.toTypedArray()
    )
}
