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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.LayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.then
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toIntRect
import androidx.compose.ui.unit.toOffset
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.window.core.ExperimentalWindowApi
import androidx.window.layout.DisplayFeature
import androidx.window.layout.FoldingFeature
import androidx.window.testing.layout.FoldingFeature
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class TwoPaneTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun fraction_horizontal_renders_correctly_ltr() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp)) then
                    DeviceConfigurationOverride.LayoutDirection(LayoutDirection.Ltr)
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = FractionHorizontalTwoPaneStrategy(
                        splitFraction = 1f / 3f
                    ),
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(300.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(300.dp, 0.dp),
                    DpSize(600.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun fraction_horizontal_renders_correctly_rtl() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp)) then
                    DeviceConfigurationOverride.LayoutDirection(LayoutDirection.Rtl)
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = FractionHorizontalTwoPaneStrategy(
                        splitFraction = 1f / 3f
                    ),
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(600.dp, 0.dp),
                    DpSize(300.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(600.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun fraction_horizontal_renders_correctly_with_split_width_ltr() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp)) then
                    DeviceConfigurationOverride.LayoutDirection(LayoutDirection.Ltr)
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = FractionHorizontalTwoPaneStrategy(
                        splitFraction = 1f / 3f,
                        gapWidth = 64.dp
                    ),
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(268.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(332.dp, 0.dp),
                    DpSize(568.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun fraction_horizontal_renders_correctly_with_split_width_rtl() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp)) then
                    DeviceConfigurationOverride.LayoutDirection(LayoutDirection.Rtl)
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = FractionHorizontalTwoPaneStrategy(
                        splitFraction = 1f / 3f,
                        gapWidth = 64.dp
                    ),
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(632.dp, 0.dp),
                    DpSize(268.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(568.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun fraction_vertical_renders_correctly() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp))
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = FractionVerticalTwoPaneStrategy(
                        splitFraction = 1f / 3f
                    ),
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(900.dp, 400.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 400.dp),
                    DpSize(900.dp, 800.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun fraction_vertical_renders_correctly_with_split_height() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp))
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = FractionVerticalTwoPaneStrategy(
                        splitFraction = 1f / 3f,
                        gapHeight = 64.dp
                    ),
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(900.dp, 368.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 432.dp),
                    DpSize(900.dp, 768.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun fixed_offset_horizontal_from_start_horizontal_renders_correctly_ltr() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp)) then
                    DeviceConfigurationOverride.LayoutDirection(LayoutDirection.Ltr)
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = FixedOffsetHorizontalTwoPaneStrategy(
                        splitOffset = 200.dp,
                        offsetFromStart = true,
                    ),
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(200.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(200.dp, 0.dp),
                    DpSize(700.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun fixed_offset_horizontal_from_start_horizontal_renders_correctly_rtl() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp)) then
                    DeviceConfigurationOverride.LayoutDirection(LayoutDirection.Rtl)
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = FixedOffsetHorizontalTwoPaneStrategy(
                        splitOffset = 200.dp,
                        offsetFromStart = true,
                    ),
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(700.dp, 0.dp),
                    DpSize(200.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(700.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun fixed_offset_horizontal_from_start_renders_correctly_with_split_width_ltr() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp)) then
                    DeviceConfigurationOverride.LayoutDirection(LayoutDirection.Ltr)
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = FixedOffsetHorizontalTwoPaneStrategy(
                        splitOffset = 200.dp,
                        offsetFromStart = true,
                        gapWidth = 64.dp
                    ),
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(168.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(232.dp, 0.dp),
                    DpSize(668.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun fixed_offset_horizontal_from_start_renders_correctly_with_split_width_rtl() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp)) then
                    DeviceConfigurationOverride.LayoutDirection(LayoutDirection.Rtl)
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = FixedOffsetHorizontalTwoPaneStrategy(
                        splitOffset = 200.dp,
                        offsetFromStart = true,
                        gapWidth = 64.dp
                    ),
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(732.dp, 0.dp),
                    DpSize(168.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(668.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun fixed_offset_vertical_from_top_renders_correctly() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp))
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = FixedOffsetVerticalTwoPaneStrategy(
                        splitOffset = 300.dp,
                        offsetFromTop = true
                    ),
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(900.dp, 300.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 300.dp),
                    DpSize(900.dp, 900.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun fixed_offset_vertical_from_top_renders_correctly_with_split_height() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp))
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = FixedOffsetVerticalTwoPaneStrategy(
                        splitOffset = 300.dp,
                        offsetFromTop = true,
                        gapHeight = 64.dp
                    ),
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(900.dp, 268.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 332.dp),
                    DpSize(900.dp, 868.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun fixed_offset_vertical_from_bottom_renders_correctly() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp))
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = FixedOffsetVerticalTwoPaneStrategy(
                        splitOffset = 300.dp,
                        offsetFromTop = false
                    ),
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(900.dp, 900.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 900.dp),
                    DpSize(900.dp, 300.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun fixed_offset_vertical_from_bottom_renders_correctly_with_split_height() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp))
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = FixedOffsetVerticalTwoPaneStrategy(
                        splitOffset = 300.dp,
                        offsetFromTop = false,
                        gapHeight = 64.dp
                    ),
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(900.dp, 868.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 932.dp),
                    DpSize(900.dp, 268.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun two_pane_strategy_uses_fallback_when_no_fold_present() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates
        val displayFeatures = DelegateList {
            fakeDisplayFeatures(
                density = density,
                twoPaneCoordinates = twoPaneCoordinates,
                localFoldingFeatures = emptyList()
            )
        }

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp))
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = VerticalTwoPaneStrategy(
                        splitFraction = 1f / 3f
                    ),
                    displayFeatures = displayFeatures,
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(900.dp, 400.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 400.dp),
                    DpSize(900.dp, 800.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun two_pane_strategy_uses_vertical_placing_when_occluding_horizontal_fold_present() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates
        val displayFeatures = DelegateList {
            fakeDisplayFeatures(
                density = density,
                twoPaneCoordinates = twoPaneCoordinates,
                localFoldingFeatures = listOf(
                    LocalFoldingFeature(
                        center = 600.dp,
                        size = 0.dp,
                        state = FoldingFeature.State.HALF_OPENED,
                        orientation = FoldingFeature.Orientation.HORIZONTAL
                    )
                )
            )
        }

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp))
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = VerticalTwoPaneStrategy(
                        splitFraction = 1f / 3f
                    ),
                    displayFeatures = displayFeatures,
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(900.dp, 600.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 600.dp),
                    DpSize(900.dp, 600.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun two_pane_strategy_uses_vertical_placing_when_separating_horizontal_fold_present() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates
        val displayFeatures = DelegateList {
            fakeDisplayFeatures(
                density = density,
                twoPaneCoordinates = twoPaneCoordinates,
                localFoldingFeatures = listOf(
                    LocalFoldingFeature(
                        center = 600.dp,
                        size = 60.dp,
                        state = FoldingFeature.State.FLAT,
                        orientation = FoldingFeature.Orientation.HORIZONTAL
                    )
                )
            )
        }

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp))
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = VerticalTwoPaneStrategy(
                        splitFraction = 1f / 3f
                    ),
                    displayFeatures = displayFeatures,
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(900.dp, 570.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 630.dp),
                    DpSize(900.dp, 570.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun two_pane_strategy_uses_fallback_when_non_occluding_horizontal_fold_present() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates
        val displayFeatures = DelegateList {
            fakeDisplayFeatures(
                density = density,
                twoPaneCoordinates = twoPaneCoordinates,
                localFoldingFeatures = listOf(
                    LocalFoldingFeature(
                        center = 600.dp,
                        size = 0.dp,
                        state = FoldingFeature.State.FLAT,
                        orientation = FoldingFeature.Orientation.HORIZONTAL
                    )
                )
            )
        }

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp))
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = VerticalTwoPaneStrategy(
                        splitFraction = 1f / 3f
                    ),
                    displayFeatures = displayFeatures,
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(900.dp, 400.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 400.dp),
                    DpSize(900.dp, 800.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun two_pane_strategy_uses_horizontal_placing_when_occluding_vertical_fold_present() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates
        val displayFeatures = DelegateList {
            fakeDisplayFeatures(
                density = density,
                twoPaneCoordinates = twoPaneCoordinates,
                localFoldingFeatures = listOf(
                    LocalFoldingFeature(
                        center = 450.dp,
                        size = 0.dp,
                        state = FoldingFeature.State.HALF_OPENED,
                        orientation = FoldingFeature.Orientation.VERTICAL
                    )
                )
            )
        }

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp))
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = VerticalTwoPaneStrategy(
                        splitFraction = 1f / 3f
                    ),
                    displayFeatures = displayFeatures,
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(450.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(450.dp, 0.dp),
                    DpSize(450.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun two_pane_strategy_uses_horizontal_placing_when_separating_vertical_fold_present() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates
        val displayFeatures = DelegateList {
            fakeDisplayFeatures(
                density = density,
                twoPaneCoordinates = twoPaneCoordinates,
                localFoldingFeatures = listOf(
                    LocalFoldingFeature(
                        center = 450.dp,
                        size = 64.dp,
                        state = FoldingFeature.State.FLAT,
                        orientation = FoldingFeature.Orientation.VERTICAL
                    )
                )
            )
        }

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp))
            ) {
                density = LocalDensity.current
                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = VerticalTwoPaneStrategy(
                        splitFraction = 1f / 3f
                    ),
                    displayFeatures = displayFeatures,
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(418.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(482.dp, 0.dp),
                    DpSize(418.dp, 1200.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }

    @Test
    fun two_pane_strategy_uses_fallback_when_non_occluding_vertical_fold_present() {
        lateinit var density: Density
        lateinit var twoPaneCoordinates: LayoutCoordinates
        lateinit var firstCoordinates: LayoutCoordinates
        lateinit var secondCoordinates: LayoutCoordinates
        val displayFeatures = DelegateList {
            fakeDisplayFeatures(
                density = density,
                twoPaneCoordinates = twoPaneCoordinates,
                localFoldingFeatures = listOf(
                    LocalFoldingFeature(
                        center = 450.dp,
                        size = 0.dp,
                        state = FoldingFeature.State.FLAT,
                        orientation = FoldingFeature.Orientation.VERTICAL
                    )
                )
            )
        }

        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(900.dp, 1200.dp))
            ) {
                density = LocalDensity.current

                TwoPane(
                    first = {
                        Spacer(
                            Modifier
                                .background(Color.Red)
                                .fillMaxSize()
                                .onPlaced { firstCoordinates = it }
                        )
                    },
                    second = {
                        Spacer(
                            Modifier
                                .background(Color.Blue)
                                .fillMaxSize()
                                .onPlaced { secondCoordinates = it }
                        )
                    },
                    strategy = VerticalTwoPaneStrategy(
                        splitFraction = 1f / 3f
                    ),
                    displayFeatures = displayFeatures,
                    modifier = Modifier.onPlaced { twoPaneCoordinates = it }
                )
            }
        }

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 0.dp),
                    DpSize(900.dp, 400.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(firstCoordinates),
            1f
        )

        compareRectWithTolerance(
            with(density) {
                DpRect(
                    DpOffset(0.dp, 400.dp),
                    DpSize(900.dp, 800.dp)
                ).toRect().round().toRect()
            },
            twoPaneCoordinates.localBoundingBoxOf(secondCoordinates),
            1f
        )
    }
}

private fun compareRectWithTolerance(
    expected: Rect,
    actual: Rect,
    tolerance: Float,
) {
    assertThat(actual.left).isWithin(tolerance).of(expected.left)
    assertThat(actual.right).isWithin(tolerance).of(expected.right)
    assertThat(actual.top).isWithin(tolerance).of(expected.top)
    assertThat(actual.bottom).isWithin(tolerance).of(expected.bottom)
}

/**
 * A descriptor of a [FoldingFeature] but with the [center] and [size] specified relative to the
 * to the coordinates of the [TwoPane] layout.
 */
private data class LocalFoldingFeature(
    val center: Dp,
    val size: Dp,
    val state: FoldingFeature.State,
    val orientation: FoldingFeature.Orientation
)

/**
 * A [List] that lazily constructs the backing delegate list by calling the provided lambda.
 */
private class DelegateList<T>(
    listFactory: () -> List<T>
) : List<T> {
    val delegate by lazy(listFactory)
    override val size: Int get() = delegate.size
    override fun get(index: Int): T = delegate[index]
    override fun isEmpty(): Boolean = delegate.isEmpty()
    override fun iterator(): Iterator<T> = delegate.iterator()
    override fun listIterator(): ListIterator<T> = delegate.listIterator()
    override fun listIterator(index: Int): ListIterator<T> = delegate.listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int): List<T> =
        delegate.subList(fromIndex, toIndex)
    override fun lastIndexOf(element: T): Int = delegate.lastIndexOf(element)
    override fun indexOf(element: T): Int = delegate.indexOf(element)
    override fun containsAll(elements: Collection<T>): Boolean = delegate.containsAll(elements)
    override fun contains(element: T): Boolean = delegate.contains(element)
}

/**
 * Folding features are always expressed in window coordinates.
 *
 * For the sake of testing, however, we want to specify them relative to the [TwoPane] under test.
 *
 * Therefore, this method takes in a list of [LocalFoldingFeature]s and the [TwoPane] layout info
 * in order to map the [LocalFoldingFeature]s into real [FoldingFeature] with the proper window
 * coordinates.
 *
 * In other words, this allows specifying [LocalFoldingFeature]s as if the [TwoPane] layout matches
 * the window bounds.
 */
@OptIn(ExperimentalWindowApi::class)
private fun fakeDisplayFeatures(
    density: Density,
    twoPaneCoordinates: LayoutCoordinates,
    localFoldingFeatures: List<LocalFoldingFeature>
): List<DisplayFeature> {
    val boundsTopLeftOffset = twoPaneCoordinates.localToWindow(
        twoPaneCoordinates.size.toIntRect().topLeft.toOffset()
    )
    val boundsBottomRightOffset = twoPaneCoordinates.localToWindow(
        twoPaneCoordinates.size.toIntRect().bottomRight.toOffset()
    )
    val bounds = Rect(
        boundsTopLeftOffset,
        boundsBottomRightOffset
    )

    return localFoldingFeatures.map { localFoldingFeature ->
        val foldLeft: Float
        val foldTop: Float
        val foldRight: Float
        val foldBottom: Float

        with(density) {
            if (localFoldingFeature.orientation == FoldingFeature.Orientation.HORIZONTAL) {
                foldLeft = 0f
                foldTop =
                    (localFoldingFeature.center - localFoldingFeature.size / 2).toPx()
                foldRight = twoPaneCoordinates.size.width.toFloat()
                foldBottom =
                    (localFoldingFeature.center + localFoldingFeature.size / 2).toPx()
            } else {
                foldLeft =
                    (localFoldingFeature.center - localFoldingFeature.size / 2).toPx()
                foldTop = 0f
                foldRight =
                    (localFoldingFeature.center + localFoldingFeature.size / 2).toPx()
                foldBottom = twoPaneCoordinates.size.height.toFloat()
            }
        }

        val foldTopLeftOffset = twoPaneCoordinates.localToWindow(
            Offset(foldLeft, foldTop)
        )
        val foldBottomRightOffset = twoPaneCoordinates.localToWindow(
            Offset(foldRight, foldBottom)
        )
        val foldBounds = Rect(
            foldTopLeftOffset,
            foldBottomRightOffset,
        )

        val center: Int
        val size: Int

        if (localFoldingFeature.orientation == FoldingFeature.Orientation.HORIZONTAL) {
            center = foldBounds.center.y.roundToInt()
            size = foldBounds.height.roundToInt()
        } else {
            center = foldBounds.center.x.roundToInt()
            size = foldBounds.width.roundToInt()
        }

        FoldingFeature(
            windowBounds = android.graphics.Rect(
                bounds.left.toInt(),
                bounds.top.toInt(),
                bounds.right.toInt(),
                bounds.bottom.toInt()
            ),
            center = center,
            size = size,
            state = localFoldingFeature.state,
            orientation = localFoldingFeature.orientation,
        )
    }
}

private fun Rect.round(): IntRect =
    IntRect(
        left = left.roundToInt(),
        top = top.roundToInt(),
        right = right.roundToInt(),
        bottom = bottom.roundToInt()
    )

private fun IntRect.toRect(): Rect =
    Rect(
        left = left.toFloat(),
        top = top.toFloat(),
        right = right.toFloat(),
        bottom = bottom.toFloat()
    )
