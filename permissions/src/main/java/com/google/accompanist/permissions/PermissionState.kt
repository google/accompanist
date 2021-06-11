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

import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

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
    activityResultRegistry: ActivityResultRegistry,
    permission: String
): PermissionState {
    val mutablePermissionState = rememberMutablePermissionState(permission)
    return rememberPermissionState(
        activityResultRegistry = activityResultRegistry,
        mutablePermissionState = mutablePermissionState,
    )
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
    activityResultRegistry: ActivityResultRegistry,
    mutablePermissionState: MutablePermissionState,
): PermissionState {
    val launcher = activityResultRegistry.rememberActivityResultLauncher(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        mutablePermissionState.permissionRequested = true
        mutablePermissionState.hasPermission = granted
        mutablePermissionState.refreshShouldShowRationaleState()
    }
    // Update the state to the current launcher
    mutablePermissionState.launcher = launcher
    return mutablePermissionState
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
 */
@Stable
interface PermissionState {

    val permission: String

    /**
     * When `true`, the user has granted the [permission].
     */
    val hasPermission: Boolean

    /**
     * When `true`, the user should be presented with a rationale.
     */
    val shouldShowRationale: Boolean

    /**
     * When `true`, the [permission] request has been done previously.
     */
    val permissionRequested: Boolean

    /**
     * Request the [permission] to the user.
     *
     * This triggers a system dialog that asks the user to grant or revoke the permission.
     * Note that this dialog might not appear on the screen if the user doesn't want to be asked
     * again or has denied the permission multiple times.
     * This behavior varies depending on the Android level API.
     */
    fun launchPermissionRequest()
}
