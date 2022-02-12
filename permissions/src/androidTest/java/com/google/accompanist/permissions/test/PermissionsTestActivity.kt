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

package com.google.accompanist.permissions.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
class PermissionsTestActivity : ComponentActivity() {

    var shouldShowRequestPermissionRationale: Map<String, Boolean> = emptyMap()

    override fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        if (permission in shouldShowRequestPermissionRationale.keys) {
            return shouldShowRequestPermissionRationale[permission]!!
        }
        return super.shouldShowRequestPermissionRationale(permission)
    }

    /**
     * Code used in `MultipleAndSinglePermissionsTest`
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column {
                Text("PermissionsTestActivity")

                val state = rememberPermissionState(android.Manifest.permission.CAMERA,)
                when (state.status) {
                    PermissionStatus.Granted -> {
                        Text("Granted")
                    }
                    is PermissionStatus.Denied -> {
                        val textToShow = if (state.status.shouldShowRationale) {
                            "ShowRationale"
                        } else {
                            "No permission"
                        }

                        Text(textToShow)
                        Button(onClick = { state.launchPermissionRequest() }) {
                            Text("Request")
                        }
                    }
                }
            }
        }
    }
}
