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
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewNavigator
import com.google.accompanist.web.rememberWebViewState

class BasicWebViewSample : ComponentActivity() {
    val initialUrl = "https://google.com"
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccompanistSampleTheme {
                val state = rememberWebViewState(url = initialUrl)
                val navigator = rememberWebViewNavigator()
                var textFieldValue by remember(state.lastLoadedUrl) {
                    mutableStateOf(state.lastLoadedUrl)
                }

                Column {
                    TopAppBar(
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
                        }
                    )

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
                                value = textFieldValue ?: "",
                                onValueChange = { textFieldValue = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Button(
                            onClick = {
                                textFieldValue?.let {
                                    navigator.loadUrl(it)
                                }
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
