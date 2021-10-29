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
import com.google.common.truth.Truth.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun testLinksCaptured() {
        lateinit var state: WebViewState

        rule.setContent {
            state = rememberWebViewState(data = TEST_DATA)
            WebTestContent(
                state
            ) { state.content = it }
        }

        onWebView()
            .withElement(findElement(Locator.LINK_TEXT, LINK_TEXT))
            .perform(webClick())

        assertThat((state.content as? WebContent.Url)?.uri)
            .isEqualTo(Uri.parse(LINK_URL))
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

    private val webNode: SemanticsNodeInteraction
        get() = rule.onNodeWithTag(WebViewTag)
}

private const val LINK_TEXT = "Click me"
private const val LINK_URL = "https://test.com/"
private const val TEST_DATA = "<html><body><a href=\"$LINK_URL\">$LINK_TEXT</a></body></html>"
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
