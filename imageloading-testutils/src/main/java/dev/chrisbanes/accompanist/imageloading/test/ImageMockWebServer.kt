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

import androidx.annotation.RawRes
import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import java.util.concurrent.TimeUnit

/**
 * A [MockWebServer] which returns a valid image responses at various paths, and a 404
 * for anything else.
 *
 * @param responseDelayMs Allows the setting of a response delay to simulate 'real-world'
 * network conditions. Defaults to 200ms.
 */
fun ImageMockWebServer(responseDelayMs: Long = 200): MockWebServer {
    val dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse = when (request.path) {
            "/image" -> {
                rawResourceAsResponse(
                    id = R.raw.sample,
                    mimeType = "image/jpeg",
                    responseDelayMs = responseDelayMs
                )
            }
            "/image2" -> {
                rawResourceAsResponse(
                    id = R.raw.image2,
                    mimeType = "image/jpeg",
                    responseDelayMs = responseDelayMs
                )
            }
            "/blue" -> {
                rawResourceAsResponse(
                    id = R.raw.blue_rectangle,
                    mimeType = "image/png",
                    responseDelayMs = responseDelayMs
                )
            }
            "/red" -> {
                rawResourceAsResponse(
                    id = R.raw.red_rectangle,
                    mimeType = "image/png",
                    responseDelayMs = responseDelayMs
                )
            }
            else -> {
                MockResponse()
                    .setHeadersDelay(responseDelayMs, TimeUnit.MILLISECONDS)
                    .setResponseCode(404)
            }
        }
    }

    return MockWebServer().apply {
        setDispatcher(dispatcher)
    }
}

private fun rawResourceAsResponse(
    @RawRes id: Int,
    mimeType: String,
    responseDelayMs: Long = 0
): MockResponse {
    val res = InstrumentationRegistry.getInstrumentation().targetContext.resources

    // Load the image into a Buffer
    val imageBuffer = Buffer().apply {
        readFrom(res.openRawResource(id))
    }

    return MockResponse()
        .setHeadersDelay(responseDelayMs, TimeUnit.MILLISECONDS)
        .addHeader("Content-Type", mimeType)
        .setBody(imageBuffer)
}
