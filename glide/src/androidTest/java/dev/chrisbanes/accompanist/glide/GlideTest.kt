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

package dev.chrisbanes.accompanist.glide

import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.material.Text
import androidx.compose.runtime.Providers
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.AmbientView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.common.truth.Truth.assertThat
import dev.chrisbanes.accompanist.glide.test.R
import dev.chrisbanes.accompanist.imageloading.ImageLoadState
import dev.chrisbanes.accompanist.imageloading.test.ImageMockWebServer
import dev.chrisbanes.accompanist.imageloading.test.assertPixels
import dev.chrisbanes.accompanist.imageloading.test.awaitNext
import dev.chrisbanes.accompanist.imageloading.test.receiveBlocking
import dev.chrisbanes.accompanist.imageloading.test.resourceUri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
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
class GlideTest {
    @Suppress("DEPRECATION") // createAndroidComposeRuleLegacy
    @get:Rule
    val composeTestRule = androidx.compose.ui.test.junit4.createAndroidComposeRuleLegacy<ComponentActivity>()

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
        val latch = CountDownLatch(1)

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
                            latch.countDown()
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
                            latch.countDown()
                            // False so that Glide still invokes the Target
                            return false
                        }
                    })
                },
                contentDescription = null,
                modifier = Modifier.preferredSize(128.dp, 128.dp),
                onRequestCompleted = { results += it }
            )
        }

        // Wait for the Glide request listener to release the latch
        latch.await(5, TimeUnit.SECONDS)

        composeTestRule.runOnIdle {
            // And assert that we got a single successful result
            assertThat(results).hasSize(1)
            assertThat(results[0]).isInstanceOf(ImageLoadState.Success::class.java)
        }
    }

    @Test
    fun basicLoad_http() {
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            GlideImage(
                data = server.url("/image").toString(),
                contentDescription = null,
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(GlideTestTags.Image),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun basicLoad_drawable() {
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            GlideImage(
                data = resourceUri(R.drawable.red_rectangle),
                contentDescription = null,
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(GlideTestTags.Image),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

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
        val loadCompleteSignal = Channel<Unit>(Channel.UNLIMITED)
        val data = MutableStateFlow(server.url("/red"))

        composeTestRule.setContent {
            val resId = data.collectAsState()
            GlideImage(
                data = resId.value.toString(),
                contentDescription = null,
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(GlideTestTags.Image),
                onRequestCompleted = { loadCompleteSignal.offer(Unit) }
            )
        }

        // Await the first load
        loadCompleteSignal.awaitNext(5, TimeUnit.SECONDS)

        // Assert that the content is completely Red
        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Red)

        // Now switch the data URI to the blue drawable
        data.value = server.url("/blue")

        // Await the second load
        loadCompleteSignal.awaitNext(5, TimeUnit.SECONDS)

        // Assert that the content is completely Blue
        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Blue)

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
            GlideImage(
                data = server.url("/red").toString(),
                contentDescription = null,
                modifier = Modifier.preferredSize(size.value).testTag(GlideTestTags.Image),
                onRequestCompleted = { loadCompleteSignal.offer(it) }
            )
        }

        // Await the first load
        assertThat(loadCompleteSignal.receiveBlocking())
            .isInstanceOf(ImageLoadState.Success::class.java)

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
            GlideImage(
                data = server.url("/image").toString(),
                contentDescription = null,
                modifier = Modifier.testTag(GlideTestTags.Image),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertWidthIsAtLeast(1.dp)
            .assertHeightIsAtLeast(1.dp)
            .assertIsDisplayed()
    }

    @Test
    fun customRequestManager_param() {
        val latch = CountDownLatch(1)
        val loaded = mutableListOf<Any>()

        composeTestRule.setContent {
            // Create a RequestManager with a listener which updates our loaded list
            val glide = Glide.with(AmbientView.current)
                .addDefaultRequestListener(SimpleRequestListener { model -> loaded += model })

            GlideImage(
                data = server.url("/image").toString(),
                requestManager = glide,
                contentDescription = null,
                modifier = Modifier.preferredSize(128.dp, 128.dp),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        // Assert that the listener was called
        assertThat(loaded).hasSize(1)
    }

    @Test
    fun customRequestManager_ambient() {
        val latch = CountDownLatch(1)
        val loaded = mutableListOf<Any>()

        composeTestRule.setContent {
            // Create a RequestManager with a listener which updates our loaded list
            val glide = Glide.with(AmbientView.current)
                .addDefaultRequestListener(SimpleRequestListener { model -> loaded += model })

            Providers(AmbientRequestManager provides glide) {
                GlideImage(
                    data = server.url("/image").toString(),
                    contentDescription = null,
                    modifier = Modifier.preferredSize(128.dp, 128.dp),
                    onRequestCompleted = { latch.countDown() }
                )
            }
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        // Assert that the listener was called
        assertThat(loaded).hasSize(1)
    }

    @Test
    fun errorStillHasSize() {
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            GlideImage(
                data = server.url("/noimage").toString(),
                contentDescription = null,
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(GlideTestTags.Image),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        // Assert that the layout is in the tree and has the correct size
        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
    }

    @Test
    fun content_error() {
        val latch = CountDownLatch(1)
        val states = ArrayList<ImageLoadState>()

        composeTestRule.setContent {
            GlideImage(
                data = server.url("/noimage").toString(),
                requestBuilder = {
                    // Disable memory cache. If the item is in the cache, the fetch is
                    // synchronous and the dispatcher pause has no effect
                    skipMemoryCache(true)
                },
                modifier = Modifier.preferredSize(128.dp, 128.dp),
                onRequestCompleted = { latch.countDown() }
            ) { state ->
                states.add(state)
            }
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        composeTestRule.runOnIdle {
            assertThat(states).hasSize(2)
            assertThat(states[0]).isEqualTo(ImageLoadState.Loading)
            assertThat(states[1]).isInstanceOf(ImageLoadState.Error::class.java)
        }
    }

    @Test
    fun content_success() {
        val latch = CountDownLatch(1)
        val states = ArrayList<ImageLoadState>()

        composeTestRule.setContent {
            GlideImage(
                data = server.url("/image").toString(),
                requestBuilder = {
                    // Disable memory cache. If the item is in the cache, the fetch is
                    // synchronous and the dispatcher pause has no effect
                    skipMemoryCache(true)
                },
                modifier = Modifier.preferredSize(128.dp, 128.dp),
                onRequestCompleted = { latch.countDown() }
            ) { state ->
                states.add(state)
            }
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        composeTestRule.runOnIdle {
            assertThat(states).hasSize(2)
            assertThat(states[0]).isEqualTo(ImageLoadState.Loading)
            assertThat(states[1]).isInstanceOf(ImageLoadState.Success::class.java)
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun content_custom() {
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            GlideImage(
                data = server.url("/image").toString(),
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(GlideTestTags.Image),
                onRequestCompleted = { latch.countDown() }
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
        latch.await(5, TimeUnit.SECONDS)

        // Assert that the whole layout is drawn cyan
        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Cyan)
    }

    @Test
    fun loading_slot() {
        val loadLatch = CountDownLatch(1)

        val glide = Glide.with(InstrumentationRegistry.getInstrumentation().targetContext)

        composeTestRule.setContent {
            SideEffect {
                // Pause all requests so that the request doesn't complete. This needs to be done
                // inside our content because Glide automatically resumeRequests() in onStart
                glide.pauseAllRequests()
            }

            GlideImage(
                data = server.url("/image").toString(),
                requestManager = glide,
                contentDescription = null,
                modifier = Modifier.preferredSize(128.dp, 128.dp),
                loading = { Text(text = "Loading") },
                onRequestCompleted = { loadLatch.countDown() }
            )
        }

        // Assert that the loading component is displayed
        composeTestRule.onNodeWithText("Loading").assertIsDisplayed()

        // Now resume all requests
        glide.resumeRequests()

        // We now wait for the request to complete
        loadLatch.await(5, TimeUnit.SECONDS)

        // And assert that the loading component no longer exists
        composeTestRule.onNodeWithText("Loading").assertDoesNotExist()
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun error_slot() {
        val latch = CountDownLatch(1)

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
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(GlideTestTags.Image),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

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
                modifier = Modifier.preferredSize(128.dp, 128.dp),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_imagebitmap_throws() {
        composeTestRule.setContent {
            GlideImage(
                data = imageResource(android.R.drawable.ic_delete),
                contentDescription = null,
                modifier = Modifier.preferredSize(128.dp, 128.dp),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_imagevector_throws() {
        composeTestRule.setContent {
            GlideImage(
                data = vectorResource(R.drawable.ic_android_black_24dp),
                contentDescription = null,
                modifier = Modifier.preferredSize(128.dp, 128.dp),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_painter_throws() {
        composeTestRule.setContent {
            GlideImage(
                data = ColorPainter(Color.Magenta),
                contentDescription = null,
                modifier = Modifier.preferredSize(128.dp, 128.dp),
            )
        }
    }
}
