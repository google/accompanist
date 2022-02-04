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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.google.accompanist.internal.test.combineWithParameters
import com.google.accompanist.internal.test.parameterizedParams
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Version of [BaseHorizontalPagerTest] which is designed to be run on Robolectric.
 */
@Config(qualifiers = "w360dp-h640dp-xhdpi")
@RunWith(ParameterizedRobolectricTestRunner::class)
class RobolectricHorizontalPagerTest(
    itemWidthFraction: Float,
    contentPadding: PaddingValues,
    itemSpacingDp: Int,
    layoutDirection: LayoutDirection,
    reverseLayout: Boolean,
) : BaseHorizontalPagerTest(
    itemWidthFraction,
    contentPadding,
    itemSpacingDp,
    layoutDirection,
    reverseLayout,
) {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(
            name = "itemWidthFraction={0}," +
                "contentPadding={1}," +
                "itemSpacing={2}," +
                "layoutDirection={3}," +
                "reverseLayout={4}"
        )
        fun data() = parameterizedParams()
            // itemWidthFraction
            .combineWithParameters(1f)
            // contentPadding
            .combineWithParameters(
                PaddingValues(horizontal = 0.dp), // Fill
                PaddingValues(end = 32.dp), // Alignment.Start
                PaddingValues(horizontal = 32.dp), // Alignment.Center
                PaddingValues(start = 32.dp), // Alignment.End
            )
            // itemSpacing
            .combineWithParameters(0, 4)
            // layoutDirection
            .combineWithParameters(LayoutDirection.Ltr, LayoutDirection.Rtl)
            // reverseLayout
            .combineWithParameters(true, false)
    }
}
