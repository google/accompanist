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
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.sample.AccompanistSampleTheme

class RequestPermissionSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AccompanistSampleTheme {
                val cameraPermissionState = rememberPermissionState(
                    android.Manifest.permission.CAMERA
                )
                Sample(
                    cameraPermissionState,
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

@Composable
private fun Sample(
    cameraPermissionState: PermissionState,
    navigateToSettingsScreen: () -> Unit
) {
    var doNotShowRationale by rememberSaveable { mutableStateOf(false) }

    when {
        cameraPermissionState.hasPermission -> {
            Text("Camera permission Granted")
        }
        cameraPermissionState.shouldShowRationale -> {
            if (doNotShowRationale) {
                Text("Feature not available")
            } else {
                Column {
                    Text("The camera is important for this app. Please grant the permission.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                            Text("Request permission")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { doNotShowRationale = true }) {
                            Text("Don't show rationale again")
                        }
                    }
                }
            }
        }
        !cameraPermissionState.permissionRequested -> {
            cameraPermissionState.launchPermissionRequest()
        }
        else -> {
            Column {
                Text(
                    "Camera permission denied. See this FAQ with information about why we " +
                        "need this permission. Please, grant us access on the Settings screen."
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = navigateToSettingsScreen) {
                    Text("Open Settings")
                }
            }
        }
    }
}
