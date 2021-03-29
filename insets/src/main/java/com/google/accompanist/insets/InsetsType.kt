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

@Stable
interface InsetsType : Insets {
    val appendLayoutInsets: Insets

    /**
     * The layout insets for this [InsetsType]. These are the insets which are defined from the
     * current window layout.
     *
     * You should not normally need to use this directly, and instead use [left], [top],
     * [right], and [bottom] to return the correct value for the current state.
     */
    val layoutInsets: Insets

    /**
     * The animated insets for this [InsetsType]. These are the insets which are updated from
     * any on-going animations. If there are no animations in progress, the returned [Insets] will
     * be empty.
     *
     * You should not normally need to use this directly, and instead use [left], [top],
     * [right], and [bottom] to return the correct value for the current state.
     */
    val animatedInsets: Insets

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
        get() = (if (animationInProgress) animatedInsets else layoutInsets).left

    /**
     * The top dimension of the insets in pixels.
     */
    override val top: Int
        get() = (if (animationInProgress) animatedInsets else layoutInsets).top

    /**
     * The right dimension of the insets in pixels.
     */
    override val right: Int
        get() = (if (animationInProgress) animatedInsets else layoutInsets).right

    /**
     * The bottom dimension of the insets in pixels.
     */
    override val bottom: Int
        get() = (if (animationInProgress) animatedInsets else layoutInsets).bottom

    /**
     * The progress of any ongoing animations, in the range of 0 to 1.
     * If there is no animation in progress, this will return 0.
     */
    @get:FloatRange(from = 0.0, to = 1.0)
    val animationFraction: Float

    fun copyWithAppend(appendLayoutInsets: Insets = Insets.Empty): InsetsType
}

/**
 * Represents the values for a type of insets, and stores information about the layout insets,
 * animating insets, and visibility of the insets.
 *
 * [InsetsType] instances are commonly stored in a [WindowInsets] instance.
 */
@Suppress("MemberVisibilityCanBePrivate")
internal class MutableInsetsType : InsetsType {
    private var ongoingAnimationsCount by mutableStateOf(0)
    var _layoutInsets = MutableInsets()
    var _animatedInsets = MutableInsets()

    override var appendLayoutInsets by mutableStateOf(Insets.Empty)
        private set

    override fun copyWithAppend(
        appendLayoutInsets: Insets
    ): InsetsType {
        val new = MutableInsetsType()
        new._animatedInsets = _animatedInsets
        new._layoutInsets = _layoutInsets
        new.appendLayoutInsets = appendLayoutInsets
        return new
    }

    /**
     * The layout insets for this [InsetsType]. These are the insets which are defined from the
     * current window layout.
     *
     * You should not normally need to use this directly, and instead use [left], [top],
     * [right], and [bottom] to return the correct value for the current state.
     */
    override val layoutInsets: Insets by derivedStateOf {
        _layoutInsets + appendLayoutInsets
    }

    /**
     * The animated insets for this [InsetsType]. These are the insets which are updated from
     * any on-going animations. If there are no animations in progress, the returned [Insets] will
     * be empty.
     *
     * You should not normally need to use this directly, and instead use [left], [top],
     * [right], and [bottom] to return the correct value for the current state.
     */
    override val animatedInsets: Insets
        get() = _animatedInsets

    /**
     * Whether the insets are currently visible.
     */
    override var isVisible by mutableStateOf(true)

    /**
     * Whether this insets type is being animated at this moment.
     */
    override val animationInProgress: Boolean
        get() = ongoingAnimationsCount > 0

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
            _animatedInsets.reset()
            animationFraction = 0f
        }
    }
}

internal class CalculatedInsetsType(vararg types: InsetsType) : InsetsType {
    override val layoutInsets: Insets by derivedStateOf {
        types.fold(Insets.Empty) { acc, insetsType ->
            // TODO: Probably want to coerce rather than add
            acc + insetsType.layoutInsets
        }
    }
    override val animatedInsets: Insets by derivedStateOf {
        types.fold(Insets.Empty) { acc, insetsType ->
            // TODO: Probably want to coerce rather than add
            acc + insetsType.animatedInsets
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

    override val appendLayoutInsets: Insets
        get() = Insets.Empty

    override fun copyWithAppend(appendLayoutInsets: Insets): InsetsType {
        return this
    }
}

internal fun InsetsType.toMutableInsetsType(): MutableInsetsType = this as MutableInsetsType
