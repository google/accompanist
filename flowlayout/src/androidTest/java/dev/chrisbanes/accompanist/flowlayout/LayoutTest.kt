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

package dev.chrisbanes.accompanist.flowlayout

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.constrain
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isFinite
import androidx.compose.ui.unit.offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.max

open class LayoutTest {
    @get:Rule
    val rule = createAndroidComposeRule<TestActivity>()

    lateinit var activity: TestActivity
    lateinit var handler: Handler
    internal lateinit var density: Density

    @Before
    fun setup() {
        activity = rule.activity
        density = Density(activity)
        activity.hasFocusLatch.await(5, TimeUnit.SECONDS)

        rule.activity.runOnUiThread {
            handler = Handler(Looper.getMainLooper())
        }
    }

    internal fun Modifier.saveLayoutInfo(
        size: Ref<IntSize>,
        position: Ref<Offset>,
        positionedLatch: CountDownLatch
    ): Modifier = this.onGloballyPositioned { coordinates ->
        size.value = IntSize(coordinates.size.width, coordinates.size.height)
        position.value = coordinates.localToRoot(Offset(0f, 0f))
        positionedLatch.countDown()
    }

    @Composable
    internal fun ConstrainedBox(
        constraints: DpConstraints,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        with(LocalDensity.current) {
            val pxConstraints = Constraints(constraints)
            val measurePolicy = object : MeasurePolicy {
                @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
                override fun MeasureScope.measure(
                    measurables: List<Measurable>,
                    incomingConstraints: Constraints
                ): MeasureResult {
                    val measurable = measurables.firstOrNull()
                    val childConstraints = incomingConstraints.constrain(Constraints(constraints))
                    val placeable = measurable?.measure(childConstraints)

                    val layoutWidth = placeable?.width ?: childConstraints.minWidth
                    val layoutHeight = placeable?.height ?: childConstraints.minHeight
                    return layout(layoutWidth, layoutHeight) {
                        placeable?.placeRelative(0, 0)
                    }
                }

                override fun IntrinsicMeasureScope.minIntrinsicWidth(
                    measurables: List<IntrinsicMeasurable>,
                    height: Int
                ): Int {
                    val width = measurables.firstOrNull()?.minIntrinsicWidth(height) ?: 0
                    return pxConstraints.constrainWidth(width)
                }

                override fun IntrinsicMeasureScope.minIntrinsicHeight(
                    measurables: List<IntrinsicMeasurable>,
                    width: Int
                ): Int {
                    val height = measurables.firstOrNull()?.minIntrinsicHeight(width) ?: 0
                    return pxConstraints.constrainHeight(height)
                }

                override fun IntrinsicMeasureScope.maxIntrinsicWidth(
                    measurables: List<IntrinsicMeasurable>,
                    height: Int
                ): Int {
                    val width = measurables.firstOrNull()?.maxIntrinsicWidth(height) ?: 0
                    return pxConstraints.constrainWidth(width)
                }

                override fun IntrinsicMeasureScope.maxIntrinsicHeight(
                    measurables: List<IntrinsicMeasurable>,
                    width: Int
                ): Int {
                    val height = measurables.firstOrNull()?.maxIntrinsicHeight(width) ?: 0
                    return pxConstraints.constrainHeight(height)
                }
            }
            Layout(
                content = content,
                modifier = modifier,
                measurePolicy = measurePolicy
            )
        }
    }

    /**
     * Similar to [Constraints], but with constraint values expressed in [Dp].
     */
    @Immutable
    data class DpConstraints(
        @Stable
        val minWidth: Dp = 0.dp,
        @Stable
        val maxWidth: Dp = Dp.Infinity,
        @Stable
        val minHeight: Dp = 0.dp,
        @Stable
        val maxHeight: Dp = Dp.Infinity
    ) {
        init {
            require(minWidth.isFinite) { "Constraints#minWidth should be finite" }
            require(minHeight.isFinite) { "Constraints#minHeight should be finite" }
            require(!minWidth.value.isNaN()) { "Constraints#minWidth should not be NaN" }
            require(!maxWidth.value.isNaN()) { "Constraints#maxWidth should not be NaN" }
            require(!minHeight.value.isNaN()) { "Constraints#minHeight should not be NaN" }
            require(!maxHeight.value.isNaN()) { "Constraints#maxHeight should not be NaN" }
            require(minWidth <= maxWidth) {
                "Constraints should be satisfiable, but minWidth > maxWidth"
            }
            require(minHeight <= maxHeight) {
                "Constraints should be satisfiable, but minHeight > maxHeight"
            }
            require(minWidth >= 0.dp) { "Constraints#minWidth should be non-negative" }
            require(maxWidth >= 0.dp) { "Constraints#maxWidth should be non-negative" }
            require(minHeight >= 0.dp) { "Constraints#minHeight should be non-negative" }
            require(maxHeight >= 0.dp) { "Constraints#maxHeight should be non-negative" }
        }
    }

    /**
     * Creates the [Constraints] corresponding to the current [DpConstraints].
     */
    @Stable
    fun Density.Constraints(dpConstraints: DpConstraints) = Constraints(
        minWidth = dpConstraints.minWidth.roundToPx(),
        maxWidth = dpConstraints.maxWidth.roundToPx(),
        minHeight = dpConstraints.minHeight.roundToPx(),
        maxHeight = dpConstraints.maxHeight.roundToPx()
    )

    internal fun assertEquals(expected: Offset?, actual: Offset?) {
        assertNotNull("Null expected position", expected)
        expected as Offset
        assertNotNull("Null actual position", actual)
        actual as Offset

        assertEquals(
            "Expected x ${expected.x} but obtained ${actual.x}",
            expected.x,
            actual.x,
            0f
        )
        assertEquals(
            "Expected y ${expected.y} but obtained ${actual.y}",
            expected.y,
            actual.y,
            0f
        )
        if (actual.x != actual.x.toInt().toFloat()) {
            fail("Expected integer x coordinate")
        }
        if (actual.y != actual.y.toInt().toFloat()) {
            fail("Expected integer y coordinate")
        }
    }

    @Composable
    internal fun Container(
        modifier: Modifier = Modifier,
        padding: PaddingValues = PaddingValues(0.dp),
        alignment: Alignment = Alignment.Center,
        expanded: Boolean = false,
        constraints: DpConstraints = DpConstraints(),
        width: Dp? = null,
        height: Dp? = null,
        content: @Composable () -> Unit
    ) {
        Layout(content, modifier) { measurables, incomingConstraints ->
            val containerConstraints = incomingConstraints.constrain(
                Constraints(constraints)
                    .copy(
                        width?.roundToPx() ?: constraints.minWidth.roundToPx(),
                        width?.roundToPx() ?: constraints.maxWidth.roundToPx(),
                        height?.roundToPx() ?: constraints.minHeight.roundToPx(),
                        height?.roundToPx() ?: constraints.maxHeight.roundToPx()
                    )
            )
            val totalHorizontal = padding.calculateLeftPadding(layoutDirection).roundToPx() +
                padding.calculateRightPadding(layoutDirection).roundToPx()
            val totalVertical = padding.calculateTopPadding().roundToPx() +
                padding.calculateBottomPadding().roundToPx()
            val childConstraints = containerConstraints
                .copy(minWidth = 0, minHeight = 0)
                .offset(-totalHorizontal, -totalVertical)
            var placeable: Placeable? = null
            val containerWidth = if ((containerConstraints.hasFixedWidth || expanded) &&
                containerConstraints.hasBoundedWidth
            ) {
                containerConstraints.maxWidth
            } else {
                placeable = measurables.firstOrNull()?.measure(childConstraints)
                max((placeable?.width ?: 0) + totalHorizontal, containerConstraints.minWidth)
            }
            val containerHeight = if ((containerConstraints.hasFixedHeight || expanded) &&
                containerConstraints.hasBoundedHeight
            ) {
                containerConstraints.maxHeight
            } else {
                if (placeable == null) {
                    placeable = measurables.firstOrNull()?.measure(childConstraints)
                }
                max((placeable?.height ?: 0) + totalVertical, containerConstraints.minHeight)
            }
            layout(containerWidth, containerHeight) {
                val p = placeable ?: measurables.firstOrNull()?.measure(childConstraints)
                p?.let {
                    val position = alignment.align(
                        IntSize(it.width + totalHorizontal, it.height + totalVertical),
                        IntSize(containerWidth, containerHeight),
                        layoutDirection
                    )
                    it.place(
                        padding.calculateLeftPadding(layoutDirection).roundToPx() + position.x,
                        padding.calculateTopPadding().roundToPx() + position.y
                    )
                }
            }
        }
    }
}
