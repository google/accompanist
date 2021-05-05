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

package com.google.accompanist.insets

import androidx.annotation.FloatRange
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Represents the values for a type of insets, and stores information about the layout insets,
 * animating insets, and visibility of the insets.
 *
 * [InsetsType] instances are commonly stored in a [WindowInsets] instance.
 */
@Stable
interface InsetsType : InsetValues {
    /**
     * The layout insets for this [InsetsType]. These are the insets which are defined from the
     * current window layout.
     *
     * You should not normally need to use this directly, and instead use [left], [top],
     * [right], and [bottom] to return the correct value for the current state.
     */
    val layoutInsetValues: InsetValues

    /**
     * The animated insets for this [InsetsType]. These are the insets which are updated from
     * any on-going animations. If there are no animations in progress, the returned [InsetValues] will
     * be empty.
     *
     * You should not normally need to use this directly, and instead use [left], [top],
     * [right], and [bottom] to return the correct value for the current state.
     */
    val animatedInsetValues: InsetValues

    /**
     * Whether the insets are currently visible.
     */
    val isVisible: Boolean

    /**
     * Whether this insets type is being animated at this moment.
     */
    val animationInProgress: Boolean

    /**
     * The left dimension of the insets in pixels.
     */
    override val left: Int
        get() = (if (animationInProgress) animatedInsetValues else layoutInsetValues).left

    /**
     * The top dimension of the insets in pixels.
     */
    override val top: Int
        get() = (if (animationInProgress) animatedInsetValues else layoutInsetValues).top

    /**
     * The right dimension of the insets in pixels.
     */
    override val right: Int
        get() = (if (animationInProgress) animatedInsetValues else layoutInsetValues).right

    /**
     * The bottom dimension of the insets in pixels.
     */
    override val bottom: Int
        get() = (if (animationInProgress) animatedInsetValues else layoutInsetValues).bottom

    /**
     * The progress of any ongoing animations, in the range of 0 to 1.
     * If there is no animation in progress, this will return 0.
     */
    @get:FloatRange(from = 0.0, to = 1.0)
    val animationFraction: Float

    companion object {
        /**
         * Empty and immutable instance of [InsetsType].
         */
        val Empty: InsetsType = ImmutableInsetsType()
    }
}

/**
 * Mutable [androidx.compose.runtime.State] backed implementation of [InsetsType].
 */
internal class MutableInsetsType : InsetsType {
    private var ongoingAnimationsCount by mutableStateOf(0)
    internal val mutableLayoutInsets = MutableInsetValues()
    internal val mutableAnimatedInsets = MutableInsetValues()

    /**
     * The layout inset values for this [InsetsType]. These are the insets which are defined from
     * the current window layout.
     *
     * You should not normally need to use this directly, and instead use [left], [top],
     * [right], and [bottom] to return the correct value for the current state.
     */
    override val layoutInsetValues: InsetValues
        get() = mutableLayoutInsets

    /**
     * The animated inset values for this [InsetsType]. These are the insets which are updated from
     * any on-going animations. If there are no animations in progress, the returned
     * [InsetValues] will be empty.
     *
     * You should not normally need to use this directly, and instead use [left], [top],
     * [right], and [bottom] to return the correct value for the current state.
     */
    override val animatedInsetValues: InsetValues
        get() = mutableAnimatedInsets

    /**
     * Whether the insets are currently visible.
     */
    override var isVisible by mutableStateOf(true)

    /**
     * Whether this insets type is being animated at this moment.
     */
    override val animationInProgress: Boolean by derivedStateOf {
        ongoingAnimationsCount > 0
    }

    /**
     * The progress of any ongoing animations, in the range of 0 to 1.
     * If there is no animation in progress, this will return 0.
     */
    @get:FloatRange(from = 0.0, to = 1.0)
    override var animationFraction by mutableStateOf(0f)

    fun onAnimationStart() {
        ongoingAnimationsCount++
    }

    fun onAnimationEnd() {
        ongoingAnimationsCount--

        if (ongoingAnimationsCount == 0) {
            // If there are no on-going animations, clear out the animated insets
            mutableAnimatedInsets.reset()
            animationFraction = 0f
        }
    }
}

/**
 * Shallow-immutable implementation of [InsetsType].
 */
internal class ImmutableInsetsType(
    override val layoutInsetValues: InsetValues = InsetValues.Empty,
    override val animatedInsetValues: InsetValues = InsetValues.Empty,
    override val isVisible: Boolean = false,
    override val animationInProgress: Boolean = false,
    override val animationFraction: Float = 0f,
) : InsetsType

/**
 * Returns an instance of [InsetsType] whose values are calculated and derived from the
 * [InsetsType] instances passed in to [types].
 *
 * Each [InsetsType] passed in will be coerced with each other, such that the maximum value for
 * each dimension is calculated and used. This is typically used for two purposes:
 *
 * 1) Creating semantic types. [WindowInsets.systemBars] is a good example, as it is the derived
 * insets of [WindowInsets.statusBars] and [WindowInsets.navigationBars].
 * 2) Combining insets for specific usages. [navigationBarsWithImePadding] is a good example, as it
 * is the derived insets of the [WindowInsets.ime] insets, coerced by the
 * [WindowInsets.navigationBars] insets.
 */
fun derivedInsetsTypeOf(vararg types: InsetsType): InsetsType = CalculatedInsetsType(*types)

/**
 * Implementation of [InsetsType] which is the backing implementation for [derivedInsetsTypeOf].
 */
private class CalculatedInsetsType(vararg types: InsetsType) : InsetsType {
    override val layoutInsetValues: InsetValues by derivedStateOf {
        types.fold(InsetValues.Empty) { acc, insetsType ->
            acc.coerceEachDimensionAtLeast(insetsType)
        }
    }

    override val animatedInsetValues: InsetValues by derivedStateOf {
        types.fold(InsetValues.Empty) { acc, insetsType ->
            acc.coerceEachDimensionAtLeast(insetsType)
        }
    }

    override val isVisible: Boolean by derivedStateOf {
        types.any { it.isVisible }
    }

    override val animationInProgress: Boolean by derivedStateOf {
        types.any { it.animationInProgress }
    }

    override val animationFraction: Float by derivedStateOf {
        types.maxOf { it.animationFraction }
    }
}
