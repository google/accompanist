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

import android.net.Uri
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.google.common.truth.Truth.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
// Emulator image doesn't have a WebView until API 26
@SdkSuppress(minSdkVersion = 26)
class WebTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun testDataLoaded() {
        lateinit var state: WebViewState

        rule.setContent {
            state = rememberWebViewState(data = TEST_DATA)
            WebTestContent(
                state
            ) { state.content = it }
        }

        onWebView()
            .withElement(findElement(Locator.TAG_NAME, "a"))
            .check(webMatches(getText(), containsString(LINK_TEXT)))
    }

    @Test
    fun testUrlLoaded() {
        lateinit var state: WebViewState

        rule.setContent {
            state = rememberWebViewState(uri = Uri.parse(LINK_URL))
            WebTestContent(
                state
            ) { state.content = it }
        }

        rule.waitForIdle()

        onWebView()
            .withElement(findElement(Locator.ID, "content"))
            .check(webMatches(getText(), containsString("Test content")))
    }

    @Test
    fun testStateUpdated() {
        lateinit var state: WebViewState

        rule.setContent {
            state = rememberWebViewState(data = TEST_DATA)
            WebTestContent(
                state
            ) { state.content = it }
        }

        val newId = "id"
        val newContent = "Updated"
        state.content = WebContent.Data("<html><body><p id=$newId>$newContent</p></body></html>")

        rule.waitForIdle()

        onWebView()
            .withElement(findElement(Locator.ID, newId))
            .check(webMatches(getText(), containsString(newContent)))
    }

    @Test
    fun testCanNavigateToBlank() {
        lateinit var state: WebViewState

        rule.setContent {
            state = rememberWebViewState(uri = Uri.parse(LINK_URL))
            WebTestContent(
                state
            ) {
                state.content = it
            }
        }

        rule.waitForIdle()

        onWebView()
            .withElement(findElement(Locator.ID, "blankurl"))
            .perform(webClick())

        rule.waitForIdle()

        assertThat((state.content as? WebContent.Url)?.uri)
            .isEqualTo(Uri.parse("about:blank"))
    }

    private val webNode: SemanticsNodeInteraction
        get() = rule.onNodeWithTag(WebViewTag)
}

private const val LINK_ID = "link"
private const val LINK_TEXT = "Click me"
private const val LINK_URL = "file:///android_asset/test.html"
private const val TEST_DATA = "<html><body><a id=$LINK_ID href=\"$LINK_URL\">$LINK_TEXT</a></body></html>"
private const val WebViewTag = "webview_tag"

@Composable
private fun WebTestContent(
    webViewState: WebViewState,
    onContentChanged: (WebContent) -> Unit
) {
    MaterialTheme {
        WebView(
            state = webViewState,
            onContentChanged = onContentChanged,
            modifier = Modifier.testTag(WebViewTag),
            onCreated = { it.settings.javaScriptEnabled = true }
        )
    }
}
