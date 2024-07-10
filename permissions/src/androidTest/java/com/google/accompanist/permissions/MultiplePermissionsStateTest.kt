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
class MultiplePermissionsStateTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<EmptyPermissionsTestActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        "android.permission.CAMERA",
        "android.permission.ACCESS_FINE_LOCATION"
    )

    @Test
    fun permissionState_hasPermission() {
        composeTestRule.setContent {
            val state = rememberMultiplePermissionsState(
                listOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.CAMERA
                )
            )

            assertThat(state.allPermissionsGranted).isTrue()
            assertThat(state.shouldShowRationale).isFalse()
        }
    }

    @Test
    fun permissionTest_shouldShowRationale() {
        composeTestRule.activity.shouldShowRequestPermissionRationale = mapOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE to true
        )

        composeTestRule.setContent {
            val state = rememberMultiplePermissionsState(
                listOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.CAMERA
                )
            )

            assertThat(state.allPermissionsGranted).isFalse()
            assertThat(state.shouldShowRationale).isTrue()
            assertThat(state.permissions).hasSize(3)
            assertThat(state.revokedPermissions).hasSize(1)
            assertThat(state.revokedPermissions[0].permission)
                .isEqualTo("android.permission.WRITE_EXTERNAL_STORAGE")
        }
    }
}
