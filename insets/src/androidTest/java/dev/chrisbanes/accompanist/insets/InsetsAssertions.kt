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

package dev.chrisbanes.accompanist.insets

import android.graphics.Rect
import androidx.core.view.WindowInsetsCompat
import com.google.common.truth.Truth

internal fun WindowInsets.assertEqualTo(insets: androidx.core.view.WindowInsetsCompat) {
    systemBars.assertEqualTo(
        insets = insets.getInsets(WindowInsetsCompat.Type.systemBars()),
        visible = insets.isVisible(WindowInsetsCompat.Type.systemBars()),
    )

    statusBars.assertEqualTo(
        insets = insets.getInsets(WindowInsetsCompat.Type.statusBars()),
        visible = insets.isVisible(WindowInsetsCompat.Type.statusBars()),
    )

    navigationBars.assertEqualTo(
        insets = insets.getInsets(WindowInsetsCompat.Type.navigationBars()),
        visible = insets.isVisible(WindowInsetsCompat.Type.navigationBars()),
    )

    ime.assertEqualTo(
        insets = insets.getInsets(WindowInsetsCompat.Type.ime()),
        visible = insets.isVisible(WindowInsetsCompat.Type.ime()),
    )
}

internal fun Insets.assertEqualTo(insets: androidx.core.graphics.Insets, visible: Boolean) {
    // This might look a bit weird, why are we using a Rect? Well, it makes the assertion
    // error message much easier to read, by containing all of the dimensions.
    Truth.assertThat(Rect(left, top, right, bottom))
        .isEqualTo(Rect(insets.left, insets.top, insets.right, insets.bottom))
    Truth.assertThat(this.isVisible).isEqualTo(visible)
}
