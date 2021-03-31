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

package com.google.accompanist.imageloading.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.google.accompanist.imageloading.AsyncImageState
import com.google.accompanist.imageloading.isFinalState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter

/**
 * Callback wrapper for tests.
 *
 * Don't copy this in apps, it's purely for tests.
 */
@Composable
inline fun LaunchedOnRequestComplete(
    state: AsyncImageState<*>,
    crossinline block: () -> Unit
) {
    LaunchedEffect(state) {
        snapshotFlow { state.loadState }
            .filter { it.isFinalState() }
            .collect { block() }
    }
}
