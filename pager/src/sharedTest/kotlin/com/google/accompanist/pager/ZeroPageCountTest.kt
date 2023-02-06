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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalPagerApi::class) // Pager is currently experimental
@RunWith(AndroidJUnit4::class)
class ZeroPageCountTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun horizontalPager() {
        rule.setContent {
            HorizontalPager(count = 0, Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize().testTag(TestTag))
            }
        }

        // Assert that we have no content
        assertThat(rule.onAllNodesWithTag(TestTag).fetchSemanticsNodes()).isEmpty()
    }

    @Test
    fun verticalPager() {
        rule.setContent {
            VerticalPager(count = 0, Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize().testTag(TestTag))
            }
        }

        // Assert that we have no content
        assertThat(rule.onAllNodesWithTag(TestTag).fetchSemanticsNodes()).isEmpty()
    }

    private companion object {
        const val TestTag = "PagerItem"
    }
}
