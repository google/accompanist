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

import android.content.res.Configuration
import androidx.annotation.UiContext
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toComposeIntRect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.size
import androidx.window.layout.WindowMetrics
import androidx.window.layout.WindowMetricsCalculator

/**
 * Calculates the current window size from the underlying [LocalContext].
 *
 * The value of [LocalContext] must be a [UiContext] (or unwrappable to a [UiContext]), to properly
 * retrieve the window metrics values. For example, if there are multiple displayed windows
 * across 1 or more physical displays, there could be multiple possible window sizes.
 *
 * This method is preferred over using `LocalConfiguration.current.screenWidthDp` and
 * `LocalConfiguration.current.screenHeightDp` to retrieve the window size for multiple reasons:
 *
 * - [Configuration.screenWidthDp] and [Configuration.screenHeightDp] are integers. However, the
 *   density may be non-integral, which means that the true width and height of the window (in dps)
 *   can be non-integral. When this occurs, the reported [Configuration.screenWidthDp] and
 *   [Configuration.screenHeightDp] are necessarily rounded from the true width and height of the
 *   window. If these values are then used to size some component, the second rounding to pixels
 *   can result in accumulated rounding errors, and the size can be off by one or more pixels.
 *
 * - [Configuration.screenWidthDp] and [Configuration.screenHeightDp] do not correspond to the
 *   width and height of the window directly. Instead, they can be smaller than the width and height
 *   by a certain amount of insets.
 */
@Composable
public fun calculateWindowSize(): DpSize = calculateWindowBounds().size

/**
 * Calculates the current window bounds from the underlying [LocalContext].
 *
 * The value of [LocalContext] must be a [UiContext] (or unwrappable to a [UiContext]), to properly
 * retrieve the window metrics values.
 */
@Composable
public fun calculateWindowBounds(): DpRect {
    val density = LocalDensity.current
    val metrics = calculateWindowMetrics()
    val bounds = metrics.bounds.toComposeIntRect()
    return with(density) {
        DpRect(
            bounds.left.toDp(),
            bounds.top.toDp(),
            bounds.right.toDp(),
            bounds.bottom.toDp(),
        )
    }
}

/**
 * Calculates the current window metrics from the underlying [LocalContext].
 *
 * The value of [LocalContext] must be a [UiContext] (or unwrappable to a [UiContext]), to properly
 * retrieve the window metrics values.
 */
@Composable
public fun calculateWindowMetrics(): WindowMetrics {
    // Observe view configuration changes and recalculate the size class on each change. We can't
    // use Activity#onConfigurationChanged as this will sometimes fail to be called on different
    // API levels, hence why this function needs to be @Composable so we can observe the
    // ComposeView's configuration changes.
    LocalConfiguration.current
    val context = LocalContext.current
    return WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(context)
}
