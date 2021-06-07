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

package com.google.accompanist.placeholder.material

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.fade
import com.google.accompanist.placeholder.shimmer

/**
 * Creates a [PlaceholderHighlight] which fades in an appropriate color, using the
 * given [animationSpec].
 *
 * @sample com.google.accompanist.sample.placeholder.DocSample_PlaceholderFade
 *
 * @param animationSpec the [AnimationSpec] to configure the animation.
 */
@Composable
fun PlaceholderHighlight.Companion.fade(
    animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(
            delayMillis = 200,
            durationMillis = 600,
        ),
        repeatMode = RepeatMode.Reverse
    ),
): PlaceholderHighlight = PlaceholderHighlight.fade(
    highlightColor = PlaceholderDefaults.fadeHighlightColor(),
    animationSpec = animationSpec,
)

/**
 * Creates a [PlaceholderHighlight] which shimmers using an appropriate color, using the
 * given [animationSpec].
 *
 * @sample com.google.accompanist.sample.placeholder.DocSample_PlaceholderShimmer
 *
 * @param animationSpec the [AnimationSpec] to configure the animation.
 */
@Composable
fun PlaceholderHighlight.Companion.shimmer(
    animationSpec: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = tween(durationMillis = 1700, delayMillis = 200),
        repeatMode = RepeatMode.Restart
    ),
): PlaceholderHighlight = PlaceholderHighlight.shimmer(
    highlightColor = PlaceholderDefaults.shimmerHighlightColor(),
    animationSpec = animationSpec
)
