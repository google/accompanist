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

package com.google.accompanist.swiperefresh

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SwipeRefreshTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    @Ignore("https://issuetracker.google.com/issues/185814751")
    fun swipeRefreshes() {
        val state = SwipeRefreshState(false)
        var refreshCallCount = 0

        rule.setContent {
            SwipeRefreshTestContent(state) {
                state.isRefreshing = true
                refreshCallCount++
            }
        }

        // Swipe down on the swipe refresh
        swipeRefreshNode.performTouchInput { swipeDown() }

        // Assert that the onRefresh lambda was called once, and that we're refreshing
        assertThat(refreshCallCount).isEqualTo(1)
        assertThat(state.isRefreshing).isTrue()

        // Assert that the indicator is displayed
        indicatorNode.assertIsDisplayed()

        // Now stop 'refreshing' and assert that the indicator is no longer displayed
        state.isRefreshing = false
        indicatorNode.assertIsNotDisplayed()
    }

    @Test
    @Ignore("https://issuetracker.google.com/issues/185814751")
    fun indicatorVisibility() {
        lateinit var state: SwipeRefreshState

        rule.setContent {
            state = rememberSwipeRefreshState(false)
            SwipeRefreshTestContent(state) {}
        }

        // Assert that the indicator is not displayed
        indicatorNode.assertIsNotDisplayed()

        // Set refreshing to true and assert that the indicator is displayed
        state.isRefreshing = true

        rule.waitForIdle()
        indicatorNode.assertIsDisplayed()
    }

    @Test
    fun refreshingInitially() {
        rule.setContent {
            SwipeRefreshTestContent(rememberSwipeRefreshState(true)) {}
        }

        // Assert that the indicator is displayed
        indicatorNode.assertIsDisplayed()
    }

    @Test
    fun refreshingIndicator_returnsToRest() {
        rule.setContent {
            SwipeRefreshTestContent(rememberSwipeRefreshState(true)) {}
        }

        // Assert that the indicator is displayed
        indicatorNode.assertIsDisplayed()
        // Store the 'resting' bounds
        val restingBounds = indicatorNode.getUnclippedBoundsInRoot()

        // Now swipe down. The indicator should react visually
        swipeRefreshNode.performTouchInput { swipeDown() }

        // Assert that the indicator returns back to it's 'resting' position
        assertThat(indicatorNode.getUnclippedBoundsInRoot()).isEqualTo(restingBounds)
    }

    private val swipeRefreshNode: SemanticsNodeInteraction
        get() = rule.onNodeWithTag(SwipeRefreshTag)

    private val indicatorNode: SemanticsNodeInteraction
        get() = rule.onNodeWithTag(SwipeRefreshIndicatorTag)

    // ------------------------- BOTTOM SWIPE TO REFRESH TESTS --------------------------- //

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun bottomSwipeRefreshes_shouldRefresh() {
        val state = SwipeRefreshState(false)
        var refreshCallCount = 0

        rule.setContent {
            SwipeRefreshTestContent(
                swipeRefreshState = state,
                indicatorAlignment = Alignment.BottomCenter,
            ) {
                state.isRefreshing = true
                refreshCallCount++
            }
        }

        // Scroll to the end of the list
        listNode.performScrollToIndex(ListItemCount - 1)

        // Swipe up on the swipe refresh
        swipeRefreshNode.performTouchInput { swipeUp() }

        // Assert that the onRefresh lambda was called and that we're refreshing
        assertThat(state.isRefreshing).isTrue()
        assertThat(refreshCallCount).isEqualTo(1)

        // Assert that the indicator is displayed
        bottomIndicatorNode.assertIsDisplayed()

        // TODO: Test if the indicator is fading out after we set [state.isRefreshing] to false,
        //  once https://issuetracker.google.com/issues/185814751 is solved.
    }

    @Test
    fun refreshingBottomIndicator_returnsToRest() {
        rule.setContent {
            SwipeRefreshTestContent(
                swipeRefreshState = rememberSwipeRefreshState(true),
                indicatorAlignment = Alignment.BottomCenter,
            )
        }

        // Assert that the indicator is displayed
        bottomIndicatorNode.assertIsDisplayed()
        // Store the 'resting' bounds
        val restingBounds = bottomIndicatorNode.getUnclippedBoundsInRoot()

        // Now swipe up. The indicator should react visually
        swipeRefreshNode.performTouchInput { swipeUp() }

        // Assert that the indicator returns back to it's 'resting' position
        assertThat(bottomIndicatorNode.getUnclippedBoundsInRoot()).isEqualTo(restingBounds)
    }

    @Test
    fun bottomIndicator_refreshingInitially() {
        rule.setContent {
            SwipeRefreshTestContent(
                swipeRefreshState = rememberSwipeRefreshState(true),
                indicatorAlignment = Alignment.BottomCenter,
            )
        }

        // Assert that the indicator is displayed
        bottomIndicatorNode.assertIsDisplayed()
    }

    private val bottomIndicatorNode: SemanticsNodeInteraction
        get() = rule.onNodeWithTag(BottomSwipeRefreshIndicatorTag)

    private val listNode: SemanticsNodeInteraction
        get() = rule.onNodeWithTag(ListTag)
}

private const val SwipeRefreshTag = "swipe_refresh"
private const val SwipeRefreshIndicatorTag = "swipe_refresh_indicator"
private const val BottomSwipeRefreshIndicatorTag = "bottom_swipe_refresh_indicator"
private const val ListTag = "list_tag"
private const val ListItemCount = 30

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwipeRefreshTestContent(
    swipeRefreshState: SwipeRefreshState,
    indicatorAlignment: Alignment = Alignment.TopCenter,
    onRefresh: () -> Unit = {},
) {
    val isTopPosition = (indicatorAlignment as BiasAlignment).verticalBias != 1f
    MaterialTheme {
        SwipeRefresh(
            state = swipeRefreshState,
            indicatorAlignment = indicatorAlignment,
            onRefresh = onRefresh,
            modifier = Modifier.testTag(SwipeRefreshTag),
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    clockwise = isTopPosition,
                    modifier = Modifier.testTag(
                        if (isTopPosition) {
                            SwipeRefreshIndicatorTag
                        } else {
                            BottomSwipeRefreshIndicatorTag
                        }
                    ),
                )
            },
        ) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .testTag(ListTag)
            ) {
                items(ListItemCount) { index ->
                    ListItem(Modifier.fillMaxWidth()) {
                        Text(text = "Item $index")
                    }
                }
            }
        }
    }
}