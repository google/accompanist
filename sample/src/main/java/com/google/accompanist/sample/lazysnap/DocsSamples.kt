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

package com.google.accompanist.sample.lazysnap

import androidx.compose.runtime.Composable
import com.google.accompanist.lazysnap.ExperimentalSnapFlingApi
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerDefaults
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class, ExperimentalSnapFlingApi::class)
@Composable
fun MultipleFlingBehavior() {
    val pagerState = rememberPagerState()
    HorizontalPager(
        count = 10,
        state = pagerState,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            maximumFlingDistance = {
                // Return a maximum fling distance which is 2x pages
                2 * PagerDefaults.singlePageFlingDistance(it)
            }
        )
    ) {
        // item layout
    }
}
