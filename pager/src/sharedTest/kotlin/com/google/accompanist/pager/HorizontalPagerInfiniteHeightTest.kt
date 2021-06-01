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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalPagerApi::class) // Pager is currently experimental
@RunWith(AndroidJUnit4::class)
class HorizontalPagerInfiniteHeightTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun horizontalPagerInScrollableColumn() {
        rule.setContent {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                HorizontalPager(
                    state = rememberPagerState(pageCount = 10),
                    modifier = Modifier
                        // fillMaxHeight() with verticalScroll() parents means that pager will
                        // receive infinite max height constraints which doesn't make much sense
                        // for pagers....
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .testTag(TestTag)
                ) {
                    Box(Modifier.fillMaxWidth().height(200.dp))
                }
            }
        }

        val rootBounds = rule.onRoot().getUnclippedBoundsInRoot()

        // Assert that HorizontalPager handled the infinite max height constraint
        // by wrapping it's content instead.
        rule.onNodeWithTag(TestTag)
            .assertWidthIsEqualTo(rootBounds.width)
            // Since the pager's content uses 200.dp height
            .assertHeightIsEqualTo(200.dp)
    }

    companion object {
        const val TestTag = "Pager"
    }
}
