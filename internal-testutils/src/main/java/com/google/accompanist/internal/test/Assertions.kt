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

package com.google.accompanist.internal.test

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.unit.Dp
import com.google.common.truth.Truth.assertThat

/**
 * Assert that all of the pixels in this image as of the [expected] color.
 */
public fun ImageBitmap.assertPixels(expected: Color, tolerance: Float = 0.001f) {
    toPixelMap().buffer.forEach { pixel ->
        val color = Color(pixel)
        assertThat(color.red).isWithin(tolerance).of(expected.red)
        assertThat(color.green).isWithin(tolerance).of(expected.green)
        assertThat(color.blue).isWithin(tolerance).of(expected.blue)
        assertThat(color.alpha).isWithin(tolerance).of(expected.alpha)
    }
}

/**
 * Run the [SemanticsNodeInteraction] provided by [block] repeatedly until either
 * the assertion succeeds, or the execution runs past [timeoutMillis].
 */
public fun SemanticsNodeInteraction.assertWithTimeout(
    timeoutMillis: Long,
    block: SemanticsNodeInteraction.() -> SemanticsNodeInteraction,
): SemanticsNodeInteraction {
    val startTime = System.nanoTime()
    while (System.nanoTime() - startTime <= timeoutMillis * 1_000_000) {
        try {
            return block()
        } catch (error: AssertionError) {
            // If the assertion failed, sleep for 10ms before the next loop iteration
            Thread.sleep(10)
        }
    }
    // If we reach here, each assertion has failed and we've reached the time out.
    // Run block one last time...
    return block()
}

public val SemanticsNodeInteraction.exists: Boolean
    get() = try {
        assertExists()
        true
    } catch (t: Throwable) {
        false
    }

public val SemanticsNodeInteraction.isLaidOut: Boolean
    get() = try {
        assertWidthIsAtLeast(Dp.Hairline).assertHeightIsAtLeast(Dp.Hairline)
        true
    } catch (t: Throwable) {
        false
    }
