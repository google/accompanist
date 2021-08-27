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

package com.google.accompanist.pager

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.math.abs
import kotlin.math.absoluteValue

/**
 * Default values used for [SnappingFlingBehavior] & [rememberSnappingFlingBehavior].
 */
object SnappingFlingBehaviorDefaults {
    /**
     * TODO
     */
    val snapOffset: LazyListLayoutInfo.(index: Int) -> Int = { viewportStartOffset }

    /**
     * TODO
     */
    val snapAnimationSpec: AnimationSpec<Float> = spring(stiffness = 600f)
}

/**
 * Create and remember a snapping [FlingBehavior] to be used with [LazyListState].
 *
 * @param lazyListState The [LazyListState] to update.
 * @param decayAnimationSpec The decay animation spec to use for decayed flings.
 * @param snapAnimationSpec The animation spec to use when snapping.
 * @param snapOffset Block which defines the snap offset for the given index. The returned offset
 * should be in the same dimension and range as [LazyListItemInfo.offset].
 */
@Composable
fun rememberSnappingFlingBehavior(
    lazyListState: LazyListState,
    decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
    snapAnimationSpec: AnimationSpec<Float> = SnappingFlingBehaviorDefaults.snapAnimationSpec,
    snapOffset: LazyListLayoutInfo.(index: Int) -> Int = SnappingFlingBehaviorDefaults.snapOffset,
): SnappingFlingBehavior = remember(lazyListState, decayAnimationSpec, snapAnimationSpec) {
    SnappingFlingBehavior(
        lazyListState = lazyListState,
        decayAnimationSpec = decayAnimationSpec,
        snapAnimationSpec = snapAnimationSpec,
        snapOffset = snapOffset,
    )
}

/**
 * A snapping [FlingBehavior] for [LazyListState]. Typically this would be created
 * via [rememberSnappingFlingBehavior].
 *
 * @param lazyListState The [LazyListState] to update.
 * @param decayAnimationSpec The decay animation spec to use for decayed flings.
 * @param snapAnimationSpec The animation spec to use when snapping.
 * @param snapOffset Block which defines the snap offset for the given index. The returned offset
 * should be in the same dimension and range as [LazyListItemInfo.offset].
 */
class SnappingFlingBehavior(
    private val lazyListState: LazyListState,
    private val decayAnimationSpec: DecayAnimationSpec<Float>,
    private val snapAnimationSpec: AnimationSpec<Float>,
    private val snapOffset: LazyListLayoutInfo.(index: Int) -> Int,
) : FlingBehavior {
    /**
     * The target page for any on-going animations.
     */
    var animationTarget: Int? by mutableStateOf(null)
        private set

    /**
     * TODO
     */
    var itemSize: (LazyListItemInfo) -> Int = LazyListItemInfo::size

    override suspend fun ScrollScope.performFling(
        initialVelocity: Float
    ): Float {
        val pageInfo = currentLayoutPageInfo ?: return initialVelocity
        val pageSize = itemSize(pageInfo)
        val decayDistance = decayAnimationSpec.calculateTargetValue(
            initialValue = 0f,
            initialVelocity = initialVelocity
        )

        // If the decay fling will scroll more than the width of the page, fling with
        // using a decaying scroll
        if (decayDistance.absoluteValue >= pageSize) {
            return performDecayFling(initialVelocity, pageInfo)
        } else {
            // Otherwise we 'spring' to current/next page
            val targetPage = when {
                // If the velocity is greater than 1 page per second (velocity is px/s), spring
                // in the relevant direction
                initialVelocity > pageSize -> {
                    (pageInfo.index + 1).coerceAtMost(lazyListState.layoutInfo.totalItemsCount - 1)
                }
                initialVelocity < -pageSize -> pageInfo.index
                else -> {
                    // If the offset exceeds the scroll threshold (in either direction), we want to
                    // move to the next/previous item
                    val snapForCurrent = snapOffset(lazyListState.layoutInfo, pageInfo.index)
                    if (pageInfo.offset - snapForCurrent < -pageSize / 2) {
                        pageInfo.index + 1
                    } else {
                        pageInfo.index
                    }
                }
            }
            return performSpringFling(
                index = targetPage,
                scrollOffset = snapOffset(lazyListState.layoutInfo, targetPage),
                initialVelocity = initialVelocity,
            )
        }
    }

    private suspend fun ScrollScope.performDecayFling(
        initialVelocity: Float,
        startPage: LazyListItemInfo,
    ): Float {
        val index = when {
            initialVelocity > 0 -> startPage.index + 1
            else -> startPage.index
        }

        // If we don't have a current layout, we can't snap
        val pageInfo = currentLayoutPageInfo ?: return initialVelocity
        val forward = index > pageInfo.index

        val targetSnapOffset = snapOffset(lazyListState.layoutInfo, index)

        // Update the animationTargetPage
        animationTarget = index

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

            val page = currentLayoutPageInfo
            if (page == null) {
                cancelAnimation()
                return@animateDecay
            }

            if (
                !forward &&
                (page.index < index || page.index == index && page.offset >= targetSnapOffset)
            ) {
                // 'snap back' to the item as we may have scrolled past it
                scrollBy(lazyListState.calculateScrollDistanceToItem(index).toFloat())
                cancelAnimation()
            } else if (
                forward &&
                (page.index > index || page.index == index && page.offset <= targetSnapOffset)
            ) {
                // 'snap back' to the item as we may have scrolled past it
                scrollBy(lazyListState.calculateScrollDistanceToItem(index).toFloat())
                cancelAnimation()
            } else if (abs(delta - consumed) > 0.5f) {
                // avoid rounding errors and stop if anything is unconsumed
                cancelAnimation()
            }
        }
        animationTarget = null
        return velocityLeft
    }

    private suspend fun ScrollScope.performSpringFling(
        index: Int,
        scrollOffset: Int,
        initialVelocity: Float = 0f,
    ): Float {
        // If we don't have a current layout, we can't snap
        val currentLayout = currentLayoutPageInfo ?: return initialVelocity

        val forward = index > currentLayout.index
        // We add 20% on to the size of the current item, to compensate for any item spacing, etc
        val target = (if (forward) currentLayout.size else -currentLayout.size) * 1.2f

        // Update the animationTargetPage
        animationTarget = index

        var velocityLeft = initialVelocity
        var lastValue = 0f
        AnimationState(
            initialValue = 0f,
            initialVelocity = initialVelocity,
        ).animateTo(
            targetValue = target,
            animationSpec = snapAnimationSpec,
        ) {
            // Springs can overshoot their target, clamp to the desired range
            val coercedValue = if (forward) {
                value.coerceAtMost(target)
            } else {
                value.coerceAtLeast(target)
            }
            val delta = coercedValue - lastValue
            val consumed = scrollBy(delta)
            lastValue = coercedValue
            velocityLeft = this.velocity

            val page = currentLayoutPageInfo
            if (page == null) {
                cancelAnimation()
                return@animateTo
            }

            if (
                !forward &&
                (page.index < index || page.index == index && page.offset >= scrollOffset)
            ) {
                // 'snap back' to the item as we may have scrolled past it
                scrollBy(lazyListState.calculateScrollDistanceToItem(index).toFloat())
                cancelAnimation()
            } else if (
                forward &&
                (page.index > index || page.index == index && page.offset <= scrollOffset)
            ) {
                // 'snap back' to the item as we may have scrolled past it
                scrollBy(lazyListState.calculateScrollDistanceToItem(index).toFloat())
                cancelAnimation()
            } else if (abs(delta - consumed) > 0.5f) {
                // avoid rounding errors and stop if anything is unconsumed
                cancelAnimation()
            }
        }
        animationTarget = null
        return velocityLeft
    }

    private fun LazyListState.calculateScrollDistanceToItem(index: Int): Int {
        val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return 0
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
