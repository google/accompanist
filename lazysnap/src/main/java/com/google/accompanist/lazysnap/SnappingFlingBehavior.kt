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

import androidx.annotation.Px
import androidx.compose.animation.core.AnimationScope
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.AnimationVector1D
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
import androidx.compose.runtime.derivedStateOf
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
import kotlin.math.truncate

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
     * [AnimationSpec] used as the default value for the `snapAnimationSpec` parameter on
     * [rememberSnappingFlingBehavior] and [SnappingFlingBehavior].
     */
    val SnapAnimationSpec: AnimationSpec<Float> = spring(stiffness = 400f)

    /**
     * The default implementation for the `maximumFlingDistance` parameter of
     * [rememberSnappingFlingBehavior] and [SnappingFlingBehavior], which does not limit
     * the fling distance.
     */
    val MaximumFlingDistance: (LazyListLayoutInfo) -> Int = { Int.MAX_VALUE }
}

/**
 * Create and remember a snapping [FlingBehavior] to be used with [LazyListState].
 *
 * @param lazyListState The [LazyListState] to update.
 * @param snapOffsetForItem Block which returns which offset the given item should 'snap' to.
 * See [SnapOffsets] for provided values.
 * @param maximumFlingDistance Block which returns the maximum fling distance in pixels.
 * The returned value should be >= 0.
 * @param decayAnimationSpec The decay animation spec to use for decayed flings.
 * @param snapAnimationSpec The animation spec to use when snapping.
 * @param endContentPadding The amount of content padding on the end edge of the lazy list
 * in pixels (end/bottom depending on the scrolling direction).
 */
@ExperimentalLazySnapApi
@Composable
fun rememberSnappingFlingBehavior(
    lazyListState: LazyListState,
    snapOffsetForItem: (layoutInfo: LazyListLayoutInfo, itemInfo: LazyListItemInfo) -> Int = SnapOffsets.Center,
    maximumFlingDistance: (LazyListLayoutInfo) -> Int = SnappingFlingBehaviorDefaults.MaximumFlingDistance,
    decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
    snapAnimationSpec: AnimationSpec<Float> = SnappingFlingBehaviorDefaults.SnapAnimationSpec,
    @Px endContentPadding: Int = 0,
): SnappingFlingBehavior = remember(
    lazyListState,
    snapOffsetForItem,
    maximumFlingDistance,
    decayAnimationSpec,
    snapAnimationSpec,
) {
    SnappingFlingBehavior(
        lazyListState = lazyListState,
        snapOffsetForItem = snapOffsetForItem,
        maximumFlingDistance = maximumFlingDistance,
        decayAnimationSpec = decayAnimationSpec,
        snapAnimationSpec = snapAnimationSpec,
    )
}.apply {
    this.endContentPadding = endContentPadding
}

/**
 * Contains a number of values which can be used for the `snapOffsetForItem` parameter on
 * [rememberSnappingFlingBehavior] and [SnappingFlingBehavior].
 */
@Suppress("unused") // public vals which aren't used in the project
object SnapOffsets {
    /**
     * Snap offset which results in the start edge of the item, snapping to the start scrolling
     * edge of the lazy list.
     */
    val Start: (LazyListLayoutInfo, LazyListItemInfo) -> Int = { _, _ -> 0 }

    /**
     * Snap offset which results in the item snapping in the center of the scrolling viewport
     * of the lazy list.
     */
    val Center: (LazyListLayoutInfo, LazyListItemInfo) -> Int = { layoutInfo, itemInfo ->
        (layoutInfo.layoutSize - itemInfo.size) / 2
    }

    /**
     * Snap offset which results in the end edge of the item, snapping to the end scrolling
     * edge of the lazy list.
     */
    val End: (LazyListLayoutInfo, LazyListItemInfo) -> Int = { layoutInfo, itemInfo ->
        layoutInfo.layoutSize - itemInfo.size
    }

    /**
     * Ideally this would exist on [LazyListLayoutInfo] but it doesn't right now.
     * Raised https://issuetracker.google.com/issues/200920410 to track.
     */
    private val LazyListLayoutInfo.layoutSize: Int
        get() = if (this is LazyListLayoutInfoWithContentPadding) {
            // If the LazyListLayoutInfo is actually a LazyListLayoutInfoWithContentPadding,
            // use it's layoutSize
            this.layoutSize
        } else {
            // This shouldn't happen through the usage of SnappingFlingBehavior, but otherwise
            // we fallback to the end offset. That value contains the end content padding though.
            viewportEndOffset
        }
}

/**
 * A snapping [FlingBehavior] for [LazyListState]. Typically this would be created
 * via [rememberSnappingFlingBehavior].
 *
 * Note: the default parameter value for [decayAnimationSpec] is different to the value used in
 * [rememberSnappingFlingBehavior], due to not being able to access composable functions.
 *
 * @param lazyListState The [LazyListState] to update.
 * @param snapOffsetForItem Block which returns which offset the given item should 'snap' to.
 * See [SnapOffsets] for provided values.
 * @param maximumFlingDistance Block which returns the maximum fling distance in pixels.
 * The returned value should be >= 0.
 * @param decayAnimationSpec The decay animation spec to use for decayed flings.
 * @param snapAnimationSpec The animation spec to use when snapping.
 * @param endContentPadding The amount of content padding on the end edge of the lazy list
 * in pixels (end/bottom depending on the scrolling direction).
 */
@ExperimentalLazySnapApi
class SnappingFlingBehavior(
    private val lazyListState: LazyListState,
    private val snapOffsetForItem: (LazyListLayoutInfo, LazyListItemInfo) -> Int = SnapOffsets.Center,
    private val maximumFlingDistance: LazyListLayoutInfo.() -> Int = SnappingFlingBehaviorDefaults.MaximumFlingDistance,
    private val decayAnimationSpec: DecayAnimationSpec<Float> = exponentialDecay(),
    private val snapAnimationSpec: AnimationSpec<Float> = SnappingFlingBehaviorDefaults.SnapAnimationSpec,
    @Px endContentPadding: Int = 0,
) : FlingBehavior {
    /**
     * The target item index for any on-going animations.
     */
    var animationTarget: Int? by mutableStateOf(null)
        private set

    private val layoutInfo: LazyListLayoutInfo by derivedStateOf {
        LazyListLayoutInfoWithContentPadding(
            layoutInfo = lazyListState.layoutInfo,
            endContentPadding = this.endContentPadding
        )
    }

    internal var endContentPadding: Int by mutableStateOf(endContentPadding)

    override suspend fun ScrollScope.performFling(
        initialVelocity: Float
    ): Float {
        val itemInfo = currentItemInfo ?: return initialVelocity

        Napier.d(message = { "performFling. initialVelocity: $initialVelocity" })

        return if (decayAnimationSpec.canDecayBeyondCurrentItem(itemInfo, initialVelocity)) {
            // If the decay fling can scroll past the current item, fling with decay
            performDecayFling(
                initialItem = itemInfo,
                targetIndex = determineTargetIndexForDecay(initialVelocity, itemInfo),
                initialVelocity = initialVelocity,
            )
        } else {
            // Otherwise we 'spring' to current/next item
            performSpringFling(
                initialItem = itemInfo,
                targetIndex = determineTargetIndexForSpring(initialVelocity, itemInfo),
                initialVelocity = initialVelocity,
            )
        }
    }

    /**
     * Performs a decaying fling.
     *
     * If [flingThenSpring] is set to true, then a fling-then-spring animation might be used.
     * If used, a decay fling will be run until we've scrolled to the preceding item of
     * [targetIndex]. Once that happens, the decay animation is stopped and a spring animation
     * is started to scroll the remainder of the distance. Visually this results in a much
     * smoother finish to the animation, as it will slowly come to a stop at [targetIndex].
     * Even if [flingThenSpring] is set to true, fling-then-spring animations are only available
     * when scrolling 2 items or more.
     *
     * When [flingThenSpring] is not used, the decay animation will be stopped immediately upon
     * scrolling past [targetIndex], which can result in an abrupt stop.
     */
    private suspend fun ScrollScope.performDecayFling(
        initialItem: LazyListItemInfo,
        targetIndex: Int,
        initialVelocity: Float,
        flingThenSpring: Boolean = true,
    ): Float {
        // If we're already at the target + snap offset, skip
        if (initialItem.index == targetIndex &&
            initialItem.offset == snapOffsetForItem(layoutInfo, initialItem)
        ) {
            Napier.d(
                message = {
                    "Skipping decay: already at target. " +
                        "vel:$initialVelocity, " +
                        "current item: ${initialItem.log()}, " +
                        "target: $targetIndex"
                }
            )
            return initialVelocity
        }

        Napier.d(
            message = {
                "Performing decay fling. " +
                    "vel:$initialVelocity, " +
                    "current item: ${currentItemInfo?.log()}, " +
                    "target: $targetIndex"
            }
        )

        var velocityLeft = initialVelocity
        var lastValue = 0f

        // We can only fling-then-spring if we're flinging >= 2 items...
        val canSpringThenFling = flingThenSpring && abs(targetIndex - initialItem.index) >= 2
        var needSpringAfter = false

        try {
            // Update the animationTarget
            animationTarget = targetIndex

            AnimationState(
                initialValue = 0f,
                initialVelocity = initialVelocity,
            ).animateDecay(decayAnimationSpec) {
                val delta = value - lastValue
                val consumed = scrollBy(delta)
                lastValue = value
                velocityLeft = velocity

                if (abs(delta - consumed) > 0.5f) {
                    // If some of the scroll was not consumed, cancel the animation now as we're
                    // likely at the end of the scroll range
                    cancelAnimation()
                }

                val currentItem = currentItemInfo
                if (isRunning && canSpringThenFling && currentItem != null) {
                    // If we're still running and fling-then-spring is enabled, check to see
                    // if we're at the 1 item width away (in the relevant direction). If we are,
                    // set the spring-after flag and cancel the current decay
                    if (initialVelocity > 0 && currentItem.index == targetIndex - 1) {
                        needSpringAfter = true
                        cancelAnimation()
                    } else if (initialVelocity < 0 && currentItem.index == targetIndex) {
                        needSpringAfter = true
                        cancelAnimation()
                    }
                }

                if (isRunning && isSnapBackNeeded(initialVelocity, targetIndex, ::scrollBy)) {
                    // If we're still running, check to see if we need to snap-back
                    // (if we've scrolled past the target)
                    cancelAnimation()
                }
            }
        } finally {
            animationTarget = null
        }

        Napier.d(
            message = {
                "Decay fling finished. Distance: $lastValue. Final vel: $velocityLeft"
            }
        )

        val currentItem = currentItemInfo
        if (currentItem != null && needSpringAfter) {
            // The needSpringAfter flag is enabled, so start a spring to the target using the
            // remaining velocity
            return performSpringFling(currentItem, targetIndex, velocityLeft)
        }

        return velocityLeft
    }

    private fun determineTargetIndexForDecay(
        initialVelocity: Float,
        currentItem: LazyListItemInfo,
    ): Int {
        val distancePerChild = lazyListState.layoutInfo.distancePerChild
        if (distancePerChild <= 0) {
            // If we don't have a valid distance, return the current item
            return currentItem.index
        }

        val maximumFlingDistance = maximumFlingDistance(lazyListState.layoutInfo)
        require(maximumFlingDistance >= 0) {
            "Values returned from maximumFlingDistance should be >= 0"
        }
        val flingDistance = decayAnimationSpec.calculateTargetValue(0f, initialVelocity)
            .coerceIn(-maximumFlingDistance.toFloat(), maximumFlingDistance.toFloat())
        val itemSpacing = lazyListState.layoutInfo.itemSpacing
        val snapOffset = snapOffsetForItem(layoutInfo, currentItem)

        val distanceToNextSnap = if (initialVelocity > 0) {
            // forwards, toward index + 1
            currentItem.size + currentItem.offset + itemSpacing - snapOffset
        } else {
            currentItem.offset - snapOffset
        }

        /**
         * We calculate the index delta by dividing the fling distance by the average
         * scroll per child.
         *
         * We take the current item offset into account by subtracting `distanceToNextSnap`
         * from the fling distance. This is then applied as an extra index delta below.
         */
        val indexDelta = truncate(
            (flingDistance - distanceToNextSnap) / distancePerChild
        ).let {
            // As we removed the `distanceToNextSnap` from the fling distance, we need to calculate
            // whether we need to take that into account...
            if (initialVelocity > 0) {
                // If we're flinging forward, distanceToNextSnap represents the scroll distance
                // to index + 1, so we need to add that (1) to the calculate delta
                it.toInt() + 1
            } else {
                // If we're going backwards, distanceToNextSnap represents the scroll distance
                // to the snap point of the current index, so there's nothing to do
                it.toInt()
            }
        }

        Napier.d(
            message = {
                "determineTargetIndexForDecay. " +
                    "currentItem: ${currentItem.log()}, " +
                    "distancePerChild: $distancePerChild, " +
                    "maximumFlingDistance: $maximumFlingDistance, " +
                    "flingDistance: $flingDistance, " +
                    "indexDelta: $indexDelta"
            }
        )

        return (currentItem.index + indexDelta).coerceIn(0, lazyListState.layoutInfo.lastIndex)
    }

    @Suppress("unused_parameter")
    private fun determineTargetIndexForSpring(
        initialVelocity: Float,
        currentItemInfo: LazyListItemInfo,
    ): Int {
        // We can't trust the velocity right now. We're waiting on
        // https://android-review.googlesource.com/c/platform/frameworks/support/+/1826965/,
        // which will be available in Compose Foundation 1.1.
        // TODO: uncomment this once we move to Compose Foundation 1.1
        // if (initialVelocity.absoluteValue > 1) {
        //    // If the velocity isn't zero, spring in the relevant direction
        //    return when {
        //        initialVelocity > 0 -> {
        //            (currentItemInfo.index + 1).coerceIn(0, lazyListState.layoutInfo.lastIndex)
        //        }
        //        else -> currentItemInfo.index
        //    }
        // }

        // Otherwise we look at the current offset, and spring to whichever is closer
        val snapOffset = snapOffsetForItem(layoutInfo, currentItemInfo)
        return when {
            currentItemInfo.offset < snapOffset - (currentItemInfo.size / 2) -> {
                (currentItemInfo.index + 1).coerceIn(0, lazyListState.layoutInfo.lastIndex)
            }
            else -> currentItemInfo.index
        }
    }

    private suspend fun ScrollScope.performSpringFling(
        initialItem: LazyListItemInfo,
        targetIndex: Int,
        initialVelocity: Float = 0f,
    ): Float {
        // If we're already at the target + snap offset, skip
        if (initialItem.index == targetIndex &&
            initialItem.offset == snapOffsetForItem(layoutInfo, initialItem)
        ) {
            Napier.d(
                message = {
                    "Skipping spring: already at target. " +
                        "vel:$initialVelocity, " +
                        "current item: ${initialItem.log()}, " +
                        "target: $targetIndex"
                }
            )
            return initialVelocity
        }

        Napier.d(
            message = {
                "Performing spring. " +
                    "vel:$initialVelocity, " +
                    "current item: ${initialItem.log()}, " +
                    "target: $targetIndex"
            }
        )

        val itemSpacing = lazyListState.layoutInfo.itemSpacing
        var velocityLeft = initialVelocity
        var lastValue = 0f

        try {
            // Update the animationTarget
            animationTarget = targetIndex

            AnimationState(
                initialValue = 0f,
                initialVelocity = initialVelocity,
            ).animateTo(
                targetValue = when {
                    targetIndex > initialItem.index -> initialItem.size + itemSpacing
                    else -> -(initialItem.size + itemSpacing)
                }.toFloat(),
                animationSpec = snapAnimationSpec,
            ) {
                val delta = value - lastValue
                val consumed = scrollBy(delta)
                lastValue = value
                velocityLeft = velocity

                if (isSnapBackNeeded(initialVelocity, targetIndex, ::scrollBy)) {
                    cancelAnimation()
                } else if (abs(delta - consumed) > 0.5f) {
                    // If we're still running but some of the scroll was not consumed,
                    // cancel the animation now
                    cancelAnimation()
                }
            }
        } finally {
            animationTarget = null
        }

        Napier.d(
            message = {
                "Spring fling finished. Distance: $lastValue. Final vel: $velocityLeft"
            }
        )

        return velocityLeft
    }

    /**
     * Returns true if we needed to perform a snap back, and the animation should be cancelled.
     */
    private fun AnimationScope<Float, AnimationVector1D>.isSnapBackNeeded(
        initialVelocity: Float,
        targetIndex: Int,
        scrollBy: (pixels: Float) -> Float,
    ): Boolean {
        val current = currentItemInfo
        if (current == null) {
            cancelAnimation()
            return true
        }

        Napier.d(
            message = {
                "scroll tick. vel:$velocity, current item: ${current.log()}"
            }
        )

        // Calculate the 'snap back'. If the returned value is 0, we don't need to do anything.
        val snapBackAmount = calculateSnapBack(initialVelocity, current, targetIndex)

        if (snapBackAmount != 0) {
            // If we've scrolled to/past the item, stop the animation. We may also need to
            // 'snap back' to the item as we may have scrolled past it
            Napier.d(
                message = {
                    "Scrolled past item. " +
                        "vel:$initialVelocity, " +
                        "current item: ${current.log()}, " +
                        "target:$targetIndex"
                }
            )
            scrollBy(snapBackAmount.toFloat())
            return true
        }

        return false
    }

    private val currentItemInfo: LazyListItemInfo?
        get() = lazyListState.layoutInfo.let { layoutInfo ->
            layoutInfo.visibleItemsInfo.lastOrNull { itemInfo ->
                itemInfo.offset <= snapOffsetForItem(layoutInfo, itemInfo)
            }
        }

    private fun DecayAnimationSpec<Float>.canDecayBeyondCurrentItem(
        currentItem: LazyListItemInfo,
        initialVelocity: Float,
    ): Boolean {
        // If we don't have a velocity, return false
        if (initialVelocity.absoluteValue < 0.5f) return false

        val flingDistance = calculateTargetValue(0f, initialVelocity)
        val snapOffset = snapOffsetForItem(layoutInfo, currentItem)
        val itemSpacing = lazyListState.layoutInfo.itemSpacing

        Napier.d(
            message = {
                "canDecayBeyondCurrentItem. " +
                    "initialVelocity: $initialVelocity, " +
                    "currentItem: ${currentItem.log()}, " +
                    "flingDistance: $flingDistance, " +
                    "snapOffset: $snapOffset, " +
                    "itemSpacing: $itemSpacing"
            }
        )

        return if (initialVelocity < 0) {
            // backwards, towards 0
            flingDistance <= currentItem.offset - snapOffset
        } else {
            // forwards, toward index + 1
            flingDistance >= currentItem.size + currentItem.offset + itemSpacing - snapOffset
        }
    }

    /**
     * Returns the distance in pixels that is required to 'snap back' to the [targetIndex].
     * Returns 0 if a snap back is not needed.
     */
    private fun calculateSnapBack(
        initialVelocity: Float,
        currentItem: LazyListItemInfo,
        targetIndex: Int,
    ): Int = when {
        // forwards
        initialVelocity > 0 && currentItem.index >= targetIndex -> {
            val target = lazyListState.layoutInfo.visibleItemsInfo.first { it.index == targetIndex }
            val targetScrollOffset = snapOffsetForItem(layoutInfo, target)
            when {
                // We've scrolled past the target index
                currentItem.index > targetIndex -> {
                    target.offset - targetScrollOffset
                }
                // The current item is the target, but we've scrolled past it
                currentItem.index == targetIndex && currentItem.offset < targetScrollOffset -> {
                    target.offset - targetScrollOffset
                }
                else -> 0
            }
        }
        initialVelocity <= 0 && currentItem.index <= targetIndex -> {
            // backwards
            val target = lazyListState.layoutInfo.visibleItemsInfo.first { it.index == targetIndex }
            val targetScrollOffset = snapOffsetForItem(layoutInfo, target)
            when {
                // We've scrolled past the target index
                currentItem.index < targetIndex -> {
                    target.offset - targetScrollOffset
                }
                // The current item is the target, but we've scrolled past it
                currentItem.index == targetIndex && currentItem.offset > targetScrollOffset -> {
                    target.offset - targetScrollOffset
                }
                else -> 0
            }
        }
        else -> 0
    }

    private companion object {
        init {
            if (DebugLog) {
                Napier.base(DebugAntilog(defaultTag = "SnappingFlingBehavior"))
            }
        }
    }
}

internal inline fun LazyListItemInfo.log(): String = "[i:$index,o:$offset,s:$size]"

private val LazyListLayoutInfo.lastIndex: Int
    get() = (totalItemsCount - 1).coerceAtLeast(0)

/**
 * Delegate class around [LazyListLayoutInfo], which allows us to pass through the
 * [endContentPadding] to receiving functions. See [SnapOffsets] for an example.
 *
 * Raised https://issuetracker.google.com/issues/200920410 to track this addition
 * to Compose Foundation.
 */
private class LazyListLayoutInfoWithContentPadding(
    layoutInfo: LazyListLayoutInfo,
    @Px private val endContentPadding: Int = 0,
) : LazyListLayoutInfo by layoutInfo {
    val layoutSize: Int
        get() = viewportEndOffset + viewportStartOffset - endContentPadding
}

/**
 * This attempts to calculate the item spacing for the layout, by looking at the distance
 * between the visible items. If there's only 1 visible item available, it returns 0.
 */
private val LazyListLayoutInfo.itemSpacing: Int
    get() = if (visibleItemsInfo.size >= 2) {
        val first = visibleItemsInfo[0]
        val second = visibleItemsInfo[1]
        second.offset - (first.size + first.offset)
    } else 0

/**
 * Computes an average pixel value to pass a single child.
 *
 * Returns a negative value if it cannot be calculated.
 *
 * @return A float value that is the average number of pixels needed to scroll by one view in
 * the relevant direction.
 */
private val LazyListLayoutInfo.distancePerChild: Float
    get() {
        if (visibleItemsInfo.isEmpty()) return -1f

        val minPosView = visibleItemsInfo.minByOrNull { it.offset } ?: return -1f
        val maxPosView = visibleItemsInfo.maxByOrNull { it.offset + it.size } ?: return -1f

        val start = min(minPosView.offset, maxPosView.offset)
        val end = max(minPosView.offset + minPosView.size, maxPosView.offset + maxPosView.size)

        // We add an extra `itemSpacing` onto the calculated total distance. This ensures that
        // the calculated mean contains an item spacing for each visible item
        // (not just spacing between items)
        return when (val distance = end - start) {
            0 -> -1f // If we don't have a distance, return -1
            else -> (distance + itemSpacing) / visibleItemsInfo.size.toFloat()
        }
    }
