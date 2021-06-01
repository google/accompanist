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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.filters.LargeTest
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalPagerApi::class) // Pager is currently experimental
@LargeTest
@RunWith(JUnit4::class)
class VerticalPagerScrollingContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun verticalPagerScrollingContentTest() {
        lateinit var pagerState: PagerState

        rule.setContent {
            pagerState = rememberPagerState(pageCount = 2)

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().testTag(TestTag)
            ) { page ->
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    // We make the scrolling Column content 2x the height, so that it scrolls
                    val width = with(LocalDensity.current) { (constraints.maxHeight * 2).toDp() }

                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        val background = if (page == 0) Color.Green else Color.Red

                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(width)
                                .background(
                                    brush = Brush.verticalGradient(
                                        listOf(background, Color.Black)
                                    )
                                )
                        )
                    }
                }
            }
        }

        // Perform a very quick, high velocity scroll which will scroll the inner content to it's
        // opposite/end edge
        rule.onNodeWithTag(TestTag)
            .swipeAcrossCenterWithVelocity(velocity = 15_000f, distancePercentageY = -0.5f)

        // Wait for the flings to end
        rule.waitForIdle()

        // Assert that we're still on page 0
        assertThat(pagerState.currentPage).isEqualTo(0)
        assertThat(pagerState.currentPageOffset).isWithin(0.01f).of(0f)
    }

    companion object {
        const val TestTag = "Pager"
    }
}
