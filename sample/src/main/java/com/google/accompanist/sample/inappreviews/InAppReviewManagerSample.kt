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

package com.google.accompanist.sample.inappreviews

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.inappreviews.rememberInAppReviewManager
import com.google.accompanist.sample.AccompanistSampleTheme

class InAppReviewSampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AccompanistSampleTheme {
                Surface {
                    InAppReviewSample()
                }
            }
        }
    }
}

@Composable
private fun InAppReviewSample() {
    // Get Activity
    val activity = LocalContext.current.findActivity()

    // Remember an InAppReviewManager
    val inAppReviewManager = rememberInAppReviewManager()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = {
                inAppReviewManager.launchReviewFlow(activity = activity, onReviewRequestSuccess = {
                    Toast.makeText(activity, "onReviewRequestSuccess", Toast.LENGTH_LONG).show()
                }, onReviewRequestFail = { exception ->
                    Toast.makeText(
                        activity, "onReviewRequestFail: ${exception?.message}", Toast.LENGTH_LONG
                    ).show()
                })
            }) {
                Text(text = "Launch Review Flow")
            }
        }
    }
}

/**
 * Find the closest Activity in a given Context.
 */
private fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("No Activity found")
}