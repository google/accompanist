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
import androidx.annotation.RawRes
import androidx.compose.Composable
import androidx.core.net.toUri
import androidx.test.filters.LargeTest
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.testTag
import androidx.ui.foundation.Text
import androidx.ui.graphics.Color
import androidx.ui.graphics.painter.ColorPainter
import androidx.ui.layout.preferredSize
import androidx.ui.test.assertIsDisplayed
import androidx.ui.test.assertPixels
import androidx.ui.test.captureToBitmap
import androidx.ui.test.createComposeRule
import androidx.ui.test.findByTag
import androidx.ui.test.findByText
import androidx.ui.test.runOnIdleCompose
import androidx.ui.unit.dp
import coil.request.CachePolicy
import coil.request.GetRequest
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

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
                request = GetRequest.Builder(ContextAmbient.current)
                    .data(rawUri(R.raw.sample))
                    .listener { _, _ -> latch.countDown() }
                    .build(),
                modifier = Modifier.preferredSize(128.dp, 128.dp),
                onRequestCompleted = { results += it }
            )
        }

        // Wait for the Coil request listener to release the latch
        latch.await(5, TimeUnit.SECONDS)

        runOnIdleCompose {
            // And assert that we got a single successful result
            assertThat(results).hasSize(1)
            assertThat(results[0]).isInstanceOf(SuccessResult::class.java)
        }
    }

    @Test
    fun basicLoad() {
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            CoilImage(
                data = rawUri(R.raw.sample),
                modifier = Modifier.preferredSize(128.dp, 128.dp).testTag(CoilTestTags.Image),
                onRequestCompleted = { latch.countDown() }
            )
        }

        // Wait for the onRequestCompleted to release the latch
        latch.await(5, TimeUnit.SECONDS)

        findByTag(CoilTestTags.Image)
            .assertIsDisplayed()
            .assertSize(composeTestRule.density, 128.dp, 128.dp)
    }

    @Test
    fun customGetPainter() {
        val latch = CountDownLatch(1)

        composeTestRule.setContent {
            CoilImage(
                data = rawUri(R.raw.sample),
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
        findByTag(CoilTestTags.Image)
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
        findByTag(CoilTestTags.Image)
            .assertIsDisplayed()
            .assertSize(composeTestRule.density, 128.dp, 128.dp)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadingSlot() {
        val dispatcher = TestCoroutineDispatcher()
        dispatcher.runBlockingTest {
            pauseDispatcher()
            val latch = CountDownLatch(1)

            composeTestRule.setContent {
                CoilImage(
                    request = GetRequest.Builder(ContextAmbient.current)
                        .data(rawUri(R.raw.sample))
                        // Disable memory cache. If the item is in the cache, the fetch is
                        // synchronous and the dispatcher pause has no effect
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .dispatcher(dispatcher)
                        .build(),
                    modifier = Modifier.preferredSize(128.dp, 128.dp),
                    loading = { Text(text = "Loading") },
                    onRequestCompleted = { latch.countDown() }
                )
            }

            // Assert that the loading component is displayed
            findByText("Loading").assertIsDisplayed()

            // Now resume the dispatcher to start the Coil request, and wait for the
            // request to complete
            dispatcher.resumeDispatcher()
            latch.await(5, TimeUnit.SECONDS)

            // And assert that the loading component no longer exists
            findByText("Loading").assertDoesNotExist()
        }
    }

    @Test
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
        findByTag(CoilTestTags.Image)
            .assertIsDisplayed()
            .captureToBitmap()
            .assertPixels { Color.Red }
    }
}

@Composable
fun rawUri(@RawRes id: Int): Uri {
    return "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${ContextAmbient.current.packageName}/$id".toUri()
}
