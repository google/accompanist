/*
 * Copyright 2022 The Android Open Source Project
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

package com.google.accompanist.themeadapter.material

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle

private val emptyTextStyle = TextStyle()

internal fun Typography.merge(
    h1: TextStyle = emptyTextStyle,
    h2: TextStyle = emptyTextStyle,
    h3: TextStyle = emptyTextStyle,
    h4: TextStyle = emptyTextStyle,
    h5: TextStyle = emptyTextStyle,
    h6: TextStyle = emptyTextStyle,
    subtitle1: TextStyle = emptyTextStyle,
    subtitle2: TextStyle = emptyTextStyle,
    body1: TextStyle = emptyTextStyle,
    body2: TextStyle = emptyTextStyle,
    button: TextStyle = emptyTextStyle,
    caption: TextStyle = emptyTextStyle,
    overline: TextStyle = emptyTextStyle
) = copy(
    h1 = this.h1.merge(h1),
    h2 = this.h2.merge(h2),
    h3 = this.h3.merge(h3),
    h4 = this.h4.merge(h4),
    h5 = this.h5.merge(h5),
    h6 = this.h6.merge(h6),
    subtitle1 = this.subtitle1.merge(subtitle1),
    subtitle2 = this.subtitle2.merge(subtitle2),
    body1 = this.body1.merge(body1),
    body2 = this.body2.merge(body2),
    button = this.button.merge(button),
    caption = this.caption.merge(caption),
    overline = this.overline.merge(overline)
)
