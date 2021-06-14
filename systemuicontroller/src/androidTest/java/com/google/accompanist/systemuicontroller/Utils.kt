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

package com.google.accompanist.systemuicontroller

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import java.util.concurrent.TimeoutException

fun <A : Activity, T : Any> ActivityScenario<A>.withActivity(
    action: (A) -> T
): T {
    lateinit var result: T
    onActivity {
        result = action(it)
    }
    return result
}

fun waitUntil(timeoutMillis: Long = 3_000, condition: () -> Boolean) {
    val startTime = System.nanoTime()
    while (!condition()) {
        // Let Android run measure, draw and in general any other async operations.
        Thread.sleep(10)
        if (System.nanoTime() - startTime > timeoutMillis * 1_000_000) {
            throw TimeoutException("Condition still not satisfied after $timeoutMillis ms")
        }
    }
}
