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

import androidx.compose.ui.Alignment
import com.google.accompanist.internal.test.combineWithParameters
import com.google.accompanist.internal.test.parameterizedParams
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class InstrumentedVerticalPagerTest(
    verticalAlignment: Alignment.Vertical,
    itemSpacingDp: Int,
    offscreenLimit: Int,
    reverseLayout: Boolean,
    infiniteLoop: Boolean
) : BaseVerticalPagerTest(
    verticalAlignment,
    itemSpacingDp,
    offscreenLimit,
    reverseLayout,
    infiniteLoop
) {
    companion object {
        /**
         * On device we only test a subset of the combined parameters.
         */
        @JvmStatic
        @Parameterized.Parameters
        fun data() = parameterizedParams()
            // verticalAlignment
            .combineWithParameters(Alignment.CenterVertically, Alignment.Top, Alignment.Bottom)
            // itemSpacingDp
            .combineWithParameters(0, 4)
            // offscreenLimit
            .combineWithParameters(1)
            // reverseLayout
            .combineWithParameters(false)
            // looping
            .combineWithParameters(false)
    }
}
