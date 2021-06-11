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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@SdkSuppress(minSdkVersion = 23)
class RequestPermissionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        composeTestRule.setContent { ComposableUnderTest() }
    }

    @Test
    fun permissionTest_grantPermission() {
        grantPermissionInDialog()
        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    }

    @Test
    fun permissionTest_denyPermission() {
        denyPermissionInDialog()
        composeTestRule.onNodeWithText("ShowRationale").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        grantPermissionInDialog()
        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    }

    @Test
    fun permissionTest_doNotAskAgainPermission() {
        denyPermissionInDialog()
        composeTestRule.onNodeWithText("ShowRationale").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        doNotAskAgainPermissionInDialog()
        composeTestRule.onNodeWithText("Denied").assertIsDisplayed()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun permissionTest_grantInTheBackground() {
        denyPermissionInDialog()
        composeTestRule.onNodeWithText("ShowRationale").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").performClick()
        doNotAskAgainPermissionInDialog()
        composeTestRule.onNodeWithText("Denied").assertIsDisplayed()

        // This simulates the user going to the Settings screen and granting the permission
        grantPermissionProgrammatically("android.permission.CAMERA")
        simulateAppComingFromTheBackground(composeTestRule)
        composeTestRule.activityRule.scenario.onActivity {
            it.setContent { ComposableUnderTest() }
        }

        composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    }

    @Composable
    private fun ComposableUnderTest() {
        val state = rememberPermissionState(android.Manifest.permission.CAMERA)
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
