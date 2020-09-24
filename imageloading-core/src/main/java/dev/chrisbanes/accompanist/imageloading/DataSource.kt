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

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import java.io.File
import java.nio.ByteBuffer

enum class DataSource {

    /**
     * Represents a memory cache.
     *
     * This is a special data source as it means the request was
     * short circuited and skipped the full image pipeline.
     */
    MEMORY_CACHE,

    /**
     * Represents an in-memory data source (e.g. [Bitmap], [ByteBuffer]).
     */
    MEMORY,

    /**
     * Represents a disk-based data source (e.g. [DrawableRes], [File]).
     */
    DISK,

    /**
     * Represents a network-based data source.
     */
    NETWORK
}
