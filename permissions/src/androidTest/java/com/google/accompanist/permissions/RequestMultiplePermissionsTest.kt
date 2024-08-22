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

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.filters.SdkSuppress
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalPermissionsApi::class)
@SdkSuppress(minSdkVersion = 27) // Flaky on 26
class RequestMultiplePermissionsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        composeTestRule.setContent { ComposableUnderTest() }
    }

    @Test
    fun permissionTest_grantPermissions() {
        composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        grantPermissionInDialog() // Grant first permission
        grantPermissionInDialog() // Grant second permission
        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    }

    @Test
    fun permissionTest_denyOnePermission() {
        composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        grantPermissionInDialog() // Grant first permission
        denyPermissionInDialog() // Deny second permission
        composeTestRule.onNodeWithText("ShowRationale").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        grantPermissionInDialog() // Grant second permission
        if (Build.VERSION.SDK_INT == 23) { // API 23 shows all permissions again
            grantPermissionInDialog()
        }

        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    }

    @Test
    fun permissionTest_doNotAskAgainPermission() {
        composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        grantPermissionInDialog() // Grant first permission
        denyPermissionInDialog() // Deny second permission
        composeTestRule.onNodeWithText("ShowRationale").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()

        if (Build.VERSION.SDK_INT == 23) { // API 23 shows all permissions again
            grantPermissionInDialog()
        }
        doNotAskAgainPermissionInDialog() // Do not ask again second permission

        composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
    }

    @Test
    fun permissionTest_grantInTheBackground() {
        composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        grantPermissionInDialog() // Grant first permission
        denyPermissionInDialog() // Deny second permission
        composeTestRule.onNodeWithText("ShowRationale").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()

        if (Build.VERSION.SDK_INT == 23) { // API 23 shows all permissions again
            grantPermissionInDialog()
        }
        doNotAskAgainPermissionInDialog() // Do not ask again second permission
        composeTestRule.onNodeWithText("No permission").assertIsDisplayed()

        // This simulates the user going to the Settings screen and granting both permissions.
        // This is cheating, I know, but the order in which the system request the permissions
        // is unpredictable. Therefore, we need to grant both to make this test deterministic.
        grantPermissionProgrammatically("android.permission.CAMERA")
        grantPermissionProgrammatically("android.permission.ACCESS_FINE_LOCATION")
        simulateAppComingFromTheBackground(composeTestRule)
        composeTestRule.activityRule.scenario.onActivity {
            it.setContent { ComposableUnderTest() }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    }

    @Composable
    private fun ComposableUnderTest() {
        val state = rememberMultiplePermissionsState(
            listOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.CAMERA
            )
        )
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
                Button(onClick = { state.launchMultiplePermissionRequest() }) {
                    Text("Request")
                }
            }
        }
    }
}
