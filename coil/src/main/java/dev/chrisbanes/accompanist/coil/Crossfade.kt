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

@file:JvmName("CoilImage")
@file:JvmMultifileClass

package dev.chrisbanes.accompanist.coil

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.unit.IntSize
import coil.ImageLoader
import coil.imageLoader
import coil.request.ImageRequest
import dev.chrisbanes.accompanist.imageloading.DefaultRefetchOnSizeChangeLambda
import dev.chrisbanes.accompanist.imageloading.EmptyRequestCompleteLambda
import dev.chrisbanes.accompanist.imageloading.ImageLoadState
import dev.chrisbanes.accompanist.imageloading.MaterialLoadingImage

private const val DefaultTransitionDuration = 1000

@Deprecated(
    "Use new `CrossfadeImage` or `CoilImage(fadeIn = true)`",
    ReplaceWith(
        """CoilImage(
    data = data,
    modifier = modifier,
    imageLoader = imageLoader,
    shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
    onRequestCompleted = onRequestCompleted,
    error = error,
    loading = loading
) { result ->
    MaterialLoadingImage(
        result = result,
        alignment = alignment,
        contentScale = contentScale,
        fadeInDurationMs = crossfadeDuration
    )
}""",
        "dev.chrisbanes.accompanist.coil.CoilImage",
        "dev.chrisbanes.accompanist.imageloading.MaterialLoadingImage"
    )
)
@Composable
fun CoilImageWithCrossfade(
    data: Any,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    crossfadeDuration: Int = DefaultTransitionDuration,
    imageLoader: ImageLoader = ContextAmbient.current.imageLoader,
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = DefaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (ImageLoadState) -> Unit = EmptyRequestCompleteLambda,
    error: @Composable ((ImageLoadState.Error) -> Unit)? = null,
    loading: @Composable (() -> Unit)? = null
) {
    @Suppress("DEPRECATION")
    CoilImageWithCrossfade(
        request = data.toImageRequest(),
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        crossfadeDuration = crossfadeDuration,
        imageLoader = imageLoader,
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        onRequestCompleted = onRequestCompleted,
        error = error,
        loading = loading
    )
}

@Deprecated(
    "Use new `CrossfadeImage` or `CoilImage(fadeIn = true)`",
    ReplaceWith(
        """CoilImage(
    request = request,
    modifier = modifier,
    imageLoader = imageLoader,
    shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
    onRequestCompleted = onRequestCompleted,
    error = error,
    loading = loading
) { result ->
    MaterialLoadingImage(
        result = result,
        alignment = alignment,
        contentScale = contentScale,
        fadeInDurationMs = crossfadeDuration
    )
}""",
        "dev.chrisbanes.accompanist.coil.CoilImage",
        "dev.chrisbanes.accompanist.imageloading.MaterialLoadingImage"
    )
)
@Composable
fun CoilImageWithCrossfade(
    request: ImageRequest,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    crossfadeDuration: Int = DefaultTransitionDuration,
    imageLoader: ImageLoader = ContextAmbient.current.imageLoader,
    shouldRefetchOnSizeChange: (currentResult: ImageLoadState, size: IntSize) -> Boolean = DefaultRefetchOnSizeChangeLambda,
    onRequestCompleted: (ImageLoadState) -> Unit = EmptyRequestCompleteLambda,
    error: @Composable ((ImageLoadState.Error) -> Unit)? = null,
    loading: @Composable (() -> Unit)? = null
) {
    CoilImage(
        request = request,
        imageLoader = imageLoader,
        shouldRefetchOnSizeChange = shouldRefetchOnSizeChange,
        modifier = modifier,
        onRequestCompleted = onRequestCompleted,
    ) { imageState ->
        when (imageState) {
            is ImageLoadState.Success -> {
                MaterialLoadingImage(
                    result = imageState,
                    fadeInEnabled = true,
                    fadeInDurationMs = crossfadeDuration,
                    alignment = alignment,
                    contentScale = contentScale,
                )
            }
            is ImageLoadState.Error -> if (error != null) error(imageState)
            ImageLoadState.Loading -> if (loading != null) loading()
            ImageLoadState.Empty -> Unit
        }
    }
}
