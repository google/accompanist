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
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.io.ByteArrayOutputStream

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
    val button = uiDevice.findPermissionButton(
        when (sdkVersion) {
            in 24..28 -> "ALLOW"
            29 -> "Allow"
            else -> "While using the app"
        }
    )

    button.clickForPermission(instrumentation)
}

internal fun denyPermissionInDialog(
    instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
) {
    val text = when (Build.VERSION.SDK_INT) {
        in 24..28 -> "DENY"
        in 29..30 -> "Deny"
        else -> "t allow" // Different sdks and devices seem to have either ' or ’
    }
    val permissionButton = UiDevice.getInstance(instrumentation).findPermissionButton(text)
    permissionButton.clickForPermission(instrumentation)
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
            uiDevice.findPermissionButton("Never ask again")
                .clickForPermission(instrumentation)
            denyPermissionInDialog(instrumentation)
        }

        else -> {
            uiDevice.findPermissionButton(
                "Don't ask again"
            ).clickForPermission(instrumentation)
            denyPermissionInDialog(instrumentation)
        }
    }
}

private fun UiDevice.findPermissionButton(
    text: String
): UiObject2 {
    val selector = By
        .textContains(text)
        .clickable(true)

    val found = wait(Until.hasObject(selector), 3000)

    if (!found) {
        val output = ByteArrayOutputStream()
        dumpWindowHierarchy(output)
        println(output.toByteArray().decodeToString())

        error("Could not find button with text $text")
    }

    return findObject(selector)
}

private fun UiObject2.clickForPermission(
    instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
): Boolean {
    click()
    // Make sure that the tests waits for this click to be processed
    instrumentation.waitForIdleSync()
    return true
}
