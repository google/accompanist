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
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector

internal fun <T : ComponentActivity> simulateAppComingFromTheBackground(
    composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<T>, T>
) {
    // Make Activity go through ON_START, and ON_RESUME
    composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
    composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
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
            when (Build.VERSION.SDK_INT) {
                in 24..28 -> "ALLOW"
                else -> "Allow"
            }
        )
    )
    allowButton.clickForPermission()

    // Or maybe this permission doesn't have the Allow option
    if (Build.VERSION.SDK_INT == 30) {
        val whileUsingTheAppButton = uiDevice.findObject(
            UiSelector().text("While using the app")
        )
        whileUsingTheAppButton.clickForPermission()
    }
}

internal fun denyPermissionInDialog(
    uiDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
) {
    val denyButton = uiDevice.findObject(
        UiSelector().text(
            when (Build.VERSION.SDK_INT) {
                in 24..28 -> "DENY"
                else -> "Deny"
            }
        )
    )
    denyButton.clickForPermission()
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
            denyAndDoNotAskAgainButton.clickForPermission()
        }
        Build.VERSION.SDK_INT == 23 -> {
            val doNotAskAgainCheckbox = uiDevice.findObject(
                UiSelector().text(
                    "Never ask again"
                )
            )
            doNotAskAgainCheckbox.clickForPermission()
            denyPermissionInDialog(uiDevice)
        }
        else -> {
            val doNotAskAgainCheckbox = uiDevice.findObject(
                UiSelector().text(
                    "Don't ask again"
                )
            )
            doNotAskAgainCheckbox.clickForPermission()
            denyPermissionInDialog(uiDevice)
        }
    }
}

private fun UiObject.clickForPermission() {
    var objectExists = false
    var timesRetried = 0
    while (!objectExists && timesRetried < 3) {
        objectExists = exists()
        if (!objectExists) {
            Thread.sleep(250)
            timesRetried++
        }
    }
    if (!objectExists) return

    var clicked = false
    timesRetried = 0
    while (exists() && !clicked && timesRetried < 3) {
        clicked = click()
        if (!clicked) {
            Thread.sleep(250)
            timesRetried++
        }
    }
}
