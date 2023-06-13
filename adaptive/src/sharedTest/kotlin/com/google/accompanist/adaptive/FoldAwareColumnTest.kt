/*
 * Copyright 2023 The Android Open Source Project
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

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowLayoutInfo
import androidx.window.layout.WindowMetricsCalculator
import androidx.window.testing.layout.FoldingFeature
import androidx.window.testing.layout.WindowLayoutInfoPublisherRule
import com.google.accompanist.adaptive.FoldAwareColumnScopeInstance.ignoreFold
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoldAwareColumnTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val publisherRule = WindowLayoutInfoPublisherRule()

    private val testTag = "FoldAwareColumnTestTag"
    private var firstSpacerHeightDp = 0.dp
    private var secondSpacerTopPx = 0f
    private var secondSpacerBottomPx = 0f

    @After
    fun cleanUp() {
        firstSpacerHeightDp = 0.dp
        secondSpacerTopPx = 0f
        secondSpacerBottomPx = 0f
    }

    @Test
    fun second_spacer_placed_below_fold_with_hinge() {
        composeTestRule.setContent {
            FoldAwareColumnWithSpacers()
        }

        val foldBoundsPx = simulateFoldingFeature()

        assertEquals(foldBoundsPx.bottom, secondSpacerTopPx)
    }

    @Test
    fun second_spacer_placed_below_fold_with_separating_fold() {
        composeTestRule.setContent {
            FoldAwareColumnWithSpacers()
        }

        val foldBoundsPx = simulateFoldingFeature(foldSizePx = 0)

        assertEquals(foldBoundsPx.bottom, secondSpacerTopPx)
    }

    @Test
    fun second_spacer_placed_below_first_spacer_without_fold() {
        composeTestRule.setContent {
            FoldAwareColumnWithSpacers()
        }

        composeTestRule.onNodeWithTag(testTag).assertTopPositionInRootIsEqualTo(firstSpacerHeightDp)
    }

    @Test
    fun second_spacer_placed_below_first_spacer_with_non_separating_fold() {
        composeTestRule.setContent {
            FoldAwareColumnWithSpacers()
        }

        simulateFoldingFeature(foldSizePx = 0, foldState = FoldingFeature.State.FLAT)

        composeTestRule.onNodeWithTag(testTag).assertTopPositionInRootIsEqualTo(firstSpacerHeightDp)
    }

    @Test
    fun second_spacer_placed_below_first_spacer_with_vertical_hinge() {
        composeTestRule.setContent {
            FoldAwareColumnWithSpacers()
        }

        simulateFoldingFeature(foldOrientation = FoldingFeature.Orientation.VERTICAL)

        composeTestRule.onNodeWithTag(testTag).assertTopPositionInRootIsEqualTo(firstSpacerHeightDp)
    }

    @Test
    fun second_spacer_placed_below_first_spacer_with_ignore_fold_modifier() {
        composeTestRule.setContent {
            FoldAwareColumnWithSpacers(secondSpacerModifier = Modifier.ignoreFold())
        }

        simulateFoldingFeature()

        composeTestRule.onNodeWithTag(testTag).assertTopPositionInRootIsEqualTo(firstSpacerHeightDp)
    }

    @Test
    fun even_fold_padding_modifier_applies_around_hinge() {
        val foldPaddingDp = 20.dp
        lateinit var density: Density

        composeTestRule.setContent {
            density = LocalDensity.current

            FoldAwareColumnWithSpacers(
                foldPadding = PaddingValues(vertical = foldPaddingDp)
            )
        }

        val foldBoundsPx = simulateFoldingFeature()

        with(density) {
            assertEquals(foldBoundsPx.bottom + foldPaddingDp.roundToPx(), secondSpacerTopPx)
        }
    }

    @Test
    fun uneven_fold_padding_modifier_applies_around_hinge() {
        val foldPaddingBottom = 40.dp
        lateinit var density: Density

        composeTestRule.setContent {
            density = LocalDensity.current

            FoldAwareColumnWithSpacers(
                foldPadding = PaddingValues(top = 15.dp, bottom = foldPaddingBottom)
            )
        }

        val foldBoundsPx = simulateFoldingFeature()

        with(density) {
            assertEquals(foldBoundsPx.bottom + foldPaddingBottom.roundToPx(), secondSpacerTopPx)
        }
    }

    @Test
    fun layout_bounds_align_with_child_bounds_without_separating_fold() {
        composeTestRule.setContent {
            FoldAwareColumnWithSpacers()
        }

        val layoutBottomPx = composeTestRule.onRoot()
            .fetchSemanticsNode().layoutInfo.coordinates.trueBoundsInWindow().bottom

        assertEquals(layoutBottomPx, secondSpacerBottomPx)
    }

    @Test
    fun layout_bounds_contain_child_bounds_when_placed_above_hinge() {
        composeTestRule.setContent {
            FoldAwareColumnWithSpacers(
                firstSpacerHeightPct = 0.1f,
                secondSpacerHeightPct = 0.1f
            )
        }

        simulateFoldingFeature()

        val layoutBottomPx = composeTestRule.onRoot()
            .fetchSemanticsNode().layoutInfo.coordinates.trueBoundsInWindow().bottom

        assert(secondSpacerBottomPx <= layoutBottomPx)
    }

    @Test
    fun layout_bounds_contain_child_bounds_when_placed_below_hinge() {
        composeTestRule.setContent {
            FoldAwareColumnWithSpacers()
        }

        simulateFoldingFeature()

        val layoutBottomPx = composeTestRule.onRoot()
            .fetchSemanticsNode().layoutInfo.coordinates.trueBoundsInWindow().bottom

        assert(secondSpacerBottomPx <= layoutBottomPx)
    }

    /**
     * Test layout for FoldAwareColumn that includes two spacers with the provided heights
     */
    @Composable
    @SuppressLint("ModifierParameter")
    private fun FoldAwareColumnWithSpacers(
        foldPadding: PaddingValues = PaddingValues(),
        firstSpacerHeightPct: Float = 0.25f,
        secondSpacerHeightPct: Float = 0.25f,
        secondSpacerModifier: Modifier = Modifier,
    ) {
        var secondSpacerHeightDp: Dp
        val metrics = remember(LocalConfiguration.current) {
            WindowMetricsCalculator.getOrCreate()
                .computeCurrentWindowMetrics(composeTestRule.activity)
        }

        with(LocalDensity.current) {
            val windowHeight = metrics.bounds.height().toDp().value
            firstSpacerHeightDp = (firstSpacerHeightPct * windowHeight).dp
            secondSpacerHeightDp = (secondSpacerHeightPct * windowHeight).dp
        }

        FoldAwareColumn(
            displayFeatures = calculateDisplayFeatures(activity = composeTestRule.activity),
            foldPadding = foldPadding,
        ) {
            Spacer(
                modifier = Modifier.height(firstSpacerHeightDp)
            )
            Spacer(
                modifier = secondSpacerModifier
                    .height(secondSpacerHeightDp)
                    .testTag(testTag)
                    .onGloballyPositioned {
                        secondSpacerTopPx = it.positionInWindow().y
                        secondSpacerBottomPx = secondSpacerTopPx + it.size.height
                    }
            )
        }
    }

    /**
     * Simulates a Jetpack Window Manager folding feature with the provided properties and returns
     * the bounding box of the fold
     */
    private fun simulateFoldingFeature(
        foldSizePx: Int = 25,
        foldState: FoldingFeature.State = FoldingFeature.State.HALF_OPENED,
        foldOrientation: FoldingFeature.Orientation = FoldingFeature.Orientation.HORIZONTAL
    ): Rect {
        val fakeFoldingFeature = FoldingFeature(
            activity = composeTestRule.activity,
            size = foldSizePx,
            state = foldState,
            orientation = foldOrientation,
        )

        publisherRule.overrideWindowLayoutInfo(WindowLayoutInfo(listOf(fakeFoldingFeature)))

        composeTestRule.waitForIdle()

        return fakeFoldingFeature.bounds.toComposeRect()
    }
}
