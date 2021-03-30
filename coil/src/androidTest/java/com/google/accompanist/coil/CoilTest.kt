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

package com.google.accompanist.coil

import android.graphics.drawable.ShapeDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import coil.EventListener
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.decode.Options
import coil.fetch.Fetcher
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.accompanist.coil.test.R
import com.google.accompanist.imageloading.ImageLoad
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.imageloading.test.ImageMockWebServer
import com.google.accompanist.imageloading.test.assertPixels
import com.google.accompanist.imageloading.test.resourceUri
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import coil.EventListener
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.decode.Options
import coil.fetch.Fetcher
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.accompanist.coil.test.R
import com.google.accompanist.imageloading.ImageLoad
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.imageloading.test.ImageMockWebServer
import com.google.accompanist.imageloading.test.assertPixels
import com.google.accompanist.imageloading.test.receiveBlocking
import com.google.accompanist.imageloading.test.resourceUri
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channelimportimportimport kotlinx.coroutines.flow.collect kotlinx.coroutines.flow.collect kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@Suppress("DEPRECATION")
@LargeTest
@RunWith(JUnit4::class)
class CoilTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // Our MockWebServer. We use a response delay to simulate real-world conditions
    private val server = ImageMockWebServer()

    @Before
    fun setup() {
        // Start our mock web server
        server.start()
    }

    @After
    fun teardown() {
        // Shutdown our mock web server
        server.shutdown()
    }

    @Test
    fun onRequestCompleted_fromImageRequest() {
        val results = ArrayList<ImageLoadState>()
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            CoilImage(
                request = ImageRequest.Builder(LocalContext.current)
                    .data(server.url("/image"))
                    .listener { _, _ -> requestCompleted = true }
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
                onRequestCompleted = { results += it }
            )
        }

        // Wait for the Coil request listener to run
        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.runOnIdle {
            // And assert that we got a single successful result
            assertThat(results).hasSize(1)
            assertThat(results[0]).isInstanceOf(ImageLoadState.Success::class.java)
        }
    }

    @Test
    fun onRequestCompleted_fromBuilder() {
        val results = ArrayList<ImageLoadState>()
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            val request = rememberCoilImageLoadRequest(
                data = server.url("/image"),
                requestBuilder = {
                    listener { _, _ -> requestCompleted = true }
                },
            )

            ImageLoad(
                request = request,
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )

            LaunchedEffect(request) {
                snapshotFlow { request.loadState }.toList(results)
            }
        }

        // Wait for the Coil request listener to run
        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.runOnIdle {
            // And assert that we got a single successful result
            assertThat(results).hasSize(1)
            assertThat(results[0]).isInstanceOf(ImageLoadState.Success::class.java)
        }
    }

    @Test
    fun basicLoad_http() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            ImageLoad(
                request = rememberCoilImageLoadRequest(
                    data = server.url("/image"),
                    onRequestCompleted = { it: ImageLoadState -> requestCompleted = true },
                ),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp).testTag(CoilTestTags.Image),
            )
        }

        // Wait for the onRequestCompleted to run
        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.onNodeWithTag(CoilTestTags.Image)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun basicLoad_drawable() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            ImageLoad(
                request = rememberCoilImageLoadRequest(
                    data = resourceUri(R.drawable.red_rectangle),
                    onRequestCompleted = { it: ImageLoadState -> requestCompleted = true },
                ),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp).testTag(CoilTestTags.Image),
            )
        }

        // Wait for the onRequestCompleted to run
        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.onNodeWithTag(CoilTestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Red)
    }

    @OptIn(ExperimentalCoilApi::class)
    @Test
    fun basicLoad_customImageLoader() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var requestCompleted by mutableStateOf(false)

        // Build a custom ImageLoader with a fake EventListener
        val eventListener = object : EventListener {
            val startCalled = AtomicInteger()

            override fun fetchStart(request: ImageRequest, fetcher: Fetcher<*>, options: Options) {
                startCalled.incrementAndGet()
            }
        }
        val imageLoader = ImageLoader.Builder(context)
            .eventListener(eventListener)
            .build()

        composeTestRule.setContent {
            ImageLoad(
                request = rememberCoilImageLoadRequest(
                    data = server.url("/image"),
                    imageLoader = imageLoader,
                    onRequestCompleted = { it: ImageLoadState -> requestCompleted = true },
                ),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }

        // Wait for the onRequestCompleted to run
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // Verify that our eventListener was invoked
        assertThat(eventListener.startCalled.get()).isAtLeast(1)
    }

    @OptIn(ExperimentalCoilApi::class)
    @Test
    fun basicLoad_customImageLoader_ambient() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var requestCompleted by mutableStateOf(false)

        // Build a custom ImageLoader with a fake EventListener
        val eventListener = object : EventListener {
            val startCalled = AtomicInteger()

            override fun fetchStart(request: ImageRequest, fetcher: Fetcher<*>, options: Options) {
                startCalled.incrementAndGet()
            }
        }
        val imageLoader = ImageLoader.Builder(context)
            .eventListener(eventListener)
            .build()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                ImageLoad(
                    request = rememberCoilImageLoadRequest(
                        data = server.url("/image"),

                    ),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp, 128.dp),
                )
            }
        }onRequestCompleted = { it: ImageLoadState -> requestCompleted = true },

        // Wait for the onRequestCompleted to run
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // Verify that our eventListener was invoked
        assertThat(eventListener.startCalled.get()).isAtLeast(1)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun basicLoad_switchData() {
        var loadCompleteSignal by mutableStateOf(false)
        var data by mutableStateOf(server.url("/red"))

        composeTestRule.setContent {
            ImageLoad(
                request = rememberCoilImageLoadRequest(
                    data = data,
                    onRequestCompleted = { it: ImageLoadState -> loadCompleteSignal = true },
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp, 128.dp)
                    .testTag(CoilTestTags.Image),
            )
        }

        // Await the first load
        composeTestRule.waitUntil(10_000) { loadCompleteSignal }
        loadCompleteSignal = false

        // Assert that the content is completely Red
        composeTestRule.onNodeWithTag(CoilTestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Red)

        // Now switch the data URI to the blue drawable
        data = server.url("/blue")

        // Await the second load
        composeTestRule.waitUntil(10_000) { loadCompleteSignal }

        // Assert that the content is completely Blue
        composeTestRule.onNodeWithTag(CoilTestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Blue)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun basicLoad_changeSize() {
        val loadCompleteSignal = Channel<ImageLoadState>(Channel.UNLIMITED)
        var size by mutableStateOf(128.dp)

        composeTestRule.setContent {
            ImageLoad(
                request = rememberCoilImageLoadRequest(
                    data = server.url("/red"),
                    onRequestCompleted = { it: ImageLoadState -> loadCompleteSignal.offer(it) },
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .testTag(CoilTestTags.Image),
            )
        }

        // Await the first load
        assertThat(loadCompleteSignal.receiveBlocking())
            .isInstanceOf(ImageLoadState.Success::class.java)

        // Now change the size
        size = 256.dp

        // Await the potential second load (which shouldn't come)
        runBlocking {
            val result = withTimeoutOrNull(3000) { loadCompleteSignal.receive() }
            assertThat(result).isNull()
        }

        // Close the signal channel
        loadCompleteSignal.close()
    }

    @Test
    fun basicLoad_nosize() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            ImageLoad(
                request = rememberCoilImageLoadRequest(
                    data = server.url("/image"),
                    onRequestCompleted = { it: ImageLoadState -> requestCompleted = true },
                ),
                contentDescription = null,
                modifier = Modifier.testTag(CoilTestTags.Image),
            )
        }

        // Wait for the onRequestCompleted to run
        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.onNodeWithTag(CoilTestTags.Image)
            .assertWidthIsAtLeast(1.dp)
            .assertHeightIsAtLeast(1.dp)
            .assertIsDisplayed()
    }

    @Test
    fun errorStillHasSize() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            ImageLoad(
                request = rememberCoilImageLoadRequest(
                    data = server.url("/noimage"),
                    onRequestCompleted = { it: ImageLoadState -> requestCompleted = true },
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp, 128.dp)
                    .testTag(CoilTestTags.Image),
            )
        }

        // Wait for the onRequestCompleted to run
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // Assert that the layout is in the tree and has the correct size
        composeTestRule.onNodeWithTag(CoilTestTags.Image)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
    }

    @Test
    fun content_error() {
        var requestCompleted by mutableStateOf(false)
        val states = ArrayList<ImageLoadState>()

        composeTestRule.setContent {
            CoilImage(
                data = server.url("/noimage"),
                modifier = Modifier.size(128.dp, 128.dp),
                // Disable any caches. If the item is in the cache, the fetch is
                // synchronous which means the Loading state is skipped
                imageLoader = noCacheImageLoader(),
                onRequestCompleted = { requestCompleted = true }
            ) { state ->
                states.add(state)
            }
        }

        // Wait for the onRequestCompleted to run
        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.runOnIdle {
            assertThat(states).hasSize(2)

            assertThat(states[0]).isEqualTo(ImageLoadState.Loading)
            assertThat(states[1]).isInstanceOf(ImageLoadState.Error::class.java)
        }
    }

    @Test
    fun content_success() {
        var requestCompleted by mutableStateOf(false)
        val states = ArrayList<ImageLoadState>()

        composeTestRule.setContent {
            CoilImage(
                data = server.url("/image"),
                modifier = Modifier.size(128.dp, 128.dp),
                // Disable any caches. If the item is in the cache, the fetch is
                // synchronous which means the Loading state is skipped
                imageLoader = noCacheImageLoader(),
                onRequestCompleted = { requestCompleted = true }
            ) { state ->
                states.add(state)
            }
        }

        // Wait for the onRequestCompleted to run
        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.runOnIdle {
            assertThat(states).hasSize(2)
            assertThat(states[0]).isEqualTo(ImageLoadState.Loading)
            assertThat(states[1]).isInstanceOf(ImageLoadState.Success::class.java)
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun content_custom() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            CoilImage(
                data = server.url("/image"),
                modifier = Modifier
                    .size(128.dp, 128.dp)
                    .testTag(CoilTestTags.Image),
                onRequestCompleted = { requestCompleted = true }
            ) {
                // Return an Image which just draws cyan
                Image(
                    painter = ColorPainter(Color.Cyan),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize()
                )
            }
        }

        // Wait for the onRequestCompleted to run
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // Assert that the whole layout is drawn cyan
        composeTestRule.onNodeWithTag(CoilTestTags.Image)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Cyan)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loading_slot() {
        val loadLatch = CountDownLatch(1)

        // Create a test dispatcher and immediately pause it
        val dispatcher = TestCoroutineDispatcher()
        dispatcher.pauseDispatcher()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val imageLoader = ImageLoader.Builder(context)
            // Load on our test dispatcher
            .dispatcher(dispatcher)
            // Disable memory cache. If the item is in the cache, the fetch is
            // synchronous and the dispatcher pause has no effect
            .memoryCachePolicy(CachePolicy.DISABLED)
            .build()

        composeTestRule.setContent {
            fun BoxScope.() {
                Text(text = "Loading")
            }
            ImageLoad(
                request = rememberCoilImageLoadRequest(
                    data = server.url("/image"),
                    imageLoader = imageLoader,
                    onRequestCompleted = { it: ImageLoadState -> loadLatch.countDown() },
                ),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }

        // Assert that the loading component is displayed
        composeTestRule.onNodeWithText("Loading").assertIsDisplayed()

        // Now resume the dispatcher to start the Coil request
        dispatcher.resumeDispatcher()

        // We now wait for the request to complete
        loadLatch.await(5, TimeUnit.SECONDS)

        // And assert that the loading component no longer exists
        composeTestRule.onNodeWithText("Loading").assertDoesNotExist()

        dispatcher.cleanupTestCoroutines()
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun error_slot() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            fun BoxScope.(it: ImageLoadState.Error) {
                // Return failure content which just draws red
                Image(
                    painter = ColorPainter(Color.Red),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize()
                )
            }
            // Return failure content which just draws red
            ImageLoad(
                request = rememberCoilImageLoadRequest(
                    data = server.url("/noimage"),
                    onRequestCompleted = { it: ImageLoadState -> requestCompleted = true },
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp, 128.dp)
                    .testTag(CoilTestTags.Image),
            )
        }

        // Wait for the onRequestCompleted to run
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // Assert that the whole layout is drawn red
        composeTestRule.onNodeWithTag(CoilTestTags.Image)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Red)
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_drawable_throws() {
        composeTestRule.setContent {
            ImageLoad(
                request = rememberCoilImageLoadRequest(
                    data = ShapeDrawable(),
                ),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_imagebitmap_throws() {
        composeTestRule.setContent {
            ImageLoad(
                request = rememberCoilImageLoadRequest(
                    data = painterResource(android.R.drawable.ic_delete),
                ),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_imagevector_throws() {
        composeTestRule.setContent {
            ImageLoad(
                request = rememberCoilImageLoadRequest(
                    data = painterResource(R.drawable.ic_android_black_24dp),
                ),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_painter_throws() {
        composeTestRule.setContent {
            ImageLoad(
                request = rememberCoilImageLoadRequest(
                    data = ColorPainter(Color.Magenta),
                ),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }
    }
}

/**
 * [ImageLoader] which disables all caching
 */
private fun noCacheImageLoader(): ImageLoader {
    val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    return ImageLoader.Builder(ctx)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .build()
}
