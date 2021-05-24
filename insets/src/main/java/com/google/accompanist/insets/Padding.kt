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

@file:Suppress("NOTHING_TO_INLINE", "unused")

package com.google.accompanist.insets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Selectively apply additional space which matches the width/height of any system bars present
 * on the respective edges of the screen.
 *
 * @param enabled Whether to apply padding using the system bars dimensions on the respective edges.
 * Defaults to `true`.
 */
inline fun Modifier.systemBarsPadding(enabled: Boolean = true): Modifier = composed {
    padding(
        rememberWindowInsetsTypePaddingValues(
            type = LocalWindowInsets.current.navigationBars,
            applyStart = enabled,
            applyTop = enabled,
            applyEnd = enabled,
            applyBottom = enabled
        )
    )
}

/**
 * Apply additional space which matches the height of the status bars height along the top edge
 * of the content.
 */
inline fun Modifier.statusBarsPadding(): Modifier = composed {
    padding(
        rememberWindowInsetsTypePaddingValues(
            type = LocalWindowInsets.current.statusBars,
            applyTop = true
        )
    )
}

/**
 * Apply additional space which matches the height of the navigation bars height
 * along the [bottom] edge of the content, and additional space which matches the width of
 * the navigation bars on the respective [start] and [end] edges.
 *
 * @param bottom Whether to apply padding to the bottom edge, which matches the navigation bars
 * height (if present) at the bottom edge of the screen. Defaults to `true`.
 * @param start Whether to apply padding to the start edge, which matches the navigation bars width
 * (if present) on the start edge of the screen. Defaults to `true`.
 * @param end Whether to apply padding to the end edge, which matches the navigation bars width
 * (if present) on the end edge of the screen. Defaults to `true`.
 */
inline fun Modifier.navigationBarsPadding(
    bottom: Boolean = true,
    start: Boolean = true,
    end: Boolean = true,
): Modifier = composed {
    padding(
        rememberWindowInsetsTypePaddingValues(
            type = LocalWindowInsets.current.navigationBars,
            applyStart = start,
            applyEnd = end,
            applyBottom = bottom
        )
    )
}

/**
 * Apply additional space which matches the height of the [WindowInsets.ime] (on-screen keyboard)
 * height along the bottom edge of the content.
 *
 * This method has no special handling for the [WindowInsets.navigationBars], which usually
 * intersect the [WindowInsets.ime]. Most apps will usually want to use the
 * [Modifier.navigationBarsWithImePadding] modifier.
 */
inline fun Modifier.imePadding(): Modifier = composed {
    padding(
        rememberWindowInsetsTypePaddingValues(
            type = LocalWindowInsets.current.ime,
            applyStart = true,
            applyEnd = true,
            applyBottom = true
        )
    )
}

/**
 * Apply additional space which matches the height of the [WindowInsets.ime] (on-screen keyboard)
 * height and [WindowInsets.navigationBars]. This is what apps should use to handle any insets
 * at the bottom of the screen.
 */
inline fun Modifier.navigationBarsWithImePadding(): Modifier = composed {
    val ime = LocalWindowInsets.current.ime
    val navBars = LocalWindowInsets.current.navigationBars
    val insets = remember(ime, navBars) { derivedWindowInsetsTypeOf(ime, navBars) }
    padding(
        rememberWindowInsetsTypePaddingValues(
            type = insets,
            applyStart = true,
            applyEnd = true,
            applyBottom = true
        )
    )
}

/**
 * Returns the current insets converted into a [PaddingValues].
 *
 * @param start Whether to apply the inset on the start dimension.
 * @param top Whether to apply the inset on the top dimension.
 * @param end Whether to apply the inset on the end dimension.
 * @param bottom Whether to apply the inset on the bottom dimension.
 * @param additionalHorizontal Value to add to the start and end dimensions.
 * @param additionalVertical Value to add to the top and bottom dimensions.
 */
@Deprecated(
    "Replaced with rememberWindowInsetsTypePaddingValues()",
    ReplaceWith(
        """rememberWindowInsetsTypePaddingValues(
            type = this,
            applyStart = start,
            applyTop = top,
            applyEnd = end,
            applyBottom = bottom,
            additionalStart = additionalHorizontal,
            additionalTop = additionalVertical,
            additionalEnd = additionalHorizontal,
            additionalBottom = additionalVertical
        )""",
        "com.google.accompanist.insets.rememberWindowInsetsTypePaddingValues"
    )
)
@Composable
inline fun WindowInsets.Type.toPaddingValues(
    start: Boolean = true,
    top: Boolean = true,
    end: Boolean = true,
    bottom: Boolean = true,
    additionalHorizontal: Dp = 0.dp,
    additionalVertical: Dp = 0.dp,
): PaddingValues = rememberWindowInsetsTypePaddingValues(
    type = this,
    applyStart = start,
    applyTop = top,
    applyEnd = end,
    applyBottom = bottom,
    additionalStart = additionalHorizontal,
    additionalTop = additionalVertical,
    additionalEnd = additionalHorizontal,
    additionalBottom = additionalVertical
)

/**
 * Returns the current insets converted into a [PaddingValues].
 *
 * @param start Whether to apply the inset on the start dimension.
 * @param top Whether to apply the inset on the top dimension.
 * @param end Whether to apply the inset on the end dimension.
 * @param bottom Whether to apply the inset on the bottom dimension.
 * @param additionalStart Value to add to the start dimension.
 * @param additionalTop Value to add to the top dimension.
 * @param additionalEnd Value to add to the end dimension.
 * @param additionalBottom Value to add to the bottom dimension.
 */
@Deprecated(
    "Replaced with rememberWindowInsetsTypePaddingValues()",
    ReplaceWith(
        """rememberWindowInsetsTypePaddingValues(
            type = this,
            applyStart = start,
            applyTop = top,
            applyEnd = end,
            applyBottom = bottom,
            additionalStart = additionalStart,
            additionalTop = additionalTop,
            additionalEnd = additionalEnd,
            additionalBottom = additionalBottom
        )""",
        "com.google.accompanist.insets.rememberWindowInsetsTypePaddingValues"
    )
)
@Composable
inline fun WindowInsets.Type.toPaddingValues(
    start: Boolean = true,
    top: Boolean = true,
    end: Boolean = true,
    bottom: Boolean = true,
    additionalStart: Dp = 0.dp,
    additionalTop: Dp = 0.dp,
    additionalEnd: Dp = 0.dp,
    additionalBottom: Dp = 0.dp,
): PaddingValues = rememberWindowInsetsTypePaddingValues(
    type = this,
    applyStart = start,
    applyTop = top,
    applyEnd = end,
    applyBottom = bottom,
    additionalStart = additionalStart,
    additionalTop = additionalTop,
    additionalEnd = additionalEnd,
    additionalBottom = additionalBottom
)

/**
 * Returns the current insets converted into a [PaddingValues].
 *
 * @param type
 * @param applyStart Whether to apply the inset on the start dimension.
 * @param applyTop Whether to apply the inset on the top dimension.
 * @param applyEnd Whether to apply the inset on the end dimension.
 * @param applyBottom Whether to apply the inset on the bottom dimension.
 * @param additionalStart Value to add to the start dimension.
 * @param additionalTop Value to add to the top dimension.
 * @param additionalEnd Value to add to the end dimension.
 * @param additionalBottom Value to add to the bottom dimension.
 */
@Composable
fun rememberWindowInsetsTypePaddingValues(
    type: WindowInsets.Type,
    applyStart: Boolean = false,
    applyTop: Boolean = false,
    applyEnd: Boolean = false,
    applyBottom: Boolean = false,
    additionalStart: Dp = 0.dp,
    additionalTop: Dp = 0.dp,
    additionalEnd: Dp = 0.dp,
    additionalBottom: Dp = 0.dp,
): PaddingValues {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    return remember(density, layoutDirection) {
        WindowInsetsTypePaddingValues(insets = type, density = density)
    }.apply {
        this.applyStart = applyStart
        this.applyTop = applyTop
        this.applyEnd = applyEnd
        this.applyBottom = applyBottom

        this.additionalStart = additionalStart
        this.additionalTop = additionalTop
        this.additionalEnd = additionalEnd
        this.additionalBottom = additionalBottom
    }
}

/**
 * Our [PaddingValues] implementation which is state-backed, reading values as [insets] as
 * appropriate. Can be created via [rememberWindowInsetsTypePaddingValues].
 */
@Stable
internal class WindowInsetsTypePaddingValues(
    private val insets: WindowInsets.Type,
    private val density: Density,
) : PaddingValues {
    var applyStart: Boolean by mutableStateOf(false)
    var applyTop: Boolean by mutableStateOf(false)
    var applyEnd: Boolean by mutableStateOf(false)
    var applyBottom: Boolean by mutableStateOf(false)

    var additionalStart: Dp by mutableStateOf(0.dp)
    var additionalTop: Dp by mutableStateOf(0.dp)
    var additionalEnd: Dp by mutableStateOf(0.dp)
    var additionalBottom: Dp by mutableStateOf(0.dp)

    override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp {
        return when (layoutDirection) {
            LayoutDirection.Ltr -> {
                additionalStart + if (applyStart) with(density) { insets.left.toDp() } else 0.dp
            }
            LayoutDirection.Rtl -> {
                additionalEnd + if (applyEnd) with(density) { insets.left.toDp() } else 0.dp
            }
        }
    }

    override fun calculateTopPadding(): Dp = additionalTop + when {
        applyTop -> with(density) { insets.top.toDp() }
        else -> 0.dp
    }

    override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp {
        return when (layoutDirection) {
            LayoutDirection.Ltr -> {
                additionalEnd + if (applyEnd) with(density) { insets.right.toDp() } else 0.dp
            }
            LayoutDirection.Rtl -> {
                additionalStart + if (applyStart) with(density) { insets.right.toDp() } else 0.dp
            }
        }
    }

    override fun calculateBottomPadding(): Dp = additionalBottom + when {
        applyBottom -> with(density) { insets.bottom.toDp() }
        else -> 0.dp
    }
}
