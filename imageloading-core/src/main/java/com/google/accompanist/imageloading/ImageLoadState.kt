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

@file:Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION")

package com.google.accompanist.imageloading

import androidx.compose.ui.graphics.painter.Painter

/**
 * Represents the state of a [LoadPainter].
 */
@Deprecated("Accompanist-ImageLoading is now deprecated. Consider using Coil: https://coil-kt.github.io/coil/compose")
sealed class ImageLoadState {
    /**
     * Indicates that a request is not in progress.
     */
    @Deprecated("Accompanist-ImageLoading is now deprecated. Consider using Coil: https://coil-kt.github.io/coil/compose")
    object Empty : ImageLoadState()

    /**
     * Indicates that the request is currently in progress.
     */
    @Deprecated("Accompanist-ImageLoading is now deprecated. Consider using Coil: https://coil-kt.github.io/coil/compose")
    data class Loading(
        val placeholder: Painter?,
        val request: Any,
    ) : ImageLoadState()

    /**
     * Indicates that the request completed successfully.
     *
     * @param result The result [Painter].
     * @param source The data source that the image was loaded from.
     * @param request The original request for this result.
     */
    @Deprecated("Accompanist-ImageLoading is now deprecated. Consider using Coil: https://coil-kt.github.io/coil/compose")
    data class Success(
        val result: Painter,
        val source: DataSource,
        val request: Any,
    ) : ImageLoadState()

    /**
     * Indicates that an error occurred while executing the request.
     *
     * @param result The error image.
     * @param throwable The optional throwable that caused the request failure.
     * @param request The original request for this result.
     */
    @Deprecated("Accompanist-ImageLoading is now deprecated. Consider using Coil: https://coil-kt.github.io/coil/compose")
    data class Error(
        val request: Any,
        val result: Painter? = null,
        val throwable: Throwable? = null
    ) : ImageLoadState()
}

/**
 * Returns true if this state represents the final state for the current request.
 */
@Deprecated("Accompanist-ImageLoading is now deprecated. Consider using Coil: https://coil-kt.github.io/coil/compose")
fun ImageLoadState.isFinalState(): Boolean {
    return this is ImageLoadState.Success || this is ImageLoadState.Error
}

internal inline val ImageLoadState.painter: Painter?
    get() = when (this) {
        is ImageLoadState.Success -> result
        is ImageLoadState.Error -> result
        is ImageLoadState.Loading -> placeholder
        else -> null
    }

internal inline val ImageLoadState.request: Any?
    get() = when (this) {
        is ImageLoadState.Success -> request
        is ImageLoadState.Error -> request
        else -> null
    }
