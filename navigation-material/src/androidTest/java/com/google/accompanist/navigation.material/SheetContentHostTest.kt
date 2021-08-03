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

package com.google.accompanist.navigation.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.TestMonotonicFrameClock
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavBackStackEntry
import androidx.navigation.testing.TestNavigatorState
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class, ExperimentalMaterialNavigationApi::class)
internal class SheetContentHostTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private val testClock = TestMonotonicFrameClock(CoroutineScope(testDispatcher))
    private val bodyContentTag = "testBodyContent"

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSheetShownAndHidden() = runBlockingTest {
        val backStackEntryState = mutableStateOf<NavBackStackEntry?>(null)
        val sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)

        composeTestRule.setBottomSheetContent(
            backStackEntryState,
            sheetState,
            onSheetShown = { },
            onSheetDismissed = { }
        )

        backStackEntryState.value = createBackStackEntry(sheetState)
        composeTestRule.runOnIdle {
            assertWithMessage("Bottom sheet was shown")
                .that(sheetState.isVisible).isTrue()
        }

        backStackEntryState.value = null
        composeTestRule.runOnIdle {
            assertWithMessage("Bottom sheet was hidden")
                .that(sheetState.isVisible).isFalse()
        }
    }

    @Test
    fun testOnSheetDismissedCalled_ManualDismiss() = runBlockingTest(testClock) {
        val sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val backStackEntry = createBackStackEntry(sheetState)

        val dismissedBackStackEntries = mutableListOf<NavBackStackEntry>()

        composeTestRule.setBottomSheetContent(
            mutableStateOf(backStackEntry),
            sheetState,
            onSheetShown = { },
            onSheetDismissed = { entry -> dismissedBackStackEntries.add(entry) }
        )

        assertThat(sheetState.currentValue == ModalBottomSheetValue.Expanded)
        composeTestRule.onNodeWithTag(bodyContentTag).performClick()
        composeTestRule.runOnIdle {
            assertWithMessage("Sheet is visible")
                .that(sheetState.isVisible).isFalse()
            assertWithMessage("Back stack entry should be in the dismissed entries list")
                .that(dismissedBackStackEntries)
                .containsExactly(backStackEntry)
        }
    }

    @Test
    fun testOnSheetDismissedCalled_initiallyExpanded() = runBlockingTest(testClock) {
        val sheetState = ModalBottomSheetState(ModalBottomSheetValue.Expanded)
        val backStackEntry = createBackStackEntry(sheetState)

        val dismissedBackStackEntries = mutableListOf<NavBackStackEntry>()

        composeTestRule.setBottomSheetContent(
            mutableStateOf(backStackEntry),
            sheetState,
            onSheetShown = { },
            onSheetDismissed = { entry -> dismissedBackStackEntries.add(entry) }
        )

        assertThat(sheetState.currentValue == ModalBottomSheetValue.Expanded)
        composeTestRule.onNodeWithTag(bodyContentTag).performClick()
        composeTestRule.runOnIdle {
            assertWithMessage("Sheet is not visible")
                .that(sheetState.isVisible).isFalse()
            assertWithMessage("Back stack entry should be in the dismissed entries list")
                .that(dismissedBackStackEntries)
                .containsExactly(backStackEntry)
        }
    }

    @Test
    fun testOnSheetShownCalled_onBackStackEntryEnter() = runBlockingTest(testClock) {
        val sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val backStackEntry = createBackStackEntry(sheetState)

        val shownBackStackEntries = mutableListOf<NavBackStackEntry>()

        composeTestRule.setBottomSheetContent(
            backStackEntry = mutableStateOf(backStackEntry),
            sheetState = sheetState,
            onSheetShown = { entry -> shownBackStackEntries.add(entry) },
            onSheetDismissed = { }
        )

        composeTestRule.runOnIdle {
            assertWithMessage("Sheet is visible")
                .that(sheetState.isVisible).isTrue()
            assertWithMessage("Back stack entry should be in the shown entries list")
                .that(shownBackStackEntries)
                .containsExactly(backStackEntry)
        }
    }

    private fun ComposeContentTestRule.setBottomSheetContent(
        backStackEntry: State<NavBackStackEntry?>,
        sheetState: ModalBottomSheetState,
        onSheetShown: (NavBackStackEntry) -> Unit,
        onSheetDismissed: (NavBackStackEntry) -> Unit
    ) {
        setContent {
            val saveableStateHolder = rememberSaveableStateHolder()
            ModalBottomSheetLayout(
                sheetContent = {
                    SheetContentHost(
                        columnHost = this,
                        backStackEntry = backStackEntry.value,
                        sheetState = sheetState,
                        saveableStateHolder = saveableStateHolder,
                        onSheetShown = onSheetShown,
                        onSheetDismissed = onSheetDismissed
                    )
                },
                sheetState = sheetState,
                content = { Box(Modifier.fillMaxSize().testTag(bodyContentTag)) }
            )
        }
    }

    private fun createBackStackEntry(sheetState: ModalBottomSheetState): NavBackStackEntry {
        val navigatorState = TestNavigatorState()
        val navigator = BottomSheetNavigator(sheetState)
        navigator.onAttach(navigatorState)

        val destination = BottomSheetNavigator.Destination(navigator) {
            Text("Fake Sheet Content")
        }
        val backStackEntry = navigatorState.createBackStackEntry(destination, null)
        navigator.navigate(listOf(backStackEntry), null, null)
        return backStackEntry
    }
}
