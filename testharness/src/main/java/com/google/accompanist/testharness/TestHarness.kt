/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.accompanist.testharness

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat

/**
 * Render [content] in a [Box] within a harness, overriding various device configuration values to
 * make testing easier.
 *
 * @param size if not [DpSize.Unspecified], the [content] will be forced to be drawn with at this
 * size, overriding [LocalDensity] if necessary to ensure that there is enough space. This
 * defaults to [DpSize.Unspecified].
 *
 * @param darkMode if true, the content will be rendered with dark mode. This defaults to the
 * current dark mode value as reported by [isSystemInDarkTheme].
 *
 * @param locales the list of locales to render the app with. This defaults to the list of locales
 * returned by [LocalConfiguration.current].
 *
 * @param layoutDirection an overriding layout direction. This defaults to `null`, which means
 * that the layout direction from the [locales] is used instead.
 *
 * @param fontScale the font scale to render text at. This defaults to the current
 * [Density.fontScale].
 *
 * @param fontWeightAdjustment the font weight adjustment for fonts. This defaults to the current
 * [fontWeightAdjustment] (if any). If `null`, the [fontWeightAdjustment] will be left unchanged.
 */
@Composable
fun TestHarness(
    content: @Composable () -> Unit
) {
    // TODO: Alex to implement this.
    content()
}
