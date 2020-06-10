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

import androidx.ui.test.SemanticsNodeInteraction
import androidx.ui.unit.Density
import androidx.ui.unit.Dp
import androidx.ui.unit.IntSize
import org.junit.Assert

fun SemanticsNodeInteraction.assertSize(density: Density, width: Dp, height: Dp) {
    assertSize(with(density) { IntSize(width.toIntPx(), height.toIntPx()) })
}

fun SemanticsNodeInteraction.assertSize(expected: IntSize) {
    val node = fetchSemanticsNode("Assert size")
    Assert.assertEquals(expected, node.size)
}
