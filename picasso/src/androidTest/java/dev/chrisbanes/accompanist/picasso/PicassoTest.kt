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

package dev.chrisbanes.accompanist.picasso

import android.graphics.drawable.ShapeDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
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
import androidx.core.net.toUri
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import dev.chrisbanes.accompanist.imageloading.ImageLoadState
import dev.chrisbanes.accompanist.imageloading.test.ImageMockWebServer
import dev.chrisbanes.accompanist.imageloading.test.assertPixels
import dev.chrisbanes.accompanist.imageloading.test.awaitNext
import dev.chrisbanes.accompanist.picasso.test.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(JUnit4::class)
class PicassoTest {
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
    fun basicLoad_http() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            PicassoImage(
                data = server.url("/image"),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp).testTag(TestTags.Image),
                onRequestCompleted = { requestCompleted = true }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.onNodeWithTag(TestTags.Image)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun basicLoad_drawable() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            PicassoImage(
                data = R.drawable.red_rectangle,
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp).testTag(TestTags.Image),
                onRequestCompleted = { requestCompleted = true }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.onNodeWithTag(TestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Red)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun basicLoad_switchData() {
        var loadCompleteSignal by mutableStateOf(false)
        var data by mutableStateOf(server.url("/red"))

        composeTestRule.setContent {
            PicassoImage(
                data = data,
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp).testTag(TestTags.Image),
                onRequestCompleted = { loadCompleteSignal = true }
            )
        }

        // Await the first load
        composeTestRule.waitUntil(10_000) { loadCompleteSignal }
        loadCompleteSignal = false

        // Assert that the content is completely Red
        composeTestRule.onNodeWithTag(TestTags.Image)
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
        composeTestRule.onNodeWithTag(TestTags.Image)
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
            PicassoImage(
                data = server.url("/red"),
                contentDescription = null,
                modifier = Modifier.size(size).testTag(TestTags.Image),
                onRequestCompleted = { loadCompleteSignal.offer(it) }
            )
        }

        // Await the first load
        loadCompleteSignal.awaitNext(5, TimeUnit.SECONDS)

        // Now change the size
        size = 256.dp

        // Await the potential second load (which shouldn't come)
        runBlocking {
            val result = withTimeoutOrNull(3000) { loadCompleteSignal.receive() }
            assertThat(result).isNull()
        }
    }

    @Test
    fun basicLoad_nosize() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            PicassoImage(
                data = server.url("/image"),
                contentDescription = null,
                modifier = Modifier.testTag(TestTags.Image),
                onRequestCompleted = { requestCompleted = true }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.onNodeWithTag(TestTags.Image)
            .assertWidthIsAtLeast(1.dp)
            .assertHeightIsAtLeast(1.dp)
            .assertIsDisplayed()
    }

    @SdkSuppress(minSdkVersion = 26) // captureToImage
    @Test
    fun customPicasso_param() {
        var requestCompleted by mutableStateOf(false)

        val picasso = Picasso.Builder(InstrumentationRegistry.getInstrumentation().targetContext)
            .requestTransformer { request ->
                // Transform any request so that it points to a valid red image uri instead
                request.buildUpon()
                    .setUri(server.url("/red").toString().toUri())
                    .build()
            }
            .build()

        composeTestRule.setContent {
            PicassoImage(
                data = server.url("/noimage"),
                picasso = picasso,
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp).testTag(TestTags.Image),
                onRequestCompleted = { requestCompleted = true }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // Assert that the layout is displayed and that we're showing the red image
        composeTestRule.onNodeWithTag(TestTags.Image)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Red)
    }

    @SdkSuppress(minSdkVersion = 26) // captureToImage
    @Test
    fun customPicasso_ambient() {
        var requestCompleted by mutableStateOf(false)

        val picasso = Picasso.Builder(InstrumentationRegistry.getInstrumentation().targetContext)
            .requestTransformer { request ->
                // Transform any request so that it points to a valid red image uri instead
                request.buildUpon()
                    .setUri(server.url("/red").toString().toUri())
                    .build()
            }
            .build()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalPicasso provides picasso) {
                PicassoImage(
                    data = server.url("/noimage"),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp, 128.dp).testTag(TestTags.Image),
                    onRequestCompleted = { requestCompleted = true }
                )
            }
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // Assert that the layout is displayed and that we're showing the red image
        @Suppress("DEPRECATION")
        composeTestRule.onNodeWithTag(TestTags.Image)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Red)
    }

    @Test
    fun errorStillHasSize() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            PicassoImage(
                data = server.url("/noimage"),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp).testTag(TestTags.Image),
                onRequestCompleted = { requestCompleted = true }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // Assert that the layout is in the tree and has the correct size
        composeTestRule.onNodeWithTag(TestTags.Image)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
    }

    @Test
    fun content_error() {
        var requestCompleted by mutableStateOf(false)
        val states = ArrayList<ImageLoadState>()

        composeTestRule.setContent {
            PicassoImage(
                data = server.url("/noimage"),
                modifier = Modifier.size(128.dp, 128.dp),
                // Disable any caches. If the item is in the cache, the fetch is
                // synchronous which means the Loading state is skipped
                requestBuilder = {
                    networkPolicy(NetworkPolicy.NO_CACHE)
                    memoryPolicy(MemoryPolicy.NO_CACHE)
                },
                onRequestCompleted = { requestCompleted = true }
            ) { state ->
                states.add(state)
            }
        }

        // Wait for the onRequestCompleted to release the latch
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
            PicassoImage(
                data = server.url("/image"),
                modifier = Modifier.size(128.dp, 128.dp),
                // Disable any caches. If the item is in the cache, the fetch is
                // synchronous which means the Loading state is skipped
                requestBuilder = {
                    networkPolicy(NetworkPolicy.NO_CACHE)
                    memoryPolicy(MemoryPolicy.NO_CACHE)
                },
                onRequestCompleted = { requestCompleted = true }
            ) { state ->
                states.add(state)
            }
        }

        // Wait for the onRequestCompleted to release the latch
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
            PicassoImage(
                data = server.url("/image"),
                modifier = Modifier.size(128.dp, 128.dp).testTag(TestTags.Image),
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

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // Assert that the whole layout is drawn cyan
        composeTestRule.onNodeWithTag(TestTags.Image)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Cyan)
    }

    @Test
    fun loading_slot() {
        var requestCompleted by mutableStateOf(false)

        // Create an executor which is paused, and build a Picasso instance which uses it
        val executor = SingleThreadPausableExecutor(paused = true)
        val picasso = Picasso.Builder(InstrumentationRegistry.getInstrumentation().targetContext)
            .executor(executor)
            .build()

        composeTestRule.setContent {
            PicassoImage(
                data = server.url("/image"),
                picasso = picasso,
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
                // Disable any caches. If the item is in the cache, the fetch is
                // synchronous which means the Loading state is skipped
                requestBuilder = {
                    networkPolicy(NetworkPolicy.NO_CACHE)
                    memoryPolicy(MemoryPolicy.NO_CACHE)
                },
                loading = { Text(text = "Loading") },
                onRequestCompleted = { requestCompleted = true }
            )
        }

        // Assert that the loading component is displayed
        composeTestRule.onNodeWithText("Loading").assertIsDisplayed()

        // Now resume the executor and let the Picasso request run
        executor.resume()

        // We now wait for the request to complete
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // And assert that the loading component no longer exists
        composeTestRule.onNodeWithText("Loading").assertDoesNotExist()
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun error_slot() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            PicassoImage(
                data = server.url("/noimage"),
                contentDescription = null,
                error = {
                    // Return failure content which just draws red
                    Image(
                        painter = ColorPainter(Color.Red),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize()
                    )
                },
                modifier = Modifier.size(128.dp, 128.dp).testTag(TestTags.Image),
                onRequestCompleted = { requestCompleted = true }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // Assert that the whole layout is drawn red
        @Suppress("DEPRECATION")
        composeTestRule.onNodeWithTag(TestTags.Image)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Red)
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_drawable_throws() {
        composeTestRule.setContent {
            PicassoImage(
                data = ShapeDrawable(),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_imagebitmap_throws() {
        composeTestRule.setContent {
            PicassoImage(
                data = painterResource(android.R.drawable.ic_delete),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_imagevector_throws() {
        composeTestRule.setContent {
            PicassoImage(
                data = painterResource(R.drawable.ic_android_black_24dp),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_painter_throws() {
        composeTestRule.setContent {
            PicassoImage(
                data = ColorPainter(Color.Magenta),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }
    }
}
