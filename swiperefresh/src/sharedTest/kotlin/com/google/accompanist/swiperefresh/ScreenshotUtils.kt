/*
 * Copyright 2022 The Android Open Source Project
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

package com.google.accompanist.swiperefresh

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasContentDescription
import androidx.test.platform.app.InstrumentationRegistry
import java.io.FileOutputStream

/**
 * A Util function to generate golden images used to compare screenshots token during tests.
 *
 * This method can be useful if your device doesn't have the same DPI as the one used to
 * generate the initial golden images present in `sharedTest/assets`.
 */
fun SwipeRefreshTest.generateGoldenImages() {
    var progress: Float? by mutableStateOf(0F)
    rule.setContent {
        SwipeRefreshTestContent(rememberSwipeRefreshState(true, progress)) {}
    }

    val picturesDirectory =
        InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(
            Environment.DIRECTORY_PICTURES
        )!!.canonicalPath

    val progressIndicatorNode =
        rule.onNode(hasContentDescription("CircularProgressIndicator_ForTest"))

    var bitmap = progressIndicatorNode.captureToImage().asAndroidBitmap()
    saveScreenshot("golden_0-00.png", bitmap, picturesDirectory)

    progress = 0.25F
    bitmap = progressIndicatorNode.captureToImage().asAndroidBitmap()
    saveScreenshot("golden_0-25.png", bitmap, picturesDirectory)

    progress = 0.50F
    bitmap = progressIndicatorNode.captureToImage().asAndroidBitmap()
    saveScreenshot("golden_0-50.png", bitmap, picturesDirectory)

    progress = 0.75F
    bitmap = progressIndicatorNode.captureToImage().asAndroidBitmap()
    saveScreenshot("golden_0-75.png", bitmap, picturesDirectory)

    progress = 1F
    bitmap = progressIndicatorNode.captureToImage().asAndroidBitmap()
    saveScreenshot("golden_1-00.png", bitmap, picturesDirectory)
}

/**
 * Simple on-device screenshot comparator that uses golden images present in
 * `sharedTest/assets`.
 *
 * Minimum SDK is O. Densities between devices must match (the images present in
 * `sharedTest/assets` was generated with the Galaxy Nexus skin with xhdpi).
 *
 * Screenshots for debug are saved on device in `/data/data/{package}/files`.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun assertNodeImageMatchGolden(
    goldenName: String,
    node: SemanticsNodeInteraction
) {
    val bitmap = node.captureToImage().asAndroidBitmap()

    // Save screenshot to file for debugging
    val debugScreenshotName = "$goldenName${System.currentTimeMillis()}.png"
    saveScreenshot(debugScreenshotName, bitmap)

    // Get and compare golden bitmap to the given [node]'s captured bitmap
    val golden = InstrumentationRegistry.getInstrumentation()
        .context.resources.assets.open("$goldenName.png")
        .use { BitmapFactory.decodeStream(it) }
    golden.compare(bitmap)
}

private fun saveScreenshot(
    filename: String,
    bmp: Bitmap,
    path: String = InstrumentationRegistry.getInstrumentation().targetContext.filesDir.canonicalPath
) {
    FileOutputStream("$path/$filename").use { out ->
        bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    println("Saved screenshot to $path/$filename")
}

private fun Bitmap.compare(other: Bitmap) {
    if (this.width != other.width || this.height != other.height) {
        throw AssertionError("Size of screenshot does not match golden file (check device density)")
    }
    // Compare row by row to save memory on device
    val row1 = IntArray(width)
    val row2 = IntArray(width)
    for (column in 0 until height) {
        // Read one row per bitmap and compare
        this.getRow(row1, column)
        other.getRow(row2, column)
        if (!row1.contentEquals(row2)) {
            throw AssertionError("Sizes match but bitmap content has differences")
        }
    }
}

private fun Bitmap.getRow(pixels: IntArray, column: Int) {
    this.getPixels(pixels, 0, width, 0, column, width, 1)
}
