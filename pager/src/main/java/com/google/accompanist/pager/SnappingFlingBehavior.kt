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
import androidx.compose.runtime.remember
import kotlin.math.abs
import kotlin.math.absoluteValue

/**
 * This attempts to mimic ViewPager's custom scroll interpolator. It's not a perfect match
 * (and we may not want it to be), but this seem to match in terms of scroll duration and 'feel'
 */

@Suppress("MemberVisibilityCanBePrivate")
object SnappingFlingBehaviorDefaults {

    /**
     * TODO
     */
    const val SnapSpringStiffness = 600f

    val snapOffset: LazyListLayoutInfo.(index: Int) -> Float = { viewportStartOffset.toFloat() }

    val snapAnimationSpec: AnimationSpec<Float> = spring(stiffness = SnapSpringStiffness)
}

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
    snapAnimationSpec: AnimationSpec<Float> = SnappingFlingBehaviorDefaults.snapAnimationSpec,
    snapOffset: LazyListLayoutInfo.(index: Int) -> Float = SnappingFlingBehaviorDefaults.snapOffset,
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
    private val snapOffset: LazyListLayoutInfo.(index: Int) -> Float,
) : FlingBehavior {
    override suspend fun ScrollScope.performFling(
        initialVelocity: Float
    ): Float {
        val startPage = currentLayoutPageInfo ?: return initialVelocity
        val decayDistance = decayAnimationSpec.calculateTargetValue(0f, initialVelocity)

        // If the decay fling will scroll more than the width of the page, fling with
        // using a decaying scroll
        if (decayDistance.absoluteValue >= startPage.size) {
            return performDecayFling(initialVelocity, startPage)
        } else {
            // Otherwise we 'spring' to current/next page
            val targetPage = when {
                initialVelocity.absoluteValue < 0.5f -> {
                    val snapForCurrent = snapOffset(lazyListState.layoutInfo, startPage.index)
                    // TODO: look at current offset and whatever is closer
                    if ((startPage.offset - snapForCurrent) < -startPage.size / 2) {
                        startPage.index + 1
                    } else {
                        startPage.index
                    }
                }
                initialVelocity > 0 -> {
                    (startPage.index + 1)
                        .coerceIn(0, lazyListState.layoutInfo.totalItemsCount - 1)
                }
                else -> startPage.index
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
        val currentLayout = currentLayoutPageInfo ?: return initialVelocity
        val forward = index > currentLayout.index

        val targetSnapOffset = snapOffset(lazyListState.layoutInfo, index)

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
                scrollBy(lazyListState.calculateScrollDistanceToItem(index))
                cancelAnimation()
            } else if (
                forward &&
                (page.index > index || page.index == index && page.offset <= targetSnapOffset)
            ) {
                // 'snap back' to the item as we may have scrolled past it
                scrollBy(lazyListState.calculateScrollDistanceToItem(index))
                cancelAnimation()
            } else if (abs(delta - consumed) > 0.5f) {
                // avoid rounding errors and stop if anything is unconsumed
                cancelAnimation()
            }
        }
        return velocityLeft
    }

    private suspend fun ScrollScope.performSpringFling(
        index: Int,
        scrollOffset: Float,
        initialVelocity: Float = 0f,
    ): Float {
        // If we don't have a current layout, we can't snap
        val currentLayout = currentLayoutPageInfo ?: return initialVelocity

        val forward = index > currentLayout.index
        // We add 20% on to the size of the current item, to compensate for any item spacing, etc
        val target = (if (forward) currentLayout.size else -currentLayout.size) * 1.2f

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
                scrollBy(lazyListState.calculateScrollDistanceToItem(index))
                cancelAnimation()
            } else if (
                forward &&
                (page.index > index || page.index == index && page.offset <= scrollOffset)
            ) {
                // 'snap back' to the item as we may have scrolled past it
                scrollBy(lazyListState.calculateScrollDistanceToItem(index))
                cancelAnimation()
            } else if (abs(delta - consumed) > 0.5f) {
                // avoid rounding errors and stop if anything is unconsumed
                cancelAnimation()
            }
        }
        return velocityLeft
    }

    private fun LazyListState.calculateScrollDistanceToItem(index: Int): Float {
        val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return 0f
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
