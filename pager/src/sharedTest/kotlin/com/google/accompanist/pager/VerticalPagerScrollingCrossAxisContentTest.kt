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

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalPagerApi::class) // Pager is currently experimental
@RunWith(AndroidJUnit4::class)
class VerticalPagerScrollingCrossAxisContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun verticalPagerScrollingContentTest() {
        val pagerState = PagerState()

        val nestedScrollConnection = object : NestedScrollConnection {
            var scroll: Offset = Offset.Zero
            var flingVelocity: Velocity = Velocity.Zero

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                scroll += available
                return super.onPostScroll(consumed, available, source)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                flingVelocity = available
                return super.onPostFling(consumed, available)
            }
        }

        rule.setContent {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
            ) {
                val rootWidth = with(LocalDensity.current) { (constraints.maxWidth).toDp() }

                VerticalPager(
                    count = 2,
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(TestTag)
                ) { page ->
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        val background = if (page % 2 == 0) Color.Green else Color.Red
                        // We make the inner scrolling Column content 2x the root height,
                        // so it scrolls
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .width(rootWidth * 2)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(background, Color.Black)
                                    )
                                )
                        )
                    }
                }
            }
        }

        // Perform a horizontal scroll which should scroll the inner scrollable row
        rule.onNodeWithTag(TestTag)
            .swipeAcrossCenterWithVelocity(velocityPerSec = 5_000.dp, distancePercentageX = -0.5f)
        // Wait for any flings to end
        rule.waitForIdle()

        // Assert that the nestedScrollConnection received the cross-axis scrolls
        assertThat(nestedScrollConnection.scroll.x).isLessThan(1f)
        assertThat(nestedScrollConnection.flingVelocity.x).isLessThan(1f)
    }

    companion object {
        const val TestTag = "Pager"
    }
}
