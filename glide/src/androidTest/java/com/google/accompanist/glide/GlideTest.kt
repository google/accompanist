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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import com.bumptech.glide.Glide
import com.google.accompanist.glide.test.R
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.imageloading.isFinalState
import com.google.accompanist.imageloading.test.ImageMockWebServer
import com.google.accompanist.imageloading.test.LaunchedOnRequestComplete
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
class GlideTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

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
            val painter = rememberGlidePainter(server.url("/image").toString())
            LaunchedOnRequestComplete(painter) { requestCompleted = true }

            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp, 128.dp)
                    .testTag(GlideTestTags.Image),
            )
        }

        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun basicLoad_drawableId() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            val painter = rememberGlidePainter(R.drawable.red_rectangle)
            LaunchedOnRequestComplete(painter) { requestCompleted = true }

            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp, 128.dp)
                    .testTag(GlideTestTags.Image),
            )
        }

        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Red)
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun basicLoad_drawableUri() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            val painter = rememberGlidePainter(resourceUri(R.drawable.red_rectangle))
            LaunchedOnRequestComplete(painter) { requestCompleted = true }

            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp, 128.dp)
                    .testTag(GlideTestTags.Image),
            )
        }

        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Red)
    }

    @Test
    fun customRequestManager_param() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            // Create a RequestManager with a listener which updates requestCompleted
            val glide = Glide.with(LocalView.current)
                .addDefaultRequestListener(SimpleRequestListener { requestCompleted = true })

            Image(
                painter = rememberGlidePainter(
                    server.url("/image").toString(),
                    requestManager = glide,
                ),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }
    }

    @Test
    fun customRequestManager_ambient() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            // Create a RequestManager with a listener which updates requestCompleted
            val glide = Glide.with(LocalView.current)
                .addDefaultRequestListener(SimpleRequestListener { requestCompleted = true })

            CompositionLocalProvider(LocalRequestManager provides glide) {
                Image(
                    painter = rememberGlidePainter(server.url("/image").toString()),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp, 128.dp),
                )
            }
        }

        // Wait for the onRequestCompleted to release the latch
        composeTestRule.waitUntil(10_000) { requestCompleted }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun basicLoad_switchData() {
        var data by mutableStateOf(server.url("/red"))
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            val painter = rememberGlidePainter(data.toString())
            LaunchedOnRequestComplete(painter) { requestCompleted = true }

            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp, 128.dp)
                    .testTag(GlideTestTags.Image),
            )
        }

        // Wait until the first image loads
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // Assert that the content is completely Red
        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Red)

        // Now switch the data URI to the blue drawable
        requestCompleted = false
        data = server.url("/blue")

        // Wait until the second image loads
        composeTestRule.waitUntil(10_000) { requestCompleted }

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
    fun basicLoad_changeSize() {
        val scope = TestCoroutineScope()
        scope.launch {
            val loadStates = Channel<ImageLoadState>()
            var size by mutableStateOf(128.dp)

            composeTestRule.setContent {
                val painter = rememberGlidePainter(server.url("/red").toString())

                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(size)
                        .testTag(GlideTestTags.Image),
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
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            val painter = rememberGlidePainter(server.url("/image").toString())
            LaunchedOnRequestComplete(painter) { requestCompleted = true }

            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.testTag(GlideTestTags.Image),
            )
        }

        // Wait until the first image loads
        composeTestRule.waitUntil(10_000) { requestCompleted }

        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertWidthIsAtLeast(1.dp)
            .assertHeightIsAtLeast(1.dp)
            .assertIsDisplayed()
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToImage is SDK 26+
    fun basicLoad_error() {
        composeTestRule.setContent {
            Image(
                painter = rememberGlidePainter(
                    data = server.url("/noimage"),
                    requestBuilder = {
                        // Display a red rectangle when errors occur
                        error(R.drawable.red_rectangle)
                    }
                ),
                contentDescription = null,
                modifier = Modifier
                    .testTag(GlideTestTags.Image)
                    .size(128.dp),
            )
        }

        // Assert that the error drawable was drawn
        composeTestRule.onNodeWithTag(GlideTestTags.Image)
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
                    painter = rememberGlidePainter(
                        data = "blah",
                        previewPlaceholder = R.drawable.red_rectangle_raster,
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(128.dp, 128.dp)
                        .testTag(GlideTestTags.Image),
                )
            }
        }

        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToImage()
            .assertPixels(Color.Red)
    }

    @Test
    fun errorStillHasSize() {
        var requestCompleted by mutableStateOf(false)

        composeTestRule.setContent {
            val painter = rememberGlidePainter(server.url("/noimage").toString())
            LaunchedOnRequestComplete(painter) { requestCompleted = true }

            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp, 128.dp)
                    .testTag(GlideTestTags.Image),
            )
        }

        // Wait until the first image loads
        composeTestRule.waitUntil(10_000) { requestCompleted }

        // Assert that the layout is in the tree and has the correct size
        composeTestRule.onNodeWithTag(GlideTestTags.Image)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
    }

    @Test(expected = IllegalArgumentException::class)
    fun data_drawable_throws() {
        composeTestRule.setContent {
            Image(
                painter = rememberGlidePainter(
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
            Image(
                painter = rememberGlidePainter(
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
                painter = rememberGlidePainter(
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
                painter = rememberGlidePainter(ColorPainter(Color.Magenta)),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }
    }

    @Test
    fun error_stoppedThenResumed() {
        composeTestRule.setContent {
            Image(
                painter = rememberGlidePainter(data = ""),
                contentDescription = null,
                modifier = Modifier.size(128.dp, 128.dp),
            )
        }

        composeTestRule.waitForIdle()

        // Now stop the activity, then resume it
        composeTestRule.activityRule.scenario
            .moveToState(Lifecycle.State.CREATED)
            .moveToState(Lifecycle.State.RESUMED)

        // And wait for idle. We shouldn't crash.
        composeTestRule.waitForIdle()
    }
}
