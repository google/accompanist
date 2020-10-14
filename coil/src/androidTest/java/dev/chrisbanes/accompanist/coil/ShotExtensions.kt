/*
 * Copyright 2020 The Android Open Source Project
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

package dev.chrisbanes.accompanist.coil

import androidx.ui.test.ComposeTestRule
import androidx.ui.test.SemanticsNodeInteraction
import androidx.ui.test.onRoot
import com.facebook.testing.screenshot.internal.TestNameDetector
import com.karumi.shot.ScreenshotTest
import com.karumi.shot.compose.ComposeScreenshotRunner
import com.karumi.shot.compose.ScreenshotMetadata

fun ScreenshotTest.compareScreenshotWithSuffix(
    rule: ComposeTestRule,
    suffix: String
) = compareScreenshotWithSuffix(rule.onRoot(), suffix = suffix)

fun ScreenshotTest.compareScreenshotWithSuffix(
    node: SemanticsNodeInteraction,
    suffix: String
) {
    disableFlakyComponentsAndWaitForIdle()

    val testClassName = TestNameDetector.getTestClass()
    val testName = TestNameDetector.getTestName()
    val screenshotName = "${testClassName}_$testName$suffix"
    val data = ScreenshotMetadata(
        name = screenshotName,
        testClassName = testClassName,
        testName = testName
    )
    ComposeScreenshotRunner.composeScreenshot.saveScreenshot(node, data)
}
