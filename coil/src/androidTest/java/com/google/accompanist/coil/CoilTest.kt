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
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalInspectionMode
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
import androidx.compose.ui.unit.dp
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import coil.Coil
import coil.EventListener
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.ImageResult
import com.google.accompanist.coil.test.R
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.imageloading.isFinalState
import com.google.accompanist.imageloading.test.ImageMockWebServer
import com.google.accompanist.imageloading.test.assertPixels
import com.google.accompanist.imageloading.test.resourceUri
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@LargeTest
@RunWith(JUnit4::class)
class CoilTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // Our MockWebServer. We use a response delay to simulate real-world conditions
    private val server = ImageMockWebServer()

    private val idlingResource = CoilIdlingResource()

    @Before
    fun setup() {
        // Start our mock web server
        server.start()

        @OptIn(ExperimentalCoilApi::class)
        val imageLoader = ImageLoader.Builder(composeTestRule.activity.applicationContext)
            .diskCachePolicy(CachePolicy.DISABLED)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .eventListener(idlingResource)
            .build()

        Coil.setImageLoader(imageLoader)

        composeTestRule.registerIdlingResource(idlingResource)
    }

    @After
    fun teardown() {
        composeTestRule.unregisterIdlingResource(idlingResource)

        // Shutdown our mock web server
        server.shutdown()
    }

    @Test
    fun basicLoad_http() {
        composeTestRule.setContent {
            Image(
                painter = rememberCoilPainter(server.url("/image")),
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp, 128.dp)
                    .testTag(CoilTestTags.Image),
            )
        }

        composeTestRule.onNodeWithTag(CoilTestTags.Image)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun basicLoad_drawableId() {
        composeTestRule.setContent {
            Image(
                painter = rememberCoilPainter(R.drawable.red_rectangle),
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp, 128.dp)
                    .testTag(CoilTestTags.Image),
            )
        }

        composeTestRule.onNodeWithTag(CoilTestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Red)
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun basicLoad_drawableUri() {
        composeTestRule.setContent {
            Image(
                painter = rememberCoilPainter(resourceUri(R.drawable.red_rectangle)),
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp, 128.dp)
                    .testTag(CoilTestTags.Image),
            )
        }

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
        var requestCompleted by mutableStateOf(false)

        // Build a custom ImageLoader with an EventListener
        val eventListener = object : EventListener {
            override fun onSuccess(request: ImageRequest, metadata: ImageResult.Metadata) {
                requestCompleted = true
            }
        }
        val imageLoader = ImageLoader.Builder(composeTestRule.activity)
            .eventListener(eventListener)
            .build()

        composeTestRule.setContent {
            Image(
                painter = rememberCoilPainter(
                    request = server.url("/image"),
                    imageLoader = imageLoader,
                ),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }

        composeTestRule.waitForIdle()
        // Wait for the event listener to run
        composeTestRule.waitUntil(10_000) { requestCompleted }
    }

    @OptIn(ExperimentalCoilApi::class)
    @Test
    fun basicLoad_customImageLoader_ambient() {
        var requestCompleted by mutableStateOf(false)

        // Build a custom ImageLoader with a fake EventListener
        val eventListener = object : EventListener {
            override fun onStart(request: ImageRequest) {
                requestCompleted = true
            }
        }
        val imageLoader = ImageLoader.Builder(composeTestRule.activity)
            .eventListener(eventListener)
            .build()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                Image(
                    painter = rememberCoilPainter(server.url("/image")),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp, 128.dp),
                )
            }
        }

        // Wait for the event listener to run
        composeTestRule.waitUntil(10_000) { requestCompleted }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun basicLoad_switchData() {
        var data by mutableStateOf(server.url("/red"))

        composeTestRule.setContent {
            Image(
                painter = rememberCoilPainter(data),
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp, 128.dp)
                    .testTag(CoilTestTags.Image),
            )
        }

        // Assert that the content is completely Red
        composeTestRule.onNodeWithTag(CoilTestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Red)

        // Now switch the data URI to the blue drawable
        data = server.url("/blue")

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
        val scope = TestCoroutineScope()
        scope.launch {
            val loadStates = Channel<ImageLoadState>()
            var size by mutableStateOf(128.dp)

            composeTestRule.setContent {
                val painter = rememberCoilPainter(server.url("/red"))

                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(size)
                        .testTag(CoilTestTags.Image),
                )

                LaunchedEffect(painter) {
                    snapshotFlow { painter.loadState }
                        .filter { it.isFinalState() }
                        .onCompletion { loadStates.cancel() }
                        .collect { loadStates.send(it) }
                }
            }

            // Await the first load
            assertThat(loadStates.receive()).isNotNull()

            // Now change the size
            size = 256.dp
            composeTestRule.awaitIdle()

            // Await any potential subsequent load (which shouldn't come)
            val result = withTimeoutOrNull(3000) { loadStates.receive() }
            assertThat(result).isNull()

            // Close the signal channel
            loadStates.close()
        }
        scope.cleanupTestCoroutines()
    }

    @Test
    fun basicLoad_nosize() {
        composeTestRule.setContent {
            Image(
                painter = rememberCoilPainter(server.url("/image")),
                contentDescription = null,
                modifier = Modifier.testTag(CoilTestTags.Image),
            )
        }

        composeTestRule.onNodeWithTag(CoilTestTags.Image)
            .assertWidthIsAtLeast(1.dp)
            .assertHeightIsAtLeast(1.dp)
            .assertIsDisplayed()
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun basicLoad_error() {
        composeTestRule.setContent {
            Image(
                painter = rememberCoilPainter(
                    request = server.url("/noimage"),
                    requestBuilder = {
                        // Display a red rectangle when errors occur
                        error(R.drawable.red_rectangle)
                    }
                ),
                contentDescription = null,
                modifier = Modifier
                    .testTag(CoilTestTags.Image)
                    .size(128.dp),
            )
        }

        // Assert that the error drawable was drawn
        composeTestRule.onNodeWithTag(CoilTestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Red)
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun previewPlaceholder() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                Image(
                    painter = rememberCoilPainter(
                        request = "blah",
                        previewPlaceholder = R.drawable.red_rectangle_raster,
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(128.dp, 128.dp)
                        .testTag(CoilTestTags.Image),
                )
            }
        }

        composeTestRule.onNodeWithTag(CoilTestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToImage()
            // We're probably scaling a bitmap up in size, so increase the tolerance to 5%
            // to not fail due to small scaling artifacts
            .assertPixels(Color.Red, tolerance = 0.05f)
    }

    @Test
    fun errorStillHasSize() {
        composeTestRule.setContent {
            Image(
                painter = rememberCoilPainter(server.url("/noimage")),
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp, 128.dp)
                    .testTag(CoilTestTags.Image),
            )
        }

        // Assert that the layout is in the tree and has the correct size
        composeTestRule.onNodeWithTag(CoilTestTags.Image)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_drawable_throws() {
        composeTestRule.setContent {
            Image(
                painter = rememberCoilPainter(
                    request = ShapeDrawable(),
                ),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_imagebitmap_throws() {
        composeTestRule.setContent {
            Image(
                painter = rememberCoilPainter(
                    painterResource(android.R.drawable.ic_delete),
                ),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_imagevector_throws() {
        composeTestRule.setContent {
            Image(
                painter = rememberCoilPainter(
                    painterResource(R.drawable.ic_android_black_24dp),
                ),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_painter_throws() {
        composeTestRule.setContent {
            Image(
                painter = rememberCoilPainter(ColorPainter(Color.Magenta)),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }
    }
}
