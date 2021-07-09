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

package com.google.accompanist.insets

import androidx.compose.ui.unit.LayoutDirection
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

/**
 * Version of [BaseInsetsTest] which is designed to be ran on Robolectric.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class RobolectricInsetsTest(
    type: TestInsetType,
    applyStart: Boolean,
    applyTop: Boolean,
    applyEnd: Boolean,
    applyBottom: Boolean,
    layoutDirection: LayoutDirection,
) : BaseInsetsTest(type, applyStart, applyTop, applyEnd, applyBottom, layoutDirection) {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data(): Collection<Array<Any>> = params()
    }
}
