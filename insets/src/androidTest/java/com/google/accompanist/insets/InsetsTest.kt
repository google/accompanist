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

package com.google.accompanist.insets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Ideally this would run on the host instead.
 */
@RunWith(TestParameterInjector::class)
class InsetsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    companion object {
        private const val ExpectedPx: Int = 30
    }

    @Test
    fun paddingValues(
        @TestParameter type: TestInsetType,
        @TestParameter applyStart: Boolean,
        @TestParameter applyTop: Boolean,
        @TestParameter applyEnd: Boolean,
        @TestParameter applyBottom: Boolean,
        @TestParameter layoutDirection: LayoutDirection,
    ) {
        // Calculate left/right values which are used later
        val applyLeft = applyStart && layoutDirection == LayoutDirection.Ltr ||
            applyEnd && layoutDirection == LayoutDirection.Rtl
        val applyRight = applyStart && layoutDirection == LayoutDirection.Rtl ||
            applyEnd && layoutDirection == LayoutDirection.Ltr

        val windowInsets = RootWindowInsets()
        lateinit var paddingValues: PaddingValues
        var expectedPxInDp: Dp = 0.dp

        composeTestRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                expectedPxInDp = with(LocalDensity.current) { ExpectedPx.toDp() }

                paddingValues = rememberInsetsPaddingValues(
                    insets = windowInsets.getTestTypeToRead(type),
                    applyStart = applyStart,
                    applyTop = applyTop,
                    applyEnd = applyEnd,
                    applyBottom = applyBottom,
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert that the expectedPxInDp value was resolved
        assertThat(expectedPxInDp.value).isAtLeast(1f)

        // Assert that everything is 0.dp to start
        assertThat(paddingValues.calculateStartPadding(layoutDirection)).isEqualTo(0.dp)
        assertThat(paddingValues.calculateTopPadding()).isEqualTo(0.dp)
        assertThat(paddingValues.calculateEndPadding(layoutDirection)).isEqualTo(0.dp)
        assertThat(paddingValues.calculateBottomPadding()).isEqualTo(0.dp)

        // Now update the WindowInsets as appropriately for the test
        (windowInsets.getTestTypeToUpdate(type).layoutInsets).apply {
            left = if (applyLeft) ExpectedPx else 0
            top = if (applyTop) ExpectedPx else 0
            right = if (applyRight) ExpectedPx else 0
            bottom = if (applyBottom) ExpectedPx else 0
        }

        // Wait for composition to happen
        composeTestRule.waitForIdle()

        // And assert that the PaddingValues return the correct values
        assertThat(paddingValues.calculateStartPadding(layoutDirection))
            .isEqualTo(if (applyStart) expectedPxInDp else 0.dp)
        assertThat(paddingValues.calculateTopPadding())
            .isEqualTo(if (applyTop) expectedPxInDp else 0.dp)
        assertThat(paddingValues.calculateEndPadding(layoutDirection))
            .isEqualTo(if (applyEnd) expectedPxInDp else 0.dp)
        assertThat(paddingValues.calculateBottomPadding())
            .isEqualTo(if (applyBottom) expectedPxInDp else 0.dp)
    }
}

enum class TestInsetType {
    StatusBars,
    NavigationBars,
    SystemGesture,
    Ime,
    SystemBars,
}

/**
 * Return the [WindowInsets.Type] to assert values for, for the given [type].
 */
private fun WindowInsets.getTestTypeToRead(
    type: TestInsetType,
): WindowInsets.Type = when (type) {
    TestInsetType.StatusBars -> statusBars
    TestInsetType.NavigationBars -> navigationBars
    TestInsetType.SystemGesture -> systemGestures
    TestInsetType.Ime -> ime
    TestInsetType.SystemBars -> systemBars
}

/**
 * Return the [MutableWindowInsetsType] to update and instrument for the given [type].
 *
 * This may not be the same return values as [getTestTypeToRead] since some types are computed.
 */
private fun WindowInsets.getTestTypeToUpdate(type: TestInsetType): MutableWindowInsetsType {
    return when (type) {
        TestInsetType.StatusBars -> statusBars
        TestInsetType.NavigationBars -> navigationBars
        TestInsetType.SystemGesture -> systemGestures
        TestInsetType.Ime -> ime
        // For SystemBars we return statusBars as SystemBars is a computed
        // result of statusBars + navigationBars
        TestInsetType.SystemBars -> statusBars
    } as MutableWindowInsetsType
}
