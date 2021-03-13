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

package com.google.accompanist.appcompattheme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

internal fun Color.calculateContrastForForeground(foreground: Color): Double {
    return ColorUtils.calculateContrast(foreground.toArgb(), toArgb())
}

/**
 * The WCAG AA minimum contrast for body text is 4.5:1. We may wish to increase this to
 * the AAA level of 7:1 ratio.
 */
private const val MINIMUM_CONTRAST = 4.5

/**
 * Calculates the 'on' color for this background color.
 *
 * This version of the function tries to use the given [textColorPrimary], as long as it
 * meets the minimum contrast against this color.
 */
internal fun Color.calculateOnColorWithTextColorPrimary(textColorPrimary: Color): Color {
    if (textColorPrimary != Color.Unspecified &&
        calculateContrastForForeground(textColorPrimary) >= MINIMUM_CONTRAST
    ) {
        return textColorPrimary
    }
    return calculateOnColor()
}

/**
 * Calculates the 'on' color for this background color.
 *
 * In practice this returns either black or white, depending on which has the highest
 * contrast against this color.
 */
internal fun Color.calculateOnColor(): Color {
    val contrastForBlack = calculateContrastForForeground(Color.Black)
    val contrastForWhite = calculateContrastForForeground(Color.White)
    return if (contrastForBlack > contrastForWhite) Color.Black else Color.White
}
