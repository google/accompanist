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

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * A wrapper around the Android View WebView to provide a basic WebView composable.
 *
 * If you require more customisation you are most likely better rolling your own and using this
 * wrapper as an example.
 *
 * @param state The webview state holder where the Uri to load is defined.
 * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
 * the WebView back.
 * @param onCreated Called when the WebView is first created, this can be used to set additional
 * settings on the WebView.
 *
 * @sample com.google.accompanist.sample.webview.BasicWebViewSample
 */
@Composable
fun WebView(
    state: WebViewState,
    onContentChanged: (WebContent) -> Unit,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    onCreated: (WebView) -> Unit = {}
) {
    val context = LocalContext.current
    val view = remember(context) { WebView(context) }

    BackHandler(captureBackPresses && state.canGoBack) {
        view.goBack()
    }

    AndroidView(
        factory = {
            view.apply {
                onCreated(this)

                webViewClient = object : WebViewClient() {
                    override fun onLoadResource(view: WebView?, url: String?) {
                        super.onLoadResource(view, url)
                    }
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        state.isLoading = true
                        onContentChanged(WebContent.Url(Uri.parse(url)))
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        state.isLoading = false
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        // Override all url loads to make the single source of truth
                        // of the URL the state holder Url
                        request?.let {
                            val content = WebContent.Url(it.url)
                            onContentChanged(content)
                        }
                        return true
                    }
                }
            }
        },
        modifier = modifier
    ) { webView ->
        when (val l = state.content) {
            is WebContent.Url -> {
                val url = l.uri.toString()

                if (url.isNotEmpty() && url != webView.url) {
                    webView.loadUrl(url)
                }
            }
            is WebContent.Data -> {
                webView.loadDataWithBaseURL(l.baseUrl, l.data, null, null, null)
            }
        }

        state.canGoBack = webView.canGoBack()
    }
}

sealed class WebContent {
    data class Url(val uri: Uri) : WebContent()
    data class Data(val data: String, val baseUrl: String? = null) : WebContent()
}

/**
 * A state holder to hold the state for the WebView. In most cases this will be remembered
 * using the rememberWebViewState(uri) function.
 */
class WebViewState(webContent: WebContent) {
    /**
     *  The content being loaded by the WebView
     */
    var content by mutableStateOf<WebContent>(webContent)

    /**
     * Whether the WebView is currently loading data in it's main frame
     */
    var isLoading: Boolean by mutableStateOf(false)
        internal set

    /**
     * Whether the WebView can navigate back
     */
    var canGoBack: Boolean by mutableStateOf(false)
        internal set
}

/**
 * Creates a WebView state that is remembered across Compositions.
 *
 * @param uri The uri to load in the WebView
 */
@Composable
fun rememberWebViewState(uri: Uri) =
    remember { WebViewState(WebContent.Url(uri)) }

/**
 * Creates a WebView state that is remembered across Compositions.
 *
 * @param data The uri to load in the WebView
 */
@Composable
fun rememberWebViewState(data: String) =
    remember { WebViewState(WebContent.Data(data)) }
