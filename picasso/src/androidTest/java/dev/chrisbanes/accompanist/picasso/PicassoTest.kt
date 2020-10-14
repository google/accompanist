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

import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.ui.test.assertHeightIsAtLeast
import androidx.ui.test.assertHeightIsEqualTo
import androidx.ui.test.assertIsDisplayed
import androidx.ui.test.assertPixels
import androidx.ui.test.assertWidthIsAtLeast
import androidx.ui.test.assertWidthIsEqualTo
import androidx.ui.test.captureToBitmap
import androidx.ui.test.createComposeRule
import androidx.ui.test.onNodeWithTag
import androidx.ui.test.onNodeWithText
import com.google.common.truth.Truth.assertThat
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import dev.chrisbanes.accompanist.imageloading.ImageLoadState
import dev.chrisbanes.accompanist.imageloading.test.ImageMockWebServer
import dev.chrisbanes.accompanist.picasso.test.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(JUnit4::class)
class PicassoTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // Our MockWebServer. We use a response delay to simulate real-world conditions
    private val server = ImageMockWebServer(responseDelayMs = 200)

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
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            PicassoImage(
                data = server.url("/image"),
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(TestTags.Image),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        composeTestRule.onNodeWithTag(TestTags.Image)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToBitmap is SDK 26+
    fun basicLoad_drawable() {
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            PicassoImage(
                data = R.drawable.red_rectangle,
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(TestTags.Image),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        composeTestRule.onNodeWithTag(TestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToBitmap()
            .assertPixels { Color.Red }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToBitmap is SDK 26+
    fun basicLoad_switchData() {
        val loadCompleteSignal = Channel<Unit>(Channel.UNLIMITED)
        val data = MutableStateFlow(server.url("/red"))

        composeTestRule.setContent {
            val resId = data.collectAsState()
            PicassoImage(
                data = resId.value,
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(TestTags.Image),
                onRequestCompleted = { loadCompleteSignal.offer(Unit) }
            )
        }

        // Await the first load
        runBlocking {
            withTimeout(5000) {
                loadCompleteSignal.receive()
            }
        }

        // Assert that the content is completely Red
        composeTestRule.onNodeWithTag(TestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToBitmap()
            .assertPixels { Color.Red }

        // Now switch the data URI to the blue drawable
        data.value = server.url("/blue")

        // Await the second load
        runBlocking {
            withTimeout(5000) {
                loadCompleteSignal.receive()
            }
        }

        // Assert that the content is completely Blue
        composeTestRule.onNodeWithTag(TestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToBitmap()
            .assertPixels { Color.Blue }

        // Close the signal channel
        loadCompleteSignal.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun basicLoad_changeSize() {
        val loadCompleteSignal = Channel<ImageLoadState>(Channel.UNLIMITED)
        val sizeFlow = MutableStateFlow(128.dp)

        composeTestRule.setContent {
            val size = sizeFlow.collectAsState()
            PicassoImage(
                data = server.url("/red"),
                modifier = Modifier.preferredSize(size.value).testTag(TestTags.Image),
                onRequestCompleted = { loadCompleteSignal.offer(it) }
            )
        }

        // Await the first load
        runBlocking {
            assertThat(loadCompleteSignal.receive())
                .isInstanceOf(ImageLoadState.Success::class.java)
        }

        // Now change the size
        sizeFlow.value = 256.dp

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
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            PicassoImage(
                data = server.url("/image"),
                modifier = Modifier.testTag(TestTags.Image),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        composeTestRule.onNodeWithTag(TestTags.Image)
            .assertWidthIsAtLeast(1.dp)
            .assertHeightIsAtLeast(1.dp)
            .assertIsDisplayed()
    }

    @Test
    fun errorStillHasSize() {
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            PicassoImage(
                data = server.url("/noimage"),
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(TestTags.Image),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        // Assert that the layout is in the tree and has the correct size
        composeTestRule.onNodeWithTag(TestTags.Image)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
    }

    @Test
    fun content_error() {
        val latch = CountDownLatch(1)
        val states = ArrayList<ImageLoadState>()

        composeTestRule.setContent {
            PicassoImage(
                data = server.url("/noimage"),
                modifier = Modifier.preferredSize(128.dp, 128.dp),
                // Disable any caches. If the item is in the cache, the fetch is
                // synchronous which means the Loading state is skipped
                requestBuilder = {
                    networkPolicy(NetworkPolicy.NO_CACHE)
                    memoryPolicy(MemoryPolicy.NO_CACHE)
                },
                onRequestCompleted = { latch.countDown() }
            ) { state ->
                states.add(state)
            }
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        composeTestRule.runOnIdle {
            assertThat(states).hasSize(3)
            assertThat(states[0]).isEqualTo(ImageLoadState.Empty)
            assertThat(states[1]).isEqualTo(ImageLoadState.Loading)
            assertThat(states[2]).isInstanceOf(ImageLoadState.Error::class.java)
        }
    }

    @Test
    fun content_success() {
        val latch = CountDownLatch(1)
        val states = ArrayList<ImageLoadState>()

        composeTestRule.setContent {
            PicassoImage(
                data = server.url("/image"),
                modifier = Modifier.preferredSize(128.dp, 128.dp),
                // Disable any caches. If the item is in the cache, the fetch is
                // synchronous which means the Loading state is skipped
                requestBuilder = {
                    networkPolicy(NetworkPolicy.NO_CACHE)
                    memoryPolicy(MemoryPolicy.NO_CACHE)
                },
                onRequestCompleted = { latch.countDown() }
            ) { state ->
                states.add(state)
            }
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        composeTestRule.runOnIdle {
            assertThat(states).hasSize(3)
            assertThat(states[0]).isEqualTo(ImageLoadState.Empty)
            assertThat(states[1]).isEqualTo(ImageLoadState.Loading)
            assertThat(states[2]).isInstanceOf(ImageLoadState.Success::class.java)
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToBitmap is SDK 26+
    fun content_custom() {
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            PicassoImage(
                data = server.url("/image"),
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(TestTags.Image),
                onRequestCompleted = { latch.countDown() }
            ) { _ ->
                // Return an Image which just draws cyan
                Image(painter = ColorPainter(Color.Cyan))
            }
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        // Assert that the whole layout is drawn cyan
        composeTestRule.onNodeWithTag(TestTags.Image)
            .assertIsDisplayed()
            .captureToBitmap()
            .assertPixels { Color.Cyan }
    }

    @Test
    fun loading_slot() {
        val loadLatch = CountDownLatch(1)

        // Create an executor which is paused, and build a Picasso instance which uses it
        val executor = SingleThreadPausableExecutor(paused = true)
        val picasso = Picasso.Builder(InstrumentationRegistry.getInstrumentation().targetContext)
            .executor(executor)
            .build()

        composeTestRule.setContent {
            PicassoImage(
                data = server.url("/image"),
                picasso = picasso,
                modifier = Modifier.preferredSize(128.dp, 128.dp),
                // Disable any caches. If the item is in the cache, the fetch is
                // synchronous which means the Loading state is skipped
                requestBuilder = {
                    networkPolicy(NetworkPolicy.NO_CACHE)
                    memoryPolicy(MemoryPolicy.NO_CACHE)
                },
                loading = { Text(text = "Loading") },
                onRequestCompleted = { loadLatch.countDown() }
            )
        }

        // Assert that the loading component is displayed
        composeTestRule.onNodeWithText("Loading").assertIsDisplayed()

        // Now resume the executor and let the Picasso request run
        executor.resume()

        // We now wait for the request to complete
        loadLatch.await(5, TimeUnit.SECONDS)

        // And assert that the loading component no longer exists
        composeTestRule.onNodeWithText("Loading").assertDoesNotExist()
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToBitmap is SDK 26+
    fun error_slot() {
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            PicassoImage(
                data = server.url("/noimage"),
                error = {
                    // Return failure content which just draws red
                    Image(painter = ColorPainter(Color.Red))
                },
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(TestTags.Image),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        // Assert that the whole layout is drawn red
        composeTestRule.onNodeWithTag(TestTags.Image)
            .assertIsDisplayed()
            .captureToBitmap()
            .assertPixels { Color.Red }
    }
}
