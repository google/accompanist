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

@file:Suppress("DEPRECATION")

package com.google.accompanist.insets

import android.graphics.Rect
import androidx.core.view.WindowInsetsCompat
import com.google.common.truth.Truth.assertThat

internal fun WindowInsets.assertEqualTo(insets: WindowInsetsCompat) {
    statusBars.assertEqualTo(insets.getInsets(WindowInsetsCompat.Type.statusBars()))
    assertThat(statusBars.isVisible)
        .isEqualTo(insets.isVisible(WindowInsetsCompat.Type.statusBars()))

    navigationBars.assertEqualTo(insets.getInsets(WindowInsetsCompat.Type.navigationBars()))
    assertThat(navigationBars.isVisible)
        .isEqualTo(insets.isVisible(WindowInsetsCompat.Type.navigationBars()))

    ime.assertEqualTo(insets.getInsets(WindowInsetsCompat.Type.ime()))
    assertThat(ime.isVisible)
        .isEqualTo(insets.isVisible(WindowInsetsCompat.Type.ime()))

    systemBars.assertEqualTo(insets.getInsets(WindowInsetsCompat.Type.systemBars()))

    // It's difficult to create an expected value for isVisible as it depends on the system ui
    // of the device.

    displayCutout.assertEqualTo(insets.getInsets(WindowInsetsCompat.Type.displayCutout()))
}

internal fun WindowInsets.Type.assertEqualTo(insets: androidx.core.graphics.Insets) {
    // This might look a bit weird, why are we using a Rect? Well, it makes the assertion
    // error message much easier to read, by containing all of the dimensions.
    assertThat(Rect(left, top, right, bottom))
        .isEqualTo(Rect(insets.left, insets.top, insets.right, insets.bottom))
}
