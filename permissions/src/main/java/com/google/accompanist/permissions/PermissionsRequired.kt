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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * Composable that exercises the permissions flows as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 *
 * @param permissionState required permission to be granted.
 * @param permissionGrantedContent content to show when the permission is granted.
 * @param permissionRationaleContent content to show when the user needs to be presented with
 * a rationale for the required permission.
 * @param doNotAskAgainPermissionContent content to show when the user denied the permission.
 * @param requestingPermissionContent content to show while the permission is being requested. This
 * will be rendered below the system permission dialog.
 */
@ExperimentalPermissionsApi
@Composable
fun PermissionRequired(
    permissionState: PermissionState,
    permissionGrantedContent: @Composable (() -> Unit),
    permissionRationaleContent: @Composable (() -> Unit),
    doNotAskAgainPermissionContent: @Composable (() -> Unit),
    requestingPermissionContent: @Composable (() -> Unit)? = null
) {
    var launchPermissionRequest by rememberSaveable { mutableStateOf(false) }

    when {
        permissionState.hasPermission -> {
            permissionGrantedContent()
        }
        permissionState.shouldShowRationale -> {
            permissionRationaleContent()
        }
        !permissionState.permissionRequested -> {
            if (requestingPermissionContent != null) {
                requestingPermissionContent()
            }
            launchPermissionRequest = true
        }
        else -> {
            doNotAskAgainPermissionContent()
        }
    }

    if (launchPermissionRequest) {
        LaunchedEffect(permissionState) {
            permissionState.launchPermissionRequest()
            launchPermissionRequest = false
        }
    }
}

/**
 * Composable that exercises the permissions flows as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 *
 * @param multiplePermissionsState required permissions to be granted.
 * @param permissionsGrantedContent content to show when the permissions are granted.
 * @param permissionsRationaleContent content to show when the user needs to be presented with
 * a rationale for the required permissions.
 * @param doNotAskAgainPermissionsContent content to show when the user denied the permissions.
 * @param requestingPermissionsContent content to show while the permissions are being requested.
 * This will be rendered below the system permission dialog.
 */
@ExperimentalPermissionsApi
@Composable
fun PermissionsRequired(
    multiplePermissionsState: MultiplePermissionsState,
    permissionsGrantedContent: @Composable (() -> Unit),
    permissionsRationaleContent: @Composable (() -> Unit),
    doNotAskAgainPermissionsContent: @Composable (() -> Unit),
    requestingPermissionsContent: @Composable (() -> Unit)? = null
) {
    var launchPermissionRequest by rememberSaveable { mutableStateOf(false) }

    when {
        multiplePermissionsState.allPermissionsGranted -> {
            permissionsGrantedContent()
        }
        multiplePermissionsState.shouldShowRationale -> {
            permissionsRationaleContent()
        }
        !multiplePermissionsState.permissionRequested -> {
            if (requestingPermissionsContent != null) {
                requestingPermissionsContent()
            }
            launchPermissionRequest = true
        }
        else -> {
            doNotAskAgainPermissionsContent()
        }
    }

    if (launchPermissionRequest) {
        LaunchedEffect(multiplePermissionsState) {
            multiplePermissionsState.launchMultiplePermissionRequest()
            launchPermissionRequest = false
        }
    }
}
