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

import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import java.util.concurrent.TimeUnit

/**
 * [MockWebServer] which returns a valid response at the path `/image`, and a 404 for anything else.
 * We add a small delay to simulate 'real-world' network conditions.
 */
fun ImageMockWebServer(responseDelayMs: Long = 0): MockWebServer {
    val dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse = when (request.path) {
            "/image" -> {
                val res = InstrumentationRegistry.getInstrumentation().targetContext.resources

                // Load the image into a Buffer
                val imageBuffer = Buffer().apply {
                    readFrom(res.openRawResource(R.raw.sample))
                }

                MockResponse()
                        .setHeadersDelay(responseDelayMs, TimeUnit.MILLISECONDS)
                        .addHeader("Content-Type", "image/jpeg")
                        .setBody(imageBuffer)
            }
            else ->
                MockResponse()
                        .setHeadersDelay(responseDelayMs, TimeUnit.MILLISECONDS)
                        .setResponseCode(404)
        }
    }

    return MockWebServer().apply {
        setDispatcher(dispatcher)
    }
}