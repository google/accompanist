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

package com.google.accompanist.placeholder

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

internal class Solid(
    private val color: Color,
    private val animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(
            delayMillis = 0,
            durationMillis = 500
        ),
        repeatMode = RepeatMode.Restart
    )
) : PlaceholderAnimatedBrush() {

    override fun minimumProgress(): Float = 0f

    override fun maximumProgress(): Float = 1f

    override fun animationSpec(): InfiniteRepeatableSpec<Float> = animationSpec

    override fun brush(progress: Float): Brush {
        return SolidColor(color)
    }
}
