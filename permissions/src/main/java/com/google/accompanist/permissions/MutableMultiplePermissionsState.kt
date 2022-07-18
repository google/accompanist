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

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

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
internal fun rememberMutableMultiplePermissionsState(
    permissions: List<String>,
    onPermissionsResult: (Map<String, Boolean>) -> Unit = {}
): MultiplePermissionsState {
    // Create mutable permissions that can be requested individually
    val mutablePermissions = rememberMutablePermissionsState(permissions)
    // Refresh permissions when the lifecycle is resumed.
    PermissionsLifecycleCheckerEffect(mutablePermissions)

    val multiplePermissionsState = remember(permissions) {
        MutableMultiplePermissionsState(mutablePermissions)
    }

    // Remember RequestMultiplePermissions launcher and assign it to multiplePermissionsState
    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        multiplePermissionsState.updatePermissionsStatus(permissionsResult)
        onPermissionsResult(permissionsResult)
    }

    // Remember Application detail setting launcher and assign it to multiplePermissionsState
    val appDetailSettingLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            multiplePermissionsState.updatePermissionStatus(permissions)
            val permissionResult = mutableMapOf<String, Boolean>().apply {
                multiplePermissionsState.permissions.forEach {
                    put(it.permission, it.status.isGranted)
                }
            }
            onPermissionsResult(permissionResult)
        }

    DisposableEffect(multiplePermissionsState, requestPermissionsLauncher) {
        multiplePermissionsState.requestPermissionsLauncher = requestPermissionsLauncher
        onDispose {
            multiplePermissionsState.requestPermissionsLauncher = null
        }
    }
    DisposableEffect(multiplePermissionsState, appDetailSettingLauncher) {
        multiplePermissionsState.appDetailSettingLauncher = appDetailSettingLauncher
        onDispose {
            multiplePermissionsState.appDetailSettingLauncher = null
        }
    }

    return multiplePermissionsState
}

@ExperimentalPermissionsApi
@Composable
private fun rememberMutablePermissionsState(
    permissions: List<String>
): List<MutablePermissionState> {
    // Create list of MutablePermissionState for each permission
    val context = LocalContext.current
    val activity = context.findActivity()
    val mutablePermissions: List<MutablePermissionState> = remember(permissions) {
        permissions.map { MutablePermissionState(it, context, activity) }
    }
    // Update each permission with its own launcher
    for (permissionState in mutablePermissions) {
        key(permissionState.permission) {
            // Remember RequestPermission launcher and assign it to the permissionState
            val requestPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {
                permissionState.refreshPermissionStatus()
            }

            // Remember Application detail setting launcher and assign it to permissionState
            val appDetailSettingLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    permissionState.refreshPermissionStatus()
                }

            DisposableEffect(requestPermissionLauncher) {
                permissionState.requestPermissionLauncher = requestPermissionLauncher
                onDispose {
                    permissionState.requestPermissionLauncher = null
                }
            }
            DisposableEffect(permissionState, appDetailSettingLauncher) {
                permissionState.appDetailSettingLauncher = appDetailSettingLauncher
                onDispose {
                    permissionState.appDetailSettingLauncher = null
                }
            }
        }
    }

    return mutablePermissions
}

/**
 * A state object that can be hoisted to control and observe multiple permission status changes.
 *
 * In most cases, this will be created via [rememberMutableMultiplePermissionsState].
 *
 * @param mutablePermissions list of mutable permissions to control and observe.
 */
@ExperimentalPermissionsApi
@Stable
internal class MutableMultiplePermissionsState(
    private val mutablePermissions: List<MutablePermissionState>
) : MultiplePermissionsState {

    override val permissions: List<PermissionState> = mutablePermissions

    override val revokedPermissions: List<PermissionState> by derivedStateOf {
        permissions.filter { it.status != PermissionStatus.Granted }
    }

    override val allPermissionsGranted: Boolean by derivedStateOf {
        permissions.all { it.status.isGranted } || // Up to date when the lifecycle is resumed
            revokedPermissions.isEmpty() // Up to date when the user launches the action
    }

    override val shouldShowRationale: Boolean by derivedStateOf {
        permissions.any {
            it.status.shouldShowRationale
        }
    }

    override fun launchMultiplePermissionRequest() {
        requestPermissionsLauncher?.launch(
            permissions.map { it.permission }.toTypedArray()
        ) ?: throw IllegalStateException("ActivityResultLauncher cannot be null")
    }

    override fun launchAppDetailSetting(context: Context) {
        appDetailSettingLauncher?.launch(context.createAppDetailSettingIntent())
    }

    internal var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>? = null
    internal var appDetailSettingLauncher: ActivityResultLauncher<Intent>? = null


    internal fun updatePermissionsStatus(permissionsStatus: Map<String, Boolean>) {
        // Update all permissions with the result
        for (permission in permissionsStatus.keys) {
            mutablePermissions.firstOrNull { it.permission == permission }?.apply {
                permissionsStatus[permission]?.let {
                    this.refreshPermissionStatus()
                }
            }
        }
    }

    internal fun updatePermissionStatus(permissions: List<String>) {
        // Update all permissions with the result
        for (permission in permissions) {
            mutablePermissions.firstOrNull { it.permission == permission }?.apply {
                this.refreshPermissionStatus()
            }
        }
    }
}
