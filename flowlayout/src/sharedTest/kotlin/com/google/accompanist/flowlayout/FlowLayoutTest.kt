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

package com.google.accompanist.flowlayout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class FlowTest : LayoutTest() {
    @Test
    fun testFlowRow() {
        val numberOfSquares = 15
        val size = 48
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxWidth = with(LocalDensity.current) { flowWidth.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 5, height = size * 3),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(x = (size * (i % 5)).toFloat(), y = (size * (i / 5)).toFloat()),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowRow_withMainAxisSize_wrap() {
        val numberOfSquares = 15
        val size = 48
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxWidth = with(LocalDensity.current) { flowWidth.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(mainAxisSize = SizeMode.Wrap) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 5, height = size * 3),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i % 5)).toFloat(),
                    y = (size * (i / 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowRow_withMainAxisSize_expand() {
        val numberOfSquares = 15
        val size = 48
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxWidth = with(LocalDensity.current) { flowWidth.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(mainAxisSize = SizeMode.Expand) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = flowWidth, height = size * 3),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i % 5)).toFloat(),
                    y = (size * (i / 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowRow_withMainAxisAlignment_center() {
        val numberOfSquares = 15
        val size = 48
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxWidth = with(LocalDensity.current) { flowWidth.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.Center
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = flowWidth, height = size * 3),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = ((flowWidth - size * 5) / 2 + size * (i % 5)).toFloat(),
                    y = (size * (i / 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowRow_withMainAxisAlignment_start() {
        val numberOfSquares = 15
        val size = 48
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxWidth = with(LocalDensity.current) { flowWidth.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.Start
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = flowWidth, height = size * 3),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i % 5)).toFloat(),
                    y = (size * (i / 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowRow_withMainAxisAlignment_end() {
        val numberOfSquares = 15
        val size = 48
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxWidth = with(LocalDensity.current) { flowWidth.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.End
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = flowWidth, height = size * 3),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (flowWidth - size * 5 + size * (i % 5)).toFloat(),
                    y = (size * (i / 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowRow_withMainAxisAlignment_spaceEvenly() {
        val numberOfSquares = 15
        val size = 48
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxWidth = with(LocalDensity.current) { flowWidth.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceEvenly
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = flowWidth, height = size * 3),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            val x = ((flowWidth - size * 5) * (i % 5 + 1) / 6f).roundToInt() + size * (i % 5)
            assertEquals(
                Offset(
                    x = x.toFloat(),
                    y = (size * (i / 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowRow_withMainAxisAlignment_spaceBetween() {
        val numberOfSquares = 15
        val size = 48
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxWidth = with(LocalDensity.current) { flowWidth.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = flowWidth, height = size * 3),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = ((flowWidth - size * 5) * (i % 5) / 4 + size * (i % 5)).toFloat(),
                    y = (size * (i / 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowRow_withMainAxisAlignment_spaceAround() {
        val numberOfSquares = 15
        val size = 48
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxWidth = with(LocalDensity.current) { flowWidth.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceAround
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = flowWidth, height = size * 3),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = ((flowWidth - size * 5) * (i % 5 + 0.5f) / 5 + size * (i % 5)).roundToInt()
                        .toFloat(),
                    y = (size * (i / 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowRow_withLastLineMainAxisAlignment_justify_center() {
        val numberOfSquares = 15
        val size = 48
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxWidth = with(LocalDensity.current) { flowWidth.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
                        lastLineMainAxisAlignment = FlowMainAxisAlignment.Center
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(flowWidth, size * 3),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = if (i < 10) {
                        ((flowWidth - size * 5) * (i % 5) / 4 + size * (i % 5)).toFloat()
                    } else {
                        ((flowWidth - size * 5) / 2 + size * (i % 5)).toFloat()
                    },
                    y = (size * (i / 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowRow_withLastLineMainAxisAlignment_justify_start() {
        val numberOfSquares = 15
        val size = 48
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxWidth = with(LocalDensity.current) { flowWidth.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
                        lastLineMainAxisAlignment = FlowMainAxisAlignment.Start
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(flowWidth, size * 3),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = if (i < 10) {
                        ((flowWidth - size * 5) * (i % 5) / 4 + size * (i % 5)).toFloat()
                    } else {
                        (size * (i % 5)).toFloat()
                    },
                    y = (size * (i / 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowRow_withLastLineMainAxisAlignment_justify_end() {
        val numberOfSquares = 15
        val size = 48
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxWidth = with(LocalDensity.current) { flowWidth.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
                        lastLineMainAxisAlignment = FlowMainAxisAlignment.End
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(flowWidth, size * 3),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = if (i < 10) {
                        ((flowWidth - size * 5) * (i % 5) / 4 + size * (i % 5)).toFloat()
                    } else {
                        ((flowWidth - size * 5) + size * (i % 5)).toFloat()
                    },
                    y = (size * (i / 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowRow_withMainAxisSpacing() {
        val numberOfSquares = 15
        val size = 48
        val spacing = 32
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxWidth = with(LocalDensity.current) { flowWidth.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(mainAxisSpacing = with(LocalDensity.current) { spacing.toDp() }) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 3 + spacing * 2, height = size * 5),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = ((size + spacing) * (i % 3)).toFloat(),
                    y = (size * (i / 3)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowRow_withCrossAxisAlignment_center() {
        val numberOfSquares = 15
        val size = 48
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxWidth = with(LocalDensity.current) { flowWidth.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(crossAxisAlignment = FlowCrossAxisAlignment.Center) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = if (i % 2 == 0) with(LocalDensity.current) { size.toDp() } else with(
                                    LocalDensity.current
                                ) { size.toDp() } * 2,
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 5, height = size * 6),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(
                    width = size,
                    height = if (i % 2 == 0) size else size * 2
                ),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i % 5)).toFloat(),
                    y = (size * 2 * (i / 5) + if (i % 2 == 0) size / 2 else 0)
                        .toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowRow_withCrossAxisAlignment_start() {
        val numberOfSquares = 15
        val size = 48
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxWidth = with(LocalDensity.current) { flowWidth.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(crossAxisAlignment = FlowCrossAxisAlignment.Start) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = if (i % 2 == 0) with(LocalDensity.current) { size.toDp() } else with(
                                    LocalDensity.current
                                ) { size.toDp() } * 2,
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 5, height = size * 6),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(
                    width = size,
                    height = if (i % 2 == 0) size else size * 2
                ),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i % 5)).toFloat(),
                    y = (size * 2 * (i / 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowRow_withCrossAxisAlignment_end() {
        val numberOfSquares = 15
        val size = 48
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxWidth = with(LocalDensity.current) { flowWidth.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(crossAxisAlignment = FlowCrossAxisAlignment.End) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = if (i % 2 == 0) with(LocalDensity.current) { size.toDp() } else with(
                                    LocalDensity.current
                                ) { size.toDp() } * 2,
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 5, height = size * 6),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(
                    width = size,
                    height = if (i % 2 == 0) size else size * 2
                ),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i % 5)).toFloat(),
                    y = (size * 2 * (i / 5) + if (i % 2 == 0) size else 0).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowRow_withCrossAxisSpacing() {
        val numberOfSquares = 15
        val size = 48
        val spacing = 32
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxWidth = with(LocalDensity.current) { flowWidth.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(crossAxisSpacing = with(LocalDensity.current) { spacing.toDp() }) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 5, height = size * 3 + spacing * 2),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i % 5)).toFloat(),
                    y = ((size + spacing) * (i / 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowRow_withMaxLines() {
        val requestedNumberOfSquares = 15
        val size = 48
        val maxLines = 2
        val spacing = 32
        val flowWidth = 256

        val squaresInRow = flowWidth / size
        val expectedNumberOfSquares = 10

        val flowSize = Ref<IntSize>()
        val childSize = Array(requestedNumberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(requestedNumberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(expectedNumberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(
                        maxWidth = with(LocalDensity.current) { flowWidth.toDp() }
                    ),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(
                        maxLines = maxLines,
                        crossAxisSpacing = with(LocalDensity.current) { spacing.toDp() }
                    ) {
                        for (i in 0 until requestedNumberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(
                width = size * squaresInRow,
                height = size * maxLines + spacing * (maxLines - 1)
            ),
            flowSize.value
        )
        for (i in 0 until requestedNumberOfSquares) {
            val squareSize = childSize[i].value
            val squarePosition = childPosition[i].value
            if (i < expectedNumberOfSquares) {
                assertEquals(
                    IntSize(width = size, height = size),
                    squareSize
                )

                assertEquals(
                    Offset(
                        x = (size * (i % squaresInRow)).toFloat(),
                        y = ((size + spacing) * (i / squaresInRow)).toFloat()
                    ),
                    squarePosition
                )
            } else {
                assertNull(squareSize)
                assertNull(squarePosition)
            }
        }
    }

    @Test
    fun testFlowRow_withMaxLines_negative() {
        val requestedNumberOfSquares = 15
        val size = 48
        val maxLines = -1
        val spacing = 32
        val flowWidth = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(requestedNumberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(requestedNumberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(
                        maxWidth = with(LocalDensity.current) { flowWidth.toDp() }
                    ),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(
                        maxLines = maxLines,
                        crossAxisSpacing = with(LocalDensity.current) { spacing.toDp() }
                    ) {
                        for (i in 0 until requestedNumberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = 0, height = 0),
            flowSize.value
        )
        for (i in 0 until requestedNumberOfSquares) {
            assertNull(childSize[i].value)
            assertNull(childPosition[i].value)
        }
    }

    @Test
    fun testFlowRow_withMaxLines_fillMaxHeight() {
        val requestedNumberOfSquares = 15
        val size = 48
        val maxLines = 2
        val spacing = 32
        val flowWidth = 256
        val flowHeight = 1000

        val squaresInRow = flowWidth / size
        val expectedNumberOfSquares = 10

        val flowSize = Ref<IntSize>()
        val childSize = Array(requestedNumberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(requestedNumberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(expectedNumberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(
                        maxWidth = with(LocalDensity.current) { flowWidth.toDp() },
                        maxHeight =  with(LocalDensity.current) { flowHeight.toDp() },
                    ),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxHeight(),
                        maxLines = maxLines,
                        crossAxisSpacing = with(LocalDensity.current) { spacing.toDp() }
                    ) {
                        for (i in 0 until requestedNumberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * squaresInRow, height = flowHeight),
            flowSize.value
        )
        for (i in 0 until requestedNumberOfSquares) {
            val squareSize = childSize[i].value
            val squarePosition = childPosition[i].value
            if (i < expectedNumberOfSquares) {
                assertEquals(
                    IntSize(width = size, height = size),
                    squareSize
                )

                assertEquals(
                    Offset(
                        x = (size * (i % squaresInRow)).toFloat(),
                        y = ((size + spacing) * (i / squaresInRow)).toFloat()
                    ),
                    squarePosition
                )
            } else {
                assertNull(squareSize)
                assertNull(squarePosition)
            }
        }
    }

    @Test
    fun testFlowRow_withMaxHeight() {
        val requestedNumberOfSquares = 15
        val size = 48
        val spacing = 32
        val flowWidth = 256
        val flowHeight = (size * 2.5f + spacing * 2).roundToInt()

        val squaresInRow = flowWidth / size
        val expectedNumberOfSquares = 10

        val flowSize = Ref<IntSize>()
        val childSize = Array(requestedNumberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(requestedNumberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(expectedNumberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(
                        maxWidth = with(LocalDensity.current) { flowWidth.toDp() },
                        maxHeight = with(LocalDensity.current) { flowHeight.toDp() },
                    ),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowRow(
                        crossAxisSpacing = with(LocalDensity.current) { spacing.toDp() }
                    ) {
                        for (i in 0 until requestedNumberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * squaresInRow, height = size * 2 + spacing),
            flowSize.value
        )
        for (i in 0 until requestedNumberOfSquares) {
            val squareSize = childSize[i].value
            val squarePosition = childPosition[i].value
            if (i < expectedNumberOfSquares) {
                assertEquals(
                    IntSize(width = size, height = size),
                    squareSize
                )

                assertEquals(
                    Offset(
                        x = (size * (i % squaresInRow)).toFloat(),
                        y = ((size + spacing) * (i / squaresInRow)).toFloat()
                    ),
                    squarePosition
                )
            } else {
                assertNull(squareSize)
                assertNull(squarePosition)
            }
        }
    }

    @Test
    fun testFlowColumn() {
        val numberOfSquares = 15
        val size = 48
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 3, height = size * 5),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i / 5)).toFloat(),
                    y = (size * (i % 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowColumn_withMainAxisSize_wrap() {
        val numberOfSquares = 15
        val size = 48
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(mainAxisSize = SizeMode.Wrap) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 3, height = size * 5),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i / 5)).toFloat(),
                    y = (size * (i % 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowColumn_withMainAxisSize_expand() {
        val numberOfSquares = 15
        val size = 48
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(mainAxisSize = SizeMode.Expand) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 3, height = flowHeight),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i / 5)).toFloat(),
                    y = (size * (i % 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowColumn_withMainAxisAlignment_center() {
        val numberOfSquares = 15
        val size = 48
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.Center
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 3, height = flowHeight),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i / 5)).toFloat(),
                    y = ((flowHeight - size * 5) / 2 + size * (i % 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowColumn_withMainAxisAlignment_start() {
        val numberOfSquares = 15
        val size = 48
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.Start
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 3, height = flowHeight),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i / 5)).toFloat(),
                    y = (size * (i % 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowColumn_withMainAxisAlignment_end() {
        val numberOfSquares = 15
        val size = 48
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.End
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 3, height = flowHeight),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i / 5)).toFloat(),
                    y = (flowHeight - size * 5 + size * (i % 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowColumn_withMainAxisAlignment_spaceEvenly() {
        val numberOfSquares = 15
        val size = 48
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceEvenly
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 3, height = flowHeight),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            val y = ((flowHeight - size * 5) * (i % 5 + 1) / 6f).roundToInt() + size * (i % 5)
            assertEquals(
                Offset(
                    x = (size * (i / 5)).toFloat(),
                    y = y.toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowColumn_withMainAxisAlignment_spaceBetween() {
        val numberOfSquares = 15
        val size = 48
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 3, height = flowHeight),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i / 5)).toFloat(),
                    y = ((flowHeight - size * 5) * (i % 5) / 4 + size * (i % 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowColumn_withMainAxisAlignment_spaceAround() {
        val numberOfSquares = 15
        val size = 48
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceAround
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 3, height = flowHeight),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i / 5)).toFloat(),
                    y = ((flowHeight - size * 5) * (i % 5 + 0.5f) / 5 + size * (i % 5)).roundToInt()
                        .toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowColumn_withLastLineMainAxisAlignment_justify_center() {
        val numberOfSquares = 15
        val size = 48
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
                        lastLineMainAxisAlignment = FlowMainAxisAlignment.Center
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(size * 3, flowHeight),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i / 5)).toFloat(),
                    y = if (i < 10) {
                        ((flowHeight - size * 5) * (i % 5) / 4 + size * (i % 5)).toFloat()
                    } else {
                        ((flowHeight - size * 5) / 2 + size * (i % 5)).toFloat()
                    }
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowColumn_withLastLineMainAxisAlignment_justify_start() {
        val numberOfSquares = 15
        val size = 48
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
                        lastLineMainAxisAlignment = FlowMainAxisAlignment.Start
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(size * 3, flowHeight),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i / 5)).toFloat(),
                    y = if (i < 10) {
                        ((flowHeight - size * 5) * (i % 5) / 4 + size * (i % 5)).toFloat()
                    } else {
                        (size * (i % 5)).toFloat()
                    }
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowColumn_withLastLineMainAxisAlignment_justify_end() {
        val numberOfSquares = 15
        val size = 48
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
                        lastLineMainAxisAlignment = FlowMainAxisAlignment.End
                    ) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(size * 3, flowHeight),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i / 5)).toFloat(),
                    y = if (i < 10) {
                        ((flowHeight - size * 5) * (i % 5) / 4 + size * (i % 5)).toFloat()
                    } else {
                        ((flowHeight - size * 5) + size * (i % 5)).toFloat()
                    }
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowColumn_withMainAxisSpacing() {
        val numberOfSquares = 15
        val size = 48
        val spacing = 32
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(mainAxisSpacing = with(LocalDensity.current) { spacing.toDp() }) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 5, height = size * 3 + spacing * 2),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * (i / 3)).toFloat(),
                    y = ((size + spacing) * (i % 3)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowColumn_withCrossAxisAlignment_center() {
        val numberOfSquares = 15
        val size = 48
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(crossAxisAlignment = FlowCrossAxisAlignment.Center) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = if (i % 2 == 0) with(LocalDensity.current) { size.toDp() } else with(
                                    LocalDensity.current
                                ) { size.toDp() } * 2,
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 6, height = size * 5),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(
                    width = if (i % 2 == 0) size else size * 2,
                    height = size
                ),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * 2 * (i / 5) + if (i % 2 == 0) size / 2 else 0)
                        .toFloat(),
                    y = (size * (i % 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowColumn_withCrossAxisAlignment_start() {
        val numberOfSquares = 15
        val size = 48
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(crossAxisAlignment = FlowCrossAxisAlignment.Start) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = if (i % 2 == 0) with(LocalDensity.current) { size.toDp() } else with(
                                    LocalDensity.current
                                ) { size.toDp() } * 2,
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 6, height = size * 5),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = if (i % 2 == 0) size else size * 2, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = (size * 2 * (i / 5)).toFloat(),
                    y = (size * (i % 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowColumn_withCrossAxisAlignment_end() {
        val numberOfSquares = 15
        val size = 48
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(crossAxisAlignment = FlowCrossAxisAlignment.End) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = if (i % 2 == 0) with(LocalDensity.current) { size.toDp() } else with(
                                    LocalDensity.current
                                ) { size.toDp() } * 2,
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 6, height = size * 5),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(
                    width = if (i % 2 == 0) size else size * 2,
                    height = size
                ),
                childSize[i].value
            )
            val x = (size * 2 * (i / 5) + if (i % 2 == 0) size else 0)
            assertEquals(
                Offset(
                    x = x.toFloat(),
                    y = (size * (i % 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowColumn_withCrossAxisSpacing() {
        val numberOfSquares = 15
        val size = 48
        val spacing = 32
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(numberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(numberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(numberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(crossAxisSpacing = with(LocalDensity.current) { spacing.toDp() }) {
                        for (i in 0 until numberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 3 + spacing * 2, height = size * 5),
            flowSize.value
        )
        for (i in 0 until numberOfSquares) {
            assertEquals(
                IntSize(width = size, height = size),
                childSize[i].value
            )
            assertEquals(
                Offset(
                    x = ((size + spacing) * (i / 5)).toFloat(),
                    y = (size * (i % 5)).toFloat()
                ),
                childPosition[i].value
            )
        }
    }

    @Test
    fun testFlowColumn_withMaxLines() {
        val requestedNumberOfSquares = 15
        val size = 48
        val maxLines = 2
        val spacing = 32
        val flowHeight = 256

        val squaresInColumn = flowHeight / size
        val expectedNumberOfSquares = 10

        val flowSize = Ref<IntSize>()
        val childSize = Array(requestedNumberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(requestedNumberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(expectedNumberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(
                        maxLines = maxLines,
                        crossAxisSpacing = with(LocalDensity.current) { spacing.toDp() }
                    ) {
                        for (i in 0 until requestedNumberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(
                width = size * maxLines + spacing * (maxLines - 1),
                height = size * squaresInColumn,
            ),
            flowSize.value
        )
        for (i in 0 until requestedNumberOfSquares) {
            val squareSize = childSize[i].value
            val squarePosition = childPosition[i].value
            if (i < expectedNumberOfSquares) {
                assertEquals(
                    IntSize(width = size, height = size),
                    squareSize
                )

                assertEquals(
                    Offset(
                        x = ((size + spacing) * (i / squaresInColumn)).toFloat(),
                        y = (size * (i % squaresInColumn)).toFloat()
                    ),
                    squarePosition
                )
            } else {
                assertNull(squareSize)
                assertNull(squarePosition)
            }
        }
    }

    @Test
    fun testFlowColumn_withMaxLines_negative() {
        val requestedNumberOfSquares = 15
        val size = 48
        val maxLines = -1
        val spacing = 32
        val flowHeight = 256

        val flowSize = Ref<IntSize>()
        val childSize = Array(requestedNumberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(requestedNumberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch( 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(maxHeight = with(LocalDensity.current) { flowHeight.toDp() }),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(
                        maxLines = maxLines,
                        crossAxisSpacing = with(LocalDensity.current) { spacing.toDp() }
                    ) {
                        for (i in 0 until requestedNumberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = 0, height = 0),
            flowSize.value
        )
        for (i in 0 until requestedNumberOfSquares) {
            assertNull(childSize[i].value)
            assertNull(childPosition[i].value)
        }
    }

    @Test
    fun testFlowColumn_withMaxLines_fillMaxWidth() {
        val requestedNumberOfSquares = 15
        val size = 48
        val maxLines = 2
        val spacing = 32
        val flowWidth = 1000
        val flowHeight = 256

        val squaresInColumn = flowHeight / size
        val expectedNumberOfSquares = 10

        val flowSize = Ref<IntSize>()
        val childSize = Array(requestedNumberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(requestedNumberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(expectedNumberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(
                        maxWidth = with(LocalDensity.current) { flowWidth.toDp() },
                        maxHeight = with(LocalDensity.current) { flowHeight.toDp() }
                    ),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = maxLines,
                        crossAxisSpacing = with(LocalDensity.current) { spacing.toDp() }
                    ) {
                        for (i in 0 until requestedNumberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = flowWidth, height = size * squaresInColumn),
            flowSize.value
        )
        for (i in 0 until requestedNumberOfSquares) {
            val squareSize = childSize[i].value
            val squarePosition = childPosition[i].value
            if (i < expectedNumberOfSquares) {
                assertEquals(
                    IntSize(width = size, height = size),
                    squareSize
                )

                assertEquals(
                    Offset(
                        x = ((size + spacing) * (i / squaresInColumn)).toFloat(),
                        y = (size * (i % squaresInColumn)).toFloat()
                    ),
                    squarePosition
                )
            } else {
                assertNull(squareSize)
                assertNull(squarePosition)
            }
        }
    }

    @Test
    fun testFlowColumn_withMaxWidth() {
        val requestedNumberOfSquares = 15
        val size = 48
        val spacing = 32
        val flowWidth = (size * 2.5f + spacing * 2).roundToInt()
        val flowHeight = 256

        val squaresInColumn = flowHeight / size
        val expectedNumberOfSquares = 10

        val flowSize = Ref<IntSize>()
        val childSize = Array(requestedNumberOfSquares) { Ref<IntSize>() }
        val childPosition = Array(requestedNumberOfSquares) { Ref<Offset>() }
        val positionedLatch = CountDownLatch(expectedNumberOfSquares + 1)

        rule.setContent {
            Box {
                ConstrainedBox(
                    constraints = DpConstraints(
                        maxWidth = with(LocalDensity.current) { flowWidth.toDp() },
                        maxHeight = with(LocalDensity.current) { flowHeight.toDp() }
                    ),
                    modifier = Modifier.onGloballyPositioned { coordinates: LayoutCoordinates ->
                        flowSize.value = coordinates.size
                        positionedLatch.countDown()
                    }
                ) {
                    FlowColumn(crossAxisSpacing = with(LocalDensity.current) { spacing.toDp() }) {
                        for (i in 0 until requestedNumberOfSquares) {
                            Container(
                                width = with(LocalDensity.current) { size.toDp() },
                                height = with(LocalDensity.current) { size.toDp() },
                                modifier = Modifier.saveLayoutInfo(
                                    childSize[i],
                                    childPosition[i],
                                    positionedLatch
                                )
                            ) {
                            }
                        }
                    }
                }
            }
        }

        assertTrue(positionedLatch.await(1, TimeUnit.SECONDS))

        assertEquals(
            IntSize(width = size * 2 + spacing, height = size * squaresInColumn),
            flowSize.value
        )
        for (i in 0 until requestedNumberOfSquares) {
            val squareSize = childSize[i].value
            val squarePosition = childPosition[i].value
            if (i < expectedNumberOfSquares) {
                assertEquals(
                    IntSize(width = size, height = size),
                    squareSize
                )

                assertEquals(
                    Offset(
                        x = ((size + spacing) * (i / squaresInColumn)).toFloat(),
                        y = (size * (i % squaresInColumn)).toFloat()
                    ),
                    squarePosition
                )
            } else {
                assertNull(squareSize)
                assertNull(squarePosition)
            }
        }
    }
}
