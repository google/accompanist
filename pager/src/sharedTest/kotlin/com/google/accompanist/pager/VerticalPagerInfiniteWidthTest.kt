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

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalPagerApi::class) // Pager is currently experimental
@RunWith(AndroidJUnit4::class)
class VerticalPagerInfiniteWidthTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun verticalPagerInScrollableRow() {
        rule.setContent {
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                VerticalPager(
                    count = 10,
                    modifier = Modifier
                        .fillMaxHeight()
                        // fillMaxWidth() with horizontalScroll() parents means that pager will
                        // receive infinite max width constraints which doesn't make much sense
                        // for pagers....
                        .fillMaxWidth()
                        .testTag(TestTag)
                ) {
                    Box(Modifier.fillMaxHeight().width(200.dp))
                }
            }
        }

        val rootBounds = rule.onRoot().getUnclippedBoundsInRoot()

        // Assert that VerticalPager handled the infinite max width constraint
        // by wrapping its content instead.
        rule.onNodeWithTag(TestTag)
            // Since the pager's content uses 200.dp width
            .assertWidthIsEqualTo(200.dp)
            .assertHeightIsEqualTo(rootBounds.height)
    }

    companion object {
        const val TestTag = "Pager"
    }
}
