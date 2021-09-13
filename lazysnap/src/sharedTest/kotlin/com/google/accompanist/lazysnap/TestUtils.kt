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

package com.google.accompanist.lazysnap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.percentOffset
import androidx.compose.ui.test.performGesture
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeWithVelocity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.absoluteValue
import kotlin.math.hypot
import kotlin.math.roundToLong
import kotlin.random.Random

@Composable
fun ProvideLayoutDirection(
    layoutDirection: LayoutDirection,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalLayoutDirection provides (layoutDirection),
        content = content
    )
}

internal fun SemanticsNodeInteraction.swipeAcrossCenterWithVelocity(
    velocityPerSec: Dp,
    distancePercentageX: Float = 0f,
    distancePercentageY: Float = 0f,
): SemanticsNodeInteraction = performGesture {
    val startOffset = percentOffset(
        x = 0.5f - distancePercentageX / 2,
        y = 0.5f - distancePercentageY / 2
    )
    val endOffset = percentOffset(
        x = 0.5f + distancePercentageX / 2,
        y = 0.5f + distancePercentageY / 2
    )

    val node = fetchSemanticsNode("Failed to retrieve bounds of the node.")
    val density = node.root!!.density
    val velocityPxPerSec = with(density) { velocityPerSec.toPx() }

    try {
        swipeWithVelocity(
            start = startOffset,
            end = endOffset,
            endVelocity = velocityPxPerSec,
        )
    } catch (e: IllegalArgumentException) {
        // swipeWithVelocity throws an exception if the given distance + velocity isn't feasible:
        // https://issuetracker.google.com/182477143. To work around this, we catch the exception
        // and instead run a swipe() with a computed duration instead. This is not perfect,
        // but good enough.
        val distance = hypot(endOffset.x - startOffset.x, endOffset.y - startOffset.y)
        swipe(
            start = startOffset,
            end = endOffset,
            durationMillis = ((distance.absoluteValue / velocityPxPerSec) * 1000).roundToLong(),
        )
    }
}

fun randomColor() = Color(
    alpha = 1f,
    red = Random.nextFloat(),
    green = Random.nextFloat(),
    blue = Random.nextFloat(),
)
