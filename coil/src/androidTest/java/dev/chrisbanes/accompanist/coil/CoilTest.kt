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

package dev.chrisbanes.accompanist.coil

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
import androidx.ui.test.runOnIdle
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.common.truth.Truth.assertThat
import dev.chrisbanes.accompanist.coil.test.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(JUnit4::class)
class CoilTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun onRequestCompleted() {
        val results = ArrayList<RequestResult>()
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            CoilImage(
                request = ImageRequest.Builder(ContextAmbient.current)
                    .data(resourceUri(R.raw.sample))
                    .listener { _, _ -> latch.countDown() }
                    .build(),
                modifier = Modifier.preferredSize(128.dp, 128.dp),
                onRequestCompleted = { results += it }
            )
        }

        // Wait for the Coil request listener to release the latch
        latch.await(5, TimeUnit.SECONDS)

        runOnIdle {
            // And assert that we got a single successful result
            assertThat(results).hasSize(1)
            assertThat(results[0]).isInstanceOf(SuccessResult::class.java)
        }
    }

    @Test
    fun basicLoad_raw() {
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            CoilImage(
                data = resourceUri(R.raw.sample),
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(CoilTestTags.Image),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        onNodeWithTag(CoilTestTags.Image)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToBitmap is SDK 26+
    fun basicLoad_drawable() {
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            CoilImage(
                data = resourceUri(R.drawable.red_rectangle),
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(CoilTestTags.Image),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        onNodeWithTag(CoilTestTags.Image)
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
        val drawableResId = MutableStateFlow(R.drawable.red_rectangle)

        composeTestRule.setContent {
            val resId = drawableResId.collectAsState()
            CoilImage(
                data = resourceUri(resId.value),
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(CoilTestTags.Image),
                onRequestCompleted = { loadCompleteSignal.offer(Unit) }
            )
        }

        // Await the first load
        runBlocking {
            loadCompleteSignal.receive()
        }

        // Assert that the content is completely Red
        onNodeWithTag(CoilTestTags.Image)
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
            .assertIsDisplayed()
            .captureToBitmap()
            .assertPixels { Color.Red }

        // Now switch the data URI to the blue drawable
        drawableResId.value = R.drawable.blue_rectangle

        // Await the second load
        runBlocking { loadCompleteSignal.receive() }

        // Assert that the content is completely Blue
        onNodeWithTag(CoilTestTags.Image)
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
        val loadCompleteSignal = Channel<Unit>(Channel.UNLIMITED)
        val sizeFlow = MutableStateFlow(128.dp)

        composeTestRule.setContent {
            val size = sizeFlow.collectAsState()
            CoilImage(
                data = resourceUri(R.drawable.blue_rectangle),
                modifier = Modifier.preferredSize(size.value).testTag(CoilTestTags.Image),
                onRequestCompleted = { loadCompleteSignal.offer(Unit) }
            )
        }

        // Await the first load
        runBlocking {
            loadCompleteSignal.receive()
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
            CoilImage(
                data = resourceUri(R.raw.sample),
                modifier = Modifier.testTag(CoilTestTags.Image),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        onNodeWithTag(CoilTestTags.Image)
            .assertWidthIsAtLeast(1.dp)
            .assertHeightIsAtLeast(1.dp)
            .assertIsDisplayed()
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToBitmap is SDK 26+
    fun customGetPainter() {
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            CoilImage(
                data = resourceUri(R.raw.sample),
                getSuccessPainter = {
                    // Return a custom success painter which just draws cyan
                    ColorPainter(Color.Cyan)
                },
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(CoilTestTags.Image),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        // Assert that the whole layout is drawn cyan
        onNodeWithTag(CoilTestTags.Image)
            .assertIsDisplayed()
            .captureToBitmap()
            .assertPixels { Color.Cyan }
    }

    @Test
    fun errorStillHasSize() {
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            CoilImage(
                data = "url_which_will_never_work",
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(CoilTestTags.Image),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        // Assert that the layout is in the tree and has the correct size
        onNodeWithTag(CoilTestTags.Image)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(128.dp)
            .assertHeightIsEqualTo(128.dp)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadingSlot() {
        val dispatcher = TestCoroutineDispatcher()
        val loadLatch = CountDownLatch(1)

        dispatcher.runBlockingTest {
            pauseDispatcher()

            composeTestRule.setContent {
                CoilImage(
                    request = ImageRequest.Builder(ContextAmbient.current)
                        .data(resourceUri(R.raw.sample))
                        // Disable memory cache. If the item is in the cache, the fetch is
                        // synchronous and the dispatcher pause has no effect
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .dispatcher(dispatcher)
                        .build(),
                    modifier = Modifier.preferredSize(128.dp, 128.dp),
                    loading = { Text(text = "Loading") },
                    onRequestCompleted = { loadLatch.countDown() }
                )
            }

            // Assert that the loading component is displayed
            onNodeWithText("Loading").assertIsDisplayed()

            // Now resume the dispatcher to start the Coil request
            dispatcher.resumeDispatcher()
        }

        // We now wait for the request to complete
        loadLatch.await(5, TimeUnit.SECONDS)

        // And assert that the loading component no longer exists
        onNodeWithText("Loading").assertDoesNotExist()
    }

    @Test
    @SdkSuppress(minSdkVersion = 26) // captureToBitmap is SDK 26+
    fun customFailurePainter() {
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            CoilImage(
                data = "url_which_will_never_work",
                getFailurePainter = {
                    // Return a custom failure painter which just draws red
                    ColorPainter(Color.Red)
                },
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(CoilTestTags.Image),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        // Assert that the whole layout is drawn red
        onNodeWithTag(CoilTestTags.Image)
            .assertIsDisplayed()
            .captureToBitmap()
            .assertPixels { Color.Red }
    }
}

@Composable
fun resourceUri(id: Int): Uri {
    return "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${ContextAmbient.current.packageName}/$id".toUri()
}
