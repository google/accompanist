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

import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalPagerApi::class) // Pager is currently experimental
@RunWith(JUnit4::class)
class PagerStateUnitTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test(expected = IllegalArgumentException::class)
    fun rememberPagerState_pageCount_negative() {
        composeTestRule.setContent {
            rememberPagerState(pageCount = -1)
        }
    }

    @Test
    fun rememberPagerState_pageCount_0() {
        composeTestRule.setContent {
            rememberPagerState(pageCount = 0, initialPage = 0, initialPageOffset = 0f)
        }
    }

    @Test(expected = IllegalArgumentException::class) // using initialPage > 0
    fun rememberPagerState_pageCount_0_initialPage() {
        composeTestRule.setContent {
            rememberPagerState(pageCount = 0, initialPage = 2, initialPageOffset = 0f)
        }
    }

    @Test(expected = IllegalArgumentException::class) // using initialPageOffset > 0f
    fun rememberPagerState_pageCount_0_initialPageOffset() {
        composeTestRule.setContent {
            rememberPagerState(pageCount = 0, initialPage = 0, initialPageOffset = 0.5f)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun store_restore_state() = runBlockingTest {
        val stateRestoration = StateRestorationTester(composeTestRule)
        lateinit var state: PagerState

        stateRestoration.setContent {
            state = rememberPagerState(pageCount = 10)
        }
        // Now scroll to page 4
        state.scrollToPage(4)

        // Emulator a state save + restore
        stateRestoration.emulateSavedInstanceStateRestore()

        // And assert that everything was restored
        assertThat(state.currentPage).isEqualTo(4)
        assertThat(state.pageCount).isEqualTo(10)
    }
}
