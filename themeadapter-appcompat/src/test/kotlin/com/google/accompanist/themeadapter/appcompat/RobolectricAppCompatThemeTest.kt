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

package com.google.accompanist.themeadapter.appcompat

import androidx.appcompat.app.AppCompatActivity
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

/**
 * Version of [BaseAppCompatThemeTest] which is designed to be run using Robolectric.
 *
 * All of the tests are provided by [BaseAppCompatThemeTest].
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class RobolectricAppCompatThemeTest<T : AppCompatActivity>(
    activityClass: Class<T>
) : BaseAppCompatThemeTest<T>(activityClass) {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun activities() = listOf(
            DarkAppCompatActivity::class.java,
            LightAppCompatActivity::class.java
        )
    }
}
