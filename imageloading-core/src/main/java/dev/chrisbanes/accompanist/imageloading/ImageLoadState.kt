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

package dev.chrisbanes.accompanist.imageloading

import androidx.compose.ui.graphics.painter.Painter

/**
 * Represents the state of a [ImageLoad]
 */
sealed class ImageLoadState {
    /**
     * Indicates that a request is not in progress.
     */
    object Empty : ImageLoadState()

    /**
     * Indicates that the request is currently in progress.
     */
    object Loading : ImageLoadState()

    /**
     * Indicates that the request completed successfully.
     *
     * @param image The result image.
     * @param source The data source that the image was loaded from.
     */
    data class Success(
        val painter: Painter,
        val source: DataSource
    ) : ImageLoadState()

    /**
     * Indicates that an error occurred while executing the request.
     *
     * @param image The error image.
     * @param throwable The error that failed the request.
     */
    data class Error(
        val painter: Painter?,
        val throwable: Throwable
    ) : ImageLoadState()
}
