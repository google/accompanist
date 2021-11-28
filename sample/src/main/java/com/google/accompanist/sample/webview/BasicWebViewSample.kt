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
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.web.WebContent
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState

class BasicWebViewSample : ComponentActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccompanistSampleTheme {
                var textFieldValue by remember { mutableStateOf("") }
                val state = rememberWebViewState(Uri.parse("https://google.com"))

                Column {
                    Row {
                        OutlinedTextField(
                            value = textFieldValue,
                            onValueChange = { textFieldValue = it },
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { state.content = WebContent.Url(Uri.parse(textFieldValue)) },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Text("Go")
                        }
                    }

                    if (state.isLoading) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }

                    WebView(
                        state = state,
                        onContentChanged = { content ->
                            state.content = content
                            textFieldValue = (content as? WebContent.Url)?.uri.toString()
                        },
                        modifier = Modifier.weight(1f),
                        onCreated = { webView ->
                            webView.settings.javaScriptEnabled = true
                        }
                    )
                }
            }
        }
    }
}