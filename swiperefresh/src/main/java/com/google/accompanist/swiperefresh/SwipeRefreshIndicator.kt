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

package com.google.accompanist.swiperefresh

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun SwipeRefreshIndicator(
    isRefreshing: Boolean,
    offset: Float,
    scale: Boolean = false,
    arrowEnabled: Boolean = true,
    color: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(color),
    shape: CornerBasedShape = CircleShape,
    size: IndicatorSize = IndicatorSize.DEFAULT,
    elevation: Dp = 4.dp
) {
    val animatedElevation by animateDpAsState(
        targetValue = when (offset) {
            0f -> 0.dp
            else -> elevation
        },
        animationSpec = snap()
    )
    val maxOffset = with(LocalDensity.current) { size.refreshTrigger.toPx() }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (offset >= maxOffset) MAX_ALPHA else MIN_ALPHA,
        animationSpec = tween()
    )
    val adjustedScale = if (scale) min(1f, offset / maxOffset) else 1f
    Surface(
        modifier = Modifier
            .size(size = size.size)
            .scale(adjustedScale),
        shape = shape,
        color = color,
        elevation = animatedElevation
    ) {
        val painter = remember {
            CircularProgressPainter()
        }
        painter.arcRadius = size.arcRadius
        painter.strokeWidth = size.strokeWidth
        painter.arrowWidth = size.arrowWidth
        painter.arrowHeight = size.arrowHeight
        painter.arrowEnabled = arrowEnabled && !isRefreshing
        painter.color = contentColor
        painter.alpha = animatedAlpha
        val slingshot = calculateSlingshot(
            offsetY = offset,
            maxOffsetY = maxOffset,
            height = with(LocalDensity.current) { size.size.roundToPx() }
        )
        painter.startTrim = slingshot.startTrim
        painter.endTrim = slingshot.endTrim
        painter.rotation = slingshot.rotation
        painter.arrowScale = slingshot.arrowScale
        Crossfade(
            targetState = isRefreshing,
            animationSpec = tween(durationMillis = CROSSFADE_DURATION)
        ) { refreshing ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (refreshing) {
                    val circleSize = (size.arcRadius + size.strokeWidth) * 2
                    CircularProgressIndicator(
                        color = contentColor,
                        strokeWidth = size.strokeWidth,
                        modifier = Modifier.size(circleSize),
                    )
                } else {
                    Image(
                        painter = painter,
                        contentDescription = "Refreshing"
                    )
                }
            }
        }
    }
}

private const val MAX_ALPHA = 1f
private const val MIN_ALPHA = 0.3f
private const val CROSSFADE_DURATION = 100

@Preview
@Composable
fun PreviewSwipeRefreshIndicator() {
    MaterialTheme {
        SwipeRefreshIndicator(
            isRefreshing = false,
            offset = 0f
        )
    }
}
