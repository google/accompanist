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

package com.google.accompanist.adaptive

import androidx.activity.ComponentActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowLayoutInfo
import androidx.window.layout.WindowMetricsCalculator
import androidx.window.testing.layout.FoldingFeature
import androidx.window.testing.layout.WindowLayoutInfoPublisherRule
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@RunWith(AndroidJUnit4::class)
class WindowGeometryTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val windowLayoutInfoPublisherRule = WindowLayoutInfoPublisherRule()

    @Test
    fun empty_folding_features_is_correct() {
        lateinit var expectedWindowSizeClass: WindowSizeClass
        lateinit var windowGeometry: WindowGeometry

        composeTestRule.setContent {
            expectedWindowSizeClass = WindowSizeClass.calculateFromSize(
                with(LocalDensity.current) {
                    WindowMetricsCalculator
                        .getOrCreate()
                        .computeCurrentWindowMetrics(composeTestRule.activity)
                        .bounds
                        .toComposeRect()
                        .size
                        .toDpSize()
                }
            )
            windowGeometry = calculateWindowGeometry(activity = composeTestRule.activity)
        }

        windowLayoutInfoPublisherRule.overrideWindowLayoutInfo(WindowLayoutInfo(emptyList()))

        assertThat(windowGeometry.windowSizeClass).isEqualTo(expectedWindowSizeClass)
        assertThat(windowGeometry.displayFeatures).isEmpty()
    }

    @Test
    fun single_folding_features_is_correct() {
        lateinit var expectedWindowSizeClass: WindowSizeClass
        lateinit var windowGeometry: WindowGeometry

        composeTestRule.setContent {
            expectedWindowSizeClass = WindowSizeClass.calculateFromSize(
                with(LocalDensity.current) {
                    WindowMetricsCalculator
                        .getOrCreate()
                        .computeCurrentWindowMetrics(composeTestRule.activity)
                        .bounds
                        .toComposeRect()
                        .size
                        .toDpSize()
                }
            )
            windowGeometry = calculateWindowGeometry(activity = composeTestRule.activity)
        }

        val fakeFoldingFeature = FoldingFeature(
            activity = composeTestRule.activity,
            center = 200,
            size = 40,
            state = FoldingFeature.State.HALF_OPENED,
            orientation = FoldingFeature.Orientation.VERTICAL,
        )

        windowLayoutInfoPublisherRule.overrideWindowLayoutInfo(
            WindowLayoutInfo(
                listOf(
                    fakeFoldingFeature
                )
            )
        )

        assertThat(windowGeometry.windowSizeClass).isEqualTo(expectedWindowSizeClass)
        assertThat(windowGeometry.displayFeatures).hasSize(1)
        assertThat(windowGeometry.displayFeatures[0]).isEqualTo(fakeFoldingFeature)
    }

    @Test
    fun updating_folding_features_is_correct() {
        lateinit var expectedWindowSizeClass: WindowSizeClass
        lateinit var windowGeometry: WindowGeometry

        composeTestRule.setContent {
            expectedWindowSizeClass = WindowSizeClass.calculateFromSize(
                with(LocalDensity.current) {
                    WindowMetricsCalculator
                        .getOrCreate()
                        .computeCurrentWindowMetrics(composeTestRule.activity)
                        .bounds
                        .toComposeRect()
                        .size
                        .toDpSize()
                }
            )
            windowGeometry = calculateWindowGeometry(activity = composeTestRule.activity)
        }

        windowLayoutInfoPublisherRule.overrideWindowLayoutInfo(WindowLayoutInfo(emptyList()))

        val fakeFoldingFeature = FoldingFeature(
            activity = composeTestRule.activity,
            center = 200,
            size = 40,
            state = FoldingFeature.State.HALF_OPENED,
            orientation = FoldingFeature.Orientation.VERTICAL,
        )

        windowLayoutInfoPublisherRule.overrideWindowLayoutInfo(
            WindowLayoutInfo(
                listOf(
                    fakeFoldingFeature
                )
            )
        )

        assertThat(windowGeometry.windowSizeClass).isEqualTo(expectedWindowSizeClass)
        assertThat(windowGeometry.displayFeatures).hasSize(1)
        assertThat(windowGeometry.displayFeatures[0]).isEqualTo(fakeFoldingFeature)
    }
}
