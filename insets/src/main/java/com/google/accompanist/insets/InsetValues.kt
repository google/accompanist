/*
 * Copyright 2020 The Android Open Source Project
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

import androidx.annotation.IntRange
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Interface which represents a single set of inset values. Each instance holds four integer
 * offsets which describe changes to the four edges of a rectangle.
 */
@Stable
interface InsetValues {
    /**
     * The left dimension of these insets in pixels.
     */
    @get:IntRange(from = 0)
    val left: Int

    /**
     * The top dimension of these insets in pixels.
     */
    @get:IntRange(from = 0)
    val top: Int

    /**
     * The right dimension of these insets in pixels.
     */
    @get:IntRange(from = 0)
    val right: Int

    /**
     * The bottom dimension of these insets in pixels.
     */
    @get:IntRange(from = 0)
    val bottom: Int

    /**
     * Returns a copy of this instance with the given values.
     */
    fun copy(
        left: Int = this.left,
        top: Int = this.top,
        right: Int = this.right,
        bottom: Int = this.bottom,
    ): InsetValues = ImmutableInsetValues(left, top, right, bottom)

    operator fun minus(other: InsetValues): InsetValues = copy(
        left = this.left - other.left,
        top = this.top - other.top,
        right = this.right - other.right,
        bottom = this.bottom - other.bottom,
    )

    operator fun plus(other: InsetValues): InsetValues = copy(
        left = this.left + other.left,
        top = this.top + other.top,
        right = this.right + other.right,
        bottom = this.bottom + other.bottom,
    )

    companion object {
        /**
         * Creates an [InsetValues] instance with the given values.
         */
        fun InsetValues(
            left: Int = 0,
            top: Int = 0,
            right: Int = 0,
            bottom: Int = 0,
        ): InsetValues = ImmutableInsetValues(left, top, right, bottom)

        /**
         * An empty [InsetValues] instance, with each dimension set to a value of 0.
         */
        val Empty: InsetValues = ImmutableInsetValues()
    }
}

/**
 * Immutable implementation of [InsetValues].
 */
@Immutable
internal class ImmutableInsetValues(
    override val left: Int = 0,
    override val top: Int = 0,
    override val right: Int = 0,
    override val bottom: Int = 0,
) : InsetValues

/**
 * Mutable [androidx.compose.runtime.State] backed implementation of [InsetValues].
 */
internal class MutableInsetValues(
    left: Int = 0,
    top: Int = 0,
    right: Int = 0,
    bottom: Int = 0,
) : InsetValues {
    override var left by mutableStateOf(left)
        internal set

    override var top by mutableStateOf(top)
        internal set

    override var right by mutableStateOf(right)
        internal set

    override var bottom by mutableStateOf(bottom)
        internal set

    fun reset() {
        left = 0
        top = 0
        right = 0
        bottom = 0
    }
}

/**
 * Updates our mutable state backed [InsetsType] from an Android system insets.
 */
internal fun MutableInsetValues.updateFrom(insets: androidx.core.graphics.Insets) {
    left = insets.left
    top = insets.top
    right = insets.right
    bottom = insets.bottom
}
