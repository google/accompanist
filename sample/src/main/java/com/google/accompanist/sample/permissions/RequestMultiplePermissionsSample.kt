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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.sample.AccompanistSampleTheme

class RequestMultiplePermissionsSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AccompanistSampleTheme {
                val multiplePermissionsState =
                    activityResultRegistry.rememberMultiplePermissionsState(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.CAMERA,
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

@Composable
private fun Sample(
    multiplePermissionsState: MultiplePermissionsState,
    navigateToSettingsScreen: () -> Unit
) {
    when {
        multiplePermissionsState.allPermissionsGranted -> {
            Text("Camera and Read storage permissions Granted! Thank you!")
        }
        multiplePermissionsState.shouldShowRationale -> {
            Column {
                Text(
                    "The camera and reading your storage features are important. " +
                        "Please grant all the permissions."
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { multiplePermissionsState.launchMultiplePermissionRequest() }) {
                    Text("Request permissions")
                }
            }
        }
        !multiplePermissionsState.permissionRequested -> {
            multiplePermissionsState.launchMultiplePermissionRequest()
        }
        else -> {
            Column {
                Text(
                    "Camera and read storage permissions denied. See this FAQ with " +
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
