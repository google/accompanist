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

package com.google.accompanist.pager

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope

/**
 * Workaround for https://issuetracker.google.com/182893298
 *
 * This class fixes the direction of velocities, and the resulting scroll changes to
 * be consistent, regardless of the direction.
 */
internal class WorkaroundFlingBehavior(
    val reverseDirection: Boolean,
    val onFling: suspend ScrollScope.(initialVelocity: Float) -> Float,
) : FlingBehavior {
    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        val newScope = object : ScrollScope {
            override fun scrollBy(pixels: Float): Float = when {
                reverseDirection -> this@performFling.scrollBy(-pixels) * -1
                else -> this@performFling.scrollBy(pixels)
            }
        }

        return with(newScope) {
            onFling(if (reverseDirection) -initialVelocity else initialVelocity)
        }
    }
}
