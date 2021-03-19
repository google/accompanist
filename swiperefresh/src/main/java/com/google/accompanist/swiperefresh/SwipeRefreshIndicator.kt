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

@file:Suppress("UNUSED_PARAMETER")

package com.google.accompanist.swiperefresh

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalAbsoluteElevation
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

private const val IndicatorMaxAlpha = 1f
private const val IndicatorMinAlpha = 0f

private val AlphaEasing: Easing = LinearOutSlowInEasing

private const val MAX_PROGRESS_ANGLE = .8f

@Composable
fun SwipeRefreshIndicator(
    isRefreshing: Boolean,
    currentOffset: Float,
    isSwipeInProgress: Boolean,
    backgroundColor: Color = MaterialTheme.colors.surface,
    elevation: Dp = 6.dp,
) {
    // This might look like we've just re-implemented Surface, and that's because we have.
    val elevationPx = with(LocalDensity.current) { elevation.toPx() }
    val elevationOverlay = LocalElevationOverlay.current
    val absoluteElevation = LocalAbsoluteElevation.current + elevation
    val bgColor = if (backgroundColor == MaterialTheme.colors.surface && elevationOverlay != null) {
        elevationOverlay.apply(backgroundColor, absoluteElevation)
    } else backgroundColor

    val size = 40.dp
    val sizePx = with(LocalDensity.current) { size.toPx() }

    val modifier = Modifier
        .size(size)
        .graphicsLayer {
            shadowElevation = elevationPx
            shape = CircleShape
            alpha = when {
                // We animate the indicator in as the indicator is dragged in
                currentOffset >= 0.5 -> lerp(
                    start = IndicatorMinAlpha,
                    stop = IndicatorMaxAlpha,
                    fraction = AlphaEasing.transform((currentOffset / sizePx).coerceAtMost(1f))
                )
                else -> 0f
            }
        }
        .background(color = bgColor, shape = CircleShape)
        .clip(CircleShape)

    Box(modifier) {
        val ringPainter = remember {
            ProgressRingPainter()
        }

        ringPainter.colors = listOf(
            MaterialTheme.colors.primary
        )

        val circumference = Math.PI * 2f * with(LocalDensity.current) { (size / 2).toPx() }
        ringPainter.endTrim = (currentOffset / circumference.toFloat()).coerceAtMost(1f)

        Image(
            painter = ringPainter,
            contentDescription = "Refreshing",
            modifier = Modifier
                .align(Alignment.Center)
                .size(24.dp)
        )
    }
}

@Preview
@Composable
fun PreviewSwipeRefreshIndicator() {
    MaterialTheme {
        SwipeRefreshIndicator(
            isRefreshing = false,
            currentOffset = 0f,
            isSwipeInProgress = true
        )
    }
}
