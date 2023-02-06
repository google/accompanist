/*
 * Copyright 2022 The Android Open Source Project
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.times
import androidx.compose.ui.unit.width
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalPagerApi::class)
@RunWith(AndroidJUnit4::class)
class TabIndicatorTest {
    @get:Rule
    val rule = createComposeRule()

    private val IndicatorTag = "indicator"
    private val TabRowTag = "TabRow"

    @Test
    fun emptyPager() {
        rule.setContent {
            val pagerState = rememberPagerState()
            TabRow(pagerState)
        }
    }

    @Test
    fun scrollOffsetIsPositive() {
        lateinit var pagerState: PagerState
        rule.setContent {
            pagerState = rememberPagerState()
            Column {
                TabRow(pagerState)
                HorizontalPager(count = 4, state = pagerState) {
                    Box(Modifier.fillMaxSize())
                }
            }
        }

        rule.runOnIdle {
            runBlocking { pagerState.scrollToPage(1, 0.25f) }
        }

        val tab1Bounds = rule.onNodeWithTag("1").getBoundsInRoot()
        val tab2Bounds = rule.onNodeWithTag("2").getBoundsInRoot()
        val indicatorBounds = rule.onNodeWithTag(IndicatorTag).getBoundsInRoot()

        with(rule.density) {
            assertThat(indicatorBounds.left.roundToPx())
                .isEqualTo(lerp(tab1Bounds.left, tab2Bounds.left, 0.25f).roundToPx())
            assertThat(indicatorBounds.width.roundToPx())
                .isEqualTo(lerp(tab1Bounds.width, tab2Bounds.width, 0.25f).roundToPx())
        }
    }

    @Test
    fun scrollOffsetIsNegative() {
        lateinit var pagerState: PagerState
        rule.setContent {
            pagerState = rememberPagerState()
            Column {
                TabRow(pagerState)
                HorizontalPager(count = 4, state = pagerState) {
                    Box(Modifier.fillMaxSize())
                }
            }
        }

        rule.runOnIdle {
            runBlocking { pagerState.scrollToPage(0, 0.75f) }
        }

        val tab1Bounds = rule.onNodeWithTag("1").getBoundsInRoot()
        val tab0Bounds = rule.onNodeWithTag("0").getBoundsInRoot()
        val indicatorBounds = rule.onNodeWithTag(IndicatorTag).getBoundsInRoot()

        with(rule.density) {
            assertThat(indicatorBounds.left.roundToPx())
                .isEqualTo(lerp(tab1Bounds.left, tab0Bounds.left, 0.25f).roundToPx())
            assertThat(indicatorBounds.width.roundToPx())
                .isEqualTo(lerp(tab1Bounds.width, tab0Bounds.width, 0.25f).roundToPx())
        }
    }

    @Test
    fun indicatorIsAtBottom() {
        lateinit var pagerState: PagerState
        rule.setContent {
            pagerState = rememberPagerState()
            Column {
                TabRow(pagerState)
                HorizontalPager(count = 4, state = pagerState) {
                    Box(Modifier.fillMaxSize())
                }
            }
        }

        rule.runOnIdle {
            runBlocking { pagerState.scrollToPage(1, 0.25f) }
        }

        val tabRowBounds = rule.onNodeWithTag(TabRowTag).getBoundsInRoot()

        val indicatorBounds = rule.onNodeWithTag(IndicatorTag).getBoundsInRoot()

        with(rule.density) {
            assertThat(indicatorBounds.height.roundToPx()).isEqualTo(2.dp.roundToPx())
            assertThat(indicatorBounds.bottom).isEqualTo(tabRowBounds.bottom)
        }
    }

    @Composable
    private fun TabRow(pagerState: PagerState) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier
                        .pagerTabIndicatorOffset(pagerState, tabPositions)
                        .testTag(IndicatorTag),
                    height = 2.dp
                )
            },
            modifier = Modifier.testTag(TabRowTag)
        ) {
            // Add tabs for all of our pages
            (0 until pagerState.pageCount).forEach { index ->
                Tab(
                    text = { Text("Tab $index", Modifier.padding(horizontal = index * 5.dp)) },
                    selected = pagerState.currentPage == index,
                    modifier = Modifier.testTag("$index"),
                    onClick = {}
                )
            }
        }
    }
}
