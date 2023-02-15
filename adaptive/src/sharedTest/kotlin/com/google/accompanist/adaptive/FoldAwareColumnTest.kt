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

import android.graphics.RectF
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toRectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.window.layout.DisplayFeature
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowLayoutInfo
import androidx.window.layout.WindowMetricsCalculator
import androidx.window.testing.layout.FoldingFeature
import androidx.window.testing.layout.WindowLayoutInfoPublisherRule
import com.google.accompanist.adaptive.FoldAwareColumnScopeInstance.ignoreFold
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoldAwareColumnTest {
    private val testTag = "FoldAwareColumnTestTag"

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val publisherRule = WindowLayoutInfoPublisherRule()

    private var windowHeight = 0.dp
    private lateinit var density: Density
    private lateinit var foldBoundsPx: RectF
    private var firstSpacerDp = 0.dp
    private var secondSpacerDp = 0.dp
    private var secondSpacerTopPx = 0f
    private var secondSpacerBoundsPx = RectF()

    @Test
    fun hinge_places_correctly() {
        setUp()

        assertEquals(foldBoundsPx.bottom, secondSpacerTopPx)
    }

    @Test
    fun separating_fold_places_correctly() {
        setUp(foldSizePx = 0)

        assertEquals(foldBoundsPx.bottom, secondSpacerTopPx)
    }

    @Test
    fun no_fold_places_normally() {
        setUp(includeFold = false)

        composeTestRule.onNodeWithTag(testTag).assertTopPositionInRootIsEqualTo(firstSpacerDp)
    }

    @Test
    fun non_separating_fold_places_normally() {
        setUp(foldSizePx = 0, foldState = FoldingFeature.State.FLAT)

        composeTestRule.onNodeWithTag(testTag).assertTopPositionInRootIsEqualTo(firstSpacerDp)
    }

    @Test
    fun vertical_fold_places_normally() {
        setUp(foldOrientation = FoldingFeature.Orientation.VERTICAL)

        composeTestRule.onNodeWithTag(testTag).assertTopPositionInRootIsEqualTo(firstSpacerDp)
    }

    @Test
    fun ignore_fold_modifier_places_correctly() {
        setUp(secondSpacerModifier = Modifier.ignoreFold())

        composeTestRule.onNodeWithTag(testTag).assertTopPositionInRootIsEqualTo(firstSpacerDp)
    }

    @Test
    fun even_fold_padding_modifier_places_correctly_around_fold() {
        val foldPadding = 20.dp
        setUp(foldPadding = PaddingValues(vertical = foldPadding))

        with(density) {
            assertEquals(foldBoundsPx.bottom + foldPadding.toPx(), secondSpacerTopPx)
        }
    }

    @Test
    fun uneven_fold_padding_modifier_places_correctly_around_fold() {
        val foldPaddingBottom = 40.dp
        setUp(foldPadding = PaddingValues(top = 15.dp, bottom = 40.dp))

        with(density) {
            assertEquals(foldBoundsPx.bottom + foldPaddingBottom.toPx(), secondSpacerTopPx)
        }
    }

    @Test
    fun layout_height_matches_child_height_normally() {
        setUp(includeFold = false)

        composeTestRule.onRoot().assertHeightIsEqualTo(firstSpacerDp + secondSpacerDp)
    }

    @Test
    fun layout_height_contains_child_height_with_separating_fold() {
        setUp(firstSpacerPct = 0.1f, secondSpacerPct = 0.1f)

        val childHeightDp = firstSpacerDp + secondSpacerDp
        val layoutHeightDp = with(density) {
            composeTestRule.onRoot().fetchSemanticsNode().boundsInWindow.height.toDp()
        }

        assert(childHeightDp <= layoutHeightDp)
    }

    @Test
    fun layout_height_aligns_with_bottom_child_when_pushed_below_separating_fold() {
        setUp()

        val layoutBottomPx = composeTestRule.onRoot().fetchSemanticsNode().boundsInWindow.bottom

        assertEquals(layoutBottomPx, secondSpacerBoundsPx.bottom)
    }

    private fun setUp(
        secondSpacerModifier: Modifier = Modifier,
        foldPadding: PaddingValues = PaddingValues(),
        horizontalAlignment: Alignment.Horizontal = Alignment.Start,
        firstSpacerPct: Float = 0.25f,
        secondSpacerPct: Float = 0.25f,
        includeFold: Boolean = true,
        foldSizePx: Int = 25,
        foldState: FoldingFeature.State = FoldingFeature.State.HALF_OPENED,
        foldOrientation: FoldingFeature.Orientation = FoldingFeature.Orientation.HORIZONTAL
    ) {
        val fakeFoldingFeature = FoldingFeature(
            activity = composeTestRule.activity,
            size = foldSizePx,
            state = foldState,
            orientation = foldOrientation,
        )

        lateinit var displayFeatures: List<DisplayFeature>

        composeTestRule.setContent {
            density = LocalDensity.current

            displayFeatures = calculateDisplayFeatures(activity = composeTestRule.activity)

            remember(LocalConfiguration.current) {
                val metrics = WindowMetricsCalculator.getOrCreate()
                    .computeCurrentWindowMetrics(composeTestRule.activity)

                with(density) {
                    foldBoundsPx = fakeFoldingFeature.bounds.toRectF()
                    windowHeight = metrics.bounds.height().toDp()
                }

                firstSpacerDp = (firstSpacerPct * windowHeight.value).dp
                secondSpacerDp = (secondSpacerPct * windowHeight.value).dp

                null
            }

            FoldAwareColumn(
                displayFeatures = displayFeatures,
                foldPadding = foldPadding,
                horizontalAlignment = horizontalAlignment
            ) {
                Spacer(
                    modifier = Modifier
                        .height(firstSpacerDp)
                        .width(10.dp)
                )
                Spacer(
                    modifier = secondSpacerModifier
                        .height(secondSpacerDp)
                        .width(10.dp)
                        .testTag(testTag)
                        .onGloballyPositioned {
                            secondSpacerTopPx = it.positionInWindow().y
                            secondSpacerBoundsPx = it
                                .boundsInWindow()
                                .toAndroidRectF()
                        }
                )
            }
        }

        if (includeFold)
            publisherRule.overrideWindowLayoutInfo(WindowLayoutInfo(listOf(fakeFoldingFeature)))

        composeTestRule.waitForIdle()
    }
}
