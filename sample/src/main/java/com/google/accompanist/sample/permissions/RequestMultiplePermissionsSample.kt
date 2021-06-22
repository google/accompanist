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

package com.google.accompanist.sample.permissions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.sample.AccompanistSampleTheme

@OptIn(ExperimentalPermissionsApi::class)
class RequestMultiplePermissionsSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AccompanistSampleTheme {
                val multiplePermissionsState = rememberMultiplePermissionsState(
                    listOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.CAMERA,
                    )
                )
                Sample(
                    multiplePermissionsState,
                    navigateToSettingsScreen = {
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", packageName, null)
                            )
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun Sample(
    multiplePermissionsState: MultiplePermissionsState,
    navigateToSettingsScreen: () -> Unit
) {
    // Track if the user doesn't want to see the rationale any more.
    var doNotShowRationale by rememberSaveable { mutableStateOf(false) }

    when {
        // If all permissions are granted, then show screen with the feature enabled
        multiplePermissionsState.allPermissionsGranted -> {
            Text("Camera and Read storage permissions Granted! Thank you!")
        }
        // If the user denied any permission but a rationale should be shown, or the user sees
        // the permissions for the first time, explain why the feature is needed by the app and
        // allow the user decide if they don't want to see the rationale any more.
        multiplePermissionsState.shouldShowRationale ||
            !multiplePermissionsState.permissionRequested ->
            {
                if (doNotShowRationale) {
                    Text("Feature not available")
                } else {
                    Column {
                        val revokedPermissionsText = getPermissionsText(
                            multiplePermissionsState.revokedPermissions
                        )
                        Text(
                            "$revokedPermissionsText important. " +
                                "Please grant all of them for the app to function properly."
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Button(
                                onClick = {
                                    multiplePermissionsState.launchMultiplePermissionRequest()
                                }
                            ) {
                                Text("Request permissions")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { doNotShowRationale = true }) {
                                Text("Don't show rationale again")
                            }
                        }
                    }
                }
            }
        // If the criteria above hasn't been met, the user denied some permission. Let's present
        // the user with a FAQ in case they want to know more and send them to the Settings screen
        // to enable them the future there if they want to.
        else -> {
            Column {
                val revokedPermissionsText = getPermissionsText(
                    multiplePermissionsState.revokedPermissions
                )
                Text(
                    "$revokedPermissionsText denied. See this FAQ with " +
                        "information about why we need this permission. Please, grant us " +
                        "access on the Settings screen."
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = navigateToSettingsScreen) {
                    Text("Open Settings")
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun getPermissionsText(permissions: List<PermissionState>): String {
    val revokedPermissionsSize = permissions.size
    if (revokedPermissionsSize == 0) return ""

    val textToShow = StringBuilder().apply {
        append("The ")
    }

    for (i in permissions.indices) {
        textToShow.append(permissions[i].permission)
        when {
            revokedPermissionsSize > 1 && i == revokedPermissionsSize - 2 -> {
                textToShow.append(", and ")
            }
            i == revokedPermissionsSize - 1 -> {
                textToShow.append(" ")
            }
            else -> {
                textToShow.append(", ")
            }
        }
    }
    textToShow.append(if (revokedPermissionsSize == 1) "permission is" else "permissions are")
    return textToShow.toString()
}
