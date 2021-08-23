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

@file:Suppress("unused")

package com.google.accompanist.pager

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.math.abs
import kotlin.math.absoluteValue

/**
 * This attempts to mimic ViewPager's custom scroll interpolator. It's not a perfect match
 * (and we may not want it to be), but this seem to match in terms of scroll duration and 'feel'
 */
private const val SnapSpringStiffness = 2750f

/**
 * Create and remember the default [FlingBehavior] that represents the scroll curve.
 *
 * @param state The [PagerState] to update.
 * @param decayAnimationSpec The decay animation spec to use for decayed flings.
 * @param snapAnimationSpec The animation spec to use when snapping.
 */
@Composable
fun rememberSnappingFlingBehavior(
    lazyListState: LazyListState,
    decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
    snapAnimationSpec: AnimationSpec<Float> = spring(stiffness = SnapSpringStiffness),
    snapOffset: LazyListLayoutInfo.(index: Int) -> Int = { viewportStartOffset },
): FlingBehavior = remember(lazyListState, decayAnimationSpec, snapAnimationSpec) {
    SnappingFlingBehavior(
        lazyListState = lazyListState,
        decayAnimationSpec = decayAnimationSpec,
        snapAnimationSpec = snapAnimationSpec,
        snapOffset = snapOffset,
    )
}

class SnappingFlingBehavior(
    private val lazyListState: LazyListState,
    private val decayAnimationSpec: DecayAnimationSpec<Float>,
    private val snapAnimationSpec: AnimationSpec<Float>,
    private val snapOffset: LazyListLayoutInfo.(index: Int) -> Int,
) : FlingBehavior {
    override suspend fun ScrollScope.performFling(
        initialVelocity: Float
    ): Float {
        if (initialVelocity.absoluteValue < 0.5f) {
            // TODO: snap
            return initialVelocity
        } else {
            val startPage = currentLayoutPageInfo ?: return initialVelocity
            val decayDistance = decayAnimationSpec.calculateTargetValue(0f, initialVelocity)

            // If the decay fling will scroll more than
            if (decayDistance.absoluteValue >= startPage.size) {
                return performFlingDecay(initialVelocity, startPage)
            } else {
                // TODO
                return 0f
            }
        }
    }

    private suspend fun ScrollScope.performFlingDecay(
        initialVelocity: Float,
        startPage: LazyListItemInfo,
    ): Float {
        val targetIndex = when {
            initialVelocity > 0 -> startPage.index + 1
            else -> startPage.index
        }
        val targetSnapOffset = snapOffset(lazyListState.layoutInfo, targetIndex)

        var velocityLeft = initialVelocity
        var lastValue = 0f
        AnimationState(
            initialValue = 0f,
            initialVelocity = initialVelocity,
        ).animateDecay(decayAnimationSpec) {
            val delta = value - lastValue
            val consumed = scrollBy(delta)
            lastValue = value
            velocityLeft = this.velocity

            val current = currentLayoutPageInfo!!
            if (initialVelocity < 0 &&
                (current.index < targetIndex || current.index == targetIndex && current.offset >= targetSnapOffset)
            ) {
                // 'snap back' to the item as we may have scrolled past it
                scrollBy(lazyListState.calculateScrollDistanceToItem(targetIndex).toFloat())
                cancelAnimation()
            } else if (
                initialVelocity > 0 && (
                    current.index > targetIndex ||
                        current.index == targetIndex && current.offset <= targetSnapOffset
                    )
            ) {
                // 'snap back' to the item as we may have scrolled past it
                scrollBy(lazyListState.calculateScrollDistanceToItem(targetIndex).toFloat())
                cancelAnimation()
            } else if (abs(delta - consumed) > 0.5f) {
                // avoid rounding errors and stop if anything is unconsumed
                cancelAnimation()
            }
        }
        return velocityLeft
    }

    private fun LazyListState.calculateScrollDistanceToItem(index: Int): Int {
        val itemInfo = layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == index } ?: return 0

        return itemInfo.offset - snapOffset(layoutInfo, itemInfo.index)
    }

    private val currentLayoutPageInfo: LazyListItemInfo?
        get() {
            val layoutInfo = lazyListState.layoutInfo
            return layoutInfo.visibleItemsInfo.asSequence()
                .filter { it.offset <= snapOffset(layoutInfo, it.index) }
                .lastOrNull()
        }
}
