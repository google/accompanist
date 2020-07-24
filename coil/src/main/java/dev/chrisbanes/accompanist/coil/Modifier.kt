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

package dev.chrisbanes.accompanist.coil

import androidx.compose.getValue
import androidx.compose.setValue
import androidx.compose.state
import androidx.compose.ui.unit.IntSize
import androidx.ui.core.LayoutCoordinates
import androidx.ui.core.Modifier
import androidx.ui.core.OnPositionedModifier
import androidx.ui.core.composed

/**
 * [Modifier] which will invoke [onSizeChanged] whenever the size of the element changes. This
 * will be called after positioning, similar to `Modifier.onPositioned`.
 */
internal fun Modifier.onSizeChanged(
    onSizeChanged: (IntSize) -> Unit
) = composed {
    var lastSize by state<IntSize?> { null }

    object : OnPositionedModifier {
        override fun onPositioned(coordinates: LayoutCoordinates) {
            if (coordinates.size != lastSize) {
                lastSize = coordinates.size
                onSizeChanged(coordinates.size)
            }
        }
    }
}
