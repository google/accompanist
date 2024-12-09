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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.util.fastMap

/**
 * Creates a [MultiplePermissionsState] that is remembered across compositions.
 *
 * It's recommended that apps exercise the permissions workflow as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 *
 * @param permissions the permissions to control and observe.
 * @param onPermissionsResult will be called with whether or not the user granted the permissions
 *  after [MultiplePermissionsState.launchMultiplePermissionRequest] is called.
 */
@ExperimentalPermissionsApi
@Composable
public fun rememberMultiplePermissionsState(
    permissions: List<String>,
    onPermissionsResult: (Map<String, Boolean>) -> Unit = {}
): MultiplePermissionsState {
    return rememberMultiplePermissionsState(permissions, onPermissionsResult, emptyMap())
}

/**
 * Creates a [MultiplePermissionsState] that is remembered across compositions.
 *
 * It's recommended that apps exercise the permissions workflow as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 *
 * @param permissions the permissions to control and observe.
 * @param onPermissionsResult will be called with whether or not the user granted the permissions
 *  after [MultiplePermissionsState.launchMultiplePermissionRequest] is called.
 * @param previewPermissionStatuses provides a [PermissionStatus] for a given permission when running
 *  in a preview.
 */
@ExperimentalPermissionsApi
@Composable
public fun rememberMultiplePermissionsState(
    permissions: List<String>,
    onPermissionsResult: (Map<String, Boolean>) -> Unit = {},
    previewPermissionStatuses: Map<String, PermissionStatus> = emptyMap()
): MultiplePermissionsState {
    return when {
        LocalInspectionMode.current ->
            PreviewMultiplePermissionsState(permissions, previewPermissionStatuses)
        else -> rememberMutableMultiplePermissionsState(permissions, onPermissionsResult)
    }
}

/**
 * A state object that can be hoisted to control and observe multiple [permissions] status changes.
 *
 * In most cases, this will be created via [rememberMultiplePermissionsState].
 *
 * It's recommended that apps exercise the permissions workflow as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 */
@ExperimentalPermissionsApi
@Stable
public interface MultiplePermissionsState {

    /**
     * List of all permissions to request.
     */
    public val permissions: List<PermissionState>

    /**
     * List of permissions revoked by the user.
     */
    public val revokedPermissions: List<PermissionState>

    /**
     * When `true`, the user has granted all [permissions].
     */
    public val allPermissionsGranted: Boolean

    /**
     * When `true`, the user should be presented with a rationale.
     */
    public val shouldShowRationale: Boolean

    /**
     * Request the [permissions] to the user.
     *
     * This should always be triggered from non-composable scope, for example, from a side-effect
     * or a non-composable callback. Otherwise, this will result in an IllegalStateException.
     *
     * This triggers a system dialog that asks the user to grant or revoke the permission.
     * Note that this dialog might not appear on the screen if the user doesn't want to be asked
     * again or has denied the permission multiple times.
     * This behavior varies depending on the Android level API.
     */
    public fun launchMultiplePermissionRequest(): Unit
}

@OptIn(ExperimentalPermissionsApi::class)
@Immutable
private class PreviewMultiplePermissionsState(
    permissions: List<String>,
    permissionStatuses: Map<String, PermissionStatus>
) : MultiplePermissionsState {
    override val permissions: List<PermissionState> = permissions.fastMap { permission ->
        PreviewPermissionState(
            permission = permission,
            status = permissionStatuses[permission] ?: PermissionStatus.Granted,
        )
    }

    override val revokedPermissions: List<PermissionState> = emptyList()
    override val allPermissionsGranted: Boolean = false
    override val shouldShowRationale: Boolean = false

    override fun launchMultiplePermissionRequest() {}
}
