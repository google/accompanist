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

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.rememberPermissionState

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
            val state = rememberPermissionState(Manifest.permission.CAMERA)
            Column {
                Text("PermissionsTestActivity")
                Spacer(Modifier.height(16.dp))
                when {
                    state.hasPermission -> {
                        Text("Granted")
                    }
                    state.shouldShowRationale -> {
                        Column {
                            Text("ShowRationale")
                            Button(onClick = { state.launchPermissionRequest() }) {
                                Text("Request")
                            }
                        }
                    }
                    !state.permissionRequested -> {
                        Text("Requesting")
                        state.launchPermissionRequest()
                    }
                    else -> {
                        Text("Denied")
                    }
                }
            }
        }
    }
}
