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

@file:OptIn(ExperimentalComposeUiApi::class)

package com.google.accompanist.sample.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewNavigator
import com.google.accompanist.web.rememberWebViewState

@OptIn(ExperimentalMaterial3Api::class)
class BasicWebViewSample : ComponentActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var url by remember { mutableStateOf("https://google.com") }
                val state = rememberWebViewState(url = url)
                val navigator = rememberWebViewNavigator()
                var textFieldValue by remember(state.content.getCurrentUrl()) {
                    mutableStateOf(state.content.getCurrentUrl() ?: "")
                }

                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
                    rememberTopAppBarState()
                )

                Scaffold(topBar = {
                    MediumTopAppBar(
                        title = { Text(text = "WebView Sample") },
                        navigationIcon = {
                            if (navigator.canGoBack) {
                                IconButton(onClick = { navigator.navigateBack() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        },
                        scrollBehavior = scrollBehavior
                    )
                }) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                    ) {
                        Row {
                            Box(modifier = Modifier.weight(1f)) {
                                if (state.errorsForCurrentRequest.isNotEmpty()) {
                                    Image(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Error",
                                        colorFilter = ColorFilter.tint(Color.Red),
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(8.dp)
                                    )
                                }

                                OutlinedTextField(
                                    value = textFieldValue,
                                    onValueChange = { textFieldValue = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }

                            Button(
                                onClick = {
                                    url = textFieldValue
                                },
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                Text("Go")
                            }
                        }

                        val loadingState = state.loadingState
                        if (loadingState is LoadingState.Loading) {
                            LinearProgressIndicator(
                                progress = loadingState.progress,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // A custom WebViewClient and WebChromeClient can be provided via subclassing
                        val webClient = remember {
                            object : AccompanistWebViewClient() {
                                override fun onPageStarted(
                                    view: WebView?,
                                    url: String?,
                                    favicon: Bitmap?
                                ) {
                                    super.onPageStarted(view, url, favicon)
                                    Log.d("Accompanist WebView", "Page started loading for $url")
                                }
                            }
                        }

                        SwipeRefresh(
                            state = rememberSwipeRefreshState(isRefreshing = state.isLoading),
                            onRefresh = { navigator.reload() }
                        ) {
                            WebView(
                                state = state,
                                modifier = Modifier
                                    .weight(1f),
                                navigator = navigator,
                                onCreated = { webView ->
                                    webView.settings.javaScriptEnabled = true
                                },
                                client = webClient
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun WebViewPreview() {
    AccompanistSampleTheme {
        Column {
            Text("Preview should still load but WebView will be grey box.")
            WebView(
                state = rememberWebViewState(url = "localhost"),
                modifier = Modifier.height(100.dp)
            )
        }
    }
}
