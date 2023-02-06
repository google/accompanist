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
@file:Suppress("DEPRECATION")
package com.google.accompanist.pager

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalPagerApi::class) // Pager is currently experimental
@RunWith(AndroidJUnit4::class)
class PagerStateUnitTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Ignore // Not currently working after migration to Lazy
    @Suppress("DEPRECATION")
    @Test
    fun store_restore_state() = runBlockingTest {
        val stateRestoration = StateRestorationTester(composeTestRule)
        lateinit var state: PagerState

        stateRestoration.setContent {
            state = rememberPagerState()
            HorizontalPager(count = 10, state = state) { page ->
                BasicText(text = "Page:$page")
            }
        }
        composeTestRule.awaitIdle()

        // Now scroll to page 4
        state.scrollToPage(4)

        // Emulator a state save + restore
        stateRestoration.emulateSavedInstanceStateRestore()

        // And assert that everything was restored
        assertThat(state.currentPage).isEqualTo(4)
        assertThat(state.pageCount).isEqualTo(10)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun change_pager_count() {
        val pagerState = PagerState()
        var count by mutableStateOf(10)

        composeTestRule.setContent {
            HorizontalPager(count = count, state = pagerState) { page ->
                BasicText(text = "Page:$page")
            }
        }
        composeTestRule.waitForIdle()

        // Now scroll to page 8
        composeTestRule.runOnIdle {
            runBlocking {
                pagerState.scrollToPage(8)
            }
        }

        // And assert that currentPage and pageCount are correct
        assertThat(pagerState.currentPage).isEqualTo(8)
        assertThat(pagerState.pageCount).isEqualTo(10)

        // Change page count to 4
        count = 4
        composeTestRule.waitForIdle()

        // And assert that currentPage and pageCount are correct
        assertThat(pagerState.currentPage).isEqualTo(3)
        assertThat(pagerState.pageCount).isEqualTo(4)

        // Change page count to 0
        count = 0
        composeTestRule.waitForIdle()

        // And assert that currentPage and pageCount are correct
        assertThat(pagerState.currentPage).isEqualTo(0)
        assertThat(pagerState.pageCount).isEqualTo(0)
    }
}
