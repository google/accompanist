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

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.view.WindowInsetsCompat

@Stable
interface WindowInsets {

    /**
     * Inset values which match [WindowInsetsCompat.Type.navigationBars]
     */
    val navigationBars: InsetsType

    /**
     * Inset values which match [WindowInsetsCompat.Type.statusBars]
     */
    val statusBars: InsetsType

    /**
     * Inset values which match [WindowInsetsCompat.Type.ime]
     */
    val ime: InsetsType

    /**
     * Inset values which match [WindowInsetsCompat.Type.systemGestures]
     */
    val systemGestures: InsetsType

    /**
     * Inset values which match [WindowInsetsCompat.Type.systemBars]
     */
    val systemBars: InsetsType

    /**
     * TODO
     */
    fun copy(
        navigationBars: InsetsType = this.navigationBars,
        statusBars: InsetsType = this.statusBars,
        systemGestures: InsetsType = this.systemGestures,
        ime: InsetsType = this.ime,
    ): WindowInsets = ImmutableWindowInsets(
        systemGestures = systemGestures,
        navigationBars = navigationBars,
        statusBars = statusBars,
        ime = ime,
    )

    companion object {
        /**
         * TODO
         */
        val Empty: WindowInsets = ImmutableWindowInsets()
    }
}

/**
 * Main holder of our inset values.
 */
internal class RootWindowInsets : WindowInsets {
    /**
     * Inset values which match [WindowInsetsCompat.Type.systemGestures]
     */
    override val systemGestures: MutableInsetsType = MutableInsetsType()

    /**
     * Inset values which match [WindowInsetsCompat.Type.navigationBars]
     */
    override val navigationBars: MutableInsetsType = MutableInsetsType()

    /**
     * Inset values which match [WindowInsetsCompat.Type.statusBars]
     */
    override val statusBars: MutableInsetsType = MutableInsetsType()

    /**
     * Inset values which match [WindowInsetsCompat.Type.ime]
     */
    override val ime: MutableInsetsType = MutableInsetsType()

    /**
     * Inset values which match [WindowInsetsCompat.Type.systemBars]
     */
    override val systemBars: InsetsType = CalculatedInsetsType(statusBars, navigationBars)
}

internal class ImmutableWindowInsets(
    override val systemGestures: InsetsType = InsetsType.Empty,
    override val navigationBars: InsetsType = InsetsType.Empty,
    override val statusBars: InsetsType = InsetsType.Empty,
    override val ime: InsetsType = InsetsType.Empty,
) : WindowInsets {
    override val systemBars: InsetsType = CalculatedInsetsType(statusBars, navigationBars)
}

/**
 * Composition local containing the current [WindowInsets].
 */
val LocalWindowInsets = staticCompositionLocalOf { WindowInsets.Empty }
