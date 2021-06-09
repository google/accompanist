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
import com.google.accompanist.permissions.test.PermissionsTestActivity
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

/**
 * Test that proves the data comes from the right place
 */
@SdkSuppress(minSdkVersion = 23)
class PermissionStateTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<PermissionsTestActivity>()

    @get:Rule
    val permissionRule = GrantPermissionRule.grant("android.permission.CAMERA")!!

    @Test
    fun permissionState_hasPermission() {
        composeTestRule.setContent {
            val state =
                composeTestRule.activity.activityResultRegistry.rememberPermissionState(
                    android.Manifest.permission.CAMERA
                )
            assertThat(state.hasPermission).isTrue()
            assertThat(state.shouldShowRationale).isFalse()
        }
    }

    @Test
    fun permissionTest_shouldShowRationale() {
        val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        composeTestRule.activity.shouldShowRequestPermissionRationale = mapOf(
            permission to true
        )

        composeTestRule.setContent {
            val state =
                composeTestRule.activity.activityResultRegistry.rememberPermissionState(permission)

            assertThat(state.hasPermission).isFalse()
            assertThat(state.shouldShowRationale).isTrue()
        }
    }
}
