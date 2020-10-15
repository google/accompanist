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

package dev.chrisbanes.accompanist.imageloading.test

import android.content.ContentResolver
import android.net.Uri
import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit

fun resourceUri(id: Int): Uri {
    val packageName = InstrumentationRegistry.getInstrumentation().targetContext.packageName
    return "${ContentResolver.SCHEME_ANDROID_RESOURCE}://$packageName/$id".toUri()
}

/**
 * Designed to mirror `CountdownLatch.await()`
 */
fun <T> ReceiveChannel<T>.awaitNext(timeout: Long, unit: TimeUnit): T = runBlocking {
    withTimeout(unit.toMillis(timeout)) { receive() }
}

fun <T> ReceiveChannel<T>.receiveBlocking(): T = runBlocking { receive() }
