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

import android.app.Instrumentation
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector

internal fun <T : ComponentActivity> simulateAppComingFromTheBackground(
    composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<T>, T>
) {
    // Recreate Activity and set content again to check for the permission again
    composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
    // A weird dialog appears when moving to the STARTED state
    UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()
}

internal fun grantPermissionProgrammatically(
    permission: String,
    instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
) {
    if (Build.VERSION.SDK_INT < 28) {
        val fileDescriptor = instrumentation.uiAutomation.executeShellCommand(
            "pm grant ${instrumentation.targetContext.packageName} $permission"
        )
        fileDescriptor.checkError()
        fileDescriptor.close()
    } else {
        instrumentation.uiAutomation.grantRuntimePermission(
            instrumentation.targetContext.packageName, permission
        )
    }
}

internal fun grantPermissionInDialog(
    uiDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
) {
    val allowButton = uiDevice.findObject(
        UiSelector().text(
            when {
                Build.VERSION.SDK_INT <= 28 && Build.VERSION.SDK_INT != 23 -> "ALLOW"
                else -> "Allow"
            }
        )
    )
    if (allowButton.exists()) { allowButton.click() }

    // Or maybe this permission doesn't have the Allow option
    if (Build.VERSION.SDK_INT == 30) {
        val whileUsingTheAppButton = uiDevice.findObject(
            UiSelector().text("While using the app")
        )
        if (whileUsingTheAppButton.exists()) { whileUsingTheAppButton.click() }
    }
}

internal fun denyPermissionInDialog(
    uiDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
) {
    val denyButton = uiDevice.findObject(
        UiSelector().text(
            when {
                Build.VERSION.SDK_INT <= 28 && Build.VERSION.SDK_INT != 23 -> "DENY"
                else -> "Deny"
            }
        )
    )
    if (denyButton.exists()) { denyButton.click() }
}

internal fun doNotAskAgainPermissionInDialog(
    uiDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
) {
    when {
        Build.VERSION.SDK_INT == 30 -> {
            denyPermissionInDialog(uiDevice)
        }
        Build.VERSION.SDK_INT > 28 -> {
            val denyAndDoNotAskAgainButton = uiDevice.findObject(
                UiSelector().text(
                    "Deny & don’t ask again"
                )
            )
            if (denyAndDoNotAskAgainButton.exists()) {
                denyAndDoNotAskAgainButton.click()
            }
        }
        Build.VERSION.SDK_INT == 23 -> {
            val doNotAskAgainCheckbox = uiDevice.findObject(
                UiSelector().text(
                    "Never ask again"
                )
            )
            if (doNotAskAgainCheckbox.exists()) {
                doNotAskAgainCheckbox.click()
            }
            denyPermissionInDialog(uiDevice)
        }
        else -> {
            val doNotAskAgainCheckbox = uiDevice.findObject(
                UiSelector().text(
                    "Don't ask again"
                )
            )
            if (doNotAskAgainCheckbox.exists()) {
                doNotAskAgainCheckbox.click()
            }
            denyPermissionInDialog(uiDevice)
        }
    }
}
