/*
 * Copyright 2020 The Android Open Source Project
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

package dev.chrisbanes.accompanist.appcompattheme

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

/**
 * An [AppCompatActivity] which forces the night mode to 'light theme'.
 */
class LightMdcActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
        super.attachBaseContext(newBase)
    }
}
