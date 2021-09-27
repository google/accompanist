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

package com.google.accompanist.lazysnap

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import com.google.accompanist.internal.test.combineWithParameters
import com.google.accompanist.internal.test.parameterizedParams
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Version of [BaseSnappingLazyColumnTest] which is designed to be run on device/emulators.
 */
@RunWith(Parameterized::class)
class InstrumentedSnappingLazyColumnTest(
    maxScrollDistanceDp: Float,
    contentPadding: PaddingValues,
    itemSpacingDp: Int,
    reverseLayout: Boolean,
) : BaseSnappingLazyColumnTest(
    maxScrollDistanceDp,
    contentPadding,
    itemSpacingDp,
    reverseLayout,
) {
    companion object {
        /**
         * On device we only test a subset of the combined parameters.
         */
        @JvmStatic
        @Parameterized.Parameters(
            name = "maxScrollDistanceDp={0}," +
                "contentPadding={1}," +
                "itemSpacing={2}," +
                "reverseLayout={3}"
        )
        fun data() = parameterizedParams()
            // maxScrollDistanceDp
            .combineWithParameters(
                ItemSize.value,
                ItemSize.value * 4,
            )
            // contentPadding
            .combineWithParameters(
                PaddingValues(bottom = 32.dp), // Alignment.Top
                PaddingValues(vertical = 32.dp), // Alignment.Center
                PaddingValues(top = 32.dp), // Alignment.Bottom
            )
            // itemSpacingDp
            .combineWithParameters(0, 4)
            // reverseLayout
            .combineWithParameters(false)
    }
}
