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
 * @param permission a permission to control and observe.
 * @param otherPermissions additional permissions to control and observe.
 * @param onPermissionsResult will be called with whether or not the user granted the permissions
 *  after [MultiplePermissionsState.launchMultiplePermissionRequest] is called.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
public fun rememberMultiplePermissionsState(
    permission: String,
    vararg otherPermissions: String,
    onPermissionsResult: (Map<String, Boolean>) -> Unit = {}
): MultiplePermissionsState = rememberMultiplePermissionsState(
    permissions = listOf(permission) + otherPermissions.toList(),
    onPermissionsResult = onPermissionsResult,
)

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
    return when {
        LocalInspectionMode.current -> PreviewMultiplePermissionsState(permissions)
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
     * When `true`, the user hasn't requested [permissions] yet.
     */
    public val isNotRequested: Boolean

    /**
     * When `true`, the user has granted all [permissions].
     */
    public val allPermissionsGranted: Boolean

    /**
     * When `true`, the user has permanently denied all [permissions] that haven't been granted.
     */
    public val allNotGrantedPermissionsArePermanentlyDenied: Boolean

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

    /**
     * Open the app settings page.
     *
     * If the first request permission in [permissions] is [android.Manifest.permission.POST_NOTIFICATIONS] then
     * the notification settings will be opened. Otherwise the app's settings will be opened.
     *
     * This should always be triggered from non-composable scope, for example, from a side-effect
     * or a non-composable callback. Otherwise, this will result in an IllegalStateException.
     */
    public fun openAppSettings(): Unit
}

/**
 * Calls [MultiplePermissionsState.openAppSettings] when
 * [MultiplePermissionsState.allNotGrantedPermissionsArePermanentlyDenied] is `true`; otherwise calls
 * [MultiplePermissionsState.launchMultiplePermissionRequest].
 *
 * This should always be triggered from non-composable scope, for example, from a side-effect
 * or a non-composable callback. Otherwise, this will result in an IllegalStateException.
 */
@ExperimentalPermissionsApi
public fun MultiplePermissionsState.launchMultiplePermissionRequestOrAppSettings() {
    when {
        allNotGrantedPermissionsArePermanentlyDenied -> openAppSettings()
        else -> launchMultiplePermissionRequest()
    }
}

/**
 * List of permissions granted by the user.
 */
@ExperimentalPermissionsApi
public inline val MultiplePermissionsState.grantedPermissions: List<PermissionState>
    get() = permissions.filter { it.status.isGranted }

/**
 * List of permissions not granted by the user.
 */
@ExperimentalPermissionsApi
public inline val MultiplePermissionsState.notGrantedPermissions: List<PermissionState>
    get() = permissions.filter { it.status.isNotGranted }

/**
 * List of permissions denied by the user.
 */
@ExperimentalPermissionsApi
public inline val MultiplePermissionsState.deniedPermissions: List<PermissionState>
    get() = permissions.filter { it.status.isDenied }

/**
 * List of permissions permanently denied by the user.
 */
@ExperimentalPermissionsApi
public inline val MultiplePermissionsState.permanentlyDeniedPermissions: List<PermissionState>
    get() = permissions.filter { it.status.isPermanentlyDenied }

/**
 * Returns `true` if [permission] was granted, otherwise `false`.
 *
 * If [permission] wasn't requested a [IllegalArgumentException] will be thrown.
 */
@ExperimentalPermissionsApi
public fun MultiplePermissionsState.isGranted(permission: String): Boolean =
    requireNotNull(permissions.find { it.permission == permission }) {
        "$permission is not present in the list of requested permissions"
    }.status.isGranted

/**
 * Returns `true` if [permission] was not granted, otherwise `false`.
 *
 * If [permission] wasn't requested a [IllegalArgumentException] will be thrown.
 */
@ExperimentalPermissionsApi
public fun MultiplePermissionsState.isNotGranted(permission: String): Boolean =
    requireNotNull(permissions.find { it.permission == permission }) {
        "$permission is not present in the list of requested permissions"
    }.status.isNotGranted

/**
 * Returns `true` if [permission] was denied, otherwise `false`.
 *
 * If [permission] wasn't requested a [IllegalArgumentException] will be thrown.
 */
@ExperimentalPermissionsApi
public fun MultiplePermissionsState.isDenied(permission: String): Boolean =
    requireNotNull(permissions.find { it.permission == permission }) {
        "$permission is not present in the list of requested permissions"
    }.status.isDenied

/**
 * Returns `true` if [permission] was permanently denied, otherwise `false`.
 *
 * If [permission] wasn't requested a [IllegalArgumentException] will be thrown.
 */
@ExperimentalPermissionsApi
public fun MultiplePermissionsState.isPermanentlyDenied(permission: String): Boolean =
    requireNotNull(permissions.find { it.permission == permission }) {
        "$permission is not present in the list of requested permissions"
    }.status.isPermanentlyDenied

@OptIn(ExperimentalPermissionsApi::class)
@Immutable
private class PreviewMultiplePermissionsState(
    permissions: List<String>
) : MultiplePermissionsState {
    override val permissions: List<PermissionState> = permissions.fastMap(::PreviewPermissionState)
    override val isNotRequested: Boolean = true
    override val allPermissionsGranted: Boolean = false
    override val allNotGrantedPermissionsArePermanentlyDenied: Boolean = false
    override val shouldShowRationale: Boolean = false

    override fun launchMultiplePermissionRequest() {}
    override fun openAppSettings() {}
}
