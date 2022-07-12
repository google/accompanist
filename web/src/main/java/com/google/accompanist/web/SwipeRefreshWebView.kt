/*
 * Copyright 2022 The Android Open Source Project
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
import android.util.AttributeSet
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout



class SwipeRefreshWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs) {

    val webView = WebView(context)

    fun addWebView() {
        webView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        addView(webView)
    }

}

/**
 * A wrapper around the Android View WebView in SwipeRefreshLayout to provide a basic WebView composable.
 *
 * If you require more customisation you are most likely better rolling your own and using this
 * wrapper as an example.
 *
 * @param state The webview state holder where the Uri to load is defined.
 * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
 * the WebView back.
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param onCreated Called when the WebView is first created, this can be used to set additional
 * settings on the WebView. WebChromeClient and WebViewClient should not be set here as they will be
 * subsequently overwritten after this lambda is called.
 * @param client Provides access to WebViewClient via subclassing
 * @param chromeClient Provides access to WebChromeClient via subclassing
 * @param swipeRefreshState the state object to be used to control or observe the [SwipeRefresh] state.
 * @param swipeEnabled Whether the the layout should react to swipe gestures or not.
 * @param onRefresh Lambda which is invoked when a swipe to refresh gesture is completed.
 * @sample com.google.accompanist.sample.webview.SwipeRefreshWebViewSample
 */
@Composable
fun SwipeRefreshWebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: AccompanistWebViewClient = remember { AccompanistWebViewClient() },
    chromeClient: AccompanistWebChromeClient = remember { AccompanistWebChromeClient() },
    swipeRefreshState: SwipeRefreshState,
    swipeEnabled: Boolean = true,
    onRefresh: () -> Unit = {},
) {

    var swipeRefreshWebView by remember {
        mutableStateOf<SwipeRefreshWebView?>(null)
    }

    BackHandler(captureBackPresses && navigator.canGoBack) {
        swipeRefreshWebView?.webView?.goBack()
    }

    LaunchedEffect(swipeRefreshWebView?.webView, navigator) {
        with(navigator) { swipeRefreshWebView?.webView?.handleNavigationEvents() }
    }

    val currentOnDispose by rememberUpdatedState(onDispose)

    swipeRefreshWebView?.webView?.let { it ->
        DisposableEffect(it) {
            onDispose { currentOnDispose(it) }
        }
    }

    swipeRefreshWebView?.let {
        it.isEnabled = swipeEnabled
    }

    val updatedOnRefresh = rememberUpdatedState(onRefresh)

    // Set the state of the client and chrome client
    // This is done internally to ensure they always are the same instance as the
    // parent Web composable
    client.state = state
    client.navigator = navigator
    chromeClient.state = state

    val runningInPreview = LocalInspectionMode.current

    AndroidView(
        factory = { context ->
            SwipeRefreshWebView(context).apply {
                onCreated(this.webView)

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                this.webView.webChromeClient = chromeClient
                this.webView.webViewClient = client
            }.also {
                it.setOnRefreshListener {
                    updatedOnRefresh.value.invoke()
                }
                it.addWebView()
                swipeRefreshWebView = it
            }
        },
        modifier = modifier
    ) { view ->
        // AndroidViews are not supported by preview, bail early
        if (runningInPreview) return@AndroidView

        view.isRefreshing = swipeRefreshState.isRefreshing

        when (val content = state.content) {
            is WebContent.Url -> {
                val url = content.url

                if (url.isNotEmpty() && url != view.webView.url) {
                    view.webView.loadUrl(url, content.additionalHttpHeaders.toMutableMap())
                }
            }
            is WebContent.Data -> {
                view.webView.loadDataWithBaseURL(content.baseUrl, content.data, null, "utf-8", null)
            }
        }

        navigator.canGoBack = view.webView.canGoBack()
        navigator.canGoForward = view.webView.canGoForward()
    }
}


/**
 * Creates a [SwipeRefreshState] that is remembered across compositions.
 *
 * Changes to [isRefreshing] will result in the [SwipeRefreshState] being updated.
 *
 * @param isRefreshing the value for [SwipeRefreshState.isRefreshing]
 *
 * @see com.google.accompanist.swiperefresh.rememberSwipeRefreshState
 */
@Composable
fun rememberSwipeRefreshState(
    isRefreshing: Boolean
): SwipeRefreshState {
    return remember {
        SwipeRefreshState(
            isRefreshing = isRefreshing
        )
    }.apply {
        this.isRefreshing = isRefreshing
    }
}

/**
 * A state object that can be hoisted to control and observe changes for [SwipeRefresh].
 *
 * In most cases, this will be created via [rememberSwipeRefreshState].
 *
 * @param isRefreshing the initial value for [SwipeRefreshState.isRefreshing]
 *
 * @see com.google.accompanist.swiperefresh.SwipeRefreshState
 */
@Stable
class SwipeRefreshState(
    isRefreshing: Boolean
) {
    var isRefreshing: Boolean by mutableStateOf(isRefreshing)
}