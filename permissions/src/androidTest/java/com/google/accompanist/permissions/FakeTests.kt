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

package com.google.accompanist.permissions

import androidx.test.filters.SdkSuppress
import org.junit.Test

/**
 * Fake tests to avoid the "No tests found error" when running in Build.VERSION.SDK_INT < 23
 */
class FakeTests {

    @SdkSuppress(maxSdkVersion = 22)
    @Test
    fun fakeTestToAvoidNoTestsFoundErrorInAPI22AndBelow() = Unit

    // More Fake tests to help with sharding: https://github.com/android/android-test/issues/973
    @Test
    fun fake1() = Unit

    @Test
    fun fake2() = Unit

    @Test
    fun fake3() = Unit

    @Test
    fun fake4() = Unit

    @Test
    fun fake5() = Unit

    @Test
    fun fake6() = Unit

    @Test
    fun fake7() = Unit

    @Test
    fun fake8() = Unit

    @Test
    fun fake9() = Unit
}
