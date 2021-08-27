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

package com.google.accompanist.pager

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.google.accompanist.internal.test.combineWithParameters
import com.google.accompanist.internal.test.parameterizedParams
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Version of [BaseVerticalPagerTest] which is designed to be run on Robolectric.
 */
@Config(qualifiers = "w360dp-h640dp-xhdpi")
@RunWith(ParameterizedRobolectricTestRunner::class)
class RobolectricVerticalPagerTest(
    contentPadding: PaddingValues,
    itemSpacingDp: Int,
    reverseLayout: Boolean,
) : BaseVerticalPagerTest(
    contentPadding,
    itemSpacingDp,
    reverseLayout,
) {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "contentPadding={0}," +
                "itemSpacing={1}," +
                "reverseLayout={2}"
        )
        fun data() = parameterizedParams()
            // contentPadding
            .combineWithParameters(
                PaddingValues(bottom = 32.dp), // Alignment.Top
                PaddingValues(vertical = 32.dp), // Alignment.Center
                PaddingValues(top = 32.dp), // Alignment.Bottom
            )
            // itemSpacingDp
            .combineWithParameters(0, 4)
            // reverseLayout
            .combineWithParameters(true, false)
    }
}
