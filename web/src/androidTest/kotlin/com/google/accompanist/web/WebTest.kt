/*
 * Copyright 2021 The Android Open Source Project
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

package com.google.accompanist.web

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.model.Atoms.getCurrentUrl
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.google.common.truth.Truth.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
// Emulator image doesn't have a WebView until API 26
@SdkSuppress(minSdkVersion = 26)
class WebTest {
    @get:Rule
    val rule = createComposeRule()

    lateinit var idleResource: CountingIdlingResource

    @Before
    fun setup() {
        idleResource = CountingIdlingResource("webview")
        IdlingRegistry.getInstance().register(idleResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(idleResource)
    }

    @Test
    fun testDataLoaded() {
        lateinit var state: WebViewState

        rule.setContent {
            state = rememberWebViewStateWithHTMLData(data = TEST_DATA)
            WebTestContent(
                state,
                idleResource
            )
        }

        onWebView()
            .withElement(findElement(Locator.TAG_NAME, "a"))
            .check(webMatches(getText(), containsString(LINK_TEXT)))
    }

    @Test
    fun testUrlLoaded() {
        lateinit var state: WebViewState

        rule.setContent {
            state = rememberWebViewState(url = LINK_URL)
            WebTestContent(
                state,
                idleResource
            )
        }

        onWebView()
            .withElement(findElement(Locator.ID, "content"))
            .check(webMatches(getText(), containsString("Test content")))
    }

    @Test
    fun testStateUpdated() {
        lateinit var state: WebViewState

        rule.setContent {
            state = rememberWebViewStateWithHTMLData(data = TEST_DATA)
            WebTestContent(
                state,
                idleResource
            )
        }

        // Ensure the data is loaded first
        onWebView()
            .check(webMatches(getCurrentUrl(), containsString("about:blank")))

        // Update the state, the webview should change url
        state.content = WebContent.Url(LINK_URL)
        rule.waitForIdle()

        onWebView()
            .check(webMatches(getCurrentUrl(), containsString(LINK_URL)))
    }

    // SDKs less than 29 do not call onPageStarted when loading about:blank.
    // This breaks the idling resource counter.
    // This test was testing an edge case that no longer exists and could potentially
    // just be removed.
    @SdkSuppress(minSdkVersion = 29)
    @Test
    fun testCanNavigateToBlank() {
        lateinit var state: WebViewState

        rule.setContent {
            state = rememberWebViewState(url = LINK_URL)
            WebTestContent(
                state,
                idleResource
            )
        }

        onWebView()
            .withElement(findElement(Locator.ID, "blankurl"))
            .perform(webClick())

        // Perform the check on the webview first which will wait for the idling resource
        onWebView().check(webMatches(getCurrentUrl(), containsString("about:blank")))
        assertThat(state.content.getCurrentUrl())
            .isEqualTo("about:blank")
    }

    @Test
    fun testLinksCaptured() {
        lateinit var state: WebViewState

        rule.setContent {
            state = rememberWebViewStateWithHTMLData(data = TEST_DATA)
            WebTestContent(
                state,
                idleResource
            )
        }

        onWebView()
            .withElement(findElement(Locator.ID, "link"))
            .perform(webClick())

        // Perform the check on the webview first which will wait for the idling resource
        onWebView().check(webMatches(getCurrentUrl(), containsString(LINK_URL)))
        assertThat(state.content.getCurrentUrl())
            .isEqualTo(LINK_URL)
    }

    private val webNode: SemanticsNodeInteraction
        get() = rule.onNodeWithTag(WebViewTag)
}

private const val LINK_ID = "link"
private const val LINK_TEXT = "Click me"
private const val LINK_URL = "file:///android_asset/test.html"
private const val TEST_DATA =
    "<html><body><a id=$LINK_ID href=\"$LINK_URL\">$LINK_TEXT</a></body></html>"
private const val WebViewTag = "webview_tag"

@Composable
private fun WebTestContent(
    webViewState: WebViewState,
    idlingResource: CountingIdlingResource
) {
    MaterialTheme {
        WebView(
            state = webViewState,
            modifier = Modifier.testTag(WebViewTag),
            onCreated = { it.settings.javaScriptEnabled = true },
            onPageStarted = { _, _ ->
                idlingResource.increment()
            },
            onPageFinished = { _ ->
                idlingResource.decrement()
            }
        )
    }
}
