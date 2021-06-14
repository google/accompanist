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
    uiDevice.findPermissionButton(
        when (Build.VERSION.SDK_INT) {
            in 24..28 -> "ALLOW"
            else -> "Allow"
        }
    ).clickForPermission()

    // Or maybe this permission doesn't have the Allow option
    if (Build.VERSION.SDK_INT == 30) {
        uiDevice.findPermissionButton("While using the app").clickForPermission()
    }
}

internal fun denyPermissionInDialog(
    uiDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
) {
    uiDevice.findPermissionButton(
        when (Build.VERSION.SDK_INT) {
            in 24..28 -> "DENY"
            else -> "Deny"
        }
    ).clickForPermission()
}

internal fun doNotAskAgainPermissionInDialog(
    uiDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
) {
    when {
        Build.VERSION.SDK_INT == 30 -> {
            denyPermissionInDialog(uiDevice)
        }
        Build.VERSION.SDK_INT > 28 -> {
            uiDevice.findPermissionButton("Deny & don’t ask again").clickForPermission()
        }
        Build.VERSION.SDK_INT == 23 -> {
            uiDevice.findObject(UiSelector().text("Never ask again")).clickForPermission()
            denyPermissionInDialog(uiDevice)
        }
        else -> {
            uiDevice.findObject(UiSelector().text("Don't ask again")).clickForPermission()
            denyPermissionInDialog(uiDevice)
        }
    }
}

private fun UiDevice.findPermissionButton(text: String): UiObject =
    findObject(
        UiSelector()
            .textMatches(text)
            .clickable(true)
            .className("android.widget.Button")
    )

private fun UiObject.clickForPermission() {
    waitUntil { exists() }
    if (!exists()) return

    waitUntil { exists() && click() }
}

private fun waitUntil(timeoutMillis: Long = 1_000, condition: () -> Boolean) {
    val startTime = System.nanoTime()
    while (!condition()) {
        // Let Android run measure, draw and in general any other async operations.
        Thread.sleep(10)
        if (System.nanoTime() - startTime > timeoutMillis * 1_000_000) {
            break
        }
    }
}
