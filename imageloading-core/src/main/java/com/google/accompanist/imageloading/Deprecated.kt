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

@file:Suppress("unused")

package com.google.accompanist.imageloading

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize

/**
 * Default lambda for use in the `shouldRefetchOnSizeChange` parameter.
 */
@Deprecated("Create your own lambda instead", ReplaceWith("{ _, _ -> false }"))
val DefaultRefetchOnSizeChangeLambda: (ImageLoadState, IntSize) -> Boolean = { _, _ -> false }

/**
 * @hide
 */
@Deprecated("Only used to help migration. DO NOT USE.")
@Composable
fun <R> ImageSuchDeprecated(
    loadPainter: LoadPainter<R>,
    modifier: Modifier = Modifier,
    @DrawableRes previewPlaceholder: Int = 0,
    content: @Composable BoxScope.(imageLoadState: ImageLoadState) -> Unit,
) {
    if (LocalInspectionMode.current && previewPlaceholder != 0) {
        // If we're in inspection mode (preview) and we have a preview placeholder, just draw
        // that using an Image and return
        Image(
            painter = painterResource(previewPlaceholder),
            contentDescription = null,
            modifier = modifier,
        )
        return
    }

    Box(
        propagateMinConstraints = true,
        modifier = modifier
            // Layout modifier to receive the incoming constraints, such that we can use them
            // to update our request size
            .layout { measurable, constraints ->
                loadPainter.requestSize = IntSize(
                    width = if (constraints.hasBoundedWidth) constraints.maxWidth else -1,
                    height = if (constraints.hasBoundedHeight) constraints.maxHeight else -1
                )

                // No-op measure + layout
                val placeable = measurable.measure(constraints)
                layout(width = placeable.width, height = placeable.height) {
                    placeable.place(0, 0)
                }
            }
    ) {
        content(loadPainter.loadState)
    }
}
