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

package com.google.accompanist.glide

import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

/**
 * Simple wrapper around RequestListener for use in tests
 */
internal class SimpleRequestListener(
    private val onComplete: (Any) -> Unit
) : RequestListener<Any> {
    override fun onLoadFailed(
        e: GlideException?,
        model: Any,
        target: Target<Any>?,
        isFirstResource: Boolean
    ): Boolean {
        onComplete(model)
        return false
    }

    override fun onResourceReady(
        resource: Any?,
        model: Any,
        target: Target<Any>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        onComplete(model)
        return false
    }
}
