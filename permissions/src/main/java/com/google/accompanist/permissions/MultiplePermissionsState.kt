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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

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
            permissionRequestedState = permissionRequested,
            shouldShowRationaleState = shouldShowRationale
        )
    }
}

class MultiplePermissionsState(
    private val permissions: List<PermissionState>,
    private val launcher: ActivityResultLauncher<Array<String>>,
    revokedPermissionsState: State<List<PermissionState>>,
    permissionRequestedState: State<Boolean>,
    shouldShowRationaleState: State<Boolean>
) {
    val permissionRequested by permissionRequestedState
    val revokedPermissions by revokedPermissionsState
    val shouldShowRationale by shouldShowRationaleState

    val allPermissionsGranted: Boolean
        get() = permissions.all { it.hasPermission } || // Up to date when the lifecycle is STARTED
            revokedPermissions.isEmpty() // Up to date when the user launches the action

    fun launchMultiplePermissionRequest() = launcher.launch(
        permissions.map { it.permission }.toTypedArray()
    )
}
