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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performGesture
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class SwipeRefreshTest {
    @get:Rule
    val rule = createComposeRule()

    private val swipeRefreshNode: SemanticsNodeInteraction
        get() = rule.onNodeWithTag(SwipeRefreshTag)

    private val indicatorNode: SemanticsNodeInteraction
        get() = rule.onNodeWithTag(SwipeRefreshIndicatorTag)

    private val bottomIndicatorNode: SemanticsNodeInteraction
        get() = rule.onNodeWithTag(BottomSwipeRefreshIndicatorTag)

    private val listNode: SemanticsNodeInteraction
        get() = rule.onNodeWithTag(ListTag)

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
        swipeRefreshNode.performGesture { swipeDown() }

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
        swipeRefreshNode.performGesture { swipeDown() }

        // Assert that the indicator returns back to it's 'resting' position
        assertThat(indicatorNode.getUnclippedBoundsInRoot()).isEqualTo(restingBounds)
    }

    // ------------------------- BOTTOM SWIPE TO REFRESH TESTS --------------------------- //

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun bottomSwipeRefreshes_shouldRefresh() {
        val state = SwipeRefreshState(false)
        var refreshCallCount = 0

        rule.setContent {
            SwipeRefreshTestContent(bottomSwipeRefreshState = state) {
                state.isRefreshing = true
                refreshCallCount++
            }
        }

        // Scroll to the end of the list
        listNode.performScrollToIndex(ListItemCount - 1)

        // Swipe up on the swipe refresh
        swipeRefreshNode.performGesture { swipeUp() }

        // Assert that the onRefresh lambda was called and that we're refreshing
        assertThat(refreshCallCount).isEqualTo(1)
        assertThat(state.isRefreshing).isTrue()

        // Assert that the indicator is displayed
        bottomIndicatorNode.assertIsDisplayed()

        // TODO: Test if the indicator is fading out after we set [state.isRefreshing] to false,
        //  once https://issuetracker.google.com/issues/185814751 is solved.
    }

    @Test
    fun refreshingBottomIndicator_returnsToRest() {
        rule.setContent {
            SwipeRefreshTestContent(bottomSwipeRefreshState = SwipeRefreshState(true)) {}
        }

        // Assert that the indicator is displayed
        bottomIndicatorNode.assertIsDisplayed()
        // Store the 'resting' bounds
        val restingBounds = bottomIndicatorNode.getUnclippedBoundsInRoot()

        // Now swipe up. The indicator should react visually
        swipeRefreshNode.performGesture { swipeUp() }

        // Assert that the indicator returns back to it's 'resting' position
        assertThat(bottomIndicatorNode.getUnclippedBoundsInRoot()).isEqualTo(restingBounds)
    }

    @Test
    fun bottomIndicator_refreshingInitially() {
        rule.setContent {
            SwipeRefreshTestContent(bottomSwipeRefreshState = rememberSwipeRefreshState(true)) {}
        }

        // Assert that the indicator is displayed
        bottomIndicatorNode.assertIsDisplayed()
    }
}

private const val SwipeRefreshTag = "swipe_refresh"
private const val SwipeRefreshIndicatorTag = "swipe_refresh_indicator"
private const val BottomSwipeRefreshIndicatorTag = "bottom_swipe_refresh_indicator"
private const val ListTag = "list_tag"
private const val ListItemCount = 30

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwipeRefreshTestContent(
    topSwipeRefreshState: SwipeRefreshState? = null,
    bottomSwipeRefreshState: SwipeRefreshState? = null,
    onRefresh: (Position) -> Unit,
) {
    MaterialTheme {
        SwipeRefresh(
            topRefreshIndicatorState = topSwipeRefreshState,
            bottomRefreshIndicatorState = bottomSwipeRefreshState,
            onRefresh = onRefresh,
            modifier = Modifier.testTag(SwipeRefreshTag),
            topIndicator = { state, trigger ->
                SwipeRefreshIndicator(state, trigger, Modifier.testTag(SwipeRefreshIndicatorTag))
            },
            bottomIndicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state,
                    trigger,
                    Modifier.testTag(BottomSwipeRefreshIndicatorTag)
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
