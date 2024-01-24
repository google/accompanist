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

@file:Suppress("DEPRECATION")

package com.google.accompanist.sample.testharness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.google.accompanist.sample.AccompanistSampleTheme
import com.google.accompanist.sample.R
import com.google.accompanist.testharness.TestHarness
import java.util.Locale

/**
 * A visual sample for the TestHarness Composable. Note that it should not be used in production.
 */
class TestHarnessSample : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestHarnessSampleScreen()
        }
    }
}

@Preview
@Composable
fun TestHarnessSampleScreen() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TestHarnessScreen()
        TestHarness(size = DpSize(100.dp, 100.dp)) {
            TestHarnessScreen("with a set size")
        }
        TestHarness(darkMode = true) {
            TestHarnessScreen("with darkMode enabled")
        }
        TestHarness(fontScale = 2f) {
            TestHarnessScreen("with a big font scale")
        }
        TestHarness(layoutDirection = LayoutDirection.Rtl) {
            TestHarnessScreen("in RTL")
        }
        TestHarness(locales = LocaleListCompat.create(Locale("ar"))) {
            TestHarnessScreen("in Arabic")
        }
    }
}

@Preview
@Composable
fun TestHarnessScreen(text: String = "") {
    AccompanistSampleTheme {
        Surface(
            modifier = Modifier
                .border(1.dp, Color.LightGray)
                .height(100.dp)
                .fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.this_is_content, text),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
