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

@file:Suppress("NOTHING_TO_INLINE")

package com.google.accompanist.lazysnap

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
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
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val DebugLog = false

@RequiresOptIn(message = "Accompanist Lazy Snap is experimental. The API may be changed in the future.")
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalLazySnapApi

/**
 * Default values used for [SnappingFlingBehavior] & [rememberSnappingFlingBehavior].
 */
@ExperimentalLazySnapApi
object SnappingFlingBehaviorDefaults {
    /**
     * TODO
     */
    val snapAnimationSpec: AnimationSpec<Float> = spring(stiffness = 400f)

    val maximumFlingDistance: LazyListLayoutInfo.() -> Int = { Int.MAX_VALUE }
}

/**
 * Create and remember a snapping [FlingBehavior] to be used with [LazyListState].
 *
 * @param lazyListState The [LazyListState] to update.
 * @param decayAnimationSpec The decay animation spec to use for decayed flings.
 * @param snapAnimationSpec The animation spec to use when snapping.
 */
@ExperimentalLazySnapApi
@Composable
fun rememberSnappingFlingBehavior(
    lazyListState: LazyListState,
    snapOffsetForItem: LazyListLayoutInfo.(LazyListItemInfo) -> Int = SnapOffsets.Center,
    maximumFlingDistance: LazyListLayoutInfo.() -> Int = SnappingFlingBehaviorDefaults.maximumFlingDistance,
    decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
    snapAnimationSpec: AnimationSpec<Float> = SnappingFlingBehaviorDefaults.snapAnimationSpec,
): SnappingFlingBehavior = remember(
    lazyListState,
    snapOffsetForItem,
    maximumFlingDistance,
    decayAnimationSpec,
    snapAnimationSpec
) {
    SnappingFlingBehavior(
        lazyListState = lazyListState,
        snapOffsetForItem = snapOffsetForItem,
        maximumFlingDistance = maximumFlingDistance,
        decayAnimationSpec = decayAnimationSpec,
        snapAnimationSpec = snapAnimationSpec,
    )
}

/**
 * TODO
 */
@Suppress("unused")
object SnapOffsets {
    /**
     * TODO
     */
    val Start: LazyListLayoutInfo.(LazyListItemInfo) -> Int = { 0 }

    /**
     * TODO
     */
    val Center: LazyListLayoutInfo.(LazyListItemInfo) -> Int = {
        ((viewportEndOffset + viewportStartOffset) / 2) - (it.size / 2)
    }

    /**
     * TODO
     */
    val End: LazyListLayoutInfo.(LazyListItemInfo) -> Int = { viewportEndOffset - it.size }
}

/**
 * A snapping [FlingBehavior] for [LazyListState]. Typically this would be created
 * via [rememberSnappingFlingBehavior].
 *
 * Note: the default parameter value for [decayAnimationSpec] is different to the value used in
 * [rememberSnappingFlingBehavior], due to not being able to access composable functions.
 *
 * @param lazyListState The [LazyListState] to update.
 * @param decayAnimationSpec The decay animation spec to use for decayed flings.
 * @param snapAnimationSpec The animation spec to use when snapping.
 */

@ExperimentalLazySnapApi
class SnappingFlingBehavior(
    private val lazyListState: LazyListState,
    private val snapOffsetForItem: LazyListLayoutInfo.(LazyListItemInfo) -> Int = SnapOffsets.Center,
    private val maximumFlingDistance: LazyListLayoutInfo.() -> Int = SnappingFlingBehaviorDefaults.maximumFlingDistance,
    private val decayAnimationSpec: DecayAnimationSpec<Float> = exponentialDecay(),
    private val snapAnimationSpec: AnimationSpec<Float> = SnappingFlingBehaviorDefaults.snapAnimationSpec,
) : FlingBehavior {
    /**
     * The target item index for any on-going animations.
     */
    var animationTarget: Int? by mutableStateOf(null)
        private set

    override suspend fun ScrollScope.performFling(
        initialVelocity: Float
    ): Float {
        val itemInfo = currentItemInfo ?: return initialVelocity

        return if (decayAnimationSpec.canFlingPastCurrentItem(itemInfo, initialVelocity)) {
            // If the decay fling can scroll past the current item, fling with decay
            performDecayFling(
                targetIndex = determineTargetIndexForDecay(initialVelocity, itemInfo),
                initialVelocity = initialVelocity,
            )
        } else {
            // Otherwise we 'spring' to current/next item
            performSpringFling(
                targetIndex = determineTargetIndexForSpring(initialVelocity, itemInfo),
                initialVelocity = initialVelocity,
            )
        }
    }

    private suspend fun ScrollScope.performDecayFling(
        targetIndex: Int,
        initialVelocity: Float,
    ): Float {
        Napier.d(
            message = {
                "Performing decay fling. " +
                    "vel:$initialVelocity, " +
                    "current item: ${currentItemInfo?.log()}, " +
                    "target: $targetIndex"
            }
        )

        // Update the animationTarget
        animationTarget = targetIndex

        var velocityLeft = initialVelocity
        var lastValue = 0f
        var targetScrollOffset: Int? = null

        AnimationState(
            initialValue = 0f,
            initialVelocity = initialVelocity,
        ).animateDecay(decayAnimationSpec) {
            val delta = value - lastValue
            val consumed = scrollBy(delta)
            lastValue = value
            velocityLeft = this.velocity

            val current = currentItemInfo
            if (current == null) {
                cancelAnimation()
                return@animateDecay
            }

            Napier.d(
                message = { "decay tick. vel:$velocityLeft, current item: ${current.log()}" }
            )

            if (targetScrollOffset == null) {
                // If we don't have the target scroll offset yet, try and calculate it
                targetScrollOffset = lazyListState.layoutInfo.visibleItemsInfo
                    .firstOrNull { it.index == targetIndex }
                    ?.let { snapOffsetForItem(lazyListState.layoutInfo, it) }
            }

            val scrollOffset = targetScrollOffset ?: 0

            if (scrolledPastItem(initialVelocity, current, targetIndex, scrollOffset)) {
                Napier.d(
                    message = {
                        "Scrolled past item. " +
                            "vel:$initialVelocity, " +
                            "current item: ${current.log()}, " +
                            "target:$targetIndex, " +
                            "offset:$scrollOffset"
                    }
                )

                // If we've scrolled to/past the item, stop the animation. We may also need to
                // 'snap back' to the item as we may have scrolled past it
                scrollBy(
                    lazyListState.calculateScrollOffsetToItem(targetIndex, scrollOffset).toFloat()
                )
                cancelAnimation()
            } else if (abs(delta - consumed) > 0.5f) {
                // avoid rounding errors and stop if anything is unconsumed
                cancelAnimation()
            }
        }
        animationTarget = null
        return velocityLeft
    }

    private fun determineTargetIndexForDecay(
        initialVelocity: Float,
        currentItem: LazyListItemInfo,
    ): Int {
        val distancePerChild = computeDistancePerChild()
        if (distancePerChild < 0) {
            // If we don't have a valid distance, return the current item
            return currentItem.index
        }

        val maximumFlingDistance = maximumFlingDistance(lazyListState.layoutInfo)
        val flingDistance = decayAnimationSpec.calculateTargetValue(
            initialValue = -currentItem.offset.toFloat(),
            initialVelocity = initialVelocity,
        ).coerceIn(-maximumFlingDistance.toFloat(), maximumFlingDistance.toFloat())

        val indexDelta = (flingDistance / distancePerChild).roundToInt()

        return currentItem.index + if (initialVelocity > 0) {
            // If we're flinging forward, the current item is likely the same item as
            // which the user started dragging
            indexDelta
        } else {
            // If we're going backwards though, any backwards drag immediately goes back one item,
            // so we need to cater for that by adding 1 to the result
            (indexDelta + 1).coerceAtMost(lazyListState.layoutInfo.totalItemsCount - 1)
        }
    }

    /**
     * Computes an average pixel value to pass a single child.
     *
     * Returns a negative value if it cannot be calculated.
     *
     * @return A float value that is the average number of pixels needed to scroll by one view in
     * the relevant direction.
     */
    private fun computeDistancePerChild(): Float {
        if (lazyListState.layoutInfo.visibleItemsInfo.isEmpty()) return -1f

        val minPosView = lazyListState.layoutInfo.visibleItemsInfo
            .minByOrNull { it.offset } ?: return -1f
        val maxPosView = lazyListState.layoutInfo.visibleItemsInfo
            .maxByOrNull { it.offset + it.size } ?: return -1f

        val start: Int = min(minPosView.offset, maxPosView.offset)
        val end: Int = max(minPosView.offset + minPosView.size, maxPosView.offset + maxPosView.size)
        val distance = end - start

        return when {
            distance != 0 -> distance.toFloat() / lazyListState.layoutInfo.visibleItemsInfo.size
            else -> -1f
        }
    }

    private fun determineTargetIndexForSpring(
        initialVelocity: Float,
        currentItemInfo: LazyListItemInfo,
    ): Int {
        return if (initialVelocity.absoluteValue > 1) {
            // If the velocity isn't zero, spring in the relevant direction
            if (initialVelocity > 0) currentItemInfo.index + 1 else currentItemInfo.index
        } else {
            // Otherwise we look at the current offset, and spring to whichever is closer
            val snapOffset = snapOffsetForItem(lazyListState.layoutInfo, currentItemInfo)
            if (currentItemInfo.offset < snapOffset - (currentItemInfo.size / 2)) {
                currentItemInfo.index + 1
            } else currentItemInfo.index
        }.coerceIn(0, lazyListState.layoutInfo.totalItemsCount - 1)
    }

    private suspend fun ScrollScope.performSpringFling(
        targetIndex: Int,
        initialVelocity: Float = 0f,
    ): Float {
        // If we don't have a current layout, we can't snap
        val initialItem = currentItemInfo ?: return initialVelocity

        Napier.d(
            message = {
                "Performing spring. " +
                    "vel:$initialVelocity, " +
                    "current item: ${initialItem.log()}, " +
                    "target: $targetIndex"
            }
        )

        val forward = targetIndex > initialItem.index
        // We add 10% on to the size of the current item, to compensate for any item spacing, etc
        val target = (if (forward) initialItem.size else -initialItem.size) * 1.1f

        // Update the animationTarget
        animationTarget = targetIndex

        var targetScrollOffset = lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == targetIndex }
            ?.let { snapOffsetForItem(lazyListState.layoutInfo, it) }

        var velocityLeft = initialVelocity
        var lastValue = 0f
        AnimationState(
            initialValue = 0f,
            initialVelocity = initialVelocity,
        ).animateTo(
            targetValue = target,
            animationSpec = snapAnimationSpec,
        ) {
            val delta = value - lastValue
            val consumed = scrollBy(delta)
            lastValue = value
            velocityLeft = this.velocity

            val current = currentItemInfo
            if (current == null) {
                cancelAnimation()
                return@animateTo
            }

            Napier.d(
                message = { "spring tick. vel:$velocityLeft, current item: ${current.log()}" }
            )

            if (targetScrollOffset == null) {
                // If we don't have the target scroll offset yet, try and calculate it
                targetScrollOffset = lazyListState.layoutInfo.visibleItemsInfo
                    .firstOrNull { it.index == targetIndex }
                    ?.let { snapOffsetForItem(lazyListState.layoutInfo, it) }
            }

            val scrollOffset = targetScrollOffset ?: 0

            if (scrolledPastItem(initialVelocity, current, targetIndex, scrollOffset)) {
                Napier.d(
                    message = {
                        "Scrolled past item. " +
                            "vel:$initialVelocity, " +
                            "current item: ${current.log()}, " +
                            "target:$targetIndex, " +
                            "offset:$scrollOffset"
                    }
                )

                // If we've scrolled to/past the item, stop the animation. We may also need to
                // 'snap back' to the item as we may have scrolled past it
                scrollBy(
                    lazyListState.calculateScrollOffsetToItem(targetIndex, scrollOffset).toFloat()
                )
                cancelAnimation()
            } else if (abs(delta - consumed) > 0.5f) {
                // avoid rounding errors and stop if anything is unconsumed
                cancelAnimation()
            }
        }
        animationTarget = null
        return velocityLeft
    }

    private fun LazyListState.calculateScrollOffsetToItem(
        index: Int,
        scrollOffset: Int,
    ): Int {
        val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
        return if (item != null) item.offset - scrollOffset else 0
    }

    private val currentItemInfo: LazyListItemInfo?
        get() {
            val layoutInfo = lazyListState.layoutInfo
            return layoutInfo.visibleItemsInfo.asSequence()
                .filter {
                    val snapOffset = snapOffsetForItem(layoutInfo, it)
                    it.offset <= snapOffset && it.offset + it.size > snapOffset
                }
                .lastOrNull()
        }

    private fun DecayAnimationSpec<Float>.canFlingPastCurrentItem(
        currentItem: LazyListItemInfo,
        initialVelocity: Float,
    ): Boolean {
        val targetValue = calculateTargetValue(
            initialValue = currentItem.offset.toFloat(),
            initialVelocity = initialVelocity,
        )
        val snapOffset = snapOffsetForItem(lazyListState.layoutInfo, currentItem)
        return when {
            // forwards. We add 10% onto the size to cater for any item spacing
            initialVelocity < 0 -> targetValue <= snapOffset - (currentItem.size * 1.1f)
            // backwards. We add 10% onto the size to cater for any item spacing
            else -> targetValue >= snapOffset + (currentItem.size * 0.1f)
        }
    }

    private companion object {
        init {
            if (DebugLog) {
                Napier.base(DebugAntilog(defaultTag = "SnappingFlingBehavior"))
            }
        }
    }
}

private inline fun LazyListItemInfo.log(): String = "[i:$index,o:$offset,s:$size]"

private fun scrolledPastItem(
    initialVelocity: Float,
    currentItem: LazyListItemInfo,
    targetIndex: Int,
    targetScrollOffset: Int = 0,
): Boolean {
    return if (initialVelocity > 0) {
        // forward
        currentItem.index > targetIndex ||
            (currentItem.index == targetIndex && currentItem.offset <= targetScrollOffset)
    } else {
        // backwards
        currentItem.index < targetIndex ||
            (currentItem.index == targetIndex && currentItem.offset >= targetScrollOffset)
    }
}
