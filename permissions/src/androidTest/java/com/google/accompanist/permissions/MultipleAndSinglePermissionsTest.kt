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

import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.filters.FlakyTest
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.accompanist.permissions.test.PermissionsTestActivity
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalPermissionsApi::class)
@FlakyTest(detail = "https://github.com/google/accompanist/issues/490")
@SdkSuppress(minSdkVersion = 23)
class MultipleAndSinglePermissionsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val instrumentation by lazy {
        InstrumentationRegistry.getInstrumentation()
    }

    private val uiDevice by lazy {
        UiDevice.getInstance(instrumentation)
    }

    @Test
    fun singlePermission_granted() {
        composeTestRule.setContent {
            ComposableUnderTest(listOf(android.Manifest.permission.CAMERA))
        }

        composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
        composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        grantPermissionInDialog()
        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
        composeTestRule.onNodeWithText("Navigate").performClick()
        instrumentation.waitForIdleSync()
        composeTestRule.onNodeWithText("PermissionsTestActivity").assertIsDisplayed()
        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    }

    @Test
    fun singlePermission_deniedAndGrantedInSecondActivity() {
        composeTestRule.setContent {
            ComposableUnderTest(listOf(android.Manifest.permission.CAMERA))
        }

        composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
        composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        denyPermissionInDialog()
        composeTestRule.onNodeWithText("ShowRationale").assertIsDisplayed()
        composeTestRule.onNodeWithText("Navigate").performClick()
        instrumentation.waitForIdleSync()
        composeTestRule.onNodeWithText("PermissionsTestActivity").assertIsDisplayed()
        composeTestRule.onNodeWithText("ShowRationale").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        grantPermissionInDialog()
        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
        uiDevice.pressBack()
        instrumentation.waitForIdleSync()
        composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    }

    @Test
    fun singlePermission_deniedAndGrantedInFirstActivity() {
        composeTestRule.setContent {
            ComposableUnderTest(listOf(android.Manifest.permission.CAMERA))
        }

        composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
        composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        denyPermissionInDialog()
        composeTestRule.onNodeWithText("ShowRationale").assertIsDisplayed()
        composeTestRule.onNodeWithText("Navigate").performClick()
        instrumentation.waitForIdleSync()
        composeTestRule.onNodeWithText("PermissionsTestActivity").assertIsDisplayed()
        composeTestRule.onNodeWithText("ShowRationale").assertIsDisplayed()
        uiDevice.pressBack()
        instrumentation.waitForIdleSync()
        composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        grantPermissionInDialog()
        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
        composeTestRule.onNodeWithText("Navigate").performClick()
        instrumentation.waitForIdleSync()
        composeTestRule.onNodeWithText("PermissionsTestActivity").assertIsDisplayed()
        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    }

    @Test
    fun singlePermission_deniedAndGrantedInFirstActivity_singlePermission() {
        composeTestRule.setContent {
            ComposableUnderTest(
                listOf(android.Manifest.permission.CAMERA),
                requestSinglePermission = true
            )
        }

        composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
        composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        denyPermissionInDialog()
        composeTestRule.onNodeWithText("ShowRationale").assertIsDisplayed()
        composeTestRule.onNodeWithText("Navigate").performClick()
        instrumentation.waitForIdleSync()
        composeTestRule.onNodeWithText("PermissionsTestActivity").assertIsDisplayed()
        composeTestRule.onNodeWithText("ShowRationale").assertIsDisplayed()
        uiDevice.pressBack()
        instrumentation.waitForIdleSync()
        composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        grantPermissionInDialog()
        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
        composeTestRule.onNodeWithText("Navigate").performClick()
        instrumentation.waitForIdleSync()
        composeTestRule.onNodeWithText("PermissionsTestActivity").assertIsDisplayed()
        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    }

    @Test
    fun multiplePermissions_granted() {
        composeTestRule.setContent {
            ComposableUnderTest(
                listOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.CAMERA
                )
            )
        }

        composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
        composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        grantPermissionInDialog() // Grant first permission
        grantPermissionInDialog() // Grant second permission
        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
        composeTestRule.onNodeWithText("Navigate").performClick()
        instrumentation.waitForIdleSync()
        composeTestRule.onNodeWithText("PermissionsTestActivity").assertIsDisplayed()
        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    }

    @Test
    fun multiplePermissions_denied() {
        composeTestRule.setContent {
            ComposableUnderTest(
                listOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.CAMERA
                )
            )
        }

        composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
        composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        denyPermissionInDialog() // Deny first permission
        denyPermissionInDialog() // Deny second permission
        composeTestRule.onNodeWithText("ShowRationale").assertIsDisplayed()
        composeTestRule.onNodeWithText("Navigate").performClick()
        instrumentation.waitForIdleSync()
        composeTestRule.onNodeWithText("PermissionsTestActivity").assertIsDisplayed()
        composeTestRule.onNodeWithText("ShowRationale").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        grantPermissionInDialog() // Grant the permission
        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
        uiDevice.pressBack()
        instrumentation.waitForIdleSync()
        composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        grantPermissionInDialog() // only one permission to grant now
        if (Build.VERSION.SDK_INT == 23) { // API 23 shows all permissions again
            grantPermissionInDialog()
        }

        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    }

    @Composable
    private fun ComposableUnderTest(
        permissions: List<String>,
        requestSinglePermission: Boolean = false
    ) {
        val state = rememberMultiplePermissionsState(permissions)
        Column {
            Text("MultipleAndSinglePermissionsTest")
            Spacer(Modifier.height(16.dp))
            if (state.allPermissionsGranted) {
                Text("Granted")
            } else {
                Column {
                    val textToShow = if (state.shouldShowRationale) {
                        "ShowRationale"
                    } else {
                        "No permission"
                    }

                    Text(textToShow)
                    Button(
                        onClick = {
                            if (requestSinglePermission && state.revokedPermissions.size == 1) {
                                state.revokedPermissions[0].launchPermissionRequest()
                            } else {
                                state.launchMultiplePermissionRequest()
                            }
                        }
                    ) {
                        Text("Request")
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    composeTestRule.activity.startActivity(
                        Intent(composeTestRule.activity, PermissionsTestActivity::class.java)
                    )
                }
            ) {
                Text("Navigate")
            }
        }
    }
}
