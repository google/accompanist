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

import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.IdlingResource
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.model.Atoms.getCurrentUrl
import androidx.test.espresso.web.model.Atoms.getTitle
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.FlakyTest
import androidx.test.filters.SdkSuppress
import com.google.common.truth.Truth.assertThat
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.toCollection
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
// Emulator image doesn't have a WebView until API 26
// Google API emulator image seems to be really flaky before 28 so currently we will set these tests
// to min 29 and max 30. 31/32 image is also really flaky
@SdkSuppress(minSdkVersion = 28, maxSdkVersion = 30)
class WebTest {
    @get:Rule
    val rule = createComposeRule()

    private lateinit var idleResource: WebViewIdlingResource

    @Before
    fun setup() {
        idleResource = WebViewIdlingResource()
        rule.registerIdlingResource(idleResource)
    }

    @After
    fun tearDown() {
        rule.unregisterIdlingResource(idleResource)
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
    fun testDataLoadedWithBaseUrl() {
        lateinit var state: WebViewState

        rule.setContent {
            state = rememberWebViewStateWithHTMLData(
                data = TEST_DATA,
                baseUrl = "file:///android_asset/"
            )
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
    fun testCanNavigateFromDataToUrl() {
        lateinit var state: WebViewState

        rule.setContent {
            state = rememberWebViewStateWithHTMLData(
                data = TEST_DATA,
                baseUrl = "file:///android_asset/"
            )
            WebTestContent(
                state,
                idleResource
            )
        }

        onWebView()
            .withElement(findElement(Locator.ID, "link"))
            .perform(webClick())

        // Wait for the webview to load and then perform the check
        rule.waitForIdle()
        onWebView().check(webMatches(getCurrentUrl(), containsString(LINK_URL)))
        assertThat(state.content.getCurrentUrl())
            .isEqualTo(LINK_URL)
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

        onWebView()
            .check(webMatches(getCurrentUrl(), containsString(LINK_URL)))
    }

    @Test
    fun testUrlUpdatedWithRemember() {
        val url = mutableStateOf("about:blank")
        rule.setContent {
            @Composable
            fun MyWebView(url: String) {
                val webViewState = rememberWebViewState(url = url)
                WebTestContent(webViewState = webViewState, idlingResource = idleResource)
            }

            MyWebView(url = url.value)
        }

        // Ensure the data is loaded first
        onWebView()
            .check(webMatches(getCurrentUrl(), containsString("about:blank")))

        url.value = LINK_URL

        rule.waitForIdle()

        onWebView()
            .check(webMatches(getCurrentUrl(), containsString(LINK_URL)))
    }

    @Test
    fun testDataUpdatedWithRemember() {
        val data = mutableStateOf(TEST_DATA)
        rule.setContent {
            @Composable
            fun MyWebView(data: String) {
                val webViewState = rememberWebViewStateWithHTMLData(data = data)
                WebTestContent(webViewState = webViewState, idlingResource = idleResource)
            }

            MyWebView(data = data.value)
        }

        // Ensure the data is loaded first
        onWebView()
            .withElement(findElement(Locator.TAG_NAME, "a"))
            .check(webMatches(getText(), containsString(LINK_TEXT)))

        data.value = TEST_TITLE_DATA

        onWebView()
            .check(webMatches(getTitle(), containsString(TITLE_TEXT)))
    }

    @Test
    fun testCustomClientIsAssigned() {
        lateinit var state: WebViewState

        var pageStartCalled = false

        rule.setContent {
            state = rememberWebViewStateWithHTMLData(data = TEST_DATA)
            WebTestContent(
                state,
                idleResource,
                client = object : AccompanistWebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        pageStartCalled = true
                    }
                }
            )
        }

        // Ensure the data is loaded first
        onWebView()
            .check(webMatches(getCurrentUrl(), containsString("about:blank")))

        // Wait for the webview to load and then perform the check
        rule.waitForIdle()

        assertThat(pageStartCalled).isTrue()
    }

    @Test
    fun testChromeClientIsAssigned() {
        lateinit var state: WebViewState

        var titleReceived: String? = null

        rule.setContent {
            state = rememberWebViewStateWithHTMLData(data = TEST_TITLE_DATA)
            WebTestContent(
                state,
                idleResource,
                chromeClient = object : AccompanistWebChromeClient() {
                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        titleReceived = title
                    }
                }
            )
        }

        // Wait for new title
        onWebView()
            .check(webMatches(getTitle(), equalTo(TITLE_TEXT)))

        // Wait for the webview to load and then perform the check
        rule.waitForIdle()

        assertThat(titleReceived).isEqualTo(TITLE_TEXT)
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

        // Wait for the webview to load and then perform the check
        rule.waitForIdle()

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

        // Wait for the webview to load and then perform the check
        rule.waitForIdle()
        onWebView().check(webMatches(getCurrentUrl(), containsString(LINK_URL)))
        assertThat(state.content.getCurrentUrl())
            .isEqualTo(LINK_URL)
    }

    @Test
    fun testImageResourceLoadError() {
        lateinit var state: WebViewState

        rule.setContent {
            state = rememberWebViewStateWithHTMLData(data = TEST_BAD_DATA)
            WebTestContent(
                state,
                idleResource
            )
        }

        // Wait for content to be loaded
        onWebView()
            .withElement(findElement(Locator.TAG_NAME, "p"))
            .check(webMatches(getText(), containsString(LINK_TEXT)))

        // Check an error was captured
        assertThat(state.errorsForCurrentRequest)
            .isNotEmpty()

        state.content = WebContent.Data(TEST_DATA)

        // Check the webview can recover from an error and successfully load another request
        onWebView()
            .check(webMatches(getCurrentUrl(), containsString("about:blank")))

        // Check that the error is cleared on a new request
        assertThat(state.errorsForCurrentRequest)
            .isEmpty()
    }

    @Test
    fun testPageTitle() {
        lateinit var state: WebViewState

        rule.setContent {
            state = rememberWebViewStateWithHTMLData(data = TEST_TITLE_DATA)
            WebTestContent(webViewState = state, idlingResource = idleResource)
        }

        // Wait for new title
        onWebView()
            .check(webMatches(getTitle(), equalTo(TITLE_TEXT)))

        // Check that the web view state has been updated with the HTML title
        assertThat(state.pageTitle)
            .isEqualTo(TITLE_TEXT)

        // Check that state is reset on the loading of a new page (without a title or favicon set)
        state.content = WebContent.Data(TEST_DATA)

        // Wait for new title
        onWebView()
            .check(webMatches(getTitle(), equalTo("")))

        assertThat(state.pageTitle)
            .isEqualTo("about:blank") // No title results in about:blank being received
    }

    @FlakyTest(detail = "https://github.com/google/accompanist/issues/1085")
    @Test
    fun testLoadingState() {
        lateinit var state: WebViewState

        val mockServer = MockWebServer()
        // Simulate time spent loading
        mockServer.enqueue(MockResponse().setBody("Test").setBodyDelay(1, TimeUnit.SECONDS))
        mockServer.start()
        val baseUrl = mockServer.url("/")

        val collectedLoadingStates = mutableListOf<LoadingState>()

        rule.setContent {
            state = rememberWebViewState(url = baseUrl.toString())

            LaunchedEffect(state) {
                snapshotFlow { state.loadingState }
                    .toCollection(collectedLoadingStates)
            }

            WebTestContent(webViewState = state, idlingResource = idleResource)
        }

        rule.waitUntil(3_000) { collectedLoadingStates.any { it is LoadingState.Loading } }
        rule.waitForIdle()

        assertThat(collectedLoadingStates.first()).isInstanceOf(LoadingState.Initializing::class.java)
        assertThat(collectedLoadingStates.last()).isInstanceOf(LoadingState.Finished::class.java)
        assertThat(collectedLoadingStates.filterIsInstance<LoadingState.Loading>()).isNotEmpty()

        mockServer.shutdown()
    }

    @Test
    fun testNavigatorBack() {
        lateinit var state: WebViewState
        lateinit var navigator: WebViewNavigator

        rule.setContent {
            state = rememberWebViewStateWithHTMLData(data = TEST_DATA)
            navigator = rememberWebViewNavigator()

            WebTestContent(
                webViewState = state,
                idlingResource = idleResource,
                navigator = navigator
            )
        }

        rule.waitForIdle()

        onWebView()
            .withElement(findElement(Locator.ID, "link"))
            .perform(webClick())

        rule.waitUntil { navigator.canGoBack }
        assertThat(state.content.getCurrentUrl()).isEqualTo(LINK_URL)

        navigator.navigateBack()

        // Check that we're back on the original page with the link
        onWebView()
            .withElement(findElement(Locator.ID, "link"))
            .check(webMatches(getText(), equalTo(LINK_TEXT)))
    }

    @FlakyTest
    @Test
    fun testNavigatorForward() {
        lateinit var state: WebViewState
        lateinit var navigator: WebViewNavigator

        rule.setContent {
            state = rememberWebViewStateWithHTMLData(data = TEST_DATA)
            navigator = rememberWebViewNavigator()

            WebTestContent(
                webViewState = state,
                idlingResource = idleResource,
                navigator = navigator
            )
        }

        rule.waitForIdle()

        onWebView()
            .withElement(findElement(Locator.ID, "link"))
            .perform(webClick())

        rule.waitUntil { navigator.canGoBack }
        navigator.navigateBack()

        // Check that we're back on the original page with the link
        onWebView()
            .withElement(findElement(Locator.ID, "link"))
            .check(webMatches(getText(), equalTo(LINK_TEXT)))

        navigator.navigateForward()
        rule.waitUntil { navigator.canGoBack }
        rule.waitForIdle()

        assertThat(state.content.getCurrentUrl()).isEqualTo(LINK_URL)
    }

    @Test
    fun testNavigatorCanGoBack() {
        lateinit var state: WebViewState
        lateinit var navigator: WebViewNavigator

        rule.setContent {
            state = rememberWebViewStateWithHTMLData(data = TEST_DATA)
            navigator = rememberWebViewNavigator()

            WebTestContent(
                webViewState = state,
                idlingResource = idleResource,
                navigator = navigator
            )
        }

        rule.waitForIdle()
        assertThat(navigator.canGoBack).isFalse()

        onWebView()
            .withElement(findElement(Locator.ID, "link"))
            .perform(webClick())

        rule.waitUntil { navigator.canGoBack }
        assertThat(navigator.canGoBack).isTrue()
    }

    @Test
    fun testNavigatorCanGoForward() {
        lateinit var state: WebViewState
        lateinit var navigator: WebViewNavigator

        rule.setContent {
            state = rememberWebViewStateWithHTMLData(data = TEST_DATA)
            navigator = rememberWebViewNavigator()

            WebTestContent(
                webViewState = state,
                idlingResource = idleResource,
                navigator = navigator
            )
        }

        rule.waitForIdle()

        onWebView()
            .withElement(findElement(Locator.ID, "link"))
            .perform(webClick())

        rule.waitUntil { navigator.canGoBack }
        navigator.navigateBack()

        rule.waitUntil { navigator.canGoForward }
        assertThat(navigator.canGoForward).isTrue()
    }

    @FlakyTest
    @Ignore("Slow and flaky: https://github.com/google/accompanist/issues/1085")
    @Test
    fun testAdditionalHttpHeaders() {
        val mockServer = MockWebServer()
        mockServer.start()
        val baseUrl = mockServer.url("/")

        rule.setContent {
            val state = rememberWebViewState(
                url = baseUrl.toString(),
                additionalHttpHeaders = mapOf(
                    "first-additional-header" to "first",
                    "second-additional-header" to "second",
                )
            )

            WebTestContent(
                webViewState = state,
                idlingResource = idleResource,
            )
        }

        rule.waitForIdle()

        val request = mockServer.takeRequest()

        assertThat(request.getHeader("first-additional-header")).isEqualTo("first")
        assertThat(request.getHeader("second-additional-header")).isEqualTo("second")

        mockServer.shutdown()
    }

    @Test
    fun testOnDisposeCalled() {
        lateinit var state: WebViewState
        lateinit var toggle: MutableState<Boolean>
        var isOnDisposeCalled = false

        rule.setContent {
            toggle = remember { mutableStateOf(true) }
            if (toggle.value) {
                state = rememberWebViewStateWithHTMLData(data = TEST_DATA)
                WebTestContent(
                    state,
                    idleResource,
                    onDispose = { isOnDisposeCalled = true },
                )
            }
        }

        toggle.value = false
        webNode.assertDoesNotExist()
        assertThat(isOnDisposeCalled).isTrue()
    }

    @Test
    fun testJSReloadTriggersRefresh() {
        lateinit var state: WebViewState
        var pageStartedCalled = 0
        val client = object : AccompanistWebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                pageStartedCalled++
            }
        }

        rule.setContent {
            state = rememberWebViewStateWithHTMLData(
                data =
                """
                <html><body>
                   <input id="button" type="button" value="Reload" 
                   onclick="window.location.reload()" />
                </body></html>
                """.trimIndent()
            )

            WebTestContent(
                webViewState = state,
                idlingResource = idleResource,
                client = client
            )
        }

        rule.waitForIdle()

        onWebView()
            .withElement(findElement(Locator.ID, "button"))
            .perform(webClick())

        // Check the url remained about:blank
        onWebView()
            .check(webMatches(getCurrentUrl(), equalTo("about:blank")))

        assertEquals("Page should be loaded twice", 2, pageStartedCalled)
    }

    private val webNode: SemanticsNodeInteraction
        get() = rule.onNodeWithTag(WebViewTag)

    @Test
    fun testWebViewFactoryUsedIfSupplied() {
        lateinit var constructedWebView: WebView

        rule.setContent {
            WebTestContent(
                rememberWebViewState(url = LINK_URL),
                idleResource,
                onCreated = { constructedWebView = it },
                factory = { context -> CustomWebView(context) }
            )
        }

        assertThat(constructedWebView).isInstanceOf(CustomWebView::class.java)
    }

    @Test
    fun testWebViewCanWrapHeight() {
        rule.setContent {
            Column(Modifier.fillMaxSize()) {
                val webViewState = rememberWebViewStateWithHTMLData(data = TEST_DATA)
                WebTestContent(
                    webViewState = webViewState,
                    idlingResource = idleResource,
                    modifier = Modifier.wrapContentHeight()
                )
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Red)
                        .testTag("box")
                )
            }
        }

        rule.waitForIdle()

        // If the WebView is wrapping it's content successfully, the box will have some height.
        rule.onNodeWithTag("box").assertHeightIsAtLeast(1.dp)
    }
}

private const val LINK_ID = "link"
private const val LINK_TEXT = "Click me"
private const val LINK_URL = "file:///android_asset/test.html"
private const val TITLE_TEXT = "A Test Title"
private const val TEST_DATA =
    "<html><body><a id=$LINK_ID href=\"$LINK_URL\">$LINK_TEXT</a></body></html>"
private const val TEST_BAD_DATA =
    "<html><body><img src=\"https://alwaysfail.zxyz.zxyz\" /><p>$LINK_TEXT</p></body></html>"
private const val TEST_TITLE_DATA =
    "<html><head><title>$TITLE_TEXT</title></head><body></body></html>"
private const val WebViewTag = "webview_tag"

@Composable
private fun WebTestContent(
    webViewState: WebViewState,
    idlingResource: WebViewIdlingResource,
    modifier: Modifier = Modifier,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    onCreated: (WebView) -> Unit = { it.settings.javaScriptEnabled = true },
    onDispose: (WebView) -> Unit = {},
    client: AccompanistWebViewClient = remember { AccompanistWebViewClient() },
    chromeClient: AccompanistWebChromeClient = remember { AccompanistWebChromeClient() },
    factory: ((Context) -> WebView)? = null
) {
    idlingResource.webviewLoading = webViewState.loadingState !is LoadingState.Finished

    MaterialTheme {
        WebView(
            state = webViewState,
            modifier = modifier.testTag(WebViewTag),
            navigator = navigator,
            onCreated = onCreated,
            onDispose = onDispose,
            client = client,
            chromeClient = chromeClient,
            factory = factory
        )
    }
}

private class WebViewIdlingResource : IdlingResource {
    var webviewLoading: Boolean = false

    override val isIdleNow: Boolean
        get() = !webviewLoading
}

private class CustomWebView(context: Context) : WebView(context)
