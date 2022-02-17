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

@file:Suppress("DEPRECATION")

package com.google.accompanist.insets

import androidx.annotation.FloatRange
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Mutable [androidx.compose.runtime.State] backed implementation of [WindowInsets.Type].
 */
internal class MutableWindowInsetsType : WindowInsets.Type {
    private var ongoingAnimationsCount by mutableStateOf(0)

    /**
     * The layout inset values for this [WindowInsets.Type]. These are the insets which are defined from
     * the current window layout.
     *
     * You should not normally need to use this directly, and instead use [left], [top],
     * [right], and [bottom] to return the correct value for the current state.
     */
    override val layoutInsets: MutableInsets = MutableInsets()

    /**
     * The animated inset values for this [WindowInsets.Type]. These are the insets which are updated from
     * any on-going animations. If there are no animations in progress, the returned
     * [Insets] will be empty.
     *
     * You should not normally need to use this directly, and instead use [left], [top],
     * [right], and [bottom] to return the correct value for the current state.
     */
    override val animatedInsets: MutableInsets = MutableInsets()

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
            animatedInsets.reset()
            animationFraction = 0f
        }
    }
}

/**
 * Shallow-immutable implementation of [WindowInsets.Type].
 */
internal class ImmutableWindowInsetsType(
    override val layoutInsets: Insets = Insets.Empty,
    override val animatedInsets: Insets = Insets.Empty,
    override val isVisible: Boolean = false,
    override val animationInProgress: Boolean = false,
    override val animationFraction: Float = 0f,
) : WindowInsets.Type

/**
 * Returns an instance of [WindowInsets.Type] whose values are calculated and derived from the
 * [WindowInsets.Type] instances passed in to [types].
 *
 * Each [WindowInsets.Type] passed in will be coerced with each other, such that the maximum value for
 * each dimension is calculated and used. This is typically used for two purposes:
 *
 * 1) Creating semantic types. [WindowInsets.systemBars] is a good example, as it is the derived
 * insets of [WindowInsets.statusBars] and [WindowInsets.navigationBars].
 * 2) Combining insets for specific usages. [navigationBarsWithImePadding] is a good example, as it
 * is the derived insets of the [WindowInsets.ime] insets, coerced by the
 * [WindowInsets.navigationBars] insets.
 */
@Deprecated("accompanist/insets is deprecated")
fun derivedWindowInsetsTypeOf(vararg types: WindowInsets.Type): WindowInsets.Type = CalculatedWindowInsetsType(*types)

/**
 * Implementation of [WindowInsets.Type] which is the backing implementation for [derivedWindowInsetsTypeOf].
 */
private class CalculatedWindowInsetsType(vararg types: WindowInsets.Type) : WindowInsets.Type {
    override val layoutInsets: Insets by derivedStateOf {
        types.fold(Insets.Empty) { acc, insetsType ->
            acc.coerceEachDimensionAtLeast(insetsType)
        }
    }

    override val animatedInsets: Insets by derivedStateOf {
        types.fold(Insets.Empty) { acc, insetsType ->
            acc.coerceEachDimensionAtLeast(insetsType)
        }
    }

    override val isVisible: Boolean by derivedStateOf {
        types.all { it.isVisible }
    }

    override val animationInProgress: Boolean by derivedStateOf {
        types.any { it.animationInProgress }
    }

    override val animationFraction: Float by derivedStateOf {
        types.maxOf { it.animationFraction }
    }
}
