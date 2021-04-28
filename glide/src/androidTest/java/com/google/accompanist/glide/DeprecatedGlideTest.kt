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

import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.accompanist.glide.test.R
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.imageloading.test.ImageMockWebServer
import com.google.accompanist.imageloading.test.assertPixels
import com.google.accompanist.imageloading.test.receiveBlocking
import com.google.accompanist.imageloading.test.resourceUri
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@Suppress("DEPRECATION")
@OptIn(ExperimentalCoroutinesApi::class)
@LargeTest
@RunWith(JUnit4::class)
class DeprecatedGlideTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule(ComponentActivity::class.java)

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
    fun onRequestCompleted() {
        val results = ArrayList<ImageLoadState>()
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            GlideImage(
                data = server.url("/image").toString(),
                requestBuilder = {
                    listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            exception: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            requestCompleted = true
                            // False so that Glide still invokes the Target
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            source: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            requestCompleted = true
                            // False so that Glide still invokes the Target
                            return false
                        }
                    })
                },
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
                onRequestCompleted = { results += it }
            )
        }

        // Wait for the Glide request listener to release the latch
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
            GlideImage(
                data = server.url("/image").toString(),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp).testTag(GlideTestTags.Image),
                onRequestCompleted = { requestCompleted = true }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun basicLoad_drawable() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            GlideImage(
                data = resourceUri(R.drawable.red_rectangle),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp).testTag(GlideTestTags.Image),
                onRequestCompleted = { requestCompleted = true }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.onNodeWithTag(GlideTestTags.Image)
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
            GlideImage(
                data = data.toString(),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp).testTag(GlideTestTags.Image),
                onRequestCompleted = { loadCompleteSignal = true }
            )
        }

        // Await the first load
        composeTestRule.waitUntil(10_000) { loadCompleteSignal }
        loadCompleteSignal = false

        // Assert that the content is completely Red
        composeTestRule.onNodeWithTag(GlideTestTags.Image)
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
        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Blue)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun basicLoad_changeSize() = runBlockingTest {
        val loadCompleteSignal = Channel<ImageLoadState>(Channel.UNLIMITED)
        var size by mutableStateOf(128.dp)

        composeTestRule.setContent {
            GlideImage(
                data = server.url("/red").toString(),
                contentDescription = null,
                modifier = Modifier.size(size).testTag(GlideTestTags.Image),
                onRequestCompleted = { loadCompleteSignal.offer(it) }
            )
        }

        // Await the first load
        assertThat(loadCompleteSignal.receiveBlocking())
            .isInstanceOf(ImageLoadState.Success::class.java)

        // Now change the size
        size = 256.dp

        // Await the potential second load (which shouldn't come)
        val result = withTimeoutOrNull(3000) { loadCompleteSignal.receive() }
        assertThat(result).isNull()

        // Close the signal channel
        loadCompleteSignal.close()
    }

    @Test
    fun basicLoad_nosize() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            GlideImage(
                data = server.url("/image").toString(),
                contentDescription = null,
                modifier = Modifier.testTag(GlideTestTags.Image),
                onRequestCompleted = { requestCompleted = true }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertWidthIsAtLeast(1.dp)
            .assertHeightIsAtLeast(1.dp)
            .assertIsDisplayed()
    }

    @Test
    fun customRequestManager_param() {
        var requestCompleted by mutableStateOf(false)
        val loaded = mutableListOf<Any>()

        composeTestRule.setContent {
            // Create a RequestManager with a listener which updates our loaded list
            val glide = Glide.with(LocalView.current)
                .addDefaultRequestListener(SimpleRequestListener { model -> loaded += model })

            GlideImage(
                data = server.url("/image").toString(),
                requestManager = glide,
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
                onRequestCompleted = { requestCompleted = true }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // Assert that the listener was called
        assertThat(loaded).hasSize(1)
    }

    @Test
    fun customRequestManager_ambient() {
        var requestCompleted by mutableStateOf(false)
        val loaded = mutableListOf<Any>()

        composeTestRule.setContent {
            // Create a RequestManager with a listener which updates our loaded list
            val glide = Glide.with(LocalView.current)
                .addDefaultRequestListener(SimpleRequestListener { model -> loaded += model })

            CompositionLocalProvider(LocalRequestManager provides glide) {
                GlideImage(
                    data = server.url("/image").toString(),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp, 128.dp),
                    onRequestCompleted = { requestCompleted = true }
                )
            }
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // Assert that the listener was called
        assertThat(loaded).hasSize(1)
    }

    @Test
    fun errorStillHasSize() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            GlideImage(
                data = server.url("/noimage").toString(),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp).testTag(GlideTestTags.Image),
                onRequestCompleted = { requestCompleted = true }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // Assert that the layout is in the tree and has the correct size
        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
    }

    @Test
    fun content_error() {
        var requestCompleted by mutableStateOf(false)
        val states = ArrayList<ImageLoadState>()

        composeTestRule.setContent {
            GlideImage(
                data = server.url("/noimage").toString(),
                requestBuilder = {
                    // Disable memory cache. If the item is in the cache, the fetch is
                    // synchronous and the dispatcher pause has no effect
                    skipMemoryCache(true)
                },
                modifier = Modifier.size(128.dp, 128.dp),
                onRequestCompleted = { requestCompleted = true }
            ) { state ->
                states.add(state)
            }
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.runOnIdle {
            // Check that the final state is an Error
            assertThat(states.last()).isInstanceOf(ImageLoadState.Error::class.java)
        }
    }

    @Test
    fun content_success() {
        var requestCompleted by mutableStateOf(false)
        val states = ArrayList<ImageLoadState>()

        composeTestRule.setContent {
            GlideImage(
                data = server.url("/image").toString(),
                requestBuilder = {
                    // Disable memory cache. If the item is in the cache, the fetch is
                    // synchronous and the dispatcher pause has no effect
                    skipMemoryCache(true)
                },
                modifier = Modifier.size(128.dp, 128.dp),
                onRequestCompleted = { requestCompleted = true }
            ) { state ->
                states.add(state)
            }
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.runOnIdle {
            // Check that the final state is a Success
            assertThat(states.last()).isInstanceOf(ImageLoadState.Success::class.java)
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun content_custom() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            GlideImage(
                data = server.url("/image").toString(),
                modifier = Modifier.size(128.dp, 128.dp).testTag(GlideTestTags.Image),
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
        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Cyan, 0.05f)
    }

    @Test
    fun loading_slot() = runBlockingTest {
        var requestCompleted by mutableStateOf(false)

        // Create a test dispatcher and immediately pause it
        pauseDispatcher()

        composeTestRule.setContent {
            GlideImage(
                data = server.url("/image").toString(),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
                loading = { Text(text = "Loading") },
                onRequestCompleted = { requestCompleted = true }
            )
        }

        // Assert that the loading component is displayed
        composeTestRule.onNodeWithText("Loading").assertIsDisplayed()

        // Now resume the dispatcher to start the Coil request
        resumeDispatcher()

        // We now wait for the request to complete
        composeTestRule.waitUntil(10_000) { requestCompleted }
        composeTestRule.waitForIdle()

        // And assert that the loading component no longer exists
        composeTestRule.onNodeWithText("Loading").assertDoesNotExist()
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun error_slot() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            GlideImage(
                data = server.url("/noimage").toString(),
                error = {
                    // Return failure content which just draws red
                    Image(
                        painter = ColorPainter(Color.Red),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize()
                    )
                },
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp).testTag(GlideTestTags.Image),
                onRequestCompleted = { requestCompleted = true }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // Assert that the whole layout is drawn red
        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Red)
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_drawable_throws() {
        composeTestRule.setContent {
            GlideImage(
                data = ShapeDrawable(),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_imagebitmap_throws() {
        composeTestRule.setContent {
            GlideImage(
                data = painterResource(android.R.drawable.ic_delete),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_imagevector_throws() {
        composeTestRule.setContent {
            GlideImage(
                data = painterResource(R.drawable.ic_android_black_24dp),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_painter_throws() {
        composeTestRule.setContent {
            GlideImage(
                data = ColorPainter(Color.Magenta),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }
    }

    @Test
    fun error_stoppedThenResumed() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            GlideImage(
                data = "",
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
                onRequestCompleted = { requestCompleted = true }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // Now stop the activity, then resume it
        composeTestRule.activityRule.scenario
            .moveToState(Lifecycle.State.CREATED)
            .moveToState(Lifecycle.State.RESUMED)

        // And wait for idle. We shouldn't crash.
        composeTestRule.waitForIdle()
    }
}
