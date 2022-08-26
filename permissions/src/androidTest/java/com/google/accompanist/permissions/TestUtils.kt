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
    instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
) {
    val uiDevice = UiDevice.getInstance(instrumentation)
    val sdkVersion = Build.VERSION.SDK_INT
    val clicked = uiDevice.findPermissionButton(
        when (sdkVersion) {
            in 24..28 -> "ALLOW"
            else -> "Allow"
        }
    ).clickForPermission(instrumentation)

    // Or maybe this permission doesn't have the Allow option
    if (!clicked && sdkVersion > 28) {
        uiDevice.findPermissionButton(
            when (sdkVersion) {
                29 -> "Allow only while using the app"
                else -> "While using the app"
            }
        ).clickForPermission(instrumentation)
    }
}

internal fun denyPermissionInDialog(
    instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
) {
    val text = when (Build.VERSION.SDK_INT) {
        in 24..28 -> "DENY"
        in 29..30 -> "Deny"
        else -> "Don’t allow"
    }
    val permissionButton = UiDevice.getInstance(instrumentation).findPermissionButton(text)
    assert(permissionButton.clickForPermission(instrumentation)) { "Could not deny permission" }
}

internal fun doNotAskAgainPermissionInDialog(
    instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
) {
    val uiDevice = UiDevice.getInstance(instrumentation)
    when {
        Build.VERSION.SDK_INT >= 30 -> {
            denyPermissionInDialog(instrumentation)
        }
        Build.VERSION.SDK_INT > 28 -> {
            uiDevice
                .findPermissionButton("Deny & don’t ask again")
                .clickForPermission(instrumentation)
        }
        Build.VERSION.SDK_INT == 23 -> {
            uiDevice.findObject(
                UiSelector().text("Never ask again")
            ).clickForPermission(instrumentation)
            denyPermissionInDialog(instrumentation)
        }
        else -> {
            uiDevice.findObject(
                UiSelector().text("Don't ask again")
            ).clickForPermission(instrumentation)
            denyPermissionInDialog(instrumentation)
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

private fun UiObject.clickForPermission(
    instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
): Boolean {
    waitUntil { exists() }
    if (!exists()) return false

    val clicked = waitUntil { exists() && click() }
    // Make sure that the tests waits for this click to be processed
    if (clicked) { instrumentation.waitForIdleSync() }
    return clicked
}

private fun waitUntil(timeoutMillis: Long = 2_000, condition: () -> Boolean): Boolean {
    val startTime = System.nanoTime()
    while (true) {
        if (condition()) return true
        // Let Android run measure, draw and in general any other async operations.
        Thread.sleep(10)
        if (System.nanoTime() - startTime > timeoutMillis * 1_000_000) {
            return false
        }
    }
}
