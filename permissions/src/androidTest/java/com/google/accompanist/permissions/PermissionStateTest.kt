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

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.filters.SdkSuppress
import androidx.test.rule.GrantPermissionRule
import com.google.accompanist.permissions.test.EmptyPermissionsTestActivity
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

/**
 * Simple tests that prove the data comes from the right place
 */
@OptIn(ExperimentalPermissionsApi::class)
@SdkSuppress(minSdkVersion = 23)
class PermissionStateTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<EmptyPermissionsTestActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant("android.permission.CAMERA")

    @Test
    fun permissionState_hasPermission() {
        composeTestRule.setContent {
            val state = rememberPermissionState(android.Manifest.permission.CAMERA)
            assertThat(state.status.isGranted).isTrue()
            assertThat(state.status.shouldShowRationale).isFalse()
        }
    }

    @Test
    fun permissionTest_shouldShowRationale() {
        val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
        composeTestRule.activity.shouldShowRequestPermissionRationale = mapOf(
            permission to true
        )

        composeTestRule.setContent {
            val state = rememberPermissionState(permission)

            assertThat(state.status.isGranted).isFalse()
            assertThat(state.status.shouldShowRationale).isTrue()
        }
    }
}
